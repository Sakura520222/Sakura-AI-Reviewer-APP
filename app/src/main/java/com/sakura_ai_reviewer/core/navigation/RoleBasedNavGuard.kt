package com.sakura_ai_reviewer.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sakura_ai_reviewer.core.auth.AuthState
import com.sakura_ai_reviewer.core.auth.Role
import com.sakura_ai_reviewer.feature.auth.ui.AuthViewModel

@Composable
fun RoleBasedNavGuard(
    navController: NavHostController,
    requiredRole: Role = Role.USER,
    content: @Composable () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val authState by viewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Unauthenticated -> {
            navController.navigate(NavRoute.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
        is AuthState.SetupRequired -> {
            navController.navigate(NavRoute.SetupWizard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
        is AuthState.Authenticated -> {
            if (state.role.canAccess(requiredRole)) {
                content()
            } else {
                navController.navigate(NavRoute.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}
