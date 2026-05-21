package com.masbek.couponsystem.data.model

import com.google.gson.annotations.SerializedName

// ─── Auth ───

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val email: String
)

// ─── Dashboard ───

data class DashboardStatsResponse(
    val data: DashboardStats
)

data class DashboardStats(
    @SerializedName("total_projects") val totalProjects: Int,
    @SerializedName("generated_batches") val generatedBatches: Int,
    @SerializedName("total_coupons") val totalCoupons: Int,
    @SerializedName("recent_projects") val recentProjects: List<ProjectSummary>?
)

data class ProjectSummary(
    val id: Int,
    val code: String,
    val name: String,
    val status: String,
    @SerializedName("total_coupons") val totalCoupons: Int,
    @SerializedName("created_by") val createdBy: String?
)

// ─── Projects ───

data class ProjectListResponse(
    val data: List<Project>,
    val links: PaginationLinks?,
    val meta: PaginationMeta?
)

data class PaginationLinks(
    val first: String?,
    val last: String?,
    val prev: String?,
    val next: String?
)

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    val total: Int
)

data class Project(
    val id: Int,
    val code: String,
    val name: String,
    val description: String?,
    val status: String,
    @SerializedName("total_coupons") val totalCoupons: Int,
    @SerializedName("coupons_per_box") val couponsPerBox: Int?,
    @SerializedName("total_boxes") val totalBoxes: Int?,
    @SerializedName("total_batches") val totalBatches: Int?,
    @SerializedName("boxes_per_batch") val boxesPerBatch: Int?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("prize_tiers") val prizeTiers: List<PrizeTier>?
)

data class ProjectDetailResponse(
    val data: Project
)

data class PrizeTier(
    val id: Int?,
    val name: String,
    val amount: Int,
    @SerializedName("per_box_qty") val perBoxQty: Int,
    @SerializedName("total_qty") val totalQty: Int?
)

data class CreateProjectRequest(
    val name: String,
    val code: String,
    val description: String?,
    @SerializedName("total_coupons") val totalCoupons: Int,
    @SerializedName("coupons_per_box") val couponsPerBox: Int,
    @SerializedName("total_batches") val totalBatches: Int,
    @SerializedName("prize_tiers") val prizeTiers: List<TierRequest>
)

data class TierRequest(
    val name: String,
    val amount: Int,
    @SerializedName("per_box_qty") val perBoxQty: Int
)

data class CreateProjectResponse(
    val data: Project,
    val message: String?
)

// ─── Batches ───

data class BatchListResponse(
    val data: List<Batch>
)

data class Batch(
    val id: Int,
    @SerializedName("batch_number") val batchNumber: Int,
    val status: String,
    val operator: String?,
    val location: String?,
    @SerializedName("generated_at") val generatedAt: String?,
    @SerializedName("total_boxes") val totalBoxes: Int?,
    @SerializedName("project_id") val projectId: Int?
)

data class GenerateBatchRequest(
    val location: String
)

data class GenerateBatchResponse(
    val data: Batch,
    val message: String?
)

// ─── Batch Report ───

data class BatchReportResponse(
    val data: BatchReport
)

data class BatchReport(
    val id: Int,
    @SerializedName("batch_number") val batchNumber: Int,
    val status: String,
    val operator: String?,
    val location: String?,
    @SerializedName("total_boxes") val totalBoxes: Int?,
    @SerializedName("project_name") val projectName: String?,
    val boxes: List<BoxReport>?
)

data class BoxReport(
    @SerializedName("box_number") val boxNumber: Int,
    @SerializedName("coupon_count") val couponCount: Int,
    @SerializedName("prize_distribution") val prizeDistribution: List<PrizeDistribution>?
)

data class PrizeDistribution(
    @SerializedName("tier_name") val tierName: String,
    val count: Int
)

// ─── Coupons ───

data class CouponListResponse(
    val data: List<Coupon>,
    val links: PaginationLinks?,
    val meta: PaginationMeta?
)

data class Coupon(
    val id: Int,
    @SerializedName("serial_number") val serialNumber: String,
    @SerializedName("box_number") val boxNumber: Int,
    val position: Int,
    @SerializedName("prize_tier") val prizeTier: String?,
    val amount: Int,
    @SerializedName("batch_number") val batchNumber: Int?
)

// ─── Errors ───

data class ApiErrorResponse(
    val message: String?,
    val errors: Map<String, List<String>>?
)

// ─── Settings ───

data class UpdateProfileRequest(
    val name: String,
    val email: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String
)

data class MessageResponse(
    val message: String?
)
