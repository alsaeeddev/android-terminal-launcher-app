package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.core.ui.theme.TerminalColors
import com.example.core.ui.theme.TerminalThemeType
import com.example.core.ui.theme.TerminalThemes

val LocalTerminalColors = staticCompositionLocalOf<TerminalColors> {
    TerminalThemes.Dracula
}

@Composable
fun ShellHomeTheme(
    themeType: TerminalThemeType = TerminalThemeType.DRACULA,
    fontSize: Float = 14f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val terminalColors = TerminalThemes.forType(themeType, context)
    val colorScheme = TerminalThemes.toM3ColorScheme(terminalColors)
    val typography = createTerminalTypography(baseSizeSp = fontSize)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                val insetsController = WindowCompat.getInsetsController(window, view)
                // If dark theme, light status bars should be false (white icons)
                insetsController.isAppearanceLightStatusBars = !terminalColors.isDark
                insetsController.isAppearanceLightNavigationBars = !terminalColors.isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalTerminalColors provides terminalColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
