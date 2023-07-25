package com.buysell.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationInterface {
    @POST("send")
    suspend fun sendMessage(@Header("Authorization") Token:String,
                            @Header("Content-Type") value:String,
                            @Body jsonObject: JsonObject
    ): JsonObject
}