package com.masbek.couponsystem.data.model

import com.google.gson.JsonElement
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
    val id: String,
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

data class ProjectConfig(
    @SerializedName("total_coupons") val totalCoupons: Int,
    @SerializedName("total_boxes") val totalBoxes: Int?,
    @SerializedName("coupons_per_box") val couponsPerBox: Int?,
    @SerializedName("total_batches") val totalBatches: Int?,
    @SerializedName("boxes_per_batch") val boxesPerBatch: Int?
)

data class ProjectCreator(
    val id: String,
    val name: String
)

data class ProjectSummary(
    val id: Int,
    val code: String,
    val name: String,
    val status: String,
    val config: ProjectConfig?,
    val creator: ProjectCreator?,
    @SerializedName("total_coupons") private val _totalCoupons: Int?,
    @SerializedName("created_by") private val _createdBy: String?
) {
    val totalCoupons: Int get() = config?.totalCoupons ?: _totalCoupons ?: 0
    val createdBy: String? get() = creator?.name ?: _createdBy
}

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
    val config: ProjectConfig?,
    val creator: ProjectCreator?,
    @SerializedName("total_coupons") private val _totalCoupons: Int?,
    @SerializedName("coupons_per_box") private val _couponsPerBox: Int?,
    @SerializedName("total_boxes") private val _totalBoxes: Int?,
    @SerializedName("total_batches") private val _totalBatches: Int?,
    @SerializedName("boxes_per_batch") private val _boxesPerBatch: Int?,
    @SerializedName("created_by") private val _createdBy: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("prize_tiers") val prizeTiers: List<PrizeTier>?
) {
    val totalCoupons: Int get() = config?.totalCoupons ?: _totalCoupons ?: 0
    val couponsPerBox: Int? get() = config?.couponsPerBox ?: _couponsPerBox
    val totalBoxes: Int? get() = config?.totalBoxes ?: _totalBoxes
    val totalBatches: Int? get() = config?.totalBatches ?: _totalBatches
    val boxesPerBatch: Int? get() = config?.boxesPerBatch ?: _boxesPerBatch
    val createdBy: String? get() = creator?.name ?: _createdBy
}

data class ProjectDetailResponse(
    val data: Project
)

data class PrizeTier(
    val id: Int?,
    val name: String,
    val amount: Int,
    @SerializedName("per_box_qty") val perBoxQty: Int? = 0,
    @SerializedName("total_qty") val totalQty: Int? = 0
)

data class CreateProjectRequest(
    val name: String,
    val code: String,
    val description: String?,
    @SerializedName("total_coupons") val totalCoupons: Int,
    @SerializedName("coupons_per_box") val couponsPerBox: Int,
    @SerializedName("total_boxes") val totalBoxes: Int,
    @SerializedName("total_batches") val totalBatches: Int,
    @SerializedName("boxes_per_batch") val boxesPerBatch: Int,
    @SerializedName("tiers") val tiers: List<TierRequest>
)

data class TierRequest(
    val name: String,
    val amount: Int,
    @SerializedName("total_quantity") val totalQuantity: Int,
    @SerializedName("per_box_quantity") val perBoxQuantity: Int
)

data class CreateProjectResponse(
    val data: Project,
    val message: String?
)

// ─── Batches ───

data class BatchOperator(
    val id: String,
    val name: String,
    val email: String?
)

data class BatchListResponse(
    val data: List<Batch>
)

