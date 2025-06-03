package com.example.ch13_activity.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://43.203.174.53:3000/"  // 👉 HTTPS 권장

    val api: AppRuleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AppRuleApi::class.java)
    }
}
