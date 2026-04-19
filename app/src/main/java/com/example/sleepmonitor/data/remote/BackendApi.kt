package com.example.sleepmonitor.data.remote

import com.example.sleepmonitor.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface BackendApi {
    @GET("api/users/{userId}/snapshot")
    suspend fun getUserSnapshot(@Path("userId") userId: String): RemoteUserSnapshotDto

    @PUT("api/users/{userId}")
    suspend fun upsertUser(
        @Path("userId") userId: String,
        @Body user: RemoteUserDto
    )

    @DELETE("api/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String)

    @PUT("api/sessions/{sessionId}")
    suspend fun upsertSession(
        @Path("sessionId") sessionId: String,
        @Body session: RemoteSessionBundleDto
    )

    @DELETE("api/sessions/{sessionId}")
    suspend fun deleteSession(@Path("sessionId") sessionId: String)
}

object BackendApiFactory {
    fun create(): BackendApi? {
        if (!BuildConfig.BACKEND_SYNC_ENABLED || BuildConfig.BACKEND_BASE_URL.isBlank()) {
            return null
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(BackendApi::class.java)
    }
}
