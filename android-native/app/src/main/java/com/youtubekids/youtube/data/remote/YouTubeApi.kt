package com.youtubekids.youtube.data.remote

import kotlinx.serialization.json.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Url

interface YouTubeApi {
    @POST
    suspend fun postRequest(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: JsonObject
    ): Response<JsonObject>

    /**
     * Raw GET request that returns the response body as a plain string.
     * This is used for fetching the YouTube homepage HTML to extract
     * InnerTube credentials (API key, client version, visitor data).
     * We use ResponseBody instead of String because Retrofit's
     * kotlinx-serialization converter cannot deserialize raw HTML.
     */
    @GET
    suspend fun getRequest(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}
