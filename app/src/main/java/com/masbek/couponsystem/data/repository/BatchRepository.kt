package com.masbek.couponsystem.data.repository

import com.masbek.couponsystem.data.api.ApiService
import com.masbek.couponsystem.data.model.*
import com.masbek.couponsystem.util.Result
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getProjectBatches(projectId: Int): Result<List<Batch>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getProjectBatches(projectId)
                if (response.isSuccessful) {
                    Result.Success(response.body()!!.data)
                } else {
                    val apiError = parseApiError(response.errorBody()?.string())
                    Result.Error(apiError?.message ?: "Gagal memuat batch")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan jaringan")
            }
        }

    suspend fun generateBatch(batchId: Int, location: String): Result<Batch> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.generateBatch(batchId, GenerateBatchRequest(location))
                if (response.isSuccessful) {
                    Result.Success(response.body()!!.data)
                } else {
                    val apiError = parseApiError(response.errorBody()?.string())
                    Result.Error(
                        apiError?.message ?: "Gagal generate batch",
                        apiError?.errors
                    )
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan jaringan")
            }
        }

    suspend fun getBatchReport(batchId: Int): Result<BatchReport> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getBatchReport(batchId)
                if (response.isSuccessful) {
                    Result.Success(response.body()!!.data)
                } else {
                    val apiError = parseApiError(response.errorBody()?.string())
                    Result.Error(apiError?.message ?: "Gagal memuat laporan batch")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan jaringan")
            }
        }

    private fun parseApiError(errorBody: String?): ApiErrorResponse? {
        return try {
            Gson().fromJson(errorBody, ApiErrorResponse::class.java)
        } catch (e: Exception) { null }
    }
}
