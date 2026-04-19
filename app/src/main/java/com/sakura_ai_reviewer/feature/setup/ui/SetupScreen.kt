package com.sakura_ai_reviewer.feature.setup.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.sakura_ai_reviewer.core.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Wizard") }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.isComplete) {
            SetupCompleteContent(
                modifier = Modifier.padding(innerPadding),
                onContinue = onComplete
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Step indicator
                StepIndicator(
                    currentStep = uiState.currentStep,
                    totalSteps = uiState.totalSteps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Loading indicator
                if (uiState.isSaving || uiState.testLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Primary
                    )
                }

                // Error display
                uiState.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Test result display
                uiState.testResult?.let { result ->
                    val isSuccess = result.startsWith("\u2713")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) {
                                CardDefaults.cardColors().containerColor
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Text(
                            text = result,
                            color = if (isSuccess) StatusSuccess else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Step content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (uiState.currentStep) {
                        0 -> DatabaseStepContent(
                            formValues = uiState.formValues,
                            onFieldValueChange = viewModel::updateFieldValue,
                            onTestConnection = viewModel::testConnection,
                            testLoading = uiState.testLoading
                        )
                        1 -> GitHubStepContent(
                            formValues = uiState.formValues,
                            onFieldValueChange = viewModel::updateFieldValue,
                            onTestConnection = viewModel::testConnection,
                            testLoading = uiState.testLoading
                        )
                        2 -> AiStepContent(
                            formValues = uiState.formValues,
                            onFieldValueChange = viewModel::updateFieldValue,
                            onTestConnection = viewModel::testConnection,
                            testLoading = uiState.testLoading
                        )
                        3 -> RagAdminStepContent(
                            formValues = uiState.formValues,
                            onFieldValueChange = viewModel::updateFieldValue
                        )
                        4 -> CompleteStepContent(
                            formValues = uiState.formValues
                        )
                    }
                }

                // Navigation buttons
                NavigationButtons(
                    currentStep = uiState.currentStep,
                    totalSteps = uiState.totalSteps,
                    isSaving = uiState.isSaving,
                    onPrevious = viewModel::goToPreviousStep,
                    onSaveNext = viewModel::saveCurrentStep,
                    onComplete = viewModel::completeSetup
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSteps) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep

            Surface(
                shape = MaterialTheme.shapes.small,
                color = when {
                    isCompleted -> StatusSuccess
                    isCurrent -> Primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Step ${i + 1} completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${i + 1}",
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (i < totalSteps - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun DatabaseStepContent(
    formValues: Map<String, String>,
    onFieldValueChange: (String, String) -> Unit,
    onTestConnection: (String) -> Unit,
    testLoading: Boolean
) {
    Text(
        text = "Database Configuration",
        style = MaterialTheme.typography.titleLarge,
        color = Primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Configure your database and Redis connections.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = formValues["DATABASE_URL"] ?: "",
        onValueChange = { onFieldValueChange("DATABASE_URL", it) },
        label = { Text("Database URL") },
        placeholder = { Text("postgresql://user:pass@host:5432/dbname") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = { onTestConnection("database") },
        enabled = !testLoading
    ) {
        Text("Test Database")
    }
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = formValues["REDIS_URL"] ?: "",
        onValueChange = { onFieldValueChange("REDIS_URL", it) },
        label = { Text("Redis URL") },
        placeholder = { Text("redis://host:6379") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = { onTestConnection("redis") },
        enabled = !testLoading
    ) {
        Text("Test Redis")
    }
}

@Composable
private fun GitHubStepContent(
    formValues: Map<String, String>,
    onFieldValueChange: (String, String) -> Unit,
    onTestConnection: (String) -> Unit,
    testLoading: Boolean
) {
    Text(
        text = "GitHub Configuration",
        style = MaterialTheme.typography.titleLarge,
        color = Primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Configure your GitHub App integration.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = formValues["GITHUB_APP_ID"] ?: "",
        onValueChange = { onFieldValueChange("GITHUB_APP_ID", it) },
        label = { Text("GitHub App ID") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["GITHUB_PRIVATE_KEY"] ?: "",
        onValueChange = { onFieldValueChange("GITHUB_PRIVATE_KEY", it) },
        label = { Text("GitHub Private Key") },
        placeholder = { Text("-----BEGIN RSA PRIVATE KEY-----...") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["GITHUB_WEBHOOK_SECRET"] ?: "",
        onValueChange = { onFieldValueChange("GITHUB_WEBHOOK_SECRET", it) },
        label = { Text("GitHub Webhook Secret") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = { onTestConnection("github") },
        enabled = !testLoading
    ) {
        Text("Test GitHub")
    }
}

@Composable
private fun AiStepContent(
    formValues: Map<String, String>,
    onFieldValueChange: (String, String) -> Unit,
    onTestConnection: (String) -> Unit,
    testLoading: Boolean
) {
    Text(
        text = "AI Configuration",
        style = MaterialTheme.typography.titleLarge,
        color = Primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Configure your OpenAI API connection.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = formValues["OPENAI_API_KEY"] ?: "",
        onValueChange = { onFieldValueChange("OPENAI_API_KEY", it) },
        label = { Text("OpenAI API Key") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["OPENAI_API_BASE"] ?: "",
        onValueChange = { onFieldValueChange("OPENAI_API_BASE", it) },
        label = { Text("OpenAI API Base URL") },
        placeholder = { Text("https://api.openai.com/v1") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["OPENAI_MODEL"] ?: "",
        onValueChange = { onFieldValueChange("OPENAI_MODEL", it) },
        label = { Text("OpenAI Model") },
        placeholder = { Text("gpt-4") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = { onTestConnection("openai") },
        enabled = !testLoading
    ) {
        Text("Test OpenAI")
    }
}

@Composable
private fun RagAdminStepContent(
    formValues: Map<String, String>,
    onFieldValueChange: (String, String) -> Unit
) {
    Text(
        text = "RAG & Admin Configuration",
        style = MaterialTheme.typography.titleLarge,
        color = Primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Configure embedding, reranking, and admin settings.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Embedding",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["EMBEDDING_API_KEY"] ?: "",
        onValueChange = { onFieldValueChange("EMBEDDING_API_KEY", it) },
        label = { Text("Embedding API Key") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["EMBEDDING_BASE_URL"] ?: "",
        onValueChange = { onFieldValueChange("EMBEDDING_BASE_URL", it) },
        label = { Text("Embedding Base URL") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["EMBEDDING_MODEL"] ?: "",
        onValueChange = { onFieldValueChange("EMBEDDING_MODEL", it) },
        label = { Text("Embedding Model") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Rerank",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["RERANK_API_KEY"] ?: "",
        onValueChange = { onFieldValueChange("RERANK_API_KEY", it) },
        label = { Text("Rerank API Key") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["RERANK_BASE_URL"] ?: "",
        onValueChange = { onFieldValueChange("RERANK_BASE_URL", it) },
        label = { Text("Rerank Base URL") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["RERANK_MODEL"] ?: "",
        onValueChange = { onFieldValueChange("RERANK_MODEL", it) },
        label = { Text("Rerank Model") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Admin",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["ADMIN_GITHUB_USERNAME"] ?: "",
        onValueChange = { onFieldValueChange("ADMIN_GITHUB_USERNAME", it) },
        label = { Text("Admin GitHub Username") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = formValues["ADMIN_TELEGRAM_ID"] ?: "",
        onValueChange = { onFieldValueChange("ADMIN_TELEGRAM_ID", it) },
        label = { Text("Admin Telegram ID") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun CompleteStepContent(
    formValues: Map<String, String>
) {
    Text(
        text = "Review & Complete",
        style = MaterialTheme.typography.titleLarge,
        color = Primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Review your configuration before completing setup.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    val fieldLabels = linkedMapOf(
        "DATABASE_URL" to "Database URL",
        "REDIS_URL" to "Redis URL",
        "GITHUB_APP_ID" to "GitHub App ID",
        "GITHUB_PRIVATE_KEY" to "GitHub Private Key",
        "GITHUB_WEBHOOK_SECRET" to "GitHub Webhook Secret",
        "OPENAI_API_KEY" to "OpenAI API Key",
        "OPENAI_API_BASE" to "OpenAI API Base",
        "OPENAI_MODEL" to "OpenAI Model",
        "EMBEDDING_API_KEY" to "Embedding API Key",
        "EMBEDDING_BASE_URL" to "Embedding Base URL",
        "EMBEDDING_MODEL" to "Embedding Model",
        "RERANK_API_KEY" to "Rerank API Key",
        "RERANK_BASE_URL" to "Rerank Base URL",
        "RERANK_MODEL" to "Rerank Model",
        "ADMIN_GITHUB_USERNAME" to "Admin GitHub Username",
        "ADMIN_TELEGRAM_ID" to "Admin Telegram ID"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            fieldLabels.forEach { (key, label) ->
                val value = formValues[key]
                if (!value.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "$label:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.4f)
                        )
                        Text(
                            text = if (key.contains("KEY", ignoreCase = true) ||
                                key.contains("SECRET", ignoreCase = true) ||
                                key.contains("PRIVATE", ignoreCase = true)
                            ) {
                                "\u2022".repeat(minOf(value.length, 12))
                            } else {
                                value
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    isSaving: Boolean,
    onPrevious: () -> Unit,
    onSaveNext: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !isSaving
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (currentStep < totalSteps - 1) {
            Button(
                onClick = onSaveNext,
                enabled = !isSaving
            ) {
                Text("Save & Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Button(
                onClick = onComplete,
                enabled = !isSaving
            ) {
                Text("Complete Setup")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SetupCompleteContent(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = StatusSuccess
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Setup Complete!",
            style = MaterialTheme.typography.headlineLarge,
            color = Primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Server is restarting...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continue to Login")
        }
    }
}
