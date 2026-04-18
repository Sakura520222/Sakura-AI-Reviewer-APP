package com.sakura_ai_reviewer.core.navigation

sealed class NavRoute(val route: String) {
    // Auth (no role required)
    data object Login : NavRoute("login")
    data object SetupWizard : NavRoute("setup")

    // All authenticated users
    data object Dashboard : NavRoute("dashboard")
    data object ReviewList : NavRoute("reviews")
    data object ReviewDetail : NavRoute("reviews/{reviewId}") {
        fun create(reviewId: Int) = "reviews/$reviewId"
    }
    data object IssueList : NavRoute("issues")
    data object IssueDetail : NavRoute("issues/{issueId}") {
        fun create(issueId: Int) = "issues/$issueId"
    }
    data object ScanList : NavRoute("scans")
    data object ScanDetail : NavRoute("scans/{scanId}") {
        fun create(scanId: Int) = "scans/$scanId"
    }
    data object ReviewLogs : NavRoute("logs/reviews")
    data object ReviewLogDetail : NavRoute("logs/reviews/{reviewId}") {
        fun create(reviewId: Int) = "logs/reviews/$reviewId"
    }
    data object Settings : NavRoute("settings")

    // Admin only
    data object UserList : NavRoute("admin/users")
    data object UserDetail : NavRoute("admin/users/{userId}") {
        fun create(userId: Int) = "admin/users/$userId"
    }
    data object RepoList : NavRoute("admin/repos")
    data object ActionLogs : NavRoute("admin/logs/actions")
    data object QueueMonitor : NavRoute("admin/queue")

    // Super admin only
    data object Config : NavRoute("super/config")
}
