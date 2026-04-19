package com.sakura_ai_reviewer.core.sse

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the SSE (Server-Sent Events) connection for real-time event streaming.
 *
 * Uses OkHttp's [EventSource] to maintain a persistent connection to the server's
 * `/api/v1/events` endpoint. Events are parsed into typed [SseEvent] instances and
 * emitted through a [SharedFlow] that ViewModels can collect.
 *
 * Features:
 * - Token-based authentication via query parameter (required for SSE since headers
 *   are not easily set on EventSource requests)
 * - Exponential backoff reconnection on failure (1s -> 2s -> 4s -> ... -> 30s max)
 * - Automatic keepalive comment handling (`: keepalive` comments are ignored)
 * - Thread-safe reconnect state with `@Volatile` fields
 */
@Singleton
class SseManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) {

    companion object {
        private const val TAG = "SseManager"
        private const val SSE_PATH = "events"
        private const val INITIAL_BACKOFF_MS = 1_000L
        private const val MAX_BACKOFF_MS = 30_000L
        private const val EVENTS_BUFFER_SIZE = 64
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<SseEvent>(
        extraBufferCapacity = EVENTS_BUFFER_SIZE
    )

    /** Flow of parsed SSE events for ViewModels to collect. */
    val events: SharedFlow<SseEvent> = _events.asSharedFlow()

    /** The current active EventSource connection, or null when disconnected. */
    @Volatile
    private var currentEventSource: EventSource? = null

    /** Whether the manager is intentionally disconnected and should not reconnect. */
    @Volatile
    private var intentionallyDisconnected = false

    /** Current exponential backoff delay in milliseconds. */
    @Volatile
    private var currentBackoffMs = INITIAL_BACKOFF_MS

    /** The last base URL used for connection, stored for reconnect. */
    @Volatile
    private var connectedBaseUrl: String? = null

    /** The last token used for connection, stored for reconnect. */
    @Volatile
    private var connectedToken: String? = null

    /**
     * Opens an SSE connection to `{baseUrl}events?token={token}`.
     *
     * If a connection is already active it will be closed first.
     *
     * @param baseUrl The API base URL (e.g. `https://server.com/api/v1/`).
     * @param token   The Bearer JWT token for authentication.
     */
    fun connect(baseUrl: String, token: String) {
        Log.d(TAG, "connect() called with baseUrl=$baseUrl")
        intentionallyDisconnected = false
        connectedBaseUrl = baseUrl
        connectedToken = token
        currentBackoffMs = INITIAL_BACKOFF_MS

        // Close any existing connection before opening a new one
        disconnectInternal()

        openConnection(baseUrl, token)
    }

    /**
     * Closes the SSE connection and stops automatic reconnection.
     */
    fun disconnect() {
        Log.d(TAG, "disconnect() called")
        intentionallyDisconnected = true
        disconnectInternal()
        connectedBaseUrl = null
        connectedToken = null
    }

    /**
     * Returns true if there is an active SSE connection.
     */
    fun isConnected(): Boolean = currentEventSource != null

    // ── Internal helpers ─────────────────────────────────────────────

    private fun disconnectInternal() {
        currentEventSource?.cancel()
        currentEventSource = null
    }

    private fun openConnection(baseUrl: String, token: String) {
        val sseUrl = buildSseUrl(baseUrl, token)
        Log.d(TAG, "Opening SSE connection to: ${sseUrl.substringBefore("token=")}token=***")

        val request = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val factory = EventSources.createFactory(okHttpClient)

        currentEventSource = factory.newEventSource(request, object : EventSourceListener() {

            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                Log.d(TAG, "SSE connection opened — response code: ${response.code}")
                // Reset backoff on successful connection
                currentBackoffMs = INITIAL_BACKOFF_MS
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                // Ignore keepalive comments — type will be null for comments
                if (type == null || type == "keepalive") {
                    Log.v(TAG, "Ignored keepalive/comment event")
                    return
                }

                Log.d(TAG, "SSE event received — type=$type, data=${data.take(200)}")
                val sseEvent = parseEvent(type, data)
                scope.launch {
                    _events.emit(sseEvent)
                }
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d(TAG, "SSE connection closed by server")
                currentEventSource = null
                scheduleReconnect()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                val statusCode = response?.code
                Log.w(TAG, "SSE connection failure — status=$statusCode, error=${t?.message}")
                currentEventSource = null

                // Do not reconnect on client errors (4xx) except 429 (rate limit)
                if (statusCode != null && statusCode in 400..499 && statusCode != 429) {
                    Log.w(TAG, "Client error $statusCode — not reconnecting")
                    return
                }

                scheduleReconnect()
            }
        })
    }

    /**
     * Builds the SSE URL as `{baseUrl}events?token={token}`.
     * Handles both trailing-slash and non-trailing-slash base URLs.
     */
    private fun buildSseUrl(baseUrl: String, token: String): String {
        val base = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return "${base}${SSE_PATH}?token=$token"
    }

