package com.sakura_ai_reviewer.feature.config.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.config.data.ConfigApiService
import com.sakura_ai_reviewer.feature.config.data.GeneralConfigData
import com.sakura_ai_reviewer.feature.config.data.LabelsData
import com.sakura_ai_reviewer.feature.config.data.StrategiesData
import com.sakura_ai_reviewer.feature.config.data.UpdateGeneralConfigRequest
import com.sakura_ai_reviewer.feature.config.data.UpdateLabelsRequest
import com.sakura_ai_reviewer.feature.config.data.UpdateStrategyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigUiState(
    val selectedTab: Int = 0,
    val generalConfig: ApiResult<GeneralConfigData> = ApiResult.Loading,
    val strategies: ApiResult<StrategiesData> = ApiResult.Loading,
    val labels: ApiResult<LabelsData> = ApiResult.Loading,
    val editingKey: String? = null,
    val editingValue: String = "",
    val actionResult: String? = null
)

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val configApiService: ConfigApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        loadGeneralConfig()
        loadStrategies()
        loadLabels()
    }

    private fun loadGeneralConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(generalConfig = ApiResult.Loading)
            try {
                val response = configApiService.getGeneralConfig()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(generalConfig = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        generalConfig = ApiResult.Error(response.error ?: "Failed to load general config")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    generalConfig = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    private fun loadStrategies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(strategies = ApiResult.Loading)
            try {
                val response = configApiService.getStrategies()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(strategies = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        strategies = ApiResult.Error(response.error ?: "Failed to load strategies")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    strategies = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    private fun loadLabels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(labels = ApiResult.Loading)
            try {
                val response = configApiService.getLabels()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(labels = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        labels = ApiResult.Error(response.error ?: "Failed to load labels")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    labels = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun updateGeneralConfig(configs: Map<String, String>) {
        viewModelScope.launch {
            try {
                val response = configApiService.updateGeneralConfig(UpdateGeneralConfigRequest(configs))
                if (response.success) {
                    _uiState.value = _uiState.value.copy(actionResult = "Config updated")
                    loadGeneralConfig()
                } else {
                    _uiState.value = _uiState.value.copy(
                        actionResult = response.error ?: "Failed to update config"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun updateStrategy(section: String, data: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val response = configApiService.updateStrategy(section, UpdateStrategyRequest(data))
                if (response.success) {
                    _uiState.value = _uiState.value.copy(actionResult = "Strategy updated")
                    loadStrategies()
                } else {
                    _uiState.value = _uiState.value.copy(
                        actionResult = response.error ?: "Failed to update strategy"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun updateLabels(labels: List<Any>) {
        viewModelScope.launch {
            try {
                val response = configApiService.updateLabels(UpdateLabelsRequest(labels))
                if (response.success) {
                    _uiState.value = _uiState.value.copy(actionResult = "Labels updated")
                    loadLabels()
                } else {
                    _uiState.value = _uiState.value.copy(
                        actionResult = response.error ?: "Failed to update labels"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(actionResult = null)
    }

    fun refresh() {
        loadGeneralConfig()
        loadStrategies()
        loadLabels()
    }
}
