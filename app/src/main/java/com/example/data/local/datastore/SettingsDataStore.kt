package com.example.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.ui.theme.TerminalThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shell_home_settings")

data class ShellPreferences(
    val theme: TerminalThemeType = TerminalThemeType.DRACULA,
    val fontSize: Float = 13f,
    val fontName: String = "Monospace",
    val username: String = "user",
    val hostname: String = "shellhome",
    val bootAnimationEnabled: Boolean = true,
    val autoLaunchSingleMatch: Boolean = false,
    val showNeofetchOnBoot: Boolean = true,
    val crtScanlinesEnabled: Boolean = false,
    val scrollbackLimit: Int = 500,
    val defaultMode: String = "term", // "term" or "gui"
    val searchEngine: String = "google", // "google", "duckduckgo", "bing"
    val wallpaperDim: Float = 0.65f, // 0.0f to 1.0f
    val swipeUpAction: String = "drawer",
    val swipeDownAction: String = "notifications",
    val doubleTapAction: String = "lock",
    val onboardingCompleted: Boolean = false
)

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val FONT_NAME = stringPreferencesKey("font_name")
        val USERNAME = stringPreferencesKey("username")
        val HOSTNAME = stringPreferencesKey("hostname")
        val BOOT_ANIMATION = booleanPreferencesKey("boot_animation")
        val AUTO_LAUNCH = booleanPreferencesKey("auto_launch")
        val NEOFETCH_ON_BOOT = booleanPreferencesKey("neofetch_on_boot")
        val CRT_SCANLINES = booleanPreferencesKey("crt_scanlines")
        val SCROLLBACK_LIMIT = intPreferencesKey("scrollback_limit")
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
        val SWIPE_UP_ACTION = stringPreferencesKey("swipe_up_action")
        val SWIPE_DOWN_ACTION = stringPreferencesKey("swipe_down_action")
        val DOUBLE_TAP_ACTION = stringPreferencesKey("double_tap_action")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val preferencesFlow: Flow<ShellPreferences> = context.dataStore.data.map { prefs ->
        val themeString = prefs[PreferencesKeys.THEME] ?: TerminalThemeType.DRACULA.name
        val themeType = try {
            TerminalThemeType.valueOf(themeString)
        } catch (_: Exception) {
            TerminalThemeType.DRACULA
        }

        ShellPreferences(
            theme = themeType,
            fontSize = prefs[PreferencesKeys.FONT_SIZE] ?: 13f,
            fontName = prefs[PreferencesKeys.FONT_NAME] ?: "Monospace",
            username = prefs[PreferencesKeys.USERNAME] ?: "user",
            hostname = prefs[PreferencesKeys.HOSTNAME] ?: "shellhome",
            bootAnimationEnabled = prefs[PreferencesKeys.BOOT_ANIMATION] ?: true,
            autoLaunchSingleMatch = prefs[PreferencesKeys.AUTO_LAUNCH] ?: false,
            showNeofetchOnBoot = prefs[PreferencesKeys.NEOFETCH_ON_BOOT] ?: true,
            crtScanlinesEnabled = prefs[PreferencesKeys.CRT_SCANLINES] ?: false,
            scrollbackLimit = prefs[PreferencesKeys.SCROLLBACK_LIMIT] ?: 500,
            defaultMode = prefs[PreferencesKeys.DEFAULT_MODE] ?: "term",
            searchEngine = prefs[PreferencesKeys.SEARCH_ENGINE] ?: "google",
            wallpaperDim = prefs[PreferencesKeys.WALLPAPER_DIM] ?: 0.65f,
            swipeUpAction = prefs[PreferencesKeys.SWIPE_UP_ACTION] ?: "drawer",
            swipeDownAction = prefs[PreferencesKeys.SWIPE_DOWN_ACTION] ?: "notifications",
            doubleTapAction = prefs[PreferencesKeys.DOUBLE_TAP_ACTION] ?: "lock",
            onboardingCompleted = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun setTheme(theme: TerminalThemeType) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SIZE] = size.coerceIn(9f, 24f) }
    }

    suspend fun setFontName(fontName: String) {
        context.dataStore.edit { it[PreferencesKeys.FONT_NAME] = fontName }
    }

    suspend fun setPrompt(user: String, host: String) {
        context.dataStore.edit {
            it[PreferencesKeys.USERNAME] = user.ifBlank { "user" }
            it[PreferencesKeys.HOSTNAME] = host.ifBlank { "shellhome" }
        }
    }

    suspend fun setBootAnimation(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BOOT_ANIMATION] = enabled }
    }

    suspend fun setAutoLaunch(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_LAUNCH] = enabled }
    }

    suspend fun setNeofetchOnBoot(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NEOFETCH_ON_BOOT] = enabled }
    }

    suspend fun setCrtScanlines(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CRT_SCANLINES] = enabled }
    }

    suspend fun setScrollbackLimit(limit: Int) {
        context.dataStore.edit { it[PreferencesKeys.SCROLLBACK_LIMIT] = limit.coerceIn(50, 2000) }
    }

    suspend fun setDefaultMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_MODE] = mode }
    }

    suspend fun setSearchEngine(engine: String) {
        context.dataStore.edit { it[PreferencesKeys.SEARCH_ENGINE] = engine }
    }

    suspend fun setWallpaperDim(dim: Float) {
        context.dataStore.edit { it[PreferencesKeys.WALLPAPER_DIM] = dim.coerceIn(0.0f, 1.0f) }
    }

    suspend fun setGestures(swipeUp: String, swipeDown: String, doubleTap: String) {
        context.dataStore.edit {
            it[PreferencesKeys.SWIPE_UP_ACTION] = swipeUp
            it[PreferencesKeys.SWIPE_DOWN_ACTION] = swipeDown
            it[PreferencesKeys.DOUBLE_TAP_ACTION] = doubleTap
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }
}
