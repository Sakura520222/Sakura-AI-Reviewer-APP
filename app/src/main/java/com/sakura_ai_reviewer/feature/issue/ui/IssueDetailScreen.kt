package com.sakura_ai_reviewer.feature.issue.ui

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.feature.issue.data.IssueDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    onBack: () -> Unit = {},
    viewModel: IssueDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Issue Detail") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when (val issue = uiState.issue) {
            is ApiResult.Success -> {
                IssueDetailContent(
                    issue = issue.data,
                    isReanalyzing = uiState.isReanalyzing,
                    reanalyzeResult = uiState.reanalyzeResult,
                    onReanalyze = { viewModel.reanalyze() },
                    onRetry = { viewModel.retry() }
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
                        Text("Failed to load issue", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(issue.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.retry() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                IssueDetailContent(
                    issue = issue.data,
                    isReanalyzing = uiState.isReanalyzing,
                    reanalyzeResult = uiState.reanalyzeResult,
                    onReanalyze = { viewModel.reanalyze() },
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

@Composable
private fun IssueDetailContent(
    issue: IssueDetailData,
    isReanalyzing: Boolean,
    reanalyzeResult: String?,
    onReanalyze: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = issue.title ?: "Issue #${issue.issueNumber}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IssueStatusChip(issue.status)
            issue.category?.let {
                Text(it.replaceFirstChar { c -> c.uppercase() }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
            issue.priority?.let { pri ->
                val color = when (pri) {
                    "high" -> MaterialTheme.colorScheme.error
                    "medium" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text("${pri.replaceFirstChar { c -> c.uppercase() }} Priority", style = MaterialTheme.typography.labelMedium, color = color)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        InfoRow("Repository", listOfNotNull(issue.repoOwner, issue.repoName).joinToString("/"))
        InfoRow("Author", issue.author ?: "-")
        InfoRow("Feasibility", issue.feasibility ?: "-")
        issue.appliedLabelNames?.let { InfoRow("Labels", it) }

        // Summary
        if (!issue.summary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = issue.summary,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Analysis detail
        if (!issue.analysisDetail.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Analysis", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = issue.analysisDetail,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Suggestions
        if (!issue.suggestedLabels.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Suggested Labels", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                issue.suggestedLabels.forEach { label ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (!issue.suggestedAssignees.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Suggested Assignees", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(issue.suggestedAssignees.joinToString(", "), style = MaterialTheme.typography.bodySmall)
        }

        // Reanalyze button
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onReanalyze,
            enabled = !isReanalyzing,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (isReanalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isReanalyzing) "Analyzing..." else "Reanalyze")
        }

        reanalyzeResult?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun IssueStatusChip(status: String?) {
    val color = when (status) {
        "completed" -> MaterialTheme.colorScheme.primary
        "pending" -> MaterialTheme.colorScheme.secondary
        "failed" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = status?.replaceFirstChar { it.uppercase() } ?: "Unknown",
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}
