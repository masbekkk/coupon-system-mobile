package com.masbek.couponsystem.data.repository

import com.masbek.couponsystem.BuildConfig
import com.masbek.couponsystem.data.api.ApiService
import com.masbek.couponsystem.data.model.*
import com.masbek.couponsystem.util.Result
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getCoupons(
        projectId: Int,
        page: Int = 1,
        perPage: Int = 25,
        search: String? = null,
        batchId: Int? = null,
        prizeTier: String? = null,
        sort: String? = null
    ): Result<CouponListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProjectCoupons(
                projectId, page, perPage, search, batchId, prizeTier, sort
            )
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                val apiError = try {
                    Gson().fromJson(response.errorBody()?.string(), ApiErrorResponse::class.java)
                } catch (e: Exception) { null }
                Result.Error(apiError?.message ?: "Gagal memuat kupon")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    fun buildExportUrl(projectId: Int): String {
        return "${BuildConfig.BASE_URL}projects/$projectId/coupons/export"
    }
}
