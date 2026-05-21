package com.masbek.couponsystem.data.api

import com.masbek.couponsystem.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ───

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponse>

    // ─── Dashboard ───

    @GET("dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStatsResponse>

    // ─── Projects ───

    @GET("projects")
    suspend fun getProjects(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15
    ): Response<ProjectListResponse>

    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<CreateProjectResponse>

    @GET("projects/{id}")
    suspend fun getProjectDetail(@Path("id") id: Int): Response<ProjectDetailResponse>

    @DELETE("projects/{id}")
    suspend fun deleteProject(@Path("id") id: Int): Response<MessageResponse>

    // ─── Batches ───

    @GET("projects/{id}/batches")
    suspend fun getProjectBatches(@Path("id") projectId: Int): Response<BatchListResponse>

    @POST("batches/{id}/generate")
    suspend fun generateBatch(
        @Path("id") batchId: Int,
        @Body request: GenerateBatchRequest
    ): Response<GenerateBatchResponse>

    @GET("batches/{id}/report")
    suspend fun getBatchReport(@Path("id") batchId: Int): Response<BatchReportResponse>

    // ─── Coupons ───

    @GET("projects/{id}/coupons")
    suspend fun getProjectCoupons(
        @Path("id") projectId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 25,
        @Query("search") search: String? = null,
        @Query("batch_id") batchId: Int? = null,
        @Query("prize_tier") prizeTier: String? = null,
        @Query("sort") sort: String? = null
    ): Response<CouponListResponse>

    // ─── Settings ───

    @PATCH("user")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<MessageResponse>

    @POST("user/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>
}
