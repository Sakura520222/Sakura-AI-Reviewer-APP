package com.sakura_ai_reviewer.feature.scan.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.components.InfoRow
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.core.ui.theme.SeverityCritical
import com.sakura_ai_reviewer.core.ui.theme.SeverityMajor
import com.sakura_ai_reviewer.core.ui.theme.SeverityMinor
import com.sakura_ai_reviewer.core.ui.theme.SeveritySuggestion
import com.sakura_ai_reviewer.feature.scan.data.ScanDetailData
import com.sakura_ai_reviewer.feature.scan.data.ScanFindingData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    onBack: () -> Unit = {},
    viewModel: ScanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val scan = uiState.scan) {
            is ApiResult.Success -> {
                ScanDetailContent(
                    scan = scan.data,
                    actionResult = uiState.actionResult,
                    onRetry = { viewModel.retryScan() },
                    onCancel = { viewModel.cancelScan() },
                    onClearResult = { viewModel.clearResult() }
                )
            }
            is ApiResult.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
                }
            }
            is ApiResult.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load scan", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            scan.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                ScanDetailContent(
                    scan = scan.data,
                    actionResult = uiState.actionResult,
                    onRetry = { viewModel.retryScan() },
                    onCancel = { viewModel.cancelScan() },
                    onClearResult = { viewModel.clearResult() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScanDetailContent(
    scan: ScanDetailData,
    actionResult: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onClearResult: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // === Scan Info Card ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Repo name
                Text(
                    text = scan.repoName ?: "Scan #${scan.id}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Status badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", style = MaterialTheme.typography.bodyMedium)
                    ScanStatusChip(scan.status)
                }

                // Progress bar (only if in-progress)
                val inProgressStatuses = setOf("pending", "indexing", "analyzing", "reporting")
                if (scan.status in inProgressStatuses && scan.progress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (scan.progress ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${scan.progress}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Health score
                scan.overallHealthScore?.let { score ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Health Score: ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format("%.1f", score),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(" / 100", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Trigger info
                InfoRow("Trigger Type", scan.triggerType?.replaceFirstChar { it.uppercase() } ?: "-")
                InfoRow("Triggered By", scan.triggeredBy ?: "-")

                // Commit SHA (truncated)
                scan.commitSha?.let { sha ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(
                            text = "Commit: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = sha.take(8),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Current phase
                scan.currentPhase?.let { phase ->
                    InfoRow("Current Phase", phase.replaceFirstChar { it.uppercase() })
                }

                Spacer(modifier = Modifier.height(4.dp))

                // File counts
                InfoRow("Files", "${scan.codeFileCount ?: 0} code / ${scan.fileCount ?: 0} total")

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                // Timestamps
                InfoRow("Created", scan.createdAt ?: "-")
                InfoRow("Started", scan.startedAt ?: "-")
                InfoRow("Completed", scan.completedAt ?: "-")

                // Error message if failed
                scan.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === Severity Summary Card ===
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Severity Summary", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SeverityItem("Critical", scan.criticalCount ?: 0, SeverityCritical)
                    SeverityItem("Major", scan.majorCount ?: 0, SeverityMajor)
                    SeverityItem("Minor", scan.minorCount ?: 0, SeverityMinor)
                    SeverityItem("Suggestion", scan.suggestionCount ?: 0, SeveritySuggestion)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === Findings Section ===
        val findings = scan.findings.orEmpty()
        Text(
            text = "Findings (${findings.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (findings.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No findings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            findings.forEach { finding ->
                FindingCard(finding = finding)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // === Report Link ===
        scan.reportIssueUrl?.let { url ->
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Report Issue")
            }
        }

        // === Action Buttons ===
        Spacer(modifier = Modifier.height(16.dp))

        if (scan.status == "failed") {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry Scan")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        val cancellableStatuses = setOf("pending", "indexing", "analyzing", "reporting")
        if (scan.status in cancellableStatuses) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel Scan")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Action result message
        actionResult?.let { result ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onClearResult) { Text("Dismiss") }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ScanStatusChip(status: String?) {
    val color = when (status) {
        "completed" -> MaterialTheme.colorScheme.primary
        "indexing", "analyzing", "reporting" -> MaterialTheme.colorScheme.tertiary
        "pending" -> MaterialTheme.colorScheme.secondary
        "failed" -> MaterialTheme.colorScheme.error
        "cancelled" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
        Text(
            text = status?.replaceFirstChar { it.uppercase() } ?: "Unknown",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SeverityItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FindingCard(finding: ScanFindingData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            // File path (monospace)
            finding.filePath?.let { path ->
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Line range
            if (finding.lineStart != null && finding.lineEnd != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Lines ${finding.lineStart} - ${finding.lineEnd}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Severity badge + category row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                finding.severity?.let { severity ->
                    val (chipColor, chipText) = when (severity) {
                        "critical" -> SeverityCritical to "Critical"
                        "major" -> SeverityMajor to "Major"
                        "minor" -> SeverityMinor to "Minor"
                        "suggestion" -> SeveritySuggestion to "Suggestion"
                        else -> MaterialTheme.colorScheme.onSurfaceVariant to severity.replaceFirstChar { it.uppercase() }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = chipColor.copy(alpha = 0.15f))) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelSmall,
                            color = chipColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                finding.category?.let { category ->
                    Text(
                        text = category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title (bold)
            finding.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Description
            finding.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Suggestion (expandable)
            finding.suggestion?.let { suggestion ->
                Spacer(modifier = Modifier.height(6.dp))
                var expanded by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) "Hide suggestion" else "Show suggestion",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (expanded) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Confidence
            finding.confidence?.let { conf ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Confidence: ${String.format("%.0f%%", conf * 100)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
