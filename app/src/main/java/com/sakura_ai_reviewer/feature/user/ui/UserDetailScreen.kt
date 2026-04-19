package com.sakura_ai_reviewer.feature.user.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.components.InfoRow
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.feature.user.data.UserDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    onBack: () -> Unit = {},
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("User Detail") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when (val user = uiState.user) {
            is ApiResult.Success -> {
                UserDetailContent(
                    user = user.data,
                    isUpdating = uiState.isUpdating,
                    updateResult = uiState.updateResult,
                    onUpdateRole = { viewModel.updateRole(it) },
                    onToggleActive = { viewModel.toggleActive() },
                    onResetQuota = { viewModel.resetQuota() }
                )
            }
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
                }
            }
            is ApiResult.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load user", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(user.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.retry() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                UserDetailContent(
                    user = user.data,
                    isUpdating = uiState.isUpdating,
                    updateResult = uiState.updateResult,
                    onUpdateRole = { viewModel.updateRole(it) },
                    onToggleActive = { viewModel.toggleActive() },
                    onResetQuota = { viewModel.resetQuota() }
                )
            }
        }
    }
}

@Composable
private fun UserDetailContent(
    user: UserDetailData,
    isUpdating: Boolean,
    updateResult: String?,
    onUpdateRole: (String) -> Unit,
    onToggleActive: () -> Unit,
    onResetQuota: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SelectionContainer {
            Column {
                Text(
                    text = user.githubUsername ?: "User #${user.id}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        user.role?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                        style = MaterialTheme.typography.labelMedium,
                        color = when (user.role) {
                            "super_admin" -> MaterialTheme.colorScheme.error
                            "admin" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = if (user.isActive == true) "Active" else "Disabled",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (user.isActive == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("ID", user.id.toString())
                InfoRow("Telegram ID", user.telegramId?.toString() ?: "-")
                InfoRow("Created", user.createdAt ?: "-")

                // PR Quota
                Spacer(modifier = Modifier.height(12.dp))
                Text("PR Quota", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        QuotaRow("Daily", user.dailyUsed ?: 0, user.dailyQuota ?: 0)
                        QuotaRow("Weekly", user.weeklyUsed ?: 0, user.weeklyQuota ?: 0)
                        QuotaRow("Monthly", user.monthlyUsed ?: 0, user.monthlyQuota ?: 0)
                    }
                }

                // Issue Quota
                Spacer(modifier = Modifier.height(8.dp))
                Text("Issue Quota", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        QuotaRow("Daily", user.issueDailyUsed ?: 0, user.issueDailyQuota ?: 0)
                        QuotaRow("Weekly", user.issueWeeklyUsed ?: 0, user.issueWeeklyQuota ?: 0)
                        QuotaRow("Monthly", user.issueMonthlyUsed ?: 0, user.issueMonthlyQuota ?: 0)
                    }
                }
            }
        }

        // Actions (outside SelectionContainer so buttons are clickable)
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Text("Role", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("user", "admin", "super_admin").forEach { role ->
                OutlinedButton(
                    onClick = { onUpdateRole(role) },
                    enabled = !isUpdating && role != user.role,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (role == user.role) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(role.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onToggleActive,
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.isActive == true) MaterialTheme.colorScheme.error else Primary
                )
            ) {
                Text(if (user.isActive == true) "Disable" else "Enable")
            }
            OutlinedButton(
                onClick = onResetQuota,
                enabled = !isUpdating
            ) {
                Text("Reset Quota")
            }
        }

        updateResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuotaRow(period: String, used: Int, quota: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$period:", style = MaterialTheme.typography.bodySmall)
        Text("$used / $quota", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
