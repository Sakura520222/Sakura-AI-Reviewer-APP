package com.sakura_ai_reviewer.feature.scan.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScanApiService {

    @GET("scans")
    suspend fun getScans(
        @Query("search") search: String = "",
        @Query("repo_name") repoName: String = "",
        @Query("status") status: String = "",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<ScanListData>

    @GET("scans/stats")
    suspend fun getStats(): ApiResponse<ScanStatsData>

    @GET("scans/{scanId}")
    suspend fun getScanDetail(
        @Path("scanId") scanId: Int
    ): ApiResponse<ScanDetailData>

    @POST("scans/trigger")
    suspend fun triggerScan(): ApiResponse<TriggerResultData>

    @POST("scans/{scanId}/retry")
    suspend fun retryScan(
        @Path("scanId") scanId: Int
    ): ApiResponse<EmptyData>

    @POST("scans/{scanId}/cancel")
    suspend fun cancelScan(
        @Path("scanId") scanId: Int
    ): ApiResponse<EmptyData>
}

@JsonClass(generateAdapter = true)
data class ScanListData(
    @Json(name = "items") val items: List<ScanItemData>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class ScanItemData(
    @Json(name = "id") val id: Int,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "trigger_type") val triggerType: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "progress") val progress: Int? = null,
    @Json(name = "total_findings") val totalFindings: Int? = null,
    @Json(name = "overall_health_score") val overallHealthScore: Float? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ScanStatsData(
    @Json(name = "total") val total: Int,
    @Json(name = "by_status") val byStatus: Map<String, Int>,
    @Json(name = "avg_health_score") val avgHealthScore: Float? = null
)

@JsonClass(generateAdapter = true)
data class ScanDetailData(
    @Json(name = "id") val id: Int,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "trigger_type") val triggerType: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "progress") val progress: Int? = null,
    @Json(name = "total_findings") val totalFindings: Int? = null,
    @Json(name = "overall_health_score") val overallHealthScore: Float? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null,
    @Json(name = "triggered_by") val triggeredBy: String? = null,
    @Json(name = "commit_sha") val commitSha: String? = null,
    @Json(name = "current_phase") val currentPhase: String? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "file_count") val fileCount: Int? = null,
    @Json(name = "code_file_count") val codeFileCount: Int? = null,
    @Json(name = "critical_count") val criticalCount: Int? = null,
    @Json(name = "major_count") val majorCount: Int? = null,
    @Json(name = "minor_count") val minorCount: Int? = null,
    @Json(name = "suggestion_count") val suggestionCount: Int? = null,
    @Json(name = "report_issue_number") val reportIssueNumber: Int? = null,
    @Json(name = "report_issue_url") val reportIssueUrl: String? = null,
    @Json(name = "started_at") val startedAt: String? = null,
    @Json(name = "findings") val findings: List<ScanFindingData>? = null
)

@JsonClass(generateAdapter = true)
data class ScanFindingData(
    @Json(name = "id") val id: Int,
    @Json(name = "file_path") val filePath: String? = null,
    @Json(name = "line_start") val lineStart: Int? = null,
    @Json(name = "line_end") val lineEnd: Int? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "suggestion") val suggestion: String? = null,
    @Json(name = "confidence") val confidence: Float? = null
)

@JsonClass(generateAdapter = true)
data class TriggerResultData(
    @Json(name = "triggered") val triggered: List<TriggeredScanData>,
    @Json(name = "count") val count: Int
)

@JsonClass(generateAdapter = true)
data class TriggeredScanData(
    @Json(name = "repo") val repo: String? = null,
    @Json(name = "scan_id") val scanId: Int? = null
)
