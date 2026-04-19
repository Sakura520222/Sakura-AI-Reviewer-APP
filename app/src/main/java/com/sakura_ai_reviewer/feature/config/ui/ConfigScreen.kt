package com.sakura_ai_reviewer.feature.config.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.ui.theme.Primary
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onBack: () -> Unit = {},
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("General", "Strategies", "Labels")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("System Config") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        TabRow(selectedTabIndex = uiState.selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = { Text(title) }
                )
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

        when (uiState.selectedTab) {
            0 -> GeneralTab(uiState = uiState, viewModel = viewModel)
            1 -> StrategiesTab(uiState = uiState, viewModel = viewModel)
            2 -> LabelsTab(uiState = uiState)
        }
    }
}

@Composable
private fun GeneralTab(
    uiState: ConfigUiState,
    viewModel: ConfigViewModel
) {
    var editKey by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }

    when (val config = uiState.generalConfig) {
        is ApiResult.Success -> {
            val configs = config.data.configs
            if (configs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No configuration found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(configs.entries.toList(), key = { it.key }) { entry ->
                        val isSecret = entry.key.contains("secret", ignoreCase = true) ||
                                entry.key.contains("key", ignoreCase = true) ||
                                entry.key.contains("token", ignoreCase = true) ||
                                entry.key.contains("password", ignoreCase = true) ||
                                entry.key.contains("credential", ignoreCase = true)
                        val displayValue = if (isSecret) "****" else entry.value

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    editKey = entry.key
                                    editValue = entry.value
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = displayValue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                    Text("Failed to load config", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(config.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }
        }
        is ApiResult.Cached -> {
            val configs = config.data.configs
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(configs.entries.toList(), key = { it.key }) { entry ->
                    val isSecret = entry.key.contains("secret", ignoreCase = true) ||
                            entry.key.contains("key", ignoreCase = true) ||
                            entry.key.contains("token", ignoreCase = true) ||
                            entry.key.contains("password", ignoreCase = true) ||
                            entry.key.contains("credential", ignoreCase = true)
                    val displayValue = if (isSecret) "****" else entry.value

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable {
                                editKey = entry.key
                                editValue = entry.value
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.key,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = displayValue,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    editKey?.let { key ->
        AlertDialog(
            onDismissRequest = { editKey = null },
            title = { Text("Edit $key") },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateGeneralConfig(mapOf(key to editValue))
                        editKey = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editKey = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StrategiesTab(
    uiState: ConfigUiState,
    viewModel: ConfigViewModel
) {
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    var editingSection by remember { mutableStateOf<String?>(null) }
    var editingJson by remember { mutableStateOf("") }

    when (val strategies = uiState.strategies) {
        is ApiResult.Success -> {
            val sections = strategies.data.strategies
            if (sections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No strategies found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sections.entries.toList(), key = { it.key }) { entry ->
                        val isExpanded = entry.key in expandedSections

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedSections = if (isExpanded) {
                                                expandedSections - entry.key
                                            } else {
                                                expandedSections + entry.key
                                            }
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = formatValue(entry.value),
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                editingSection = entry.key
                                                editingJson = formatValue(entry.value)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Edit", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
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
                    Text("Failed to load strategies", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(strategies.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }
        }
        is ApiResult.Cached -> {
            val sections = strategies.data.strategies
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sections.entries.toList(), key = { it.key }) { entry ->
                    val isExpanded = entry.key in expandedSections
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedSections = if (isExpanded) {
                                            expandedSections - entry.key
                                        } else {
                                            expandedSections + entry.key
                                        }
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                                )
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = formatValue(entry.value),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            editingSection = entry.key
                                            editingJson = formatValue(entry.value)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Edit", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingSection?.let { section ->
        AlertDialog(
            onDismissRequest = { editingSection = null },
            title = { Text("Edit Strategy: $section") },
            text = {
                OutlinedTextField(
                    value = editingJson,
                    onValueChange = { editingJson = it },
                    label = { Text("JSON") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 15
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val parsed = parseJsonToMap(editingJson)
                            if (parsed != null) {
                                viewModel.updateStrategy(section, parsed)
                            }
                        } catch (_: Exception) { }
                        editingSection = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingSection = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LabelsTab(
    uiState: ConfigUiState
) {
    when (val labelsResult = uiState.labels) {
        is ApiResult.Success -> {
            val data = labelsResult.data
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = "Labels",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(data.labels.indices.toList()) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = formatValue(data.labels[index]),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (data.recommendation.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recommendation Settings",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(data.recommendation.entries.toList(), key = { it.key }) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatValue(entry.value),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                    Text("Failed to load labels", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(labelsResult.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is ApiResult.Cached -> {
            val data = labelsResult.data
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = "Labels",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(data.labels.indices.toList()) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = formatValue(data.labels[index]),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                if (data.recommendation.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recommendation Settings",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(data.recommendation.entries.toList(), key = { it.key }) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatValue(entry.value),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatValue(value: Any?): String {
    return when (value) {
        null -> "null"
        is Map<*, *> -> {
            try {
                JSONObject(value as Map<*, *>).toString(2)
            } catch (_: Exception) {
                value.toString()
            }
        }
        is List<*> -> {
            try {
                org.json.JSONArray(value as List<*>).toString(2)
            } catch (_: Exception) {
                value.toString()
            }
        }
        else -> value.toString()
    }
}

private fun parseJsonToMap(json: String): Map<String, Any>? {
    return try {
        val jsonObj = JSONObject(json)
        jsonObj.keys().asSequence().associateWith { key ->
            val v = jsonObj.get(key)
            when (v) {
                is JSONObject -> parseJsonToMap(v.toString()) ?: emptyMap<String, Any>()
                is org.json.JSONArray -> parseJsonToList(v.toString())
                else -> v
            }
        }
    } catch (_: Exception) {
        null
    }
}

private fun parseJsonToList(json: String): List<Any> {
    return try {
        val arr = org.json.JSONArray(json)
        (0 until arr.length()).map { i ->
            val v = arr.get(i)
            when (v) {
                is JSONObject -> parseJsonToMap(v.toString()) ?: emptyMap<String, Any>()
                is org.json.JSONArray -> parseJsonToList(v.toString())
                else -> v
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}
