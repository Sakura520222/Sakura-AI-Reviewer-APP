package com.sakura_ai_reviewer.feature.repo.ui

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.feature.repo.data.RepoItemData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(
    onBack: () -> Unit = {},
    viewModel: RepoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Repositories") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        uiState.indexResult?.let { result ->
            Text(
                text = result,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (val repos = uiState.repos) {
            is ApiResult.Success -> {
                if (repos.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No repositories found", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(repos.data, key = { it.id ?: it.repoName ?: "" }) { repo ->
                            RepoListItem(
                                repo = repo,
                                isIndexing = uiState.isIndexing,
                                onIndexDocs = { viewModel.indexDocs(it) },
                                onIndexCode = { viewModel.indexCode(it) },
                                onIndexIssues = { viewModel.indexIssues(it) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
                }
            }
            is ApiResult.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load repos", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(repos.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Cached -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(repos.data, key = { it.id ?: it.repoName ?: "" }) { repo ->
                        RepoListItem(
                            repo = repo,
                            isIndexing = uiState.isIndexing,
                            onIndexDocs = { viewModel.indexDocs(it) },
                            onIndexCode = { viewModel.indexCode(it) },
                            onIndexIssues = { viewModel.indexIssues(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoListItem(
    repo: RepoItemData,
    isIndexing: Boolean,
    onIndexDocs: (String) -> Unit,
    onIndexCode: (String) -> Unit,
    onIndexIssues: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = repo.repoName ?: "Unknown",
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
                    text = "Reviews: ${repo.reviewCount ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (repo.isActive == true) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (repo.isActive == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val repoName = repo.repoName ?: return@Row
                OutlinedButton(
                    onClick = { onIndexDocs(repoName) },
                    enabled = !isIndexing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Docs", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onIndexCode(repoName) },
                    enabled = !isIndexing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Code", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onIndexIssues(repoName) },
                    enabled = !isIndexing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Issues", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
