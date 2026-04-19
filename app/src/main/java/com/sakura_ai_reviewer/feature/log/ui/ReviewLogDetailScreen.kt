package com.sakura_ai_reviewer.feature.log.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.sakura_ai_reviewer.core.ui.components.InfoRow
import com.sakura_ai_reviewer.core.ui.components.MarkdownCard
import com.sakura_ai_reviewer.core.ui.components.MarkdownText
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.feature.log.data.ReviewLogCommentData
import com.sakura_ai_reviewer.feature.log.data.ReviewLogDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewLogDetailScreen(
    onBack: () -> Unit = {},
    viewModel: ReviewLogDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Review Log") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when (val log = uiState.log) {
            is ApiResult.Success -> {
                ReviewLogDetailContent(log = log.data)
            }
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
                }
            }
            is ApiResult.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load log", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.retry() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                ReviewLogDetailContent(log = log.data)
            }
        }
    }
}

@Composable
private fun ReviewLogDetailContent(log: ReviewLogDetailData) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = log.title ?: "Review #${log.id}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                log.status?.let { status ->
                    Text(
                        status.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (status) {
                            "completed" -> MaterialTheme.colorScheme.primary
                            "failed" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                log.decision?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            InfoRow("Repository", listOfNotNull(log.repoOwner, log.repoName).joinToString("/"))
            InfoRow("Author", log.author ?: "-")
            InfoRow("Score", log.overallScore?.toString() ?: "-")
            InfoRow("Strategy", log.strategy ?: "-")
            InfoRow("Tokens", "${log.promptTokens ?: 0} + ${log.completionTokens ?: 0}")
            InfoRow("Created", log.createdAt ?: "-")
            InfoRow("Completed", log.completedAt ?: "-")

            log.reviewSummary?.let { summary ->
                if (summary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    MarkdownCard(
                        markdown = summary,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            log.errorMessage?.let { error ->
                if (error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Error", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    MarkdownCard(
                        markdown = error,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    )
                }
            }

            if (!log.comments.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Comments (${log.comments.size})", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                log.comments.forEach { comment ->
                    ReviewLogCommentCard(comment)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReviewLogCommentCard(comment: ReviewLogCommentData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.filePath ?: "General",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                comment.severity?.let { severity ->
                    Text(
                        severity,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (severity) {
                            "critical" -> MaterialTheme.colorScheme.error
                            "major" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            comment.lineNumber?.let { line ->
                Text("Line $line", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            MarkdownCard(
                markdown = comment.content ?: "",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
