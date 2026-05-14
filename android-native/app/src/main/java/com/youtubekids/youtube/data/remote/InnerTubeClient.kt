package com.youtubekids.youtube.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Self-contained InnerTube client with **signature cipher decryption**.
 *
 * Flow:
 *   1. [init] – scrapes YouTube homepage for API credentials + player JS URL.
 *   2. [ensureCipher] – lazily fetches player JS, parses the decipher function
 *      into a sequence of reverse/splice/swap ops (pure Kotlin, no JS engine).
 *   3. [getInfo] – rotates through client identities calling /youtubei/v1/player.
 *   4. [getStreamUrl] – selects best format and deciphers its URL if needed.
 */
@Singleton
class InnerTubeClient @Inject constructor() {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val domain = "https://www.youtube.com"
    private val defaultHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
        "Accept-Language" to "en-US,en;q=0.9",
        "Origin" to domain,
        "Referer" to "$domain/"
    )

    // ── Credentials ─────────────────────────────────────────────────────────
    private var apiKey: String? = null
    private var clientVersion: String? = null
    private var visitorData: String? = null
    private val credMutex = Mutex()

    // ── Cipher engine state ─────────────────────────────────────────────────
    private var playerJsUrl: String? = null
    private var cipherOps: List<CipherOp>? = null
    private var sigTimestamp: Int? = null
    private val cipherMutex = Mutex()

    private sealed class CipherOp {
        object Reverse : CipherOp()
        data class Splice(val n: Int) : CipherOp()
        data class Swap(val n: Int) : CipherOp()
    }

    // ── Client identities ───────────────────────────────────────────────────
    private data class ClientIdentity(
        val name: String, val clientName: String, val version: String,
        val userAgent: String, val platform: String? = null
    )

    private val clientIdentities = listOf(
        ClientIdentity("IOS", "IOS", "19.29.1",
            "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X; en_US)", "MOBILE"),
        ClientIdentity("TV_EMBEDDED", "TVHTML5_SIMPLY_EMBEDDED_PLAYER", "2.0",
            "Mozilla/5.0 (SmartTV; Google TV) AppleWebKit/537.36 Chrome/114.0.0.0 Safari/537.36", "TV"),
        ClientIdentity("ANDROID", "ANDROID", "19.29.37",
            "com.google.android.youtube/19.29.37 (Linux; U; Android 14; en_US) gzip", "MOBILE"),
        ClientIdentity("ANDROID_MUSIC", "ANDROID_MUSIC", "6.45.54",
            "com.google.android.apps.youtube.music/6.45.54 (Linux; U; Android 14; en_US) gzip", "MOBILE"),
        ClientIdentity("ANDROID_VR", "ANDROID_VR", "1.60.19",
            "com.google.android.youtube.vr/1.60.19 (Linux; U; Android 12; en_US) gzip", "MOBILE"),
        ClientIdentity("ANDROID_TESTSUITE", "ANDROID_TESTSUITE", "1.9.3",
            "com.google.android.youtube.testsuite/1.9.3 (Linux; U; Android 12; en_US) gzip", "MOBILE"),
        ClientIdentity("WEB", "WEB", "2.20260101.01.00",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0.0.0 Safari/537.36"),
        ClientIdentity("WEB_EMBEDDED", "WEB_EMBEDDED_PLAYER", "1.20240722.01.00",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"),
        ClientIdentity("MWEB", "MWEB", "2.20240501.00.00",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148 Safari/604.1", "MOBILE"),
    )

    // ── Public data classes ─────────────────────────────────────────────────
    data class VideoInfo(
        val videoId: String, val title: String, val author: String,
        val isLive: Boolean, val lengthSeconds: Long, val viewCount: Long,
        val description: String, val thumbnail: String,
        val streamingData: StreamingData?
    )

    data class StreamingData(
        val hlsManifestUrl: String? = null, val dashManifestUrl: String? = null,
        val formats: List<StreamFormat> = emptyList(),
        val adaptiveFormats: List<StreamFormat> = emptyList()
    )

    data class StreamFormat(
        val itag: Int, val url: String?, val mimeType: String, val bitrate: Long,
        val width: Int?, val height: Int?, val quality: String,
        val qualityLabel: String?, val audioQuality: String?,
        val hasVideo: Boolean, val hasAudio: Boolean,
        val signatureCipher: String?, val cipher: String?
    ) {
        val isCiphered get() = url == null && (signatureCipher != null || cipher != null)
    }

    data class StreamResult(
        val url: String, val mimeType: String, val quality: String,
        val isAdaptive: Boolean, val isLive: Boolean, val itag: Int?,
        val audioUrl: String? = null, val audioMimeType: String? = null
    )

    // ═══════════════════════════════════════════════════════════════════════
    //  CREDENTIAL + CIPHER INITIALISATION
    // ═══════════════════════════════════════════════════════════════════════

    suspend fun init() = credMutex.withLock {
        if (apiKey != null && clientVersion != null) return@withLock
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing credentials...")
                val html = fetchUrl("$domain/?gl=US&hl=en") ?: return@withContext

                apiKey = Regex(""""(?:INNERTUBE_API_KEY|apiKey)":"(.+?)"""")
                    .find(html)?.groupValues?.get(1)
                clientVersion = Regex(""""clientVersion":"([\d.]+)"""")
                    .find(html)?.groupValues?.get(1) ?: "2.20260101.01.00"
                visitorData = Regex(""""visitorData":"(.+?)"""")
                    .find(html)?.groupValues?.get(1)

                // Extract player JS URL for cipher decryption
                playerJsUrl = extractPlayerJsUrl(html)
                Log.d(TAG, "Credentials OK. playerJs=${playerJsUrl?.takeLast(40)}")
            } catch (e: Exception) {
                Log.e(TAG, "init failed", e)
            }
        }
    }

    private fun extractPlayerJsUrl(html: String): String? {
        val patterns = listOf(
            Regex(""""jsUrl"\s*:\s*"(/s/player/[^"]+)""""),
            Regex(""""PLAYER_JS_URL"\s*:\s*"(/s/player/[^"]+)""""),
            Regex("""src="(/s/player/[^"]+?/base\.js)""""),
            Regex("""/s/player/[a-zA-Z0-9]+/player_ias\.vflset/[a-zA-Z_]+/base\.js""")
        )
        for (p in patterns) {
            val m = p.find(html)
            if (m != null) return m.groupValues.getOrElse(1) { m.value }
        }
        return null
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CIPHER ENGINE — Pure Kotlin decipher of YouTube's scrambled signatures
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Lazily fetches the player JS and parses the decipher function into
     * a list of [CipherOp] (reverse / splice / swap). Thread-safe & cached.
     */
    private suspend fun ensureCipher() = cipherMutex.withLock {
        if (cipherOps != null) return@withLock
        init()
        val jsPath = playerJsUrl ?: return@withLock
        val fullUrl = if (jsPath.startsWith("http")) jsPath else "$domain$jsPath"

        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching player JS for cipher...")
                val js = fetchUrl(fullUrl) ?: return@withContext

                // Extract signatureTimestamp
                sigTimestamp = Regex("""signatureTimestamp[:\s]+(\d+)""").find(js)
                    ?.groupValues?.get(1)?.toIntOrNull()

                cipherOps = parseCipherOps(js)
                Log.d(TAG, "Cipher ready: ${cipherOps?.size} ops, sts=$sigTimestamp")
            } catch (e: Exception) {
                Log.e(TAG, "ensureCipher failed", e)
            }
        }
    }

    /**
     * Parses the decipher function from YouTube's player JS.
     *
     * The function always follows this pattern:
     *   FUNC=function(a){a=a.split("");OBJ.method(a,N);...;return a.join("")}
     *
     * Where OBJ has 3 types of methods:
     *   - reverse: function(a){a.reverse()}
     *   - splice:  function(a,b){a.splice(0,b)}
     *   - swap:    function(a,b){var c=a[0];a[0]=a[b%a.length];a[b%a.length]=c}
     */
    private fun parseCipherOps(js: String): List<CipherOp> {
        // Step 1: Find decipher function name
        val funcName = findDecipherFuncName(js)
        if (funcName == null) {
            Log.e(TAG, "Could not find decipher function name")
            return emptyList()
        }
        Log.d(TAG, "Decipher function: $funcName")

        // Step 2: Get function body
        val esc = Regex.escape(funcName)
        val bodyRegex = Regex(
            """(?:$esc\s*=\s*function|function\s+$esc)\s*\(\s*a\s*\)\s*\{(.+?)\}""",
            RegexOption.DOT_MATCHES_ALL
        )
        val funcBody = bodyRegex.find(js)?.groupValues?.get(1)
        if (funcBody == null) {
            Log.e(TAG, "Could not find decipher function body")
            return emptyList()
        }

        // Step 3: Extract transform object name (e.g., "Uu" from "Uu.kT(a,2)")
        val objName = Regex("""([a-zA-Z0-9$]{2,})\.\w+\(a""").find(funcBody)
            ?.groupValues?.get(1)
        if (objName == null) {
            Log.e(TAG, "Could not find transform object name")
            return emptyList()
        }

        // Step 4: Get transform object body and classify methods
        val escObj = Regex.escape(objName)
        val objRegex = Regex("""var\s+$escObj\s*=\s*\{([\s\S]*?)\}\s*;""")
        val objBody = objRegex.find(js)?.groupValues?.get(1)
        if (objBody == null) {
            Log.e(TAG, "Could not find transform object body for $objName")
            return emptyList()
        }

        val methodTypes = mutableMapOf<String, String>()
        val methodRegex = Regex("""(\w+)\s*:\s*function\s*\([^)]*\)\s*\{([^}]*)\}""")
        for (m in methodRegex.findAll(objBody)) {
            val name = m.groupValues[1]
            val body = m.groupValues[2]
            methodTypes[name] = when {
                body.contains("reverse") -> "reverse"
                body.contains("splice") -> "splice"
                else -> "swap" // var c=a[0]; swap pattern
            }
        }

        // Step 5: Parse the operation calls from the function body
        val ops = mutableListOf<CipherOp>()
        val callRegex = Regex("""$escObj\.([\w$]+)\(a(?:,(\d+))?\)""")
        for (call in callRegex.findAll(funcBody)) {
            val method = call.groupValues[1]
            val arg = call.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            when (methodTypes[method]) {
                "reverse" -> ops.add(CipherOp.Reverse)
                "splice" -> ops.add(CipherOp.Splice(arg))
                "swap" -> ops.add(CipherOp.Swap(arg))
            }
        }
        return ops
    }

    private fun findDecipherFuncName(js: String): String? {
        // Multiple patterns — YouTube changes these periodically
        val patterns = listOf(
            Regex("""\b[cs]\s*&&\s*[adf]\.set\([^,]+\s*,\s*encodeURIComponent\(([a-zA-Z0-9$]+)\("""),
            Regex("""\b[a-zA-Z0-9]+\s*&&\s*[a-zA-Z0-9]+\.set\([^,]+\s*,\s*encodeURIComponent\(([a-zA-Z0-9$]+)\("""),
            Regex("""\bm=([a-zA-Z0-9$]{2,})\(decodeURIComponent\(h\.s\)\)"""),
            Regex("""\bc\s*&&\s*d\.set\([^,]+\s*,\s*(?:encodeURIComponent\s*\()([a-zA-Z0-9$]+)\("""),
            Regex("""\bc\s*&&\s*[a-z]\.set\([^,]+\s*,\s*([a-zA-Z0-9$]+)\("""),
            // Direct structure match — most reliable fallback
            Regex("""([a-zA-Z0-9$]+)\s*=\s*function\(\s*a\s*\)\s*\{\s*a\s*=\s*a\.split\(\s*""\s*\)"""),
            Regex("""function\s+([a-zA-Z0-9$]+)\(\s*a\s*\)\s*\{\s*a\s*=\s*a\.split\(\s*""\s*\)"""),
        )
        for (p in patterns) {
            val name = p.find(js)?.groupValues?.get(1)
            if (name != null) return name
        }
        return null
    }

    /** Applies the parsed decipher operations to a scrambled signature. */
    private fun decipherSignature(scrambled: String): String {
        val ops = cipherOps ?: return scrambled
        val a = scrambled.toMutableList()
        for (op in ops) {
            when (op) {
                is CipherOp.Reverse -> a.reverse()
                is CipherOp.Splice -> repeat(op.n.coerceAtMost(a.size)) { a.removeAt(0) }
                is CipherOp.Swap -> {
                    val b = op.n % a.size
                    val tmp = a[0]; a[0] = a[b]; a[b] = tmp
                }
            }
        }
        return String(a.toCharArray())
    }

    /**
     * Deciphers a `signatureCipher` or `cipher` URL-encoded param string.
     * Returns the full playable URL with the deciphered signature appended.
     */
    private fun decipherCipherUrl(cipherStr: String): String? {
        val params = cipherStr.split("&").associate { part ->
            val kv = part.split("=", limit = 2)
            kv[0] to URLDecoder.decode(kv.getOrElse(1) { "" }, "UTF-8")
        }
        val baseUrl = params["url"] ?: return null
        val scrambled = params["s"] ?: return null
        val sp = params["sp"] ?: "sig"

        val sig = decipherSignature(scrambled)
        val sep = if (baseUrl.contains("?")) "&" else "?"
        return "$baseUrl${sep}${sp}=${URLEncoder.encode(sig, "UTF-8")}"
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CORE: getInfo
    // ═══════════════════════════════════════════════════════════════════════

    suspend fun getInfo(videoId: String): VideoInfo? = withContext(Dispatchers.IO) {
        init()
        // Pre-load cipher engine so we're ready to decipher formats
        ensureCipher()

        val key = apiKey ?: run {
            Log.e(TAG, "getInfo($videoId): No API key"); return@withContext null
        }

        for (client in clientIdentities) {
            try {
                val body = buildPlayerBody(videoId, client)
                val request = Request.Builder()
                    .url("$domain/youtubei/v1/player?key=$key&prettyPrint=false")
                    .apply {
                        defaultHeaders.forEach { (k, v) -> addHeader(k, v) }
                        addHeader("Content-Type", "application/json")
                        header("User-Agent", client.userAgent)
                    }
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val responseBody = httpClient.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        Log.d(TAG, "getInfo: ${client.name} HTTP ${resp.code}")
                        return@use null
                    }
                    resp.body?.string()
                } ?: continue

                val data = json.parseToJsonElement(responseBody).jsonObject
                val status = data["playabilityStatus"]?.jsonObject
                    ?.get("status")?.jsonPrimitive?.content

                if (status != "OK") {
                    Log.d(TAG, "getInfo: ${client.name} status=$status")
                    continue
                }

                val details = data["videoDetails"]?.jsonObject ?: continue
                val sd = data["streamingData"]?.jsonObject
                val streamingData = if (sd != null) parseStreamingData(sd) else null

                if (streamingData == null || !streamingData.hasPlayable()) {
                    Log.d(TAG, "getInfo: ${client.name} OK but no playable streams")
                    continue
                }

                val title = details["title"]?.jsonPrimitive?.content ?: ""
                val author = details["author"]?.jsonPrimitive?.content ?: ""
                val isLive = details["isLiveContent"]?.jsonPrimitive?.booleanOrNull ?: false
                val thumbs = details["thumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray
                val thumb = thumbs?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.content
                    ?: "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"

                Log.d(TAG, "getInfo($videoId): SUCCESS via ${client.name} — \"$title\"")
                return@withContext VideoInfo(
                    videoId, title, author, isLive,
                    details["lengthSeconds"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                    details["viewCount"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                    details["shortDescription"]?.jsonPrimitive?.content ?: "",
                    thumb, streamingData
                )
            } catch (e: Exception) {
                Log.e(TAG, "getInfo: ${client.name} error: ${e.message}")
            }
        }
        Log.e(TAG, "getInfo($videoId): All clients exhausted")
        null
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  STREAM SELECTION + DECIPHER
    // ═══════════════════════════════════════════════════════════════════════

    suspend fun getStreamUrl(videoId: String): StreamResult? {
        val info = getInfo(videoId) ?: return null
        return chooseStream(info)
    }

    /**
     * Selects the best stream. Now handles ciphered formats by deciphering
     * their URLs on the fly using the parsed cipher operations.
     *
     * Priority:
     *   1. HLS manifest
     *   2. Muxed progressive video+audio
     *   3. Merged adaptive video+audio
     *   4. DASH manifest
     *
     * Single adaptive formats are only used as a video+audio pair. Returning
     * one track by itself causes either audio with no video or no audio.
     */
    fun chooseStream(info: VideoInfo): StreamResult? {
        val sd = info.streamingData ?: return null

        // 1. HLS manifests contain both audio and video variants and are safe
        // to play as a single MediaItem.
        if (sd.hlsManifestUrl != null) {
            return StreamResult(sd.hlsManifestUrl, "application/x-mpegURL", "auto",
                isAdaptive = false, isLive = info.isLive, itag = null)
        }

        // Live DASH manifests also describe both audio and video tracks. The app
        // already includes media3-exoplayer-dash, so ExoPlayer can parse them.
        if (info.isLive && sd.dashManifestUrl != null) {
            return StreamResult(sd.dashManifestUrl, "application/dash+xml", "auto",
                isAdaptive = false, isLive = info.isLive, itag = null)
        }

        // 3. Muxed video+audio (progressive) — PREFERRED for simple playback
        //    These contain both video and audio in a single MP4 container.
        resolveFormat(sd.formats.filter { it.hasVideo && it.hasAudio }.sortedByDescending { it.bitrate })
            ?.let {
                Log.d(TAG, "chooseStream(${info.videoId}): Using muxed progressive stream")
                return it.copy(isAdaptive = false)
            }

        resolveAdaptivePair(sd.adaptiveFormats)?.let {
            Log.d(TAG, "chooseStream(${info.videoId}): Using merged adaptive stream")
            return it
        }

        if (sd.dashManifestUrl != null) {
            return StreamResult(sd.dashManifestUrl, "application/dash+xml", "auto",
                isAdaptive = false, isLive = info.isLive, itag = null)
        }

        Log.w(TAG, "chooseStream(${info.videoId}): No playable audio/video stream")
        return null
    }

    private fun resolveAdaptivePair(formats: List<StreamFormat>): StreamResult? {
        val video = formats
            .filter { it.hasVideo && !it.hasAudio }
            .sortedWith(compareByDescending<StreamFormat> { videoFormatScore(it) }.thenByDescending { it.bitrate })
            .firstNotNullOfOrNull { f -> resolveUrl(f)?.let { f to it } }

        val audio = formats
            .filter { it.hasAudio && !it.hasVideo }
            .sortedWith(compareByDescending<StreamFormat> { audioFormatScore(it) }.thenByDescending { it.bitrate })
            .firstNotNullOfOrNull { f -> resolveUrl(f)?.let { f to it } }

        if (video == null || audio == null) return null

        val videoFormat = video.first
        val audioFormat = audio.first
        return StreamResult(
            url = video.second,
            mimeType = videoFormat.mimeType.substringBefore(";").trim(),
            quality = videoFormat.qualityLabel ?: videoFormat.quality,
            isAdaptive = true,
            isLive = false,
            itag = videoFormat.itag,
            audioUrl = audio.second,
            audioMimeType = audioFormat.mimeType.substringBefore(";").trim()
        )
    }

    private fun videoFormatScore(format: StreamFormat): Int {
        val mime = format.mimeType.lowercase()
        val codecScore = when {
            mime.contains("avc1") -> 300
            mime.contains("vp9") || mime.contains("vp09") -> 200
            mime.contains("av01") -> 100
            else -> 0
        }
        val containerScore = if (mime.startsWith("video/mp4")) 50 else 0
        return codecScore + containerScore + (format.height ?: 0)
    }

    private fun audioFormatScore(format: StreamFormat): Int {
        val mime = format.mimeType.lowercase()
        val containerScore = if (mime.startsWith("audio/mp4")) 100 else 0
        return containerScore + (format.bitrate / 1000).toInt()
    }

    /**
     * Resolves the first playable format from the list, deciphering if needed.
     * Strips codec parameters from mimeType (e.g. 'video/mp4; codecs="avc1..."'
     * → 'video/mp4') so ExoPlayer doesn't get confused by explicit codec strings.
     */
    private fun resolveFormat(formats: List<StreamFormat>): StreamResult? {
        for (f in formats) {
            val url = resolveUrl(f)
            if (url != null) {
                Log.d(TAG, "Resolved itag=${f.itag} ${f.qualityLabel ?: f.quality} ciphered=${f.isCiphered} mime=${f.mimeType}")
                // Strip codec params — ExoPlayer infers codecs from the container
                val cleanMime = f.mimeType.substringBefore(";").trim()
                return StreamResult(
                    url = url, mimeType = cleanMime,
                    quality = f.qualityLabel ?: f.quality,
                    isAdaptive = false, isLive = false, itag = f.itag
                )
            }
        }
        return null
    }

    /** Returns a playable URL — direct if available, deciphered otherwise. */
    private fun resolveUrl(f: StreamFormat): String? {
        // Direct URL available
        if (f.url != null) return f.url

        // Decipher signature cipher
        val cipherStr = f.signatureCipher ?: f.cipher ?: return null
        if (cipherOps == null) {
            Log.w(TAG, "resolveUrl: Cipher not initialized, cannot decipher itag=${f.itag}")
            return null
        }
        return decipherCipherUrl(cipherStr)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  INTERNALS
    // ═══════════════════════════════════════════════════════════════════════

    private fun buildPlayerBody(videoId: String, client: ClientIdentity): String {
        return buildJsonObject {
            putJsonObject("context") {
                putJsonObject("client") {
                    put("clientName", client.clientName)
                    put("clientVersion", client.version)
                    put("hl", "en-US"); put("gl", "US")
                    visitorData?.let { put("visitorData", it) }
                    client.platform?.let { put("platform", it) }
                }
            }
            put("videoId", videoId)
            putJsonObject("playbackContext") {
                putJsonObject("contentPlaybackContext") {
                    put("signatureTimestamp", sigTimestamp ?: 20641)
                }
            }
            put("racyCheckOk", true)
            put("contentCheckOk", true)
        }.toString()
    }

    private fun parseStreamingData(sd: JsonObject): StreamingData {
        return StreamingData(
            hlsManifestUrl = sd["hlsManifestUrl"]?.jsonPrimitive?.contentOrNull,
            dashManifestUrl = sd["dashManifestUrl"]?.jsonPrimitive?.contentOrNull,
            formats = sd["formats"]?.jsonArray?.mapNotNull { parseFormat(it.jsonObject) } ?: emptyList(),
            adaptiveFormats = sd["adaptiveFormats"]?.jsonArray?.mapNotNull { parseFormat(it.jsonObject) } ?: emptyList()
        )
    }

    private fun parseFormat(f: JsonObject): StreamFormat? {
        val itag = f["itag"]?.jsonPrimitive?.intOrNull ?: return null
        val mimeType = f["mimeType"]?.jsonPrimitive?.contentOrNull ?: return null
        val audioQuality = f["audioQuality"]?.jsonPrimitive?.contentOrNull
        val hasVideo = mimeType.startsWith("video/")
        val hasAudio = mimeType.startsWith("audio/") || (hasVideo && audioQuality != null)

        return StreamFormat(
            itag = itag,
            url = f["url"]?.jsonPrimitive?.contentOrNull,
            mimeType = mimeType,
            bitrate = f["bitrate"]?.jsonPrimitive?.longOrNull ?: 0L,
            width = f["width"]?.jsonPrimitive?.intOrNull,
            height = f["height"]?.jsonPrimitive?.intOrNull,
            quality = f["quality"]?.jsonPrimitive?.contentOrNull ?: "",
            qualityLabel = f["qualityLabel"]?.jsonPrimitive?.contentOrNull,
            audioQuality = audioQuality,
            hasVideo = hasVideo,
            hasAudio = hasAudio,
            signatureCipher = f["signatureCipher"]?.jsonPrimitive?.contentOrNull,
            cipher = f["cipher"]?.jsonPrimitive?.contentOrNull
        )
    }

    private fun StreamingData.hasPlayable(): Boolean {
        if (hlsManifestUrl != null || dashManifestUrl != null) return true
        val allFormats = formats + adaptiveFormats
        // Playable = has direct URL OR has cipher data we can decipher
        return allFormats.any { it.url != null || it.isCiphered }
    }

    private fun fetchUrl(url: String): String? {
        val request = Request.Builder().url(url)
            .apply { defaultHeaders.forEach { (k, v) -> addHeader(k, v) } }
            .build()
        return try {
            httpClient.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.string() else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchUrl($url) failed: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "InnerTubeClient"
    }
}
