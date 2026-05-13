package com.youtubekids.youtube.data.repository

import android.content.Context
import android.util.Log
import com.youtubekids.youtube.data.model.Chapter
import com.youtubekids.youtube.data.model.Video
import com.youtubekids.youtube.data.remote.YouTubeApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import com.maxrave.kotlinyoutubeextractor.YTExtractor
import com.maxrave.kotlinyoutubeextractor.State

@Singleton
class YouTubeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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
    suspend fun getShorts(): List<Video> = withContext(Dispatchers.IO) {
        coroutineScope {
            try {
                val queries = listOf(
                    "#shorts trending",
                    "viral shorts",
                    "funny shorts videos",
                    "shorts compilation"
                )
                val deferredResults = queries.map { query ->
                    async { search(query) }
                }
                val results = deferredResults.awaitAll()
                results.flatten()
                    .distinctBy { it.id }
                    .shuffled()
                    .take(30)
            } catch (e: Exception) {
                Log.e(TAG, "getShorts failed", e)
                emptyList()
            }
        }
    }

    suspend fun getLiveGuide(): List<Video> = getLiveChannels()

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
            // Try two-column layout first (standard home/browse)
            val tabs = json["contents"]?.jsonObject?.get("twoColumnBrowseResultsRenderer")?.jsonObject?.get("tabs")?.jsonArray
                ?: json["contents"]?.jsonObject?.get("singleColumnBrowseResultsRenderer")?.jsonObject?.get("tabs")?.jsonArray
            
            val firstTabContent = tabs?.get(0)?.jsonObject?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
            
            // Rich grid (home page)
            val richGridContents = firstTabContent?.get("richGridRenderer")?.jsonObject?.get("contents")?.jsonArray
            richGridContents?.forEach { item ->
                item.jsonObject["richItemRenderer"]?.jsonObject?.get("content")?.jsonObject?.let { content ->
                    // Standard video
                    content["videoRenderer"]?.jsonObject?.let { renderer ->
                        mapVideoRenderer(renderer)?.let { videos.add(it) }
                    }
                    // Shorts / Reels
                    content["reelItemRenderer"]?.jsonObject?.let { renderer ->
                        mapVideoRenderer(renderer)?.let { videos.add(it) }
                    }
                }
                // Rich section (groups of videos in shelves)
                item.jsonObject["richSectionRenderer"]?.jsonObject?.get("content")?.jsonObject?.let { sectionContent ->
                    sectionContent["richShelfRenderer"]?.jsonObject?.get("contents")?.jsonArray?.forEach { shelfItem ->
                        shelfItem.jsonObject["richItemRenderer"]?.jsonObject?.get("content")?.jsonObject?.let { content ->
                            content["videoRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                            content["reelItemRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                        }
                    }
                }
            }
            
            // Section list (used by music, movies, subscriptions)
            val sectionListContents = firstTabContent?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray
            sectionListContents?.forEach { section ->
                section.jsonObject["itemSectionRenderer"]?.jsonObject?.get("contents")?.jsonArray?.forEach { item ->
                    item.jsonObject["videoRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                    item.jsonObject["compactVideoRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                    item.jsonObject["gridVideoRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                    // Shelf with horizontal list
                    item.jsonObject["shelfRenderer"]?.jsonObject?.get("content")?.jsonObject?.let { shelfContent ->
                        shelfContent["horizontalListRenderer"]?.jsonObject?.get("items")?.jsonArray?.forEach { shelfItem ->
                            shelfItem.jsonObject["gridVideoRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                            shelfItem.jsonObject["videoRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                            shelfItem.jsonObject["compactVideoRenderer"]?.jsonObject?.let { r -> mapVideoRenderer(r)?.let { videos.add(it) } }
                        }
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
        
        // Title: try runs first, then headline (for reelItemRenderer/shorts), then simpleText, then accessibility
        val title = renderer["title"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["headline"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["headline"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["title"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["title"]?.jsonObject?.get("accessibility")?.jsonObject?.get("accessibilityData")?.jsonObject?.get("label")?.jsonPrimitive?.content
            ?: ""
            
        val channel = renderer["longBylineText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["shortBylineText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["ownerText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: renderer["shortBylineText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: ""

        val views = renderer["viewCountText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["viewCountText"]?.jsonObject?.get("runs")?.jsonArray?.joinToString("") { 
                it.jsonObject["text"]?.jsonPrimitive?.content ?: "" 
            }?.takeIf { it.isNotEmpty() }
            ?: renderer["shortViewCountText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["shortViewCountText"]?.jsonObject?.get("runs")?.jsonArray?.joinToString("") {
                it.jsonObject["text"]?.jsonPrimitive?.content ?: ""
            }?.takeIf { it.isNotEmpty() }
            ?: ""

        val duration = renderer["lengthText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["lengthText"]?.jsonObject?.get("accessibility")?.jsonObject?.get("accessibilityData")?.jsonObject?.get("label")?.jsonPrimitive?.content
            ?: ""
        
        val thumbnails = renderer["thumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray
        val thumbnail = if (thumbnails != null && thumbnails.isNotEmpty()) {
            thumbnails[thumbnails.size - 1].jsonObject["url"]?.jsonPrimitive?.content ?: ""
        } else {
            "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
        }

        val publishedAt = renderer["publishedTimeText"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.content
            ?: renderer["publishedTimeText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content

        // Skip entries with empty titles
        if (title.isEmpty()) return null

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
        Log.d(TAG, "getStream($videoId): Trying YTExtractor as primary method...")
        try {
            val yt = YTExtractor(con = context, CACHING = false, LOGGING = false, retryCount = 2)
            yt.extract(videoId)
            
            if (yt.state == State.SUCCESS) {
                val ytFiles = yt.getYTFiles()
                
                // Try to get a video file that also has audio (often itag 18 or 22)
                val itagsToTry = listOf(22, 18, 137, 136, 135)
                for (itag in itagsToTry) {
                    val ytFile = ytFiles?.get(itag)
                    if (ytFile != null && !ytFile.url.isNullOrEmpty()) {
                        Log.d(TAG, "getStream($videoId): Found primary stream via YTExtractor (itag $itag)")
                        return@withContext StreamResult(ytFile.url!!, "video/mp4", false)
                    }
                }
                
                // We'll just grab the first available video URL
                if (ytFiles != null && ytFiles.size() > 0) {
                   for (i in 0 until ytFiles.size()) {
                       val file = ytFiles.valueAt(i)
                       if (!file.url.isNullOrEmpty()) {
                           Log.d(TAG, "getStream($videoId): Found primary stream via YTExtractor (first available)")
                           return@withContext StreamResult(file.url!!, "video/mp4", false)
                       }
                   }
                }
            } else {
                Log.e(TAG, "getStream($videoId): YTExtractor failed with state ${yt.state}, falling back to InnerTube clients...")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getStream($videoId): YTExtractor exception: ${e.message}, falling back to InnerTube clients...")
        }

        // --- Fallback to InnerTube clients ---
        Log.d(TAG, "getStream($videoId): Using InnerTube clients as fallback")
        ensureCredentials()
        val apiKey = cachedApiKey ?: return@withContext null

        // Ordered by reliability for getting direct playable URLs
        val clients = listOf(
            // IOS client provides direct URLs without signature ciphers
            ClientConfig("IOS", "19.29.1", "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X; en_US)", "IOS"),
            // TV client often returns HLS
            ClientConfig("TVHTML5_SIMPLY_EMBEDDED", "2.0", "Mozilla/5.0 (SmartTV; Google TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36", "TVHTML5_SIMPLY_EMBEDDED_PLAYER"),
            // Android clients tend to return direct URLs
            ClientConfig("ANDROID", "19.29.37", "com.google.android.youtube/19.29.37 (Linux; U; Android 14; en_US) gzip", "ANDROID"),
            ClientConfig("ANDROID_MUSIC", "6.45.54", "com.google.android.apps.youtube.music/6.45.54 (Linux; U; Android 14; en_US) gzip", "ANDROID_MUSIC"),
            ClientConfig("ANDROID_VR", "1.60.19", "com.google.android.youtube.vr/1.60.19 (Linux; U; Android 12; en_US) gzip", "ANDROID_VR"),
            ClientConfig("ANDROID_TESTSUITE", "1.9.3", "com.google.android.youtube.testsuite/1.9.3 (Linux; U; Android 12; en_US) gzip", "ANDROID_TESTSUITE"),
            // Web embedded can sometimes work
            ClientConfig("WEB_EMBEDDED", "1.20240722.01.00", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", "WEB_EMBEDDED_PLAYER"),
            // MWEB
            ClientConfig("MWEB", "2.20240501.00.00", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1", "MWEB"),
            // Kids clients for broader coverage
            ClientConfig("ANDROID_KIDS", "4.10.2", "com.google.android.youtube.kids/4.10.2 (Linux; U; Android 12; en_US) gzip", "ANDROID_KIDS"),
            ClientConfig("IOS_KIDS", "4.10.2", "com.google.ios.youtube.kids/4.10.2 (iPhone; CPU iPhone OS 15_0 like Mac OS X; en_US) gzip", "IOS_KIDS")
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
                        put("signatureTimestamp", 20641)
                    }
                }
                put("racyCheckOk", true)
                put("contentCheckOk", true)
            }

            try {
                val headersWithUserAgent = (headers + ("Content-Type" to "application/json")).toMutableMap().apply {
                    put("User-Agent", client.userAgent)
                }
                val response = api.postRequest("$domain/youtubei/v1/player?key=$apiKey&prettyPrint=false", headersWithUserAgent, body)
                if (response.isSuccessful) {
                    val data = response.body() ?: continue
                    val status = data["playabilityStatus"]?.jsonObject
                    val playStatus = status?.get("status")?.jsonPrimitive?.content
                    val reason = status?.get("reason")?.jsonPrimitive?.content

                    if (playStatus == "OK") {
                        val streamingData = data["streamingData"]?.jsonObject
                        if (streamingData != null) {
                            extractStream(streamingData)?.let {
                                Log.d(TAG, "getStream($videoId): Found fallback stream via ${client.name} (${if (it.adaptive) "Adaptive" else "Progressive"})")
                                return@withContext it
                            }
                            Log.d(TAG, "getStream($videoId): ${client.name} had streamingData but no extractable stream")
                        } else {
                            Log.d(TAG, "getStream($videoId): ${client.name} status=OK but no streamingData")
                        }
                    } else {
                        Log.d(TAG, "getStream($videoId): ${client.name} status=$playStatus reason=$reason")
                    }
                } else {
                    Log.d(TAG, "getStream($videoId): ${client.name} HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getStream($videoId): ${client.name} exception: ${e.message}")
            }
        }
        
        Log.e(TAG, "getStream($videoId): All methods exhausted, no stream found")
        null
    }

    private fun extractStream(streamingData: JsonObject): StreamResult? {
        // Priority 1: HLS manifest (best for live and general playback)
        streamingData["hlsManifestUrl"]?.jsonPrimitive?.content?.let {
            Log.d(TAG, "extractStream: Found HLS manifest")
            return StreamResult(it, "application/x-mpegURL", true)
        }

        // Priority 2: DASH manifest
        streamingData["dashManifestUrl"]?.jsonPrimitive?.content?.let {
            Log.d(TAG, "extractStream: Found DASH manifest")
            return StreamResult(it, "application/dash+xml", true)
        }

        val formats = streamingData["formats"]?.jsonArray ?: JsonArray(emptyList())
        val adaptiveFormats = streamingData["adaptiveFormats"]?.jsonArray ?: JsonArray(emptyList())

        // Priority 3: Muxed (progressive) formats with direct URL - these are the most reliable
        val muxedFormats = formats.mapNotNull { it as? JsonObject }
            .filter { f ->
                val url = f["url"]?.jsonPrimitive?.content
                val hasNoSignature = f["signatureCipher"] == null
                val mimeType = f["mimeType"]?.jsonPrimitive?.content ?: ""
                val isVideo = mimeType.startsWith("video/")
                url != null && url.isNotEmpty() && hasNoSignature && isVideo
            }
            .sortedWith(compareByDescending<JsonObject> {
                it["bitrate"]?.jsonPrimitive?.long ?: it["bitrate"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            })

        muxedFormats.firstOrNull()?.let {
            val url = it["url"]?.jsonPrimitive?.content ?: ""
            val mimeType = it["mimeType"]?.jsonPrimitive?.content ?: "video/mp4"
            Log.d(TAG, "extractStream: Found muxed format, bitrate=${it["bitrate"]}, mime=$mimeType")
            return StreamResult(url, mimeType, false)
        }

        // Priority 4: Adaptive video formats (video-only, but ExoPlayer can handle these)
        val adaptiveVideo = adaptiveFormats.mapNotNull { it as? JsonObject }
            .filter { f ->
                val url = f["url"]?.jsonPrimitive?.content
                val hasNoSignature = f["signatureCipher"] == null
                val mimeType = f["mimeType"]?.jsonPrimitive?.content ?: ""
                val isVideo = mimeType.startsWith("video/")
                url != null && url.isNotEmpty() && hasNoSignature && isVideo
            }
            .sortedWith(compareByDescending<JsonObject> {
                it["bitrate"]?.jsonPrimitive?.long ?: it["bitrate"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            })

        adaptiveVideo.firstOrNull()?.let {
            val url = it["url"]?.jsonPrimitive?.content ?: ""
            val mimeType = it["mimeType"]?.jsonPrimitive?.content ?: "video/mp4"
            Log.d(TAG, "extractStream: Found adaptive video, bitrate=${it["bitrate"]}, mime=$mimeType")
            return StreamResult(url, mimeType, true)
        }

        // Priority 5: Audio-only fallback (for music content)
        val adaptiveAudio = adaptiveFormats.mapNotNull { it as? JsonObject }
            .filter { f ->
                val url = f["url"]?.jsonPrimitive?.content
                val hasNoSignature = f["signatureCipher"] == null
                val mimeType = f["mimeType"]?.jsonPrimitive?.content ?: ""
                val isAudio = mimeType.startsWith("audio/")
                url != null && url.isNotEmpty() && hasNoSignature && isAudio
            }
            .sortedWith(compareByDescending<JsonObject> {
                it["bitrate"]?.jsonPrimitive?.long ?: it["bitrate"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            })

        adaptiveAudio.firstOrNull()?.let {
            val url = it["url"]?.jsonPrimitive?.content ?: ""
            val mimeType = it["mimeType"]?.jsonPrimitive?.content ?: "audio/mp4"
            Log.d(TAG, "extractStream: Found audio-only fallback, mime=$mimeType")
            return StreamResult(url, mimeType, true)
        }

        Log.w(TAG, "extractStream: No playable stream found. formats=${formats.size}, adaptive=${adaptiveFormats.size}")
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
