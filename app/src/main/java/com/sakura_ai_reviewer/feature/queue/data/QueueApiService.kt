package com.sakura_ai_reviewer.feature.queue.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QueueApiService {

    @GET("queue/stats")
    suspend fun getStats(): ApiResponse<QueueStatsData>

    @GET("queue/items")
    suspend fun getItems(
        @Query("search") search: String = "",
        @Query("repo") repo: String = "",
        @Query("status") status: String = "",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): ApiResponse<QueueListData>

    @GET("queue/items/{itemId}")
    suspend fun getItemDetail(
        @Path("itemId") itemId: Int
    ): ApiResponse<QueueItemData>

    @POST("queue/items/{itemId}/retry")
    suspend fun retryItem(
        @Path("itemId") itemId: Int
    ): ApiResponse<EmptyData>

    @DELETE("queue/items/{itemId}")
    suspend fun deleteItem(
        @Path("itemId") itemId: Int
    ): ApiResponse<EmptyData>

    @POST("queue/purge")
    suspend fun purge(
        @Query("status") status: String = "completed"
    ): ApiResponse<PurgeResultData>
}

@JsonClass(generateAdapter = true)
data class QueueStatsData(
    @Json(name = "pending") val pending: Int,
    @Json(name = "processing") val processing: Int,
    @Json(name = "completed") val completed: Int,
    @Json(name = "failed") val failed: Int,
    @Json(name = "total") val total: Int
)

@JsonClass(generateAdapter = true)
data class QueueListData(
    @Json(name = "items") val items: List<QueueItemData>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class QueueItemData(
    @Json(name = "id") val id: Int,
    @Json(name = "pr_id") val prId: Long? = null,
    @Json(name = "repo_name") val repoName: String? = null,
    @Json(name = "action") val action: String? = null,
    @Json(name = "priority") val priority: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "retry_count") val retryCount: Int? = null,
    @Json(name = "max_retries") val maxRetries: Int? = null,
    @Json(name = "error_message") val errorMessage: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class PurgeResultData(
    @Json(name = "deleted") val deleted: Int
)
