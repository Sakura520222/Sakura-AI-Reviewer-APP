package com.sakura_ai_reviewer.feature.issue.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IssueApiService {

    @GET("issues")
    suspend fun getIssues(
        @Query("search") search: String = "",
        @Query("repo_name") repoName: String = "",
        @Query("category") category: String = "",
        @Query("priority") priority: String = "",
        @Query("status") status: String = "",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<IssueListData>

    @GET("issues/stats")
    suspend fun getIssueStats(): ApiResponse<IssueStatsData>

    @GET("issues/{issueId}")
    suspend fun getIssueDetail(
        @Path("issueId") issueId: Int
    ): ApiResponse<IssueDetailData>

    @POST("issues/{issueId}/reanalyze")
    suspend fun reanalyzeIssue(
        @Path("issueId") issueId: Int
    ): ApiResponse<ReanalyzeResultData>
}

@JsonClass(generateAdapter = true)
data class IssueListData(
    @Json(name = "items") val items: List<IssueItemData>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class IssueItemData(
    @Json(name = "id") val id: Int,
    @Json(name = "issue_number") val issueNumber: Int,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "priority") val priority: String? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "feasibility") val feasibility: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "comment_posted") val commentPosted: Int? = null,
    @Json(name = "labels_applied") val labelsApplied: Int? = null,
    @Json(name = "applied_label_names") val appliedLabelNames: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class IssueDetailData(
    @Json(name = "id") val id: Int,
    @Json(name = "issue_number") val issueNumber: Int,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "priority") val priority: String? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "feasibility") val feasibility: String? = null,
    @Json(name = "suggested_title") val suggestedTitle: String? = null,
    @Json(name = "suggested_assignees") val suggestedAssignees: List<String>? = null,
    @Json(name = "suggested_labels") val suggestedLabels: List<String>? = null,
    @Json(name = "suggested_milestone") val suggestedMilestone: String? = null,
    @Json(name = "duplicate_of") val duplicateOf: Int? = null,
    @Json(name = "related_prs") val relatedPrs: List<Any>? = null,
    @Json(name = "analysis_detail") val analysisDetail: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "comment_posted") val commentPosted: Int? = null,
    @Json(name = "comment_url") val commentUrl: String? = null,
    @Json(name = "labels_applied") val labelsApplied: Int? = null,
    @Json(name = "applied_label_names") val appliedLabelNames: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class IssueStatsData(
    @Json(name = "total") val total: Int,
    @Json(name = "by_category") val byCategory: Map<String, Int>,
    @Json(name = "by_priority") val byPriority: Map<String, Int>,
    @Json(name = "by_status") val byStatus: Map<String, Int>
)

@JsonClass(generateAdapter = true)
data class ReanalyzeResultData(
    @Json(name = "task_id") val taskId: String
)