    /**
     * Schedules a reconnect attempt with exponential backoff.
     * Does nothing if the manager is intentionally disconnected.
     */
    private fun scheduleReconnect() {
        if (intentionallyDisconnected) {
            Log.d(TAG, "Not reconnecting — intentionally disconnected")
            return
        }

        val base = connectedBaseUrl
        val token = connectedToken
        if (base == null || token == null) {
            Log.w(TAG, "Not reconnecting — no stored base URL or token")
            return
        }

        val delayMs = currentBackoffMs
        Log.d(TAG, "Scheduling reconnect in ${delayMs}ms")

        // Increase backoff for next attempt (exponential, capped at max)
        currentBackoffMs = (currentBackoffMs * 2).coerceAtMost(MAX_BACKOFF_MS)

        scope.launch {
            delay(delayMs)

            if (!intentionallyDisconnected) {
                Log.d(TAG, "Reconnecting now...")
                openConnection(base, token)
            }
        }
    }

    /**
     * Parses an SSE event by its `type` field and JSON `data` into a typed [SseEvent].
     *
     * Uses Moshi to deserialize the JSON data into the appropriate event fields.
     * Falls back to [SseEvent.Unknown] for unrecognized event types.
     */
    private fun parseEvent(type: String, data: String): SseEvent {
        return when (type) {
            "review_started" -> parseReviewStarted(data)
            "review_completed" -> parseReviewCompleted(data)
            "scan_progress" -> parseScanProgress(data)
            "scan_completed" -> parseScanCompleted(data)
            "issue_analyzed" -> parseIssueAnalyzed(data)
            "queue_updated" -> parseQueueUpdated(data)
            else -> {
                Log.d(TAG, "Unknown SSE event type: $type")
                SseEvent.Unknown(eventType = type, rawData = data)
            }
        }
    }

    // ── Per-type JSON parsing helpers ────────────────────────────────

    private fun parseReviewStarted(data: String): SseEvent.ReviewStarted {
        return try {
            val map = parseJsonToMap(data)
            SseEvent.ReviewStarted(
                reviewId = map["review_id"] as? Int,
                prId = (map["pr_id"] as? Number)?.toLong(),
                repoName = map["repo_name"] as? String,
                repoOwner = map["repo_owner"] as? String,
                author = map["author"] as? String,
                title = map["title"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse review_started event", e)
            SseEvent.ReviewStarted()
        }
    }

    private fun parseReviewCompleted(data: String): SseEvent.ReviewCompleted {
        return try {
            val map = parseJsonToMap(data)
            SseEvent.ReviewCompleted(
                reviewId = map["review_id"] as? Int,
                prId = (map["pr_id"] as? Number)?.toLong(),
                repoName = map["repo_name"] as? String,
                repoOwner = map["repo_owner"] as? String,
                author = map["author"] as? String,
                title = map["title"] as? String,
                status = map["status"] as? String,
                decision = map["decision"] as? String,
                overallScore = map["overall_score"] as? Int,
                errorMessage = map["error_message"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse review_completed event", e)
            SseEvent.ReviewCompleted()
        }
    }

    private fun parseScanProgress(data: String): SseEvent.ScanProgress {
        return try {
            val map = parseJsonToMap(data)
            SseEvent.ScanProgress(
                scanId = map["scan_id"] as? Int,
                repoName = map["repo_name"] as? String,
                progress = map["progress"] as? Int,
                currentPhase = map["current_phase"] as? String,
                status = map["status"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse scan_progress event", e)
            SseEvent.ScanProgress()
        }
    }

    private fun parseScanCompleted(data: String): SseEvent.ScanCompleted {
        return try {
            val map = parseJsonToMap(data)
            SseEvent.ScanCompleted(
                scanId = map["scan_id"] as? Int,
                repoName = map["repo_name"] as? String,
                repoOwner = map["repo_owner"] as? String,
                status = map["status"] as? String,
                totalFindings = map["total_findings"] as? Int,
                overallHealthScore = (map["overall_health_score"] as? Number)?.toFloat(),
                errorMessage = map["error_message"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse scan_completed event", e)
            SseEvent.ScanCompleted()
        }
    }

    private fun parseIssueAnalyzed(data: String): SseEvent.IssueAnalyzed {
        return try {
            val map = parseJsonToMap(data)
            SseEvent.IssueAnalyzed(
                issueId = map["issue_id"] as? Int,
                issueNumber = map["issue_number"] as? Int,
                repoName = map["repo_name"] as? String,
                repoOwner = map["repo_owner"] as? String,
                title = map["title"] as? String,
                category = map["category"] as? String,
                priority = map["priority"] as? String,
                status = map["status"] as? String,
                errorMessage = map["error_message"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse issue_analyzed event", e)
            SseEvent.IssueAnalyzed()
        }
    }

    private fun parseQueueUpdated(data: String): SseEvent.QueueUpdated {
        return try {
            val map = parseJsonToMap(data)
            SseEvent.QueueUpdated(
                itemId = map["item_id"] as? Int,
                action = map["action"] as? String,
                status = map["status"] as? String,
                repoName = map["repo_name"] as? String,
                prId = (map["pr_id"] as? Number)?.toLong(),
                retryCount = map["retry_count"] as? Int,
                errorMessage = map["error_message"] as? String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse queue_updated event", e)
            SseEvent.QueueUpdated()
        }
    }

    /**
     * Parses a JSON string into a Map using Moshi.
     * Returns an empty map if parsing fails.
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseJsonToMap(json: String): Map<String, Any?> {
        return try {
            val type = Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            val adapter = moshi.adapter<Map<String, Any?>>(type)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON: ${json.take(100)}", e)
            emptyMap()
        }
    }
}
