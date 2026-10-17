package com.example.presentation.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.FuzzyMatcher
import com.example.domain.model.AppItem
import com.example.domain.repository.AppRepository
import com.example.service.ShellNotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GuiDrawerUiState(
    val searchQuery: String = "",
    val isGridView: Boolean = true,
    val selectedAppForMenu: AppItem? = null,
    val showRenameDialog: Boolean = false,
    val renameInput: String = "",
    val shortcutsForApp: List<String> = emptyList(),
    val badgeCounts: Map<String, Int> = emptyMap()
)

class GuiDrawerViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuiDrawerUiState())
    val uiState: StateFlow<GuiDrawerUiState> = _uiState.asStateFlow()

    val apps: StateFlow<List<AppItem>> = combine(
        appRepository.appsFlow,
        _uiState
    ) { allApps, state ->
        val visible = allApps.filter { !it.isHidden }
        if (state.searchQuery.isBlank()) {
            visible.sortedBy { it.label.lowercase() }
        } else {
            FuzzyMatcher.filterAndSort(state.searchQuery, visible, { it.label }, limit = 100)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<AppItem>> = appRepository.appsFlow.combine(_uiState) { allApps, _ ->
        allApps.filter { it.isFavorite && !it.isHidden }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect badge counts from NotificationListenerService
        viewModelScope.launch {
            ShellNotificationListenerService.badgeCounts.collect { badges ->
                _uiState.update { it.copy(badgeCounts = badges) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun onAppClick(app: AppItem) {
        viewModelScope.launch {
            appRepository.launchApp(app.packageName, app.userHandle, app.activityName)
        }
    }

    fun onAppLongClick(app: AppItem) {
        viewModelScope.launch {
            val shortcuts = appRepository.getAppShortcuts(app.packageName)
            _uiState.update {
                it.copy(
                    selectedAppForMenu = app,
                    shortcutsForApp = shortcuts,
                    renameInput = app.label
                )
            }
        }
    }

    fun dismissAppMenu() {
        _uiState.update { it.copy(selectedAppForMenu = null, showRenameDialog = false) }
    }

    fun toggleFavorite(app: AppItem) {
        viewModelScope.launch {
            appRepository.setFavorite(app.packageName, !app.isFavorite)
            dismissAppMenu()
        }
    }

    fun hideApp(app: AppItem) {
        viewModelScope.launch {
            appRepository.setHidden(app.packageName, true)
            dismissAppMenu()
        }
    }

    fun openAppInfo(app: AppItem) {
        viewModelScope.launch {
            appRepository.openAppInfo(app.packageName)
            dismissAppMenu()
        }
    }

    fun uninstallApp(app: AppItem) {
        viewModelScope.launch {
            appRepository.uninstallApp(app.packageName)
            dismissAppMenu()
        }
    }

    fun openPlayStore(app: AppItem) {
        viewModelScope.launch {
            appRepository.openPlayStore(app.packageName)
            dismissAppMenu()
        }
    }

    fun showRename() {
        _uiState.update { it.copy(showRenameDialog = true) }
    }

    fun onRenameInputChange(text: String) {
        _uiState.update { it.copy(renameInput = text) }
    }

    fun saveRename() {
        val app = _uiState.value.selectedAppForMenu ?: return
        val newLabel = _uiState.value.renameInput.trim()
        viewModelScope.launch {
            appRepository.setLabelOverride(app.packageName, if (newLabel.isEmpty()) null else newLabel)
            dismissAppMenu()
        }
    }
}
