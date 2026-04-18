package com.sakura_ai_reviewer.feature.log.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LogApiService {

    // Review logs
    @GET("logs/reviews")
    suspend fun getReviewLogs(
        @Query("search") search: String = "",
        @Query("repo") repo: String = "",
        @Query("status") status: String = "",
        @Query("date_from") dateFrom: String = "",
        @Query("date_to") dateTo: String = "",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<ReviewLogListData>

    @GET("logs/reviews/{reviewId}")
    suspend fun getReviewLogDetail(
        @Path("reviewId") reviewId: Int
    ): ApiResponse<ReviewLogDetailData>

    // Action logs
    @GET("logs/actions")
    suspend fun getActionLogs(
        @Query("action") action: String = "",
        @Query("start_date") startDate: String = "",
        @Query("end_date") endDate: String = "",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<ActionLogListData>

    @GET("logs/actions/{logId}")
    suspend fun getActionLogDetail(
        @Path("logId") logId: Int
    ): ApiResponse<ActionLogDetailData>
}

@JsonClass(generateAdapter = true)
data class ReviewLogListData(
    @Json(name = "items") val items: List<ReviewLogItemData>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class ReviewLogItemData(
    @Json(name = "id") val id: Int,
    @Json(name = "pr_id") val prId: Long? = null,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "decision") val decision: String? = null,
    @Json(name = "overall_score") val overallScore: Int? = null,
    @Json(name = "strategy") val strategy: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ReviewLogDetailData(
    @Json(name = "id") val id: Int,
    @Json(name = "pr_id") val prId: Long? = null,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "decision") val decision: String? = null,
    @Json(name = "overall_score") val overallScore: Int? = null,
    @Json(name = "review_summary") val reviewSummary: String? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "strategy") val strategy: String? = null,
    @Json(name = "prompt_tokens") val promptTokens: Int? = null,
    @Json(name = "completion_tokens") val completionTokens: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null,
    @Json(name = "comments") val comments: List<ReviewLogCommentData>? = null
)

@JsonClass(generateAdapter = true)
data class ReviewLogCommentData(
    @Json(name = "id") val id: Int,
    @Json(name = "file_path") val filePath: String? = null,
    @Json(name = "line_number") val lineNumber: Int? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "content") val content: String? = null
)

@JsonClass(generateAdapter = true)
data class ActionLogListData(
    @Json(name = "items") val items: List<ActionLogItemData>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class ActionLogItemData(
    @Json(name = "id") val id: Int,
    @Json(name = "admin_id") val adminId: Int? = null,
    @Json(name = "action") val action: String? = null,
    @Json(name = "target_type") val targetType: String? = null,
    @Json(name = "target_id") val targetId: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ActionLogDetailData(
    @Json(name = "id") val id: Int,
    @Json(name = "admin_id") val adminId: Int? = null,
    @Json(name = "admin_username") val adminUsername: String? = null,
    @Json(name = "action") val action: String? = null,
    @Json(name = "target_type") val targetType: String? = null,
    @Json(name = "target_id") val targetId: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)
