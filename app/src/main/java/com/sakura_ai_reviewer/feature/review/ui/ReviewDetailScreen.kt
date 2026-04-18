package com.sakura_ai_reviewer.feature.review.ui

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.feature.review.data.ReviewCommentData
import com.sakura_ai_reviewer.feature.review.data.ReviewDetailData
import com.sakura_ai_reviewer.feature.review.data.ReviewFileData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailScreen(
    onBack: () -> Unit = {},
    viewModel: ReviewDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Review Detail") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when (val review = uiState.review) {
            is ApiResult.Success -> {
                ReviewDetailContent(
                    review = review.data,
                    filesState = uiState.files,
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
                        Text("Failed to load review", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(review.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.retry() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                ReviewDetailContent(
                    review = review.data,
                    filesState = uiState.files,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

@Composable
private fun ReviewDetailContent(
    review: ReviewDetailData,
    filesState: ApiResult<List<ReviewFileData>>,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title and status
        Text(
            text = review.title ?: "PR #${review.prId}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(review.status)
            DecisionChip(review.decision)
            review.overallScore?.let {
                Text("Score: $it", style = MaterialTheme.typography.labelMedium, color = Primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        InfoRow("Repository", listOfNotNull(review.repoOwner, review.repoName).joinToString("/"))
        InfoRow("Author", review.author ?: "-")
        InfoRow("Branch", review.branch ?: "-")
        InfoRow("Strategy", review.strategy ?: "-")
        InfoRow("Files", "${review.fileCount ?: 0} files, ${review.codeFileCount ?: 0} code files, ${review.lineCount ?: 0} lines")

        Spacer(modifier = Modifier.height(8.dp))

        // Summary
        if (!review.reviewSummary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = review.reviewSummary,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (!review.decisionReason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Decision Reason", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(review.decisionReason, style = MaterialTheme.typography.bodyMedium)
        }

        // Files section
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Files", style = MaterialTheme.typography.titleMedium)

        when (filesState) {
            is ApiResult.Success -> {
                filesState.data.forEach { file ->
                    Spacer(modifier = Modifier.height(4.dp))
                    FileItem(file)
                }
            }
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                }
            }
            else -> {}
        }

        // Comments section
        if (!review.comments.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Comments (${review.comments.size})", style = MaterialTheme.typography.titleMedium)

            review.comments.forEach { comment ->
                Spacer(modifier = Modifier.height(4.dp))
                CommentItem(comment)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FileItem(file: ReviewFileData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = file.filePath,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val counts = file.severityCounts
                if (counts.critical > 0) Text("Critical: ${counts.critical}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                if (counts.major > 0) Text("Major: ${counts.major}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                if (counts.minor > 0) Text("Minor: ${counts.minor}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (counts.suggestion > 0) Text("Sugg: ${counts.suggestion}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CommentItem(comment: ReviewCommentData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                comment.filePath?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                comment.severity?.let {
                    val color = when (it) {
                        "critical" -> MaterialTheme.colorScheme.error
                        "major" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(text = it.uppercase(), style = MaterialTheme.typography.labelSmall, color = color)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content ?: "",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatusChip(status: String?) {
    val color = when (status) {
        "completed" -> MaterialTheme.colorScheme.primary
        "reviewing" -> MaterialTheme.colorScheme.tertiary
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

@Composable
private fun DecisionChip(decision: String?) {
    val (label, color) = when (decision) {
        "approve" -> "Approve" to MaterialTheme.colorScheme.primary
        "request_changes" -> "Changes Requested" to MaterialTheme.colorScheme.error
        "comment" -> "Comment" to MaterialTheme.colorScheme.tertiary
        "skip" -> "Skip" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> "Pending" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
}
