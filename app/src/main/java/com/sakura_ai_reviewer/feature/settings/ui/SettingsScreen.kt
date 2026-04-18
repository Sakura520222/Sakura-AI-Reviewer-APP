package com.sakura_ai_reviewer.feature.settings.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (val settings = uiState.settings) {
                is ApiResult.Success -> {
                    SettingsSection(
                        theme = settings.data.theme,
                        itemsPerPage = settings.data.itemsPerPage,
                        isSaving = uiState.isSaving,
                        saveResult = uiState.saveResult,
                        onThemeChange = { viewModel.updateSettings(theme = it) },
                        onPerPageChange = { viewModel.updateSettings(itemsPerPage = it) }
                    )
                }
                is ApiResult.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(36.dp))
                    }
                }
                is ApiResult.Error -> {
                    Text("Failed to load settings: ${settings.message}", color = MaterialTheme.colorScheme.error)
                }
                is ApiResult.Cached -> {
                    SettingsSection(
                        theme = settings.data.theme,
                        itemsPerPage = settings.data.itemsPerPage,
                        isSaving = uiState.isSaving,
                        saveResult = uiState.saveResult,
                        onThemeChange = { viewModel.updateSettings(theme = it) },
                        onPerPageChange = { viewModel.updateSettings(itemsPerPage = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // About section
            when (val about = uiState.about) {
                is ApiResult.Success -> {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow("Version", about.data.version)
                            InfoRow("Build Date", about.data.buildDate)
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Logout
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    theme: String,
    itemsPerPage: Int,
    isSaving: Boolean,
    saveResult: String?,
    onThemeChange: (String) -> Unit,
    onPerPageChange: (Int) -> Unit
) {
    Text("Appearance", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Theme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("light", "dark", "system").forEach { option ->
            FilterChip(
                selected = theme == option,
                onClick = { onThemeChange(option) },
                label = { Text(option.replaceFirstChar { it.uppercase() }) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Items Per Page", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(10, 20, 50, 100).forEach { count ->
            FilterChip(
                selected = itemsPerPage == count,
                onClick = { onPerPageChange(count) },
                label = { Text(count.toString()) }
            )
        }
    }

    if (isSaving) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Saving...", style = MaterialTheme.typography.bodySmall)
        }
    }

    saveResult?.let {
        Spacer(modifier = Modifier.height(4.dp))
        Text(it, style = MaterialTheme.typography.bodySmall, color = if (it.contains("saved", ignoreCase = true)) Primary else MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
