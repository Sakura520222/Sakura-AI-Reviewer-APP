package com.sakura_ai_reviewer.feature.review.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {

    @GET("reviews")
    suspend fun getReviews(
        @Query("search") search: String = "",
        @Query("status") status: String = "",
        @Query("decision") decision: String = "",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<ReviewListData>

    @GET("reviews/{reviewId}")
    suspend fun getReviewDetail(
        @Path("reviewId") reviewId: Int
    ): ApiResponse<ReviewDetailData>

    @GET("reviews/{reviewId}/files")
    suspend fun getReviewFiles(
        @Path("reviewId") reviewId: Int
    ): ApiResponse<List<ReviewFileData>>

    @GET("reviews/{reviewId}/comments")
    suspend fun getReviewComments(
        @Path("reviewId") reviewId: Int,
        @Query("file_path") filePath: String = ""
    ): ApiResponse<List<ReviewCommentData>>
}

@JsonClass(generateAdapter = true)
data class ReviewListData(
    @Json(name = "items") val items: List<ReviewItemData>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class ReviewItemData(
    @Json(name = "id") val id: Int,
    @Json(name = "pr_id") val prId: Long,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "branch") val branch: String? = null,
    @Json(name = "file_count") val fileCount: Int? = null,
    @Json(name = "line_count") val lineCount: Int? = null,
    @Json(name = "code_file_count") val codeFileCount: Int? = null,
    @Json(name = "strategy") val strategy: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "review_summary") val reviewSummary: String? = null,
    @Json(name = "overall_score") val overallScore: Int? = null,
    @Json(name = "decision") val decision: String? = null,
    @Json(name = "decision_reason") val decisionReason: String? = null,
    @Json(name = "prompt_tokens") val promptTokens: Int? = null,
    @Json(name = "completion_tokens") val completionTokens: Int? = null,
    @Json(name = "estimated_cost") val estimatedCost: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ReviewDetailData(
    @Json(name = "id") val id: Int,
    @Json(name = "pr_id") val prId: Long,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "branch") val branch: String? = null,
    @Json(name = "file_count") val fileCount: Int? = null,
    @Json(name = "line_count") val lineCount: Int? = null,
    @Json(name = "code_file_count") val codeFileCount: Int? = null,
    @Json(name = "strategy") val strategy: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "review_summary") val reviewSummary: String? = null,
    @Json(name = "overall_score") val overallScore: Int? = null,
    @Json(name = "decision") val decision: String? = null,
    @Json(name = "decision_reason") val decisionReason: String? = null,
    @Json(name = "prompt_tokens") val promptTokens: Int? = null,
    @Json(name = "completion_tokens") val completionTokens: Int? = null,
    @Json(name = "estimated_cost") val estimatedCost: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null,
    @Json(name = "comments") val comments: List<ReviewCommentData>? = null
)

@JsonClass(generateAdapter = true)
data class ReviewCommentData(
    @Json(name = "id") val id: Int,
    @Json(name = "file_path") val filePath: String? = null,
    @Json(name = "line_number") val lineNumber: Int? = null,
    @Json(name = "comment_type") val commentType: String? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ReviewFileData(
    @Json(name = "file_path") val filePath: String,
    @Json(name = "severity_counts") val severityCounts: SeverityCountsData,
    @Json(name = "comment_count") val commentCount: Int
)

@JsonClass(generateAdapter = true)
data class SeverityCountsData(
    @Json(name = "critical") val critical: Int,
    @Json(name = "major") val major: Int,
    @Json(name = "minor") val minor: Int,
    @Json(name = "suggestion") val suggestion: Int
)
