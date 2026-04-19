package com.sakura_ai_reviewer.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sakura_ai_reviewer.R
import com.sakura_ai_reviewer.core.auth.AuthState
import com.sakura_ai_reviewer.core.auth.Role
import com.sakura_ai_reviewer.feature.auth.ui.AuthViewModel
import com.sakura_ai_reviewer.feature.auth.ui.LoginScreen
import com.sakura_ai_reviewer.feature.dashboard.ui.DashboardScreen
import com.sakura_ai_reviewer.feature.issue.ui.IssueDetailScreen
import com.sakura_ai_reviewer.feature.issue.ui.IssueListScreen
import com.sakura_ai_reviewer.feature.log.ui.ActionLogListScreen
import com.sakura_ai_reviewer.feature.log.ui.ReviewLogDetailScreen
import com.sakura_ai_reviewer.feature.log.ui.ReviewLogListScreen
import com.sakura_ai_reviewer.feature.queue.ui.QueueScreen
import com.sakura_ai_reviewer.feature.repo.ui.RepoListScreen
import com.sakura_ai_reviewer.feature.review.ui.ReviewDetailScreen
import com.sakura_ai_reviewer.feature.review.ui.ReviewListScreen
import com.sakura_ai_reviewer.feature.settings.ui.SettingsScreen
import com.sakura_ai_reviewer.feature.user.ui.UserDetailScreen
import com.sakura_ai_reviewer.feature.user.ui.UserListScreen

data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val iconRes: Int,
    val requiredRole: Role
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // Navigate to login when user is logged out (e.g. 401 response)
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && currentRoute != NavRoute.Login.route) {
                navController.navigate(NavRoute.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(
            route = NavRoute.Dashboard.route,
            labelRes = R.string.nav_dashboard,
            iconRes = R.drawable.ic_dashboard,
            requiredRole = Role.USER
        ),
        BottomNavItem(
            route = NavRoute.ReviewList.route,
            labelRes = R.string.nav_reviews,
            iconRes = R.drawable.ic_reviews,
            requiredRole = Role.USER
        ),
        BottomNavItem(
            route = NavRoute.ScanList.route,
            labelRes = R.string.nav_scans,
            iconRes = R.drawable.ic_scans,
            requiredRole = Role.USER
        ),
        BottomNavItem(
            route = NavRoute.UserList.route,
            labelRes = R.string.nav_admin,
            iconRes = R.drawable.ic_admin,
            requiredRole = Role.ADMIN
        )
    )

    val currentAuthState = authState
    val showBottomBar = currentAuthState is AuthState.Authenticated

    val userRole = (currentAuthState as? AuthState.Authenticated)?.role ?: Role.USER
    val visibleNavItems = bottomNavItems.filter { userRole.canAccess(it.requiredRole) }

    Scaffold(
        bottomBar = {
            if (showBottomBar && visibleNavItems.isNotEmpty()) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    visibleNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    ImageVector.vectorResource(id = item.iconRes),
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = stringResource(id = item.labelRes)) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == item.route
                            } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = when (currentAuthState) {
                is AuthState.Authenticated -> NavRoute.Dashboard.route
                is AuthState.SetupRequired -> NavRoute.SetupWizard.route
                is AuthState.Unauthenticated -> NavRoute.Login.route
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoute.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoute.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoute.SetupWizard.route) {
                // TODO: SetupWizardScreen
            }
            composable(NavRoute.Dashboard.route) {
                DashboardScreen(
                    onNavigateToReviews = { navController.navigate(NavRoute.ReviewList.route) },
                    onNavigateToSettings = { navController.navigate(NavRoute.Settings.route) },
                    onNavigateToAddAccount = {
                        navController.navigate(NavRoute.Login.route)
                    },
                    onLogout = {
                        navController.navigate(NavRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoute.ReviewList.route) {
                ReviewListScreen(
                    onNavigateToDetail = { reviewId ->
                        navController.navigate(NavRoute.ReviewDetail.create(reviewId))
                    }
                )
            }
            composable(NavRoute.ReviewDetail.route) {
                ReviewDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.IssueList.route) {
                IssueListScreen(
                    onNavigateToDetail = { issueId ->
                        navController.navigate(NavRoute.IssueDetail.create(issueId))
                    }
                )
            }
            composable(NavRoute.IssueDetail.route) {
                IssueDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.ScanList.route) {
                // TODO: ScanListScreen
            }
            composable(NavRoute.ScanDetail.route) {
                // TODO: ScanDetailScreen
            }
            composable(NavRoute.ReviewLogs.route) {
                ReviewLogListScreen(
                    onNavigateToDetail = { reviewId ->
                        navController.navigate(NavRoute.ReviewLogDetail.create(reviewId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.ReviewLogDetail.route) {
                ReviewLogDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.UserList.route) {
                UserListScreen(
                    onNavigateToDetail = { userId ->
                        navController.navigate(NavRoute.UserDetail.create(userId))
                    }
                )
            }
            composable(NavRoute.UserDetail.route) {
                UserDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.RepoList.route) {
                RepoListScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.ActionLogs.route) {
                ActionLogListScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.QueueMonitor.route) {
                QueueScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.Config.route) {
                // TODO: ConfigScreen (Phase 5)
            }
        }
    }
}
