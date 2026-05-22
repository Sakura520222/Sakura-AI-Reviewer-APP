package com.sakura_ai_reviewer.feature.billing.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BillingApiService {

    @GET("billing/plans")
    suspend fun getPlans(): List<PlanData>

    @POST("billing/redeem")
    suspend fun redeemCode(@Body body: RedeemCodeRequest): RedeemCodeResponse

    @GET("billing/orders")
    suspend fun getOrders(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): OrdersResponse

    @POST("billing/admin/plans")
    suspend fun createPlan(@Body body: CreatePlanRequest): CreatePlanResponse

    @PUT("billing/admin/plans/{planId}")
    suspend fun updatePlan(
        @Path("planId") planId: Int,
        @Body body: UpdatePlanRequest
    ): UpdatePlanResponse

    @DELETE("billing/admin/plans/{planId}")
    suspend fun deletePlan(
        @Path("planId") planId: Int,
        @Query("hard") hard: Boolean = false
    ): DeletePlanResponse

    @POST("billing/admin/codes/generate")
    suspend fun generateCodes(@Body body: GenerateCodesRequest): GenerateCodesResponse

    @PUT("billing/admin/codes/{codeId}")
    suspend fun updateCode(
        @Path("codeId") codeId: Int,
        @Body body: UpdateCodeRequest
    ): UpdateCodeResponse

    @DELETE("billing/admin/codes/{codeId}")
    suspend fun deleteCode(@Path("codeId") codeId: Int): DeleteCodeResponse

    @POST("billing/admin/grant")
    suspend fun grantPlan(@Body body: GrantPlanRequest): GrantPlanResponse
}

@JsonClass(generateAdapter = true)
data class PlanData(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "plan_type") val planType: String,
    @Json(name = "price_cents") val priceCents: Int? = null,
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "duration_days") val durationDays: Int? = null,
    @Json(name = "pr_quota_bonus") val prQuotaBonus: Int? = null,
    @Json(name = "pr_daily_add") val prDailyAdd: Int? = null,
    @Json(name = "pr_weekly_add") val prWeeklyAdd: Int? = null,
    @Json(name = "pr_monthly_add") val prMonthlyAdd: Int? = null,
    @Json(name = "issue_quota_bonus") val issueQuotaBonus: Int? = null,
    @Json(name = "issue_daily_add") val issueDailyAdd: Int? = null,
    @Json(name = "issue_weekly_add") val issueWeeklyAdd: Int? = null,
    @Json(name = "issue_monthly_add") val issueMonthlyAdd: Int? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class RedeemCodeRequest(
    @Json(name = "code") val code: String
)

@JsonClass(generateAdapter = true)
data class RedeemCodeResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "order_no") val orderNo: String? = null,
    @Json(name = "plan_name") val planName: String? = null
)

@JsonClass(generateAdapter = true)
data class OrdersResponse(
    @Json(name = "total") val total: Int,
    @Json(name = "orders") val orders: List<OrderData>
)

@JsonClass(generateAdapter = true)
data class OrderData(
    @Json(name = "id") val id: Int,
    @Json(name = "order_no") val orderNo: String,
    @Json(name = "plan_name") val planName: String,
    @Json(name = "amount_cents") val amountCents: Int,
    @Json(name = "currency") val currency: String,
    @Json(name = "status") val status: String,
    @Json(name = "payment_provider") val paymentProvider: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "fulfilled_at") val fulfilledAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreatePlanRequest(
    @Json(name = "name") val name: String,
    @Json(name = "plan_type") val planType: String,
    @Json(name = "price_cents") val priceCents: Int? = null,
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "duration_days") val durationDays: Int? = null,
    @Json(name = "pr_quota_bonus") val prQuotaBonus: Int? = null,
    @Json(name = "issue_quota_bonus") val issueQuotaBonus: Int? = null,
    @Json(name = "pr_daily_add") val prDailyAdd: Int? = null,
    @Json(name = "pr_weekly_add") val prWeeklyAdd: Int? = null,
    @Json(name = "pr_monthly_add") val prMonthlyAdd: Int? = null,
    @Json(name = "issue_daily_add") val issueDailyAdd: Int? = null,
    @Json(name = "issue_weekly_add") val issueWeeklyAdd: Int? = null,
    @Json(name = "issue_monthly_add") val issueMonthlyAdd: Int? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "sort_order") val sortOrder: Int? = null
)

@JsonClass(generateAdapter = true)
data class CreatePlanResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class UpdatePlanRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "plan_type") val planType: String? = null,
    @Json(name = "price_cents") val priceCents: Int? = null,
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "duration_days") val durationDays: Int? = null,
    @Json(name = "pr_quota_bonus") val prQuotaBonus: Int? = null,
    @Json(name = "issue_quota_bonus") val issueQuotaBonus: Int? = null,
    @Json(name = "pr_daily_add") val prDailyAdd: Int? = null,
    @Json(name = "pr_weekly_add") val prWeeklyAdd: Int? = null,
    @Json(name = "pr_monthly_add") val prMonthlyAdd: Int? = null,
    @Json(name = "issue_daily_add") val issueDailyAdd: Int? = null,
    @Json(name = "issue_weekly_add") val issueWeeklyAdd: Int? = null,
    @Json(name = "issue_monthly_add") val issueMonthlyAdd: Int? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "sort_order") val sortOrder: Int? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePlanResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class DeletePlanResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "hard_delete") val hardDelete: Boolean
)

@JsonClass(generateAdapter = true)
data class GenerateCodesRequest(
    @Json(name = "plan_id") val planId: Int,
    @Json(name = "count") val count: Int,
    @Json(name = "batch_name") val batchName: String? = null,
    @Json(name = "max_uses") val maxUses: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateCodesResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "codes") val codes: List<String>
)

@JsonClass(generateAdapter = true)
data class UpdateCodeRequest(
    @Json(name = "status") val status: String? = null,
    @Json(name = "expires_at") val expiresAt: String? = null,
    @Json(name = "max_uses") val maxUses: Int? = null,
    @Json(name = "plan_id") val planId: Int? = null
)

@JsonClass(generateAdapter = true)
data class UpdateCodeResponse(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "id") val id: Int? = null,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class DeleteCodeResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "id") val id: Int,
    @Json(name = "code") val code: String
)

@JsonClass(generateAdapter = true)
data class GrantPlanRequest(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "plan_id") val planId: Int
)

@JsonClass(generateAdapter = true)
data class GrantPlanResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "order_no") val orderNo: String? = null
)
