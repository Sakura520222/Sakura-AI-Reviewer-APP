package com.sakura_ai_reviewer.feature.dashboard.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST

interface DashboardApiService {

    @GET("dashboard/stats")
    suspend fun getStats(): ApiResponse<DashboardStatsData>

    @GET("dashboard/recent-reviews")
    suspend fun getRecentReviews(): ApiResponse<List<RecentReviewData>>

    @GET("dashboard/chart-data")
    suspend fun getChartData(): ApiResponse<ChartDataResponse>

    @POST("dashboard/cache/refresh")
    suspend fun refreshCache(): ApiResponse<EmptyData>
}

@JsonClass(generateAdapter = true)
data class DashboardStatsData(
    @Json(name = "total") val total: Int,
    @Json(name = "completed") val completed: Int,
    @Json(name = "reviewing") val reviewing: Int,
    @Json(name = "pending") val pending: Int,
    @Json(name = "failed") val failed: Int,
    @Json(name = "approved") val approved: Int,
    @Json(name = "changes_requested") val changesRequested: Int,
    @Json(name = "avg_score") val avgScore: Double,
    @Json(name = "comment_count") val commentCount: Int,
    @Json(name = "total_prompt_tokens") val totalPromptTokens: Long,
    @Json(name = "total_completion_tokens") val totalCompletionTokens: Long,
    @Json(name = "total_estimated_cost") val totalEstimatedCost: Long
)

@JsonClass(generateAdapter = true)
data class RecentReviewData(
    @Json(name = "id") val id: Int,
    @Json(name = "pr_id") val prId: Long,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "overall_score") val overallScore: Int? = null,
    @Json(name = "decision") val decision: String? = null,
    @Json(name = "strategy") val strategy: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartDataResponse(
    @Json(name = "trend") val trend: ChartTrendData,
    @Json(name = "decisions") val decisions: ChartDecisionsData,
    @Json(name = "top_repos") val topRepos: ChartTopReposData,
    @Json(name = "tokens") val tokens: ChartTokensData
)

@JsonClass(generateAdapter = true)
data class ChartTrendData(
    @Json(name = "labels") val labels: List<String>,
    @Json(name = "completed") val completed: List<Int>,
    @Json(name = "failed") val failed: List<Int>
)

@JsonClass(generateAdapter = true)
data class ChartDecisionsData(
    @Json(name = "labels") val labels: List<String>,
    @Json(name = "counts") val counts: List<Int>
)

@JsonClass(generateAdapter = true)
data class ChartTopReposData(
    @Json(name = "labels") val labels: List<String>,
    @Json(name = "counts") val counts: List<Int>
)

@JsonClass(generateAdapter = true)
data class ChartTokensData(
    @Json(name = "labels") val labels: List<String>,
    @Json(name = "tokens") val tokens: List<Long>
)
