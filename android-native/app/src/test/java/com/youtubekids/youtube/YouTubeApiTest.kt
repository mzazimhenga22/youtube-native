package com.youtubekids.youtube

import com.youtubekids.youtube.data.remote.YouTubeApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.scalars.ScalarsConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

class YouTubeApiTest {
    @Test
    fun testBrowse() = runBlocking {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        
        val logging = HttpLoggingInterceptor { message -> println("OKHTTP: $message") }
        logging.level = HttpLoggingInterceptor.Level.BODY
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val api = Retrofit.Builder()
            .baseUrl("https://www.youtube.com/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(YouTubeApi::class.java)
            
        println("Fetching credentials...")
        val response = api.getRequest("https://www.youtube.com/?gl=US&hl=en", emptyMap())
        val html = response.body()?.string() ?: ""
        println("HTML size: \${html.length}")
        
        val apiKey = Regex(""""(?:INNERTUBE_API_KEY|apiKey)":"(.+?)"""").find(html)?.groupValues?.get(1)
        val clientVersion = Regex(""""clientVersion":"([\d.]+)"""").find(html)?.groupValues?.get(1) ?: "2.20240101.01.00"
        val visitorData = Regex(""""visitorData":"(.+?)"""").find(html)?.groupValues?.get(1)
        
        println("Key: $apiKey, version: $clientVersion, visitor: $visitorData")
        
        if (apiKey == null) {
            throw AssertionError("API Key not found")
        }
        
        val apiUrl = "https://www.youtube.com/youtubei/v1/browse?key=$apiKey"
        val body = buildJsonObject {
            putJsonObject("context") {
                putJsonObject("client") {
                    put("clientName", "WEB")
                    put("clientVersion", clientVersion)
                    visitorData?.let { put("visitorData", it) }
                }
            }
            put("browseId", "FEwhat_to_watch")
        }
        
        val browseResponse = api.postRequest(apiUrl, mapOf("Content-Type" to "application/json"), body)
        println("Browse response code: \${browseResponse.code()}")
        if (!browseResponse.isSuccessful) {
            println("Error body: \${browseResponse.errorBody()?.string()}")
        } else {
            val responseBody = browseResponse.body()
            println("Browse successful, response: \${responseBody?.keys}")
        }
    }
}
