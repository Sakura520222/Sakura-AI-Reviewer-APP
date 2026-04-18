package com.sakura_ai_reviewer.feature.review.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.feature.review.data.ReviewItemData
import com.sakura_ai_reviewer.feature.review.data.ReviewListData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    onNavigateToDetail: (Int) -> Unit = {},
    viewModel: ReviewListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Reviews") },
            actions = {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
        )

        if (showSearch) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search reviews...") },
                singleLine = true
            )
        }

        if (showFilters) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text("Status", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("", "pending", "reviewing", "completed", "failed").forEach { status ->
                        FilterChip(
                            selected = uiState.statusFilter == status,
                            onClick = { viewModel.updateStatusFilter(status) },
                            label = { Text(status.ifEmpty { "All" }, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Decision", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("", "approve", "request_changes", "comment", "skip").forEach { decision ->
                        FilterChip(
                            selected = uiState.decisionFilter == decision,
                            onClick = { viewModel.updateDecisionFilter(decision) },
                            label = { Text(decision.ifEmpty { "All" }.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        when (val reviews = uiState.reviews) {
            is ApiResult.Success -> {
                ReviewListContent(
                    data = reviews.data,
                    onPageChange = { viewModel.goToPage(it) },
                    onItemClick = onNavigateToDetail
                )
            }
            is ApiResult.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
                }
            }
            is ApiResult.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load reviews", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(reviews.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadReviews() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                ReviewListContent(
                    data = reviews.data,
                    onPageChange = { viewModel.goToPage(it) },
                    onItemClick = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
private fun ReviewListContent(
    data: ReviewListData,
    onPageChange: (Int) -> Unit,
    onItemClick: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    if (data.items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No reviews found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(data.items, key = { it.id }) { review ->
            ReviewListItem(review = review, onClick = { onItemClick(review.id) })
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (data.totalPages > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onPageChange(data.page - 1) },
                        enabled = data.page > 1
                    ) { Text("Previous") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${data.page} / ${data.totalPages}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        onClick = { onPageChange(data.page + 1) },
                        enabled = data.page < data.totalPages
                    ) { Text("Next") }
                }
            }
        }
    }
}

@Composable
private fun ReviewListItem(review: ReviewItemData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = review.title ?: "PR #${review.prId}",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = listOfNotNull(review.repoName, review.author).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DecisionChip(review.decision)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusChip(review.status)
                review.overallScore?.let { score ->
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
            }
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
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

@Composable
private fun DecisionChip(decision: String?) {
    val (label, color) = when (decision) {
        "approve" -> "Approve" to MaterialTheme.colorScheme.primary
        "request_changes" -> "Changes" to MaterialTheme.colorScheme.error
        "comment" -> "Comment" to MaterialTheme.colorScheme.tertiary
        "skip" -> "Skip" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> "Pending" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
}
