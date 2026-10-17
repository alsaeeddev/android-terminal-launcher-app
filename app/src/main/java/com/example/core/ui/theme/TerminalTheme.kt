package com.example.core.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import android.content.Context

enum class TerminalThemeType(val displayName: String) {
    DRACULA("Dracula"),
    SOLARIZED_DARK("Solarized Dark"),
    GRUVBOX("Gruvbox"),
    NORD("Nord"),
    MONOKAI("Monokai"),
    MATRIX_GREEN("Matrix Green"),
    AMBER_CRT("Amber CRT"),
    LIGHT("Classic Light"),
    MATERIAL_YOU("Material You (Dynamic)")
}

@Immutable
data class TerminalColors(
    val background: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val promptUser: Color,
    val promptHost: Color,
    val promptPath: Color,
    val promptSymbol: Color,
    val commandText: Color,
    val successText: Color,
    val errorText: Color,
    val warningText: Color,
    val infoText: Color,
    val appLinkText: Color,
    val cursorColor: Color,
    val suggestionBg: Color,
    val suggestionText: Color,
    val isDark: Boolean = true
)

object TerminalThemes {
    val Dracula = TerminalColors(
        background = Color(0xFF282A36),
        surface = Color(0xFF21222C),
        primaryText = Color(0xFFF8F8F2),
        secondaryText = Color(0xFF6272A4),
        accent = Color(0xFFFF79C6),
        promptUser = Color(0xFF50FA7B),
        promptHost = Color(0xFF8BE9FD),
        promptPath = Color(0xFFBD93F9),
        promptSymbol = Color(0xFFFF79C6),
        commandText = Color(0xFFF1FA8C),
        successText = Color(0xFF50FA7B),
        errorText = Color(0xFFFF5555),
        warningText = Color(0xFFFFB86C),
        infoText = Color(0xFF8BE9FD),
        appLinkText = Color(0xFF8BE9FD),
        cursorColor = Color(0xFFF8F8F2),
        suggestionBg = Color(0xFF44475A),
        suggestionText = Color(0xFFF8F8F2)
    )

    val SolarizedDark = TerminalColors(
        background = Color(0xFF002B36),
        surface = Color(0xFF073642),
        primaryText = Color(0xFF839496),
        secondaryText = Color(0xFF586E75),
        accent = Color(0xFF268BD2),
        promptUser = Color(0xFF859900),
        promptHost = Color(0xFF2AA198),
        promptPath = Color(0xFF268BD2),
        promptSymbol = Color(0xFFD33682),
        commandText = Color(0xFFB58900),
        successText = Color(0xFF859900),
        errorText = Color(0xFFDC322F),
        warningText = Color(0xFFCB4B16),
        infoText = Color(0xFF2AA198),
        appLinkText = Color(0xFF2AA198),
        cursorColor = Color(0xFF93A1A1),
        suggestionBg = Color(0xFF073642),
        suggestionText = Color(0xFFEEE8D5)
    )

    val Gruvbox = TerminalColors(
        background = Color(0xFF282828),
        surface = Color(0xFF1D2021),
        primaryText = Color(0xFFEBDBB2),
        secondaryText = Color(0xFF928374),
        accent = Color(0xFFFE8019),
        promptUser = Color(0xFFB8BB26),
        promptHost = Color(0xFF8EC07C),
        promptPath = Color(0xFF83A598),
        promptSymbol = Color(0xFFFABD2F),
        commandText = Color(0xFFFABD2F),
        successText = Color(0xFFB8BB26),
        errorText = Color(0xFFFB4934),
        warningText = Color(0xFFFE8019),
        infoText = Color(0xFF8EC07C),
        appLinkText = Color(0xFF83A598),
        cursorColor = Color(0xFFEBDBB2),
        suggestionBg = Color(0xFF3C3836),
        suggestionText = Color(0xFFEBDBB2)
    )

    val Nord = TerminalColors(
        background = Color(0xFF2E3440),
        surface = Color(0xFF3B4252),
        primaryText = Color(0xFFECEFF4),
        secondaryText = Color(0xFFD8DEE9),
        accent = Color(0xFF88C0D0),
        promptUser = Color(0xFFA3BE8C),
        promptHost = Color(0xFF8FBCBB),
        promptPath = Color(0xFF81A1C1),
        promptSymbol = Color(0xFFB48EAD),
        commandText = Color(0xFFEBCB8B),
        successText = Color(0xFFA3BE8C),
        errorText = Color(0xFFBF616A),
        warningText = Color(0xFFD08770),
        infoText = Color(0xFF88C0D0),
        appLinkText = Color(0xFF88C0D0),
        cursorColor = Color(0xFFECEFF4),
        suggestionBg = Color(0xFF434C5E),
        suggestionText = Color(0xFFECEFF4)
    )

    val Monokai = TerminalColors(
        background = Color(0xFF272822),
        surface = Color(0xFF1E1F1C),
        primaryText = Color(0xFFF8F8F2),
        secondaryText = Color(0xFF75715E),
        accent = Color(0xFFF92672),
        promptUser = Color(0xFFA6E22E),
        promptHost = Color(0xFF66D9EF),
        promptPath = Color(0xFFAE81FF),
        promptSymbol = Color(0xFFFD971F),
        commandText = Color(0xFFE6DB74),
        successText = Color(0xFFA6E22E),
        errorText = Color(0xFFF92672),
        warningText = Color(0xFFFD971F),
        infoText = Color(0xFF66D9EF),
        appLinkText = Color(0xFF66D9EF),
        cursorColor = Color(0xFFF8F8F2),
        suggestionBg = Color(0xFF3E3D32),
        suggestionText = Color(0xFFF8F8F2)
    )

