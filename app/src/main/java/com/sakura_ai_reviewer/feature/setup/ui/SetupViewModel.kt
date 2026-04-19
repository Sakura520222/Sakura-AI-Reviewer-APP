package com.sakura_ai_reviewer.feature.setup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.setup.data.CompleteSetupRequest
import com.sakura_ai_reviewer.feature.setup.data.SaveStepRequest
import com.sakura_ai_reviewer.feature.setup.data.SetupApiService
import com.sakura_ai_reviewer.feature.setup.data.SetupStateData
import com.sakura_ai_reviewer.feature.setup.data.TestConnectionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val isLoading: Boolean = false,
    val setupState: SetupStateData? = null,
    val currentStep: Int = 0,
    val totalSteps: Int = 5,
    val formValues: Map<String, String> = emptyMap(),
    val testResult: String? = null,
    val testLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val setupApiService: SetupApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = setupApiService.getState()
                if (response.success && response.data != null) {
                    val stateData = response.data
                    if (stateData.state.equals("completed", ignoreCase = true)) {
                        _uiState.update { it.copy(isComplete = true, isLoading = false) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                setupState = stateData,
                                currentStep = stateData.currentStep
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.error ?: "Failed to load setup state"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.toUserMessage())
                }
            }
        }
    }

    fun updateFieldValue(key: String, value: String) {
        _uiState.update {
            it.copy(formValues = it.formValues + (key to value))
        }
    }

    fun testConnection(type: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(testLoading = true, testResult = null, error = null) }
            try {
                val request = buildTestConnectionRequest(type)
                val response = setupApiService.testConnection(request)
                if (response.success && response.data != null) {
                    val result = response.data
                    val prefix = if (result.success) "\u2713" else "\u2717"
                    _uiState.update {
                        it.copy(
                            testLoading = false,
                            testResult = "$prefix ${result.message ?: (if (result.success) "Connection successful" else "Connection failed")}"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            testLoading = false,
                            testResult = "\u2717 ${response.error ?: "Test failed"}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(testLoading = false, testResult = "\u2717 ${e.toUserMessage()}")
                }
            }
        }
    }

    private fun buildTestConnectionRequest(type: String): TestConnectionRequest {
        val values = _uiState.value.formValues
        return when (type.lowercase()) {
            "database" -> TestConnectionRequest(
                type = "database",
                databaseUrl = values["DATABASE_URL"]
            )
            "redis" -> TestConnectionRequest(
                type = "redis",
                redisUrl = values["REDIS_URL"]
            )
            "github" -> TestConnectionRequest(
                type = "github",
                appId = values["GITHUB_APP_ID"],
                privateKey = values["GITHUB_PRIVATE_KEY"]
            )
            "openai" -> TestConnectionRequest(
                type = "openai",
                apiKey = values["OPENAI_API_KEY"],
                apiBase = values["OPENAI_API_BASE"]
            )
            else -> TestConnectionRequest(type = type)
        }
    }

    fun saveCurrentStep() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val response = setupApiService.saveStep(
                    SaveStepRequest(values = _uiState.value.formValues)
                )
                if (response.success) {
                    val current = _uiState.value.currentStep
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            currentStep = current + 1,
                            formValues = emptyMap(),
                            testResult = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = response.error ?: "Failed to save step"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.toUserMessage())
                }
            }
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val values = _uiState.value.formValues
                val request = CompleteSetupRequest(
                    databaseUrl = values["DATABASE_URL"],
                    redisUrl = values["REDIS_URL"],
                    githubAppId = values["GITHUB_APP_ID"],
                    githubPrivateKey = values["GITHUB_PRIVATE_KEY"],
                    githubWebhookSecret = values["GITHUB_WEBHOOK_SECRET"],
                    openaiApiKey = values["OPENAI_API_KEY"],
                    openaiApiBase = values["OPENAI_API_BASE"],
                    openaiModel = values["OPENAI_MODEL"],
                    adminGithubUsername = values["ADMIN_GITHUB_USERNAME"],
                    adminTelegramId = values["ADMIN_TELEGRAM_ID"],
                    embeddingApiKey = values["EMBEDDING_API_KEY"],
                    embeddingBaseUrl = values["EMBEDDING_BASE_URL"],
                    embeddingModel = values["EMBEDDING_MODEL"],
                    rerankApiKey = values["RERANK_API_KEY"],
                    rerankBaseUrl = values["RERANK_BASE_URL"],
                    rerankModel = values["RERANK_MODEL"]
                )
                val response = setupApiService.complete(request)
                if (response.success) {
                    _uiState.update { it.copy(isSaving = false, isComplete = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = response.error ?: "Failed to complete setup"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.toUserMessage())
                }
            }
        }
    }

    fun goToPreviousStep() {
        val current = _uiState.value.currentStep
        if (current > 0) {
            _uiState.update {
                it.copy(
                    currentStep = current - 1,
                    formValues = emptyMap(),
                    testResult = null,
                    error = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }
}
