package com.masbek.couponsystem.util

import android.content.Intent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : Interceptor {

    companion object {
        const val ACTION_UNAUTHORIZED = "com.masbek.couponsystem.UNAUTHORIZED"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        val token = sessionManager.getToken()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            sessionManager.clearSession()
            val intent = Intent(ACTION_UNAUTHORIZED)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        }

        return response
    }
}
