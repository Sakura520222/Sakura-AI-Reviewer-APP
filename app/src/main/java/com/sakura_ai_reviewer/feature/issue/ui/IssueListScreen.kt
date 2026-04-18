package com.sakura_ai_reviewer.feature.issue.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.sakura_ai_reviewer.feature.issue.data.IssueItemData
import com.sakura_ai_reviewer.feature.issue.data.IssueListData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    onNavigateToDetail: (Int) -> Unit = {},
    viewModel: IssueListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Issues") },
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
                placeholder = { Text("Search issues...") },
                singleLine = true
            )
        }

        if (showFilters) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text("Status", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("", "pending", "completed", "failed").forEach { status ->
                        FilterChip(
                            selected = uiState.statusFilter == status,
                            onClick = { viewModel.updateStatusFilter(status) },
                            label = { Text(status.ifEmpty { "All" }, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("", "bug", "feature", "question", "other").forEach { cat ->
                        FilterChip(
                            selected = uiState.categoryFilter == cat,
                            onClick = { viewModel.updateCategoryFilter(cat) },
                            label = { Text(cat.ifEmpty { "All" }, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("", "high", "medium", "low").forEach { pri ->
                        FilterChip(
                            selected = uiState.priorityFilter == pri,
                            onClick = { viewModel.updatePriorityFilter(pri) },
                            label = { Text(pri.ifEmpty { "All" }, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        when (val issues = uiState.issues) {
            is ApiResult.Success -> {
                IssueListContent(
                    data = issues.data,
                    onPageChange = { viewModel.goToPage(it) },
                    onItemClick = onNavigateToDetail
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
                        Text("Failed to load issues", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(issues.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                IssueListContent(
                    data = issues.data,
                    onPageChange = { viewModel.goToPage(it) },
                    onItemClick = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
private fun IssueListContent(
    data: IssueListData,
    onPageChange: (Int) -> Unit,
    onItemClick: (Int) -> Unit
) {
    if (data.items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No issues found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(data.items, key = { it.id }) { issue ->
            IssueListItem(issue = issue, onClick = { onItemClick(issue.id) })
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (data.totalPages > 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onPageChange(data.page - 1) }, enabled = data.page > 1) { Text("Previous") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${data.page} / ${data.totalPages}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = { onPageChange(data.page + 1) }, enabled = data.page < data.totalPages) { Text("Next") }
                }
            }
        }
    }
}

@Composable
private fun IssueListItem(issue: IssueItemData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = issue.title ?: "Issue #${issue.issueNumber}",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = listOfNotNull(issue.repoName, issue.author).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    issue.category?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                    issue.priority?.let { pri ->
                        val color = when (pri) {
                            "high" -> MaterialTheme.colorScheme.error
                            "medium" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(pri, style = MaterialTheme.typography.labelSmall, color = color)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            IssueStatusChip(issue.status)
        }
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
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}
