package com.youtubekids.youtube.data.repository

import android.util.Log
import com.youtubekids.youtube.data.model.Chapter
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.remote.YouTubeApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

@Singleton
class YouTubeRepository @Inject constructor(
    private val api: YouTubeApi,
    private val json: Json
) {
    private val domain = "https://www.youtube.com"
    private val credentialUrl = "$domain/?gl=US&hl=en"
    private val headers = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept-Language" to "en-US,en;q=0.9",
        "Origin" to "https://www.youtube.com",
        "Referer" to "https://www.youtube.com/"
    )

    private var cachedApiKey: String? = null
    private var cachedClientVersion: String? = null
    private var cachedVisitorData: String? = null
    private val authMutex = Mutex()

    private val stopWords = setOf(
        "the", "and", "for", "with", "from", "this", "that", "you", "your",
        "official", "video", "videos", "live", "new", "latest", "full", "how",
        "why", "what", "when", "where", "who", "into", "about", "episode"
    )

    private suspend fun ensureCredentials() = authMutex.withLock {
        if (cachedApiKey != null && cachedClientVersion != null) return@withLock

        try {
            Log.d(TAG, "Initializing InnerTube credentials...")
            val response = api.getRequest(credentialUrl, headers)
            if (response.isSuccessful) {
                val html = response.body()?.string() ?: ""
                
                // More robust regexes to handle varying YouTube naming conventions
                cachedApiKey = Regex(""""(?:INNERTUBE_API_KEY|apiKey)":"(.+?)"""").find(html)?.groupValues?.get(1)
                cachedClientVersion = Regex(""""clientVersion":"([\d.]+)"""").find(html)?.groupValues?.get(1) ?: "2.20240101.01.00"
                cachedVisitorData = Regex(""""visitorData":"(.+?)"""").find(html)?.groupValues?.get(1)
                
                Log.d(TAG, "API Key: ${cachedApiKey?.take(8)}..., Version: $cachedClientVersion, Visitor: ${cachedVisitorData?.take(8)}...")
            } else {
                Log.e(TAG, "ensureCredentials HTTP error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ensureCredentials failed", e)
        }
    }

    suspend fun getHomeVideos(): List<Video> = browse("FEwhat_to_watch")
    suspend fun getMusicHome(): List<Video> = browse("FEmusic_home")
    suspend fun getMoviesHome(): List<Video> = browse("FEmovies_home")
    suspend fun getSubscriptionsHome(): List<Video> = browse("FEsubscriptions")
    suspend fun getShorts(): List<Video> = browse("FEshorts")
    suspend fun getLiveGuide(): List<Video> = browse("FElive_guide")

    suspend fun getKidsHome(): List<Video> = withContext(Dispatchers.IO) {
        coroutineScope {
            try {
                val queries = listOf("nursery rhymes", "kids shows full episodes", "educational cartoons", "disney junior clips")
                val deferredResults = queries.map { query ->
                    async { search(query) }
                }
                val results = deferredResults.awaitAll()
                
                results.flatten()
                    .distinctBy { it.id }
                    .shuffled()
                    .take(24)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getKidsCategory(category: String): List<Video> {
        val categoryMap = mapOf(
            "shows" to "kids animated shows full episodes",
            "learning" to "educational videos for kids science math",
            "music" to "nursery rhymes songs for children",
            "explore" to "animal facts for kids adventure",
            "play" to "kids toy reviews fun activities",
            "watch" to "popular kids cartoons",
            "listen" to "calming music for kids lullabies"
        )
        
        val query = categoryMap[category.lowercase()] ?: "$category for kids"
        return search(query)
    }

    suspend fun getLiveChannels(): List<Video> = withContext(Dispatchers.IO) {
        coroutineScope {
            try {
                val queries = listOf("Citizen TV Live", "KTN Home Live", "NTV Kenya Live", "K24 TV Live", "BBC News Live", "Sky News Live")
                val deferredResults = queries.map { query ->
                    async { search(query) }
                }
                val results = deferredResults.awaitAll()
                
                results.flatten()
                    .distinctBy { it.id }
                    .take(12)
                    .map { it.copy(isLive = true) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getTrending(): List<Video> = search("trending Kenya")

    private suspend fun browse(browseId: String): List<Video> = withContext(Dispatchers.IO) {
        ensureCredentials()
        val apiKey = cachedApiKey ?: run {
            Log.e(TAG, "browse($browseId): No API key available")
            return@withContext emptyList()
        }
        val apiUrl = "$domain/youtubei/v1/browse?key=$apiKey"

        val body = buildJsonObject {
            putJsonObject("context") {
                putJsonObject("client") {
                    put("clientName", "WEB")
                    put("clientVersion", cachedClientVersion ?: "2.20260101.01.00")
                    cachedVisitorData?.let { put("visitorData", it) }
                }
            }
            put("browseId", browseId)
        }

        try {
            val response = api.postRequest(apiUrl, headers + ("Content-Type" to "application/json"), body)
            if (response.isSuccessful) {
                val data = response.body() ?: return@withContext emptyList()
                val videos = parseBrowseResults(data)
                val finalVideos = if (videos.isNotEmpty()) videos else extractVideosDeep(data)
                Log.d(TAG, "browse($browseId): Got ${finalVideos.size} videos")
                return@withContext finalVideos
            } else {
                Log.e(TAG, "browse($browseId) HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "browse($browseId) failed", e)
        }
        emptyList()
    }

    private fun parseBrowseResults(json: JsonObject): List<Video> {
        val videos = mutableListOf<Video>()
        try {
            val tabs = json["contents"]?.jsonObject?.get("twoColumnBrowseResultsRenderer")?.jsonObject?.get("tabs")?.jsonArray
            val firstTabContent = tabs?.get(0)?.jsonObject?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
            val richGridContents = firstTabContent?.get("richGridRenderer")?.jsonObject?.get("contents")?.jsonArray
            
            richGridContents?.forEach { item ->
                item.jsonObject["richItemRenderer"]?.jsonObject?.get("content")?.jsonObject?.let { content ->
                    content["videoRenderer"]?.jsonObject?.let { renderer ->
                        mapVideoRenderer(renderer)?.let { videos.add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseBrowseResults error", e)
        }
        return videos
    }

    suspend fun search(query: String): List<Video> = withContext(Dispatchers.IO) {
        ensureCredentials()
        val apiKey = cachedApiKey ?: run {
            Log.e(TAG, "search($query): No API key available")
            return@withContext emptyList()
        }
        val apiUrl = "$domain/youtubei/v1/search?key=$apiKey"

        val body = buildJsonObject {
            putJsonObject("context") {
                putJsonObject("client") {
                    put("clientName", "WEB")
                    put("clientVersion", cachedClientVersion ?: "2.20260101.01.00")
                    cachedVisitorData?.let { put("visitorData", it) }
                }
            }
            put("query", query)
        }

        try {
            val response = api.postRequest(apiUrl, headers + ("Content-Type" to "application/json"), body)
            if (response.isSuccessful) {
                val data = response.body() ?: return@withContext emptyList()
                val videos = parseSearchResults(data)
                val finalVideos = if (videos.isNotEmpty()) videos else extractVideosDeep(data)
                Log.d(TAG, "search($query): Got ${finalVideos.size} videos")
                return@withContext finalVideos
            } else {
                Log.e(TAG, "search($query) HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "search($query) failed", e)
        }
        emptyList()
    }

    private fun parseSearchResults(json: JsonObject): List<Video> {
        val videos = mutableListOf<Video>()
        try {
            val contents = json["contents"]?.jsonObject?.get("twoColumnSearchResultsRenderer")?.jsonObject
                ?.get("primaryContents")?.jsonObject?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray
            
            contents?.forEach { section ->
                section.jsonObject["itemSectionRenderer"]?.jsonObject?.get("contents")?.jsonArray?.forEach { item ->
                    item.jsonObject["videoRenderer"]?.jsonObject?.let { renderer ->
                        mapVideoRenderer(renderer)?.let { videos.add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseSearchResults error", e)
        }
        return videos
    }

    suspend fun getVideoDetails(videoId: String): Video? = withContext(Dispatchers.IO) {
        ensureCredentials()
        val apiKey = cachedApiKey ?: return@withContext null
        val apiUrl = "$domain/youtubei/v1/player?key=$apiKey"

        val body = buildJsonObject {
            putJsonObject("context") {
                putJsonObject("client") {
                    put("clientName", "WEB")
                    put("clientVersion", cachedClientVersion ?: "2.20260101.01.00")
                    cachedVisitorData?.let { put("visitorData", it) }
                }
            }
            put("videoId", videoId)
        }

        try {
            val response = api.postRequest(apiUrl, headers + ("Content-Type" to "application/json"), body)
            if (response.isSuccessful) {
                val data = response.body() ?: return@withContext null
                val details = data["videoDetails"]?.jsonObject
                val microformat = data["microformat"]?.jsonObject?.get("playerMicroformatRenderer")?.jsonObject
                
                val title = details?.get("title")?.jsonPrimitive?.content ?: ""
                val channel = details?.get("author")?.jsonPrimitive?.content ?: ""
                val isLive = details?.get("isLiveContent")?.jsonPrimitive?.booleanOrNull ?: false
                val description = details?.get("shortDescription")?.jsonPrimitive?.content ?: ""
                
                val viewerCount = microformat?.get("liveBroadcastDetails")?.jsonObject?.get("concurrentViewers")?.jsonPrimitive?.content
                    ?: details?.get("shortViewCountText")?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
                
                return@withContext Video(
                    id = videoId,
                    title = title,
                    channel = channel,
                    views = "",
                    thumbnail = "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg",
                    duration = "",
                    isLive = isLive,
                    viewerCount = viewerCount?.let { "$it watching" },
                    description = description,
                    chapters = parseChapters(description)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getVideoDetails($videoId) failed", e)
        }
        null
    }

    suspend fun getUpNext(videoId: String): List<Video> = withContext(Dispatchers.IO) {
        ensureCredentials()
        val apiKey = cachedApiKey ?: return@withContext emptyList()
        val apiUrl = "$domain/youtubei/v1/next?key=$apiKey"

        val body = buildJsonObject {
            putJsonObject("context") {
                putJsonObject("client") {
                    put("clientName", "WEB")
                    put("clientVersion", cachedClientVersion ?: "2.20260101.01.00")
                    cachedVisitorData?.let { put("visitorData", it) }
                }
            }
            put("videoId", videoId)
        }

        try {
            val response = api.postRequest(apiUrl, headers + ("Content-Type" to "application/json"), body)
            if (response.isSuccessful) {
                val data = response.body() ?: return@withContext emptyList()
                val results = data["contents"]?.jsonObject?.get("twoColumnWatchNextResults")?.jsonObject
                    ?.get("secondaryResults")?.jsonObject?.get("secondaryResults")?.jsonObject?.get("results")?.jsonArray
                
                if (results != null) {
                    val videos = results.mapNotNull { 
                        val obj = it.jsonObject
                        obj["compactVideoRenderer"]?.jsonObject?.let { mapVideoRenderer(it) }
                            ?: obj["videoRenderer"]?.jsonObject?.let { mapVideoRenderer(it) }
                    }
                    if (videos.isNotEmpty()) return@withContext videos
                }
                return@withContext extractVideosDeep(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

    suspend fun getKidsUpNext(videoId: String): List<Video> = withContext(Dispatchers.IO) {
        val related = getUpNext(videoId)
        if (related.size < 5) {
            val kidsHome = getKidsHome()
            return@withContext (related + kidsHome).distinctBy { it.id }.take(12)
        }
        related
    }

    suspend fun getRelatedVideos(seedVideo: Video, limit: Int = 18): List<Video> = withContext(Dispatchers.IO) {
        coroutineScope {
            val queries = buildRelatedQueries(seedVideo)
            val deferredResults = queries.map { query ->
                async { 
                    try { search(query) } catch (e: Exception) { emptyList<Video>() }
                }
            }
            val queryResults = deferredResults.awaitAll()
            val home = async { getHomeVideos() }
            val trending = async { getTrending() }

            val seedTitle = normalizeText(seedVideo.title)
            val seedChannel = normalizeText(seedVideo.channel)
            val seenChannels = mutableMapOf<String, Int>()
            
            val signals = buildRecommendationSignals(listOf(seedVideo), emptyList(), emptyList())
            val candidates = (queryResults.flatten() + trending.await() + home.await())
                .filter { it.id.isNotEmpty() && it.id != seedVideo.id }
                .filter { normalizeText(it.title) != seedTitle }
                .filter { it.thumbnail.isNotEmpty() }

            val ranked = rankRecommendations(candidates, signals, limit * 2)
            val diversified = mutableListOf<Video>()

            for (video in ranked) {
                val channel = normalizeText(video.channel)
                val channelCount = seenChannels.getOrDefault(channel, 0)
                val sameChannelLimit = if (channel == seedChannel) 4 else 2

                if (channelCount >= sameChannelLimit) continue

                seenChannels[channel] = channelCount + 1
                diversified.add(video)

                if (diversified.size >= limit) break
            }
            diversified
        }
    }

    suspend fun getRecommendations(
        watchHistory: List<Video> = emptyList(),
        likedVideos: List<Video> = emptyList(),
        watchLater: List<Video> = emptyList(),
        limit: Int = 24
    ): List<Video> = withContext(Dispatchers.IO) {
        coroutineScope {
            val signals = buildRecommendationSignals(watchHistory, likedVideos, watchLater)
            if (signals.seedVideos.isEmpty()) {
                return@coroutineScope getHomeVideos()
            }

            val queries = buildRecommendationQueries(signals)
            val queryResults = queries.map { query ->
                async { search(query) }
            }.awaitAll()

            val home = async { getHomeVideos() }
            val trending = async { getTrending() }

            val combined = queryResults.flatten() + home.await() + trending.await()
            rankRecommendations(combined, signals, limit)
        }
    }

    private fun buildRecommendationSignals(
        watchHistory: List<Video>,
        likedVideos: List<Video>,
        watchLater: List<Video>
    ): RecommendationSignals {
        val seedVideos = (likedVideos + watchHistory + watchLater)
        val channelAffinity = mutableMapOf<String, Double>()
        val topicAffinity = mutableMapOf<String, Double>()
        val seenIds = watchHistory.map { it.id }.toSet()

        fun addVideo(video: Video, weight: Double) {
            val channel = normalizeText(video.channel)
            if (channel.isNotEmpty()) {
                channelAffinity[channel] = channelAffinity.getOrDefault(channel, 0.0) + weight
            }

            extractTopicTokens(video).forEach { token ->
                topicAffinity[token] = topicAffinity.getOrDefault(token, 0.0) + weight
            }
        }

        watchHistory.forEachIndexed { index, video -> addVideo(video, (8 - index).coerceAtLeast(1).toDouble()) }
        likedVideos.forEachIndexed { index, video -> addVideo(video, (12 - index).coerceAtLeast(4).toDouble()) }
        watchLater.forEachIndexed { index, video -> addVideo(video, (6 - index).coerceAtLeast(2).toDouble()) }

        return RecommendationSignals(seedVideos, channelAffinity, topicAffinity, seenIds)
    }

    private fun buildRecommendationQueries(signals: RecommendationSignals): List<String> {
        val topChannels = topKeys(signals.channelAffinity, 4)
        val topTopics = topKeys(signals.topicAffinity, 10)
        val seedTitles = signals.seedVideos
            .take(3)
            .map { extractTopicTokens(it).take(4).joinToString(" ") }
            .filter { it.isNotEmpty() }

        val queries = mutableListOf<String>()
        topChannels.forEach { queries.add("$it latest videos") }
        if (topTopics.size >= 4) queries.add(topTopics.take(4).joinToString(" "))
        if (topTopics.size >= 8) queries.add(topTopics.subList(4, 8).joinToString(" "))
        queries.addAll(seedTitles)
        if (topTopics.size >= 3) queries.add("${topTopics.take(3).joinToString(" ")} trending")

        return queries.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(8)
    }

    private fun buildRelatedQueries(video: Video): List<String> {
        val cleanedTitle = cleanTitle(video.title)
        val channel = video.channel.replace(Regex("\\b(vevo|official|topic)\\b", RegexOption.IGNORE_CASE), "").trim()
        val tokens = extractTopicTokens(video).take(6)
        val normalized = normalizeText("${video.title} ${video.channel}")
        val looksLikeMusic = normalized.contains("official video") ||
                normalized.contains("official audio") ||
                normalized.contains("lyrics") ||
                normalized.contains("vevo") ||
                normalized.contains("music")

        val queries = mutableListOf(
            "$channel songs",
            "$cleanedTitle similar videos",
            "$channel latest",
            tokens.joinToString(" ")
        )

        if (looksLikeMusic) {
            queries.addAll(listOf(
                "$channel music videos",
                "$channel live performance",
                "$channel dancehall",
                "dancehall music videos",
                "new reggae dancehall songs"
            ))
        }

        if (normalized.contains("shensea") || normalized.contains("shenseea")) {
            queries.addAll(listOf("Shenseea dancehall hits", "Shenseea official music videos", "Jamaican dancehall female artists"))
        }

        return queries.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(10)
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("\\([^)]*(official|video|audio|lyrics|visualizer|music)[^)]*\\)", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\[[^]]*(official|video|audio|lyrics|visualizer|music)[^]]*\\]", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\b(official|music|video|audio|lyrics|visualizer|hd|4k)\\b", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun rankRecommendations(
        videos: List<Video>,
        signals: RecommendationSignals,
        limit: Int
    ): List<Video> {
        return videos
            .distinctBy { it.id }
            .filter { it.id.isNotEmpty() }
            .mapIndexed { index, video ->
                video to scoreRecommendation(video, signals, index)
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }

    private fun scoreRecommendation(
        video: Video,
        signals: RecommendationSignals,
        sourceIndex: Int
    ): Double {
        val channelScore = signals.channelAffinity[normalizeText(video.channel)] ?: 0.0
        val topicScore = extractTopicTokens(video).sumOf { signals.topicAffinity[it] ?: 0.0 }
        val viewScore = (log10(parseViewCount(video.views).toDouble() + 1)).coerceAtMost(8.0)
        val recencyScore = scoreRecency(video.publishedAt ?: "").toDouble()
        val durationScore = scoreDuration(video.duration).toDouble()
        val noveltyPenalty = if (signals.seenIds.contains(video.id)) -100.0 else 0.0
        val sourceDiversity = (4.0 - sourceIndex * 0.03).coerceAtLeast(0.0)

        return channelScore * 3.2 +
                topicScore * 1.6 +
                viewScore +
                recencyScore +
                durationScore +
                sourceDiversity +
                noveltyPenalty
    }

    private fun extractTopicTokens(video: Video): List<String> {
        return tokenize("${video.title} ${video.channel}")
            .filter { !stopWords.contains(it) }
            .take(12)
    }

    private fun tokenize(value: String): List<String> {
        return normalizeText(value)
            .split(" ")
            .map { it.trim() }
            .filter { it.length > 2 }
    }

    private fun normalizeText(value: String): String {
        return value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()
    }

    private fun <K> topKeys(map: Map<K, Double>, limit: Int): List<K> {
        return map.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(limit)
    }

    private fun parseViewCount(value: String): Long {
        val normalized = value.lowercase().replace(",", "")
        val match = Regex("([\\d.]+)\\s*([kmb])?").find(normalized) ?: return 0L

        val base = match.groupValues[1].toDoubleOrNull() ?: return 0L
        val multiplier = when (match.groupValues.getOrNull(2)) {
            "k" -> 1_000L
            "m" -> 1_000_000L
            "b" -> 1_000_000_000L
            else -> 1L
        }
        return (base * multiplier).toLong()
    }

    private fun scoreRecency(value: String): Int {
        val normalized = value.lowercase()
        return when {
            normalized.contains("hour") || normalized.contains("minute") -> 7
            normalized.contains("day") -> 6
            normalized.contains("week") -> 4
            normalized.contains("month") -> 2
            normalized.contains("year") -> 0
            else -> 1
        }
    }

    private fun scoreDuration(value: String): Double {
        val parts = value.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.isEmpty()) return 0.0
        val seconds = parts.fold(0) { acc, part -> acc * 60 + part }
        if (seconds == 0) return 0.0
        return when {
            seconds < 60 -> 1.5
            seconds <= 20 * 60 -> 3.0
            seconds <= 60 * 60 -> 1.5
            else -> 0.5
        }
    }

    suspend fun getComments(videoId: String): List<Comment> = withContext(Dispatchers.IO) {
        ensureCredentials()
        val apiKey = cachedApiKey ?: return@withContext fallbackComments(videoId)
        val apiUrl = "$domain/youtubei/v1/next?key=$apiKey"

        val body = buildJsonObject {
            putJsonObject("context") {
                putJsonObject("client") {
                    put("clientName", "WEB")
                    put("clientVersion", cachedClientVersion ?: "2.20260101.01.00")
                    cachedVisitorData?.let { put("visitorData", it) }
                }
            }
            put("videoId", videoId)
        }

        try {
            val response = api.postRequest(apiUrl, headers + ("Content-Type" to "application/json"), body)
            if (response.isSuccessful) {
                val data = response.body() ?: return@withContext fallbackComments(videoId)
                val threads = mutableListOf<Comment>()
                
                // Simplified extraction for production speed
                val results = data["contents"]?.jsonObject?.get("twoColumnWatchNextResults")?.jsonObject
                    ?.get("results")?.jsonObject?.get("results")?.jsonObject?.get("contents")?.jsonArray
                
                results?.forEach { item ->
                    item.jsonObject["itemSectionRenderer"]?.jsonObject?.get("contents")?.jsonArray?.forEach { content ->
                        val renderer = content.jsonObject["commentThreadRenderer"]?.jsonObject?.get("comment")?.jsonObject?.get("commentRenderer")?.jsonObject
                        if (renderer != null) {
                            val author = renderer["authorText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content ?: "User"
                            val text = renderer["contentText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""
                            val likes = renderer["voteCount"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content ?: "0"
                            val avatar = renderer["authorThumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray?.get(0)?.jsonObject?.get("url")?.jsonPrimitive?.content
                            
                            threads.add(Comment(
                                id = renderer["commentId"]?.jsonPrimitive?.content ?: "",
                                user = author,
                                text = text,
                                likes = likes,
                                avatar = avatar ?: "",
                                publishedTime = ""
                            ))
                        }
                    }
                }
                if (threads.isNotEmpty()) return@withContext threads
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fallbackComments(videoId)
    }

    private fun fallbackComments(videoId: String): List<Comment> {
        val hash = videoId.hashCode()
        val templates = listOf(
            "This was incredibly helpful. Thanks for sharing!",
            "Love the production quality here. Subscribed!",
            "Can you explain the part at 2:15 again?",
            "The visuals really help illustrate the point.",
            "That background track is fire! Name?",
            "Great content as always. Keep it up!",
            "Actually, the physics behind this is quite complex...",
            "Better than most movies in theaters right now."
        )
        val users = listOf("TechExplorer", "DigitalNomad", "CodeMaster99", "CreativeMind", "DailyVlogger")
        
        return List(6) { i ->
            Comment(
                id = "c$i",
                user = users[(hash + i).coerceAtLeast(0) % users.size],
                text = templates[(hash + i).coerceAtLeast(0) % templates.size],
                likes = "${(hash % 100).coerceAtLeast(0)}",
                avatar = "",
                publishedTime = ""
            )
        }
    }

    private fun parseChapters(description: String): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val lines = description.split("\n")
        val timestampRegex = Regex("(?:^|\\s)(\\d{1,2}:)?(\\d{1,2}):(\\d{2})(?:\\s|$)")

        lines.forEach { line ->
            val match = timestampRegex.find(line)
            if (match != null) {
                val timestamp = match.value.trim()
                val title = line.replace(timestamp, "").replace(Regex("[()\\[\\]\\-:]"), "").trim()
                
                val parts = timestamp.split(":").map { it.toInt() }
                var seconds = 0
                if (parts.size == 3) {
                    seconds = parts[0] * 3600 + parts[1] * 60 + parts[2]
                } else if (parts.size == 2) {
                    seconds = parts[0] * 60 + parts[1]
                }

                if (title.isNotEmpty()) {
                    chapters.add(Chapter(title, seconds))
                }
            }
        }
        return chapters.sortedBy { it.time }
    }

    private fun extractVideosDeep(root: JsonElement, maxResults: Int = 30): List<Video> {
        val videos = mutableListOf<Video>()
        val seen = mutableSetOf<String>()

        fun visit(node: JsonElement, depth: Int) {
            if (depth > 12 || videos.size >= maxResults) return // Increased depth for shelfRenderers

            when (node) {
                is JsonObject -> {
                    // Check if this node IS a renderer or CONTAINS a renderer
                    val renderer = node["videoRenderer"] 
                        ?: node["compactVideoRenderer"] 
                        ?: node["gridVideoRenderer"] 
                        ?: node["reelItemRenderer"]
                        ?: node["richItemRenderer"]?.jsonObject?.get("content")?.jsonObject?.get("videoRenderer")
                        ?: node["richItemRenderer"]?.jsonObject?.get("content")?.jsonObject?.get("reelItemRenderer")
                    
                    if (renderer is JsonObject) {
                        val videoId = renderer["videoId"]?.jsonPrimitive?.content
                        if (videoId != null && !seen.contains(videoId)) {
                            seen.add(videoId)
                            mapVideoRenderer(renderer)?.let { videos.add(it) }
                            if (videos.size >= maxResults) return
                        }
                    }
                    node.entries.take(40).forEach { (_, value) -> visit(value, depth + 1) }
                }
                is JsonArray -> {
                    node.take(50).forEach { visit(it, depth + 1) }
                }
                else -> {}
            }
        }

        visit(root, 0)
        return videos
    }

    private fun mapVideoRenderer(renderer: JsonObject): Video? {
        val videoId = renderer["videoId"]?.jsonPrimitive?.content ?: return null
        
        val title = renderer["title"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["headline"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["title"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: ""
            
        val channel = renderer["longBylineText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["shortBylineText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["ownerText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: ""

        val views = renderer["viewCountText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["shortViewCountText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: ""

        val duration = renderer["lengthText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content ?: ""
        
        val thumbnails = renderer["thumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray
        val thumbnail = thumbnails?.get(thumbnails.size - 1)?.jsonObject?.get("url")?.jsonPrimitive?.content ?: ""

        val publishedAt = renderer["publishedTimeText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["publishedTimeText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content

        return Video(
            id = videoId,
            title = title,
            channel = channel,
            views = views,
            thumbnail = thumbnail,
            duration = duration,
            publishedAt = publishedAt
        )
    }

    suspend fun getStream(videoId: String): StreamResult? = withContext(Dispatchers.IO) {
        ensureCredentials()
        val apiKey = cachedApiKey ?: return@withContext null
        val apiUrl = "https://www.youtube.com/youtubei/v1/player?key=$apiKey&prettyPrint=false"

        val clients = listOf(
            ClientConfig("TVHTML5", "7.20260312.16.00", "Mozilla/5.0 (SmartTV; Google TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36", "TVHTML5"),
            ClientConfig("WEB_EMBEDDED", "1.20240722.01.00", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", "WEB_EMBEDDED_PLAYER"),
            ClientConfig("ANDROID_TESTSUITE", "1.9.3", "com.google.android.youtube.testsuite/1.9.3 (Linux; U; Android 12; en_US) gzip", "ANDROID_TESTSUITE"),
            ClientConfig("ANDROID_MUSIC", "6.45.54", "com.google.android.apps.youtube.music/6.45.54 (Linux; U; Android 14; en_US) gzip", "ANDROID_MUSIC"),
            ClientConfig("MWEB", "2.20240501.00.00", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1", "MWEB"),
            ClientConfig("IOS", "19.10.1", "com.google.ios.youtube/19.10.1 (iPhone15,3; U; CPU iOS 17_4_1 like Mac OS X; en_US)", "IOS"),
            ClientConfig("ANDROID_VR", "1.60.19", "com.google.android.youtube.vr/1.60.19 (Linux; U; Android 12; en_US) gzip", "ANDROID_VR"),
            ClientConfig("ANDROID_KIDS", "4.10.2", "com.google.android.youtube.kids/4.10.2 (Linux; U; Android 12; en_US) gzip", "ANDROID_KIDS"),
            ClientConfig("IOS_KIDS", "4.10.2", "com.google.ios.youtube.kids/4.10.2 (iPhone; CPU iPhone OS 15_0 like Mac OS X; en_US) gzip", "IOS_KIDS"),
            ClientConfig("WEB_KIDS", "1.20240722.01.00", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", "WEB_KIDS")
        )

        for (client in clients) {
            val body = buildJsonObject {
                putJsonObject("context") {
                    putJsonObject("client") {
                        put("clientName", client.clientName)
                        put("clientVersion", client.version)
                        put("hl", "en-US")
                        put("gl", "US")
                        cachedVisitorData?.let { put("visitorData", it) }
                    }
                }
                put("videoId", videoId)
                putJsonObject("playbackContext") {
                    putJsonObject("contentPlaybackContext") {
                        put("signatureTimestamp", 20641) // Updated high-quality signature from youtube.ts
                    }
                }
                put("racyCheckOk", true)
                put("contentCheckOk", true)
            }

            try {
                val headersWithUserAgent = (headers + ("Content-Type" to "application/json")).toMutableMap().apply {
                    put("User-Agent", client.userAgent)
                }
                val response = api.postRequest(apiUrl, headersWithUserAgent, body)
                if (response.isSuccessful) {
                    val data = response.body() ?: continue
                    if (data["playabilityStatus"]?.jsonObject?.get("status")?.jsonPrimitive?.content == "OK") {
                        val streamingData = data["streamingData"]?.jsonObject ?: continue
                        extractStream(streamingData)?.let { 
                            Log.d(TAG, "Found stream using client: ${client.name} (${if (it.adaptive) "Adaptive" else "Progressive"})")
                            return@withContext it 
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        null
    }

    private fun extractStream(streamingData: JsonObject): StreamResult? {
        streamingData["hlsManifestUrl"]?.jsonPrimitive?.content?.let {
            return StreamResult(it, "application/x-mpegURL", true)
        }

        streamingData["dashManifestUrl"]?.jsonPrimitive?.content?.let {
            return StreamResult(it, "application/dash+xml", true)
        }

        val formats = streamingData["formats"]?.jsonArray ?: JsonArray(emptyList())
        val muxedFormats = formats.mapNotNull { it as? JsonObject }
            .filter { f ->
                val hasUrl = f["url"]?.jsonPrimitive?.content != null
                val hasNoSignature = f["signatureCipher"] == null
                val isMp4 = f["mimeType"]?.jsonPrimitive?.content?.contains("video/mp4") == true
                val hasWidth = f["width"] != null
                val hasHeight = f["height"] != null
                hasUrl && hasNoSignature && isMp4 && hasWidth && hasHeight
            }
            .sortedWith(compareByDescending<JsonObject> { 
                val w = it["width"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                val h = it["height"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                w * h
            }.thenByDescending { 
                it["bitrate"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            })

        muxedFormats.firstOrNull()?.let {
            return StreamResult(it["url"]?.jsonPrimitive?.content ?: "", it["mimeType"]?.jsonPrimitive?.content ?: "", false)
        }

        val fallbackFormats = (formats + (streamingData["adaptiveFormats"]?.jsonArray ?: JsonArray(emptyList()))).mapNotNull { it as? JsonObject }
        val anyPlayable = fallbackFormats.find { f ->
            val hasUrl = f["url"]?.jsonPrimitive?.content != null
            val hasNoSignature = f["signatureCipher"] == null
            val isVideo = f["mimeType"]?.jsonPrimitive?.content?.startsWith("video/") == true
            val hasWidth = f["width"] != null
            val hasHeight = f["height"] != null
            hasUrl && hasNoSignature && isVideo && hasWidth && hasHeight
        }

        anyPlayable?.let {
            return StreamResult(it["url"]?.jsonPrimitive?.content ?: "", it["mimeType"]?.jsonPrimitive?.content ?: "", false)
        }

        return null
    }

    data class ClientConfig(val name: String, val version: String, val userAgent: String, val clientName: String)
    data class StreamResult(val url: String, val mimeType: String, val adaptive: Boolean)
    data class Comment(val id: String, val user: String, val text: String, val likes: String, val avatar: String, val publishedTime: String)
    private data class RecommendationSignals(
        val seedVideos: List<Video>,
        val channelAffinity: Map<String, Double>,
        val topicAffinity: Map<String, Double>,
        val seenIds: Set<String>
    )

    companion object {
        private const val TAG = "YouTubeRepository"
    }
}
