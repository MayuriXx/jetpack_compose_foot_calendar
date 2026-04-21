package com.example.jetpack_compose_foot_calendar.data.api

import com.example.jetpack_compose_foot_calendar.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton that provides the [FootballApiService] Retrofit instance.
 *
 * The client is lazily initialised and shared for the lifetime of the process. Two OkHttp
 * interceptors are attached:
 * - A [HttpLoggingInterceptor] at `BODY` level for debugging HTTP traffic.
 * - A custom interceptor that injects the `x-apisports-key` authentication header using
 *   the API key stored in [BuildConfig.FOOTBALL_API_KEY].
 *
 * The base URL is read from [BuildConfig.FOOTBALL_API_HOST] and must include the scheme
 * (`https://`) and a trailing slash.
 */
object RetrofitClient {

    /**
     * Lazily created [FootballApiService] instance.
     *
     * Thread-safe due to Kotlin's `by lazy` delegation with the default [LazyThreadSafetyMode.SYNCHRONIZED] mode.
     */
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