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
class ProjectRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getProjects(page: Int = 1, perPage: Int = 15): Result<ProjectListResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getProjects(page, perPage)
                if (response.isSuccessful) {
                    Result.Success(response.body()!!)
                } else {
                    parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan jaringan")
            }
        }

    suspend fun getProjectDetail(id: Int): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProjectDetail(id)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.data)
            } else {
                parseError(response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    suspend fun createProject(request: CreateProjectRequest): Result<Project> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createProject(request)
                if (response.isSuccessful) {
                    Result.Success(response.body()!!.data)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val apiError = try {
                        Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    } catch (e: Exception) { null }
                    Result.Error(
                        apiError?.message ?: "Gagal membuat proyek",
                        apiError?.errors
                    )
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan jaringan")
            }
        }

    suspend fun deleteProject(id: Int): Result<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProject(id)
            if (response.isSuccessful) {
                Result.Success(response.body() ?: MessageResponse("Proyek berhasil dihapus"))
            } else {
                parseError(response.errorBody()?.string())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    private fun <T> parseError(errorBody: String?): Result<T> {
        val apiError = try {
            Gson().fromJson(errorBody, ApiErrorResponse::class.java)
        } catch (e: Exception) { null }
        return Result.Error(apiError?.message ?: "Terjadi kesalahan", apiError?.errors)
    }
}
