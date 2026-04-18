package com.sakura_ai_reviewer.feature.repo.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RepoApiService {

    @GET("repos")
    suspend fun getRepos(): ApiResponse<List<RepoItemData>>

    @POST("repos/{repoName}/index-docs")
    suspend fun indexDocs(
        @Path("repoName") repoName: String
    ): ApiResponse<EmptyData>

    @POST("repos/{repoName}/index-code")
    suspend fun indexCode(
        @Path("repoName") repoName: String
    ): ApiResponse<EmptyData>

    @POST("repos/{repoName}/index-issues")
    suspend fun indexIssues(
        @Path("repoName") repoName: String
    ): ApiResponse<EmptyData>

    @POST("repos/{repoName}/scan")
    suspend fun scanRepo(
        @Path("repoName") repoName: String
    ): ApiResponse<EmptyData>
}

@JsonClass(generateAdapter = true)
data class RepoItemData(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "repo_owner") val repoOwner: String? = null,
    @Json(name = "review_count") val reviewCount: Int? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "created_at") val createdAt: String? = null
)
