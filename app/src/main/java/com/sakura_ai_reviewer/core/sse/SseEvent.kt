package com.sakura_ai_reviewer.core.sse

/**
 * Sealed class representing all SSE event types the server can send.
 *
 * Known event types sent by the server via `event: <type>`:
 * - `review_completed` — review done
 * - `review_started`   — review started
 * - `scan_progress`    — scan progress update
 * - `scan_completed`   — scan done
 * - `issue_analyzed`   — issue analysis done
 * - `queue_updated`    — queue item status change
 *
 * An [Unknown] fallback exists for any event type not recognized above.
 */
sealed class SseEvent {

    /** The SSE event type string (the value after `event:`). */
    abstract val eventType: String

    // ── Review events ────────────────────────────────────────────────

    data class ReviewStarted(
        override val eventType: String = "review_started",
        val reviewId: Int? = null,
        val prId: Long? = null,
        val repoName: String? = null,
        val repoOwner: String? = null,
        val author: String? = null,
        val title: String? = null
    ) : SseEvent()

    data class ReviewCompleted(
        override val eventType: String = "review_completed",
        val reviewId: Int? = null,
        val prId: Long? = null,
        val repoName: String? = null,
        val repoOwner: String? = null,
        val author: String? = null,
        val title: String? = null,
        val status: String? = null,
        val decision: String? = null,
        val overallScore: Int? = null,
        val errorMessage: String? = null
    ) : SseEvent()

    // ── Scan events ──────────────────────────────────────────────────

    data class ScanProgress(
        override val eventType: String = "scan_progress",
        val scanId: Int? = null,
        val repoName: String? = null,
        val progress: Int? = null,
        val currentPhase: String? = null,
        val status: String? = null
    ) : SseEvent()

    data class ScanCompleted(
        override val eventType: String = "scan_completed",
        val scanId: Int? = null,
        val repoName: String? = null,
        val repoOwner: String? = null,
        val status: String? = null,
        val totalFindings: Int? = null,
        val overallHealthScore: Float? = null,
        val errorMessage: String? = null
    ) : SseEvent()

    // ── Issue events ─────────────────────────────────────────────────

    data class IssueAnalyzed(
        override val eventType: String = "issue_analyzed",
        val issueId: Int? = null,
        val issueNumber: Int? = null,
        val repoName: String? = null,
        val repoOwner: String? = null,
        val title: String? = null,
        val category: String? = null,
        val priority: String? = null,
        val status: String? = null,
        val errorMessage: String? = null
    ) : SseEvent()

    // ── Queue events ─────────────────────────────────────────────────

    data class QueueUpdated(
        override val eventType: String = "queue_updated",
        val itemId: Int? = null,
        val action: String? = null,
        val status: String? = null,
        val repoName: String? = null,
        val prId: Long? = null,
        val retryCount: Int? = null,
        val errorMessage: String? = null
    ) : SseEvent()

    // ── Fallback ─────────────────────────────────────────────────────

    /**
     * Represents an SSE event whose type was not recognized.
     * Carries the raw event type name and the unparsed JSON data.
     */
    data class Unknown(
        override val eventType: String,
        val rawData: String? = null
    ) : SseEvent()

    companion object {
        /** All known event-type strings the server uses. */
        val KNOWN_TYPES: Set<String> = setOf(
            "review_completed",
            "review_started",
            "scan_progress",
            "scan_completed",
            "issue_analyzed",
            "queue_updated"
        )
    }
}
