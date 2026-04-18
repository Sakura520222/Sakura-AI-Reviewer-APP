package com.sakura_ai_reviewer.feature.user.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {

    @GET("users")
    suspend fun getUsers(
        @Query("search") search: String = "",
        @Query("role") role: String = "",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<UserListData>

    @GET("users/{userId}")
    suspend fun getUserDetail(
        @Path("userId") userId: Int
    ): ApiResponse<UserDetailData>

    @POST("users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): ApiResponse<UserItemData>

    @PATCH("users/{userId}/role")
    suspend fun updateUserRole(
        @Path("userId") userId: Int,
        @Body request: UpdateRoleRequest
    ): ApiResponse<EmptyData>

    @PATCH("users/{userId}/quota")
    suspend fun updateUserQuota(
        @Path("userId") userId: Int,
        @Body request: UpdateQuotaRequest
    ): ApiResponse<EmptyData>

    @PATCH("users/{userId}/issue-quota")
    suspend fun updateIssueQuota(
        @Path("userId") userId: Int,
        @Body request: UpdateIssueQuotaRequest
    ): ApiResponse<EmptyData>

    @POST("users/{userId}/toggle")
    suspend fun toggleUser(
        @Path("userId") userId: Int
    ): ApiResponse<EmptyData>

    @DELETE("users/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: Int
    ): ApiResponse<EmptyData>

    @PATCH("users/{userId}/info")
    suspend fun updateUserInfo(
        @Path("userId") userId: Int,
        @Body request: UpdateUserInfoRequest
    ): ApiResponse<EmptyData>

    @POST("users/{userId}/reset-quota")
    suspend fun resetUserQuota(
        @Path("userId") userId: Int
    ): ApiResponse<EmptyData>
}

@JsonClass(generateAdapter = true)
data class UserListData(
    @Json(name = "items") val items: List<UserItemData>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class UserItemData(
    @Json(name = "id") val id: Int,
    @Json(name = "telegram_id") val telegramId: Long? = null,
    @Json(name = "github_username") val githubUsername: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "daily_quota") val dailyQuota: Int? = null,
    @Json(name = "weekly_quota") val weeklyQuota: Int? = null,
    @Json(name = "monthly_quota") val monthlyQuota: Int? = null,
    @Json(name = "daily_used") val dailyUsed: Int? = null,
    @Json(name = "weekly_used") val weeklyUsed: Int? = null,
    @Json(name = "monthly_used") val monthlyUsed: Int? = null,
    @Json(name = "issue_daily_quota") val issueDailyQuota: Int? = null,
    @Json(name = "issue_weekly_quota") val issueWeeklyQuota: Int? = null,
    @Json(name = "issue_monthly_quota") val issueMonthlyQuota: Int? = null,
    @Json(name = "issue_daily_used") val issueDailyUsed: Int? = null,
    @Json(name = "issue_weekly_used") val issueWeeklyUsed: Int? = null,
    @Json(name = "issue_monthly_used") val issueMonthlyUsed: Int? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDetailData(
    @Json(name = "id") val id: Int,
    @Json(name = "telegram_id") val telegramId: Long? = null,
    @Json(name = "github_username") val githubUsername: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "daily_quota") val dailyQuota: Int? = null,
    @Json(name = "weekly_quota") val weeklyQuota: Int? = null,
    @Json(name = "monthly_quota") val monthlyQuota: Int? = null,
    @Json(name = "daily_used") val dailyUsed: Int? = null,
    @Json(name = "weekly_used") val weeklyUsed: Int? = null,
    @Json(name = "monthly_used") val monthlyUsed: Int? = null,
    @Json(name = "issue_daily_quota") val issueDailyQuota: Int? = null,
    @Json(name = "issue_weekly_quota") val issueWeeklyQuota: Int? = null,
    @Json(name = "issue_monthly_quota") val issueMonthlyQuota: Int? = null,
    @Json(name = "issue_daily_used") val issueDailyUsed: Int? = null,
    @Json(name = "issue_weekly_used") val issueWeeklyUsed: Int? = null,
    @Json(name = "issue_monthly_used") val issueMonthlyUsed: Int? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "usage_logs") val usageLogs: List<UsageLogData>? = null
)

@JsonClass(generateAdapter = true)
data class UsageLogData(
    @Json(name = "id") val id: Int,
    @Json(name = "quota_type") val quotaType: String? = null,
    @Json(name = "used_count") val usedCount: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateUserRequest(
    @Json(name = "telegram_id") val telegramId: Long,
    @Json(name = "github_username") val githubUsername: String,
    @Json(name = "role") val role: String = "user",
    @Json(name = "daily_quota") val dailyQuota: Int = 10,
    @Json(name = "weekly_quota") val weeklyQuota: Int = 50,
    @Json(name = "monthly_quota") val monthlyQuota: Int = 200,
    @Json(name = "issue_daily_quota") val issueDailyQuota: Int = 20,
    @Json(name = "issue_weekly_quota") val issueWeeklyQuota: Int = 80,
    @Json(name = "issue_monthly_quota") val issueMonthlyQuota: Int = 300
)

@JsonClass(generateAdapter = true)
data class UpdateRoleRequest(
    @Json(name = "role") val role: String
)

@JsonClass(generateAdapter = true)
data class UpdateQuotaRequest(
    @Json(name = "daily_quota") val dailyQuota: Int,
    @Json(name = "weekly_quota") val weeklyQuota: Int,
    @Json(name = "monthly_quota") val monthlyQuota: Int
)

@JsonClass(generateAdapter = true)
data class UpdateIssueQuotaRequest(
    @Json(name = "issue_daily_quota") val issueDailyQuota: Int,
    @Json(name = "issue_weekly_quota") val issueWeeklyQuota: Int,
    @Json(name = "issue_monthly_quota") val issueMonthlyQuota: Int
)

@JsonClass(generateAdapter = true)
data class UpdateUserInfoRequest(
    @Json(name = "telegram_id") val telegramId: Long,
    @Json(name = "github_username") val githubUsername: String
)
