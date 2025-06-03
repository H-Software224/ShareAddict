package com.example.ch13_activity.network

import com.example.ch13_activity.data.AppRule
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AppRuleApi {
    @POST("/rules/app-rule")
    suspend fun uploadRule(@Body rule: AppRule): Response<ResponseBody>
}