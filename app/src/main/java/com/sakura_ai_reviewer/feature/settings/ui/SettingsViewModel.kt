package com.sakura_ai_reviewer.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.auth.SessionManager
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.settings.data.AboutData
import com.sakura_ai_reviewer.feature.settings.data.SettingsApiService
import com.sakura_ai_reviewer.feature.settings.data.UpdateSettingsRequest
import com.sakura_ai_reviewer.feature.settings.data.UserSettingsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: ApiResult<UserSettingsData> = ApiResult.Loading,
    val about: ApiResult<AboutData> = ApiResult.Loading,
    val isSaving: Boolean = false,
    val saveResult: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsApiService: SettingsApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadAbout()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val response = settingsApiService.getSettings()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(settings = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        settings = ApiResult.Error(response.error ?: "Failed to load settings")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    settings = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    private fun loadAbout() {
        viewModelScope.launch {
            try {
                val response = settingsApiService.getAbout()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(about = ApiResult.Success(response.data))
                }
            } catch (_: Exception) {}
        }
    }

    fun updateSettings(theme: String? = null, itemsPerPage: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveResult = null)
            try {
                val response = settingsApiService.updateSettings(
                    UpdateSettingsRequest(theme = theme, itemsPerPage = itemsPerPage)
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveResult = if (response.success) "Settings saved" else (response.error ?: "Failed to save")
                )
                if (response.success) loadSettings()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveResult = e.toUserMessage()
                )
            }
        }
    }

    fun logout() {
        sessionManager.logout()
    }
}