    val MatrixGreen = TerminalColors(
        background = Color(0xFF0D1117),
        surface = Color(0xFF030A04),
        primaryText = Color(0xFF00FF66),
        secondaryText = Color(0xFF008F39),
        accent = Color(0xFF00FF00),
        promptUser = Color(0xFF00FF66),
        promptHost = Color(0xFF39FF14),
        promptPath = Color(0xFF00CC44),
        promptSymbol = Color(0xFF00FF88),
        commandText = Color(0xFF70FF70),
        successText = Color(0xFF00FF66),
        errorText = Color(0xFFFF3333),
        warningText = Color(0xFFFFCC00),
        infoText = Color(0xFF00FF66),
        appLinkText = Color(0xFF39FF14),
        cursorColor = Color(0xFF00FF66),
        suggestionBg = Color(0xFF162516),
        suggestionText = Color(0xFF00FF66)
    )

    val AmberCrt = TerminalColors(
        background = Color(0xFF140D00),
        surface = Color(0xFF1E1400),
        primaryText = Color(0xFFFFB000),
        secondaryText = Color(0xFFB37B00),
        accent = Color(0xFFFFCC00),
        promptUser = Color(0xFFFFC000),
        promptHost = Color(0xFFFFD033),
        promptPath = Color(0xFFE69D00),
        promptSymbol = Color(0xFFFFCC00),
        commandText = Color(0xFFFFE066),
        successText = Color(0xFFFFCC00),
        errorText = Color(0xFFFF4500),
        warningText = Color(0xFFFF8C00),
        infoText = Color(0xFFFFD700),
        appLinkText = Color(0xFFFFD700),
        cursorColor = Color(0xFFFFB000),
        suggestionBg = Color(0xFF2A1C00),
        suggestionText = Color(0xFFFFCC00)
    )

    val Light = TerminalColors(
        background = Color(0xFFF8F9FA),
        surface = Color(0xFFE9ECEF),
        primaryText = Color(0xFF212529),
        secondaryText = Color(0xFF6C757D),
        accent = Color(0xFF0D6EFD),
        promptUser = Color(0xFF198754),
        promptHost = Color(0xFF0D6EFD),
        promptPath = Color(0xFF6F42C1),
        promptSymbol = Color(0xFFDC3545),
        commandText = Color(0xFF0A58CA),
        successText = Color(0xFF198754),
        errorText = Color(0xFFDC3545),
        warningText = Color(0xFFFD7E14),
        infoText = Color(0xFF0DCAF0),
        appLinkText = Color(0xFF0D6EFD),
        cursorColor = Color(0xFF212529),
        suggestionBg = Color(0xFFDEE2E6),
        suggestionText = Color(0xFF212529),
        isDark = false
    )

    fun forType(type: TerminalThemeType, context: Context?): TerminalColors {
        return when (type) {
            TerminalThemeType.DRACULA -> Dracula
            TerminalThemeType.SOLARIZED_DARK -> SolarizedDark
            TerminalThemeType.GRUVBOX -> Gruvbox
            TerminalThemeType.NORD -> Nord
            TerminalThemeType.MONOKAI -> Monokai
            TerminalThemeType.MATRIX_GREEN -> MatrixGreen
            TerminalThemeType.AMBER_CRT -> AmberCrt
            TerminalThemeType.LIGHT -> Light
            TerminalThemeType.MATERIAL_YOU -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
                    val dynDark = dynamicDarkColorScheme(context)
                    TerminalColors(
                        background = dynDark.background,
                        surface = dynDark.surface,
                        primaryText = dynDark.onBackground,
                        secondaryText = dynDark.outline,
                        accent = dynDark.primary,
                        promptUser = dynDark.tertiary,
                        promptHost = dynDark.secondary,
                        promptPath = dynDark.primary,
                        promptSymbol = dynDark.error,
                        commandText = dynDark.onSurface,
                        successText = dynDark.primary,
                        errorText = dynDark.error,
                        warningText = dynDark.tertiary,
                        infoText = dynDark.secondary,
                        appLinkText = dynDark.primary,
                        cursorColor = dynDark.primary,
                        suggestionBg = dynDark.surfaceVariant,
                        suggestionText = dynDark.onSurfaceVariant
                    )
                } else {
                    Dracula
                }
            }
        }
    }

    fun toM3ColorScheme(colors: TerminalColors): ColorScheme {
        return if (colors.isDark) {
            darkColorScheme(
                primary = colors.accent,
                onPrimary = colors.background,
                background = colors.background,
                onBackground = colors.primaryText,
                surface = colors.surface,
                onSurface = colors.primaryText,
                secondary = colors.infoText,
                onSecondary = colors.background,
                error = colors.errorText,
                onError = colors.background
            )
        } else {
            lightColorScheme(
                primary = colors.accent,
                onPrimary = Color.White,
                background = colors.background,
                onBackground = colors.primaryText,
                surface = colors.surface,
                onSurface = colors.primaryText,
                secondary = colors.infoText,
                onSecondary = Color.White,
                error = colors.errorText,
                onError = Color.White
            )
        }
    }
}
