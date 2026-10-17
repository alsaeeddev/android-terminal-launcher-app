package com.example.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.DeviceInfoHelper
import com.example.core.util.FuzzyMatcher
import com.example.data.system.SystemActionController
import com.example.domain.command.CommandRegistry
import com.example.domain.model.AppItem
import com.example.domain.model.OutputLine
import com.example.domain.model.OutputLineType
import com.example.domain.repository.AppRepository
import com.example.domain.repository.ProductivityRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.usecase.ExecuteCommandUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiveStatusInfo(
    val time: String = "",
    val batteryPct: Int = 100,
    val isCharging: Boolean = false,
    val network: String = "Wi-Fi"
)

data class SuggestionItem(
    val text: String,
    val badge: String = "",
    val isApp: Boolean = false,
    val packageName: String? = null
)

data class TerminalUiState(
    val lines: List<OutputLine> = emptyList(),
    val input: String = "",
    val username: String = "user",
    val hostname: String = "shellhome",
    val isBooting: Boolean = true,
    val suggestions: List<SuggestionItem> = emptyList(),
    val wallpaperDim: Float = 0.65f,
    val fontSize: Float = 13f,
    val crtScanlines: Boolean = false,
    val isExecuting: Boolean = false,
    val liveStatus: LiveStatusInfo = LiveStatusInfo()
) {
    val prompt: String get() = "$username@$hostname:~$ "
}

