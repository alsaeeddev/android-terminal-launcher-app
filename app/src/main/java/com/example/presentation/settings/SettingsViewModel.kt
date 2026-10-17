package com.example.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.theme.TerminalThemeType
import com.example.data.local.datastore.ShellPreferences
import com.example.data.system.SystemActionController
import com.example.domain.model.AliasItem
import com.example.domain.model.AppItem
import com.example.domain.repository.AppRepository
import com.example.domain.repository.ProductivityRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: ShellPreferences = ShellPreferences(),
    val isDefaultLauncher: Boolean = false,
    val hiddenApps: List<AppItem> = emptyList(),
    val aliases: List<AliasItem> = emptyList(),
    val backupExportJson: String = ""
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val appRepository: AppRepository,
    private val productivityRepository: ProductivityRepository,
    private val systemActionController: SystemActionController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.preferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    preferences = prefs,
                    isDefaultLauncher = systemActionController.isDefaultLauncher()
                )
            }
        }

        viewModelScope.launch {
            appRepository.appsFlow.collect { all ->
                _uiState.value = _uiState.value.copy(
                    hiddenApps = all.filter { it.isHidden }
                )
            }
        }

        viewModelScope.launch {
            productivityRepository.aliasesFlow.collect { list ->
                _uiState.value = _uiState.value.copy(aliases = list)
            }
        }
    }

    fun setTheme(theme: TerminalThemeType) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch { settingsRepository.setFontSize(size) }
    }

    fun setPrompt(username: String, hostname: String) {
        viewModelScope.launch { settingsRepository.setPrompt(username, hostname) }
    }

    fun setBootAnimation(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBootAnimation(enabled) }
    }

    fun setAutoLaunch(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoLaunch(enabled) }
    }

    fun setCrtScanlines(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCrtScanlines(enabled) }
    }

    fun setWallpaperDim(dim: Float) {
        viewModelScope.launch { settingsRepository.setWallpaperDim(dim) }
    }

    fun setSearchEngine(engine: String) {
        viewModelScope.launch { settingsRepository.setSearchEngine(engine) }
    }

    fun setDefaultMode(mode: String) {
        viewModelScope.launch { settingsRepository.setDefaultMode(mode) }
    }

    fun unhideApp(app: AppItem) {
        viewModelScope.launch { appRepository.setHidden(app.packageName, false) }
    }

    fun removeAlias(name: String) {
        viewModelScope.launch { productivityRepository.removeAlias(name) }
    }

    fun openDefaultLauncherSettings(context: Context) {
        val intent = systemActionController.openDefaultLauncherSettings()
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun generateBackupJson(): String {
        val prefs = _uiState.value.preferences
        return """
        {
          "version": 1,
          "theme": "${prefs.theme.name}",
          "fontSize": ${prefs.fontSize},
          "username": "${prefs.username}",
          "hostname": "${prefs.hostname}",
          "wallpaperDim": ${prefs.wallpaperDim},
          "searchEngine": "${prefs.searchEngine}",
          "defaultMode": "${prefs.defaultMode}"
        }
        """.trimIndent()
    }
}
