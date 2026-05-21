package com.masbek.couponsystem.data.repository

import com.masbek.couponsystem.data.api.ApiService
import com.masbek.couponsystem.data.model.ApiErrorResponse
import com.masbek.couponsystem.data.model.LoginRequest
import com.masbek.couponsystem.data.model.LoginResponse
import com.masbek.couponsystem.data.model.MessageResponse
import com.masbek.couponsystem.util.Result
import com.masbek.couponsystem.util.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.login(
                    LoginRequest(email, password, "android_${android.os.Build.MODEL}")
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    sessionManager.saveToken(body.token)
                    sessionManager.saveUser(
                        body.user.id.toString(),
                        body.user.name,
                        body.user.email
                    )
                    Result.Success(body)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val apiError = try {
                        Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    } catch (e: Exception) { null }
                    Result.Error(
                        apiError?.message ?: "Login gagal",
                        apiError?.errors
                    )
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan jaringan")
            }
        }

    suspend fun logout(): Result<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.logout()
            sessionManager.clearSession()
            if (response.isSuccessful) {
                Result.Success(response.body() ?: MessageResponse("Berhasil keluar"))
            } else {
                Result.Success(MessageResponse("Berhasil keluar"))
            }
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.Success(MessageResponse("Berhasil keluar"))
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getToken(): String? = sessionManager.getToken()
}
