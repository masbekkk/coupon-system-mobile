package com.masbek.couponsystem.data.repository

import com.masbek.couponsystem.data.api.ApiService
import com.masbek.couponsystem.data.model.DashboardStats
import com.masbek.couponsystem.data.model.ApiErrorResponse
import com.masbek.couponsystem.util.Result
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getStats(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboardStats()
            if (response.isSuccessful) {
                Result.Success(response.body()!!.data)
            } else {
                val errorBody = response.errorBody()?.string()
                val apiError = try {
                    Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                } catch (e: Exception) { null }
                Result.Error(apiError?.message ?: "Gagal memuat dashboard")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }
}
