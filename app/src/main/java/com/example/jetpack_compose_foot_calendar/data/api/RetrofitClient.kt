package com.example.jetpack_compose_foot_calendar.data.api

import com.example.jetpack_compose_foot_calendar.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val footballApi: FootballApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-apisports-key", BuildConfig.FOOTBALL_API_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.FOOTBALL_API_HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FootballApiService::class.java)
    }
}