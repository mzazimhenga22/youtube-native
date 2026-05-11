package com.youtubekids.youtube.data.remote

import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface YouTubeApi {
    @POST
    suspend fun postRequest(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: JsonObject
    ): Response<JsonObject>

    @GET
    suspend fun getRequest(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<String>
}