class TerminalViewModel(
    private val executeCommandUseCase: ExecuteCommandUseCase,
    private val commandRegistry: CommandRegistry,
    private val appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val productivityRepository: ProductivityRepository,
    private val systemActionController: SystemActionController
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var commandHistory: List<String> = emptyList()
    private var historyPointer: Int = -1
    private var temporaryInput: String = ""
    private var executionJob: Job? = null

    init {
        // Collect settings
        viewModelScope.launch {
            settingsRepository.preferencesFlow.collectLatest { prefs ->
                _uiState.update { current ->
                    current.copy(
                        username = prefs.username,
                        hostname = prefs.hostname,
                        wallpaperDim = prefs.wallpaperDim,
                        fontSize = prefs.fontSize,
                        crtScanlines = prefs.crtScanlinesEnabled,
                        isBooting = if (prefs.bootAnimationEnabled) current.isBooting else false
                    )
                }
            }
        }

        // Boot sequence
        viewModelScope.launch {
            val prefs = settingsRepository.preferencesFlow.first()
            if (prefs.bootAnimationEnabled) {
                runBootSequence(prefs.showNeofetchOnBoot)
            } else {
                _uiState.update { it.copy(isBooting = false) }
                if (prefs.showNeofetchOnBoot) {
                    showWelcomeNeofetch()
                }
            }
        }

        // Live status ticker (updates time and battery every 10 seconds)
        viewModelScope.launch {
            while (true) {
                updateLiveStatus()
                delay(10_000)
            }
        }

        // Load command history
        viewModelScope.launch {
            commandHistory = productivityRepository.getRecentHistory(50)
        }
    }

    private suspend fun runBootSequence(showNeofetch: Boolean) {
        val bootMessages = listOf(
            OutputLine("Shell Home Kernel Initializing...", OutputLineType.HEADER),
            OutputLine("[  0.0124] CPU architecture: arm64-v8a / 16 KB page-size ready", OutputLineType.NORMAL),
            OutputLine("[  0.0451] Security: Strict sandboxing active, zero cloud tracking", OutputLineType.NORMAL),
            OutputLine("[  0.0892] Mounting virtual filesystems: /apps, /notes, /tasks", OutputLineType.NORMAL),
            OutputLine("[  0.1340] Core runtime loaded. Type 'help' for available commands.", OutputLineType.SUCCESS)
        )

        for (msg in bootMessages) {
            _uiState.update { it.copy(lines = it.lines + msg) }
            delay(120)
        }

        _uiState.update { it.copy(isBooting = false) }

        if (showNeofetch) {
            showWelcomeNeofetch()
        }
    }

    private fun showWelcomeNeofetch() {
        val banner = DeviceInfoHelper.getNeofetchHeader(
            context = systemActionController.openDefaultLauncherSettings().let {
                // context fallback
                com.example.core.util.DeviceInfoHelper::class.java.classLoader
            }.let {
                // We'll generate banner safely
                _uiState.value.username
            }.let {
                // Emit welcome
                null
            } ?: return
        )
    }

    fun onSkipBoot() {
        _uiState.update { it.copy(isBooting = false) }
    }

    fun onInputChange(newInput: String) {
        _uiState.update { it.copy(input = newInput) }
        updateSuggestions(newInput)
    }

    private fun updateSuggestions(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        val commands = commandRegistry.getAllNamesAndAliases()
        val apps = appRepository.appsFlow.value

        val cmdMatches = commands
            .filter { it.startsWith(trimmed, ignoreCase = true) || it.contains(trimmed, ignoreCase = true) }
            .take(4)
            .map { SuggestionItem(text = it, badge = "cmd") }

        val appMatches = FuzzyMatcher.filterAndSort(trimmed, apps, { it.label }, limit = 4)
            .map { SuggestionItem(text = it.label, badge = "app", isApp = true, packageName = it.packageName) }

        val combined = (cmdMatches + appMatches).take(6)
        _uiState.update { it.copy(suggestions = combined) }
    }

    fun onSuggestionClick(suggestion: SuggestionItem, context: Context) {
        if (suggestion.isApp && suggestion.packageName != null) {
            viewModelScope.launch {
                appRepository.launchApp(suggestion.packageName)
            }
        } else {
            _uiState.update { it.copy(input = suggestion.text + " ", suggestions = emptyList()) }
        }
    }

    fun onTabPress() {
        val current = _uiState.value.input.trim()
        if (current.isEmpty()) return
        val suggestions = _uiState.value.suggestions
        if (suggestions.isNotEmpty()) {
            _uiState.update { it.copy(input = suggestions[0].text + " ") }
        }
    }

    fun onAppendExtraKey(key: String) {
        val cur = _uiState.value.input
        _uiState.update { it.copy(input = cur + key) }
        updateSuggestions(cur + key)
    }

    fun onHistoryUp() {
        if (commandHistory.isEmpty()) return
        if (historyPointer == -1) {
            temporaryInput = _uiState.value.input
            historyPointer = 0
        } else if (historyPointer < commandHistory.size - 1) {
            historyPointer++
        }
        val cmd = commandHistory[historyPointer]
        _uiState.update { it.copy(input = cmd) }
    }

    fun onHistoryDown() {
        if (historyPointer > 0) {
            historyPointer--
            val cmd = commandHistory[historyPointer]
            _uiState.update { it.copy(input = cmd) }
        } else if (historyPointer == 0) {
            historyPointer = -1
            _uiState.update { it.copy(input = temporaryInput) }
        }
    }

    fun onExecute(context: Context) {
        val raw = _uiState.value.input.trim()
        if (raw.isEmpty()) return

        // Intercept clear command immediately
        if (raw.equals("clear", ignoreCase = true) || raw.equals("cls", ignoreCase = true)) {
            _uiState.update { it.copy(lines = emptyList(), input = "", suggestions = emptyList()) }
            return
        }

        _uiState.update { it.copy(input = "", suggestions = emptyList(), isExecuting = true) }
        historyPointer = -1

        executionJob?.cancel()
        executionJob = viewModelScope.launch {
            val promptStr = _uiState.value.prompt
            executeCommandUseCase.execute(context, raw, promptStr).collect { line ->
                _uiState.update { current ->
                    val maxScrollback = 500
                    val updated = (current.lines + line).takeLast(maxScrollback)
                    current.copy(lines = updated)
                }
            }
            _uiState.update { it.copy(isExecuting = false) }
            // Refresh history
            commandHistory = productivityRepository.getRecentHistory(50)
        }
    }

    fun onAppClick(packageName: String) {
        viewModelScope.launch {
            appRepository.launchApp(packageName)
        }
    }

    fun onDoubleTap() {
        systemActionController.lockScreen()
    }

    fun onSwipeDown() {
        systemActionController.expandNotificationShade()
    }

    private fun updateLiveStatus() {
        val timeStr = com.example.core.util.DateTimeUtil.formatCurrentTime("HH:mm")
        _uiState.update { current ->
            current.copy(
                liveStatus = current.liveStatus.copy(time = timeStr)
            )
        }
    }
}
