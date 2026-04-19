package com.sakura_ai_reviewer.feature.scan.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.sakura_ai_reviewer.feature.scan.data.ScanItemData
import com.sakura_ai_reviewer.feature.scan.data.ScanListData
import com.sakura_ai_reviewer.feature.scan.data.ScanStatsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanListScreen(
    onNavigateToDetail: (scanId: Int) -> Unit,
    onBack: () -> Unit = {},
    viewModel: ScanListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scans") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.triggerScan() },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Trigger Scan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats card
            when (val stats = uiState.stats) {
                is ApiResult.Success -> {
                    ScanStatsCard(stats.data)
                }
                else -> {}
            }

            // Search bar
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                label = { Text("Search scans") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true
            )
            if (searchInput != uiState.searchQuery) {
                viewModel.updateSearch(searchInput)
            }

            // Status filter chips
            if (showFilters) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text("Status", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("", "pending", "indexing", "analyzing", "reporting", "completed", "failed", "cancelled").forEach { status ->
                            FilterChip(
                                selected = uiState.statusFilter == status,
                                onClick = { viewModel.updateStatusFilter(status) },
                                label = { Text(status.ifEmpty { "All" }, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            uiState.actionResult?.let { result ->
                Text(
                    text = result,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (val scans = uiState.scans) {
                is ApiResult.Success -> {
                    ScanListContent(
                        data = scans.data,
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to load scans", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                scans.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                is ApiResult.Cached -> {
                    ScanListContent(
                        data = scans.data,
                        onPageChange = { viewModel.goToPage(it) },
                        onItemClick = onNavigateToDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanStatsCard(stats: ScanStatsData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Scans", style = MaterialTheme.typography.labelMedium)
                Text(
                    stats.total.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val byStatus = stats.byStatus
                StatItem("Completed", byStatus["completed"] ?: 0, MaterialTheme.colorScheme.primary)
                StatItem("Pending", byStatus["pending"] ?: 0, MaterialTheme.colorScheme.secondary)
                StatItem("Processing", (byStatus["indexing"] ?: 0) + (byStatus["analyzing"] ?: 0) + (byStatus["reporting"] ?: 0), MaterialTheme.colorScheme.tertiary)
                StatItem("Failed", byStatus["failed"] ?: 0, MaterialTheme.colorScheme.error)
            }
            stats.avgHealthScore?.let { score ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Avg Health Score", style = MaterialTheme.typography.labelMedium)
                    Text(
                        String.format("%.1f", score),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScanListContent(
    data: ScanListData,
    onPageChange: (Int) -> Unit,
    onItemClick: (Int) -> Unit
) {
    if (data.items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scans found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(data.items, key = { it.id }) { item ->
            ScanListItem(
                item = item,
                onClick = { onItemClick(item.id) }
            )
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
                    Text(
                        "${data.page} / ${data.totalPages}",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
private fun ScanListItem(
    item: ScanItemData,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.repoName ?: "Scan #${item.id}",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.status?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (item.status) {
                        "completed" -> MaterialTheme.colorScheme.primary
                        "processing", "indexing", "analyzing", "reporting" -> MaterialTheme.colorScheme.tertiary
                        "pending" -> MaterialTheme.colorScheme.secondary
                        "failed" -> MaterialTheme.colorScheme.error
                        "cancelled" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Progress bar for in-progress scans
            val inProgressStatuses = setOf("processing", "indexing", "analyzing", "reporting")
            if (item.status in inProgressStatuses && item.progress != null) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (item.progress ?: 0) / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.overallHealthScore?.let { score ->
                        Text(
                            text = String.format("%.0f", score),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item.totalFindings?.let { findings ->
                        Text(
                            text = "$findings findings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item.createdAt?.let { created ->
                    Text(
                        text = created,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