data class Batch(
    val id: Int,
    @SerializedName("batch_number") val batchNumber: Int,
    val status: String,
    val location: String?,
    @SerializedName("produced_at") private val _producedAt: String?,
    @SerializedName("generated_at") private val _generatedAt: String?,
    @SerializedName("operator") private val _operatorElement: JsonElement?,
    @SerializedName("operator_name") private val _operatorName: String?,
    @SerializedName("total_boxes") val totalBoxes: Int?,
    @SerializedName("project_id") val projectId: Int?
) {
    val operator: String? get() {
        val element = _operatorElement ?: return _operatorName
        return if (element.isJsonPrimitive) {
            element.asString
        } else if (element.isJsonObject) {
            element.asJsonObject.get("name")?.asString
        } else {
            null
        }
    }
    val generatedAt: String? get() = _producedAt ?: _generatedAt
}

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
    val location: String?,
    @SerializedName("produced_at") private val _producedAt: String?,
    @SerializedName("generated_at") private val _generatedAt: String?,
    @SerializedName("operator") private val _operatorElement: JsonElement?,
    @SerializedName("operator_name") private val _operatorName: String?,
    @SerializedName("total_boxes") val totalBoxes: Int?,
    @SerializedName("project_name") val projectName: String?,
    val boxes: List<BoxReport>?
) {
    val operator: String? get() {
        val element = _operatorElement ?: return _operatorName
        return if (element.isJsonPrimitive) {
            element.asString
        } else if (element.isJsonObject) {
            element.asJsonObject.get("name")?.asString
        } else {
            null
        }
    }
    val generatedAt: String? get() = _producedAt ?: _generatedAt
}

data class BoxReport(
    @SerializedName("box_number") val boxNumber: Int,
    @SerializedName("coupon_count") private val _couponCount: Int?,
    @SerializedName("total_coupons") private val _totalCoupons: Int?,
    @SerializedName("prize_distribution") private val _prizeDistributionMap: Map<String, Int>?
) {
    val couponCount: Int get() = _couponCount ?: _totalCoupons ?: 0
    val prizeDistribution: List<PrizeDistribution>?
        get() = _prizeDistributionMap?.map { (name, count) ->
            PrizeDistribution(name, count)
        }
}

data class PrizeDistribution(
    @SerializedName("tier_name") val tierName: String,
    val count: Int
)

// ─── Coupons ───

data class CouponBox(
    val id: Int,
    @SerializedName("box_number") val boxNumber: Int,
    @SerializedName("batch_id") val batchId: Int?
)

data class CouponPaginationLink(
    val url: String?,
    val label: String?,
    val active: Boolean?
)

data class CouponListResponse(
    val data: List<Coupon>,
    @SerializedName("meta") private val _metaObj: PaginationMeta?,
    val links: List<CouponPaginationLink>?,
    
    // Flat properties from Laravel paginator
    @SerializedName("current_page") private val _currentPage: Int?,
    @SerializedName("last_page") private val _lastPage: Int?,
    @SerializedName("per_page") private val _perPage: Int?,
    private val _total: Int?
) {
    val meta: PaginationMeta?
        get() = _metaObj ?: if (_currentPage != null && _lastPage != null && _perPage != null && _total != null) {
            PaginationMeta(_currentPage, _lastPage, _perPage, _total)
        } else null
}

data class Coupon(
    val id: Int,
    @SerializedName("serial_number") val serialNumber: String,
    
    // Support nested box structure
    val box: CouponBox?,
    @SerializedName("box_number") private val _boxNumber: Int?,
    
    // Support position_in_box and position
    @SerializedName("position_in_box") private val _positionInBox: Int?,
    private val _position: Int?,
    
    // Support nested prize_tier structure
    @SerializedName("prize_tier") private val _prizeTierObj: PrizeTier?,
    @SerializedName("prize_tier_name") private val _prizeTierName: String?,
    
    // Support nested amount inside prize_tier or flat amount
    private val _amount: Int?,
    
    @SerializedName("batch_number") val batchNumber: Int?
) {
    val boxNumber: Int get() = box?.boxNumber ?: _boxNumber ?: 0
    val position: Int get() = _positionInBox ?: _position ?: 0
    val prizeTier: String? get() = _prizeTierObj?.name ?: _prizeTierName
    val amount: Int get() = _prizeTierObj?.amount ?: _amount ?: 0
}

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
