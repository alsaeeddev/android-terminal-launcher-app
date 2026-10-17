package com.example.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.TerminalThemeType
import com.example.core.ui.theme.TerminalThemes
import com.example.ui.theme.LocalTerminalColors
import com.example.ui.theme.MonospaceFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalTerminalColors.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shell Home Settings",
                    color = colors.primaryText,
                    fontFamily = MonospaceFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Default Launcher Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isDefaultLauncher) colors.surface else colors.accent.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isDefaultLauncher) "Default Launcher: ACTIVE" else "Default Launcher: NOT SET",
                            color = if (uiState.isDefaultLauncher) colors.successText else colors.warningText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Shell Home acts as your primary Android desktop.",
                            color = colors.secondaryText,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.openDefaultLauncherSettings(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) {
                        Text(if (uiState.isDefaultLauncher) "Configured" else "Set Default")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Section
            SettingsSectionHeader(title = "APPEARANCE")

            Text(
                text = "Color Theme",
                color = colors.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Theme chips with preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (theme in TerminalThemeType.values()) {
                    val isSelected = theme == uiState.preferences.theme
                    val themePalette = TerminalThemes.forType(theme, context)
                    Surface(
                        color = themePalette.background,
                        shape = RoundedCornerShape(8.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, colors.accent) else null,
                        modifier = Modifier
                            .clickable { viewModel.setTheme(theme) }
                            .padding(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(themePalette.promptUser, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = theme.displayName,
                                color = themePalette.primaryText,
                                fontSize = 12.sp,
                                fontFamily = MonospaceFontFamily
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font Size Slider
            Text(
                text = "Font Size: ${uiState.preferences.fontSize.toInt()} sp",
                color = colors.primaryText,
                fontSize = 14.sp
            )
            Slider(
                value = uiState.preferences.fontSize,
                onValueChange = { viewModel.setFontSize(it) },
                valueRange = 10f..22f,
                steps = 11,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wallpaper Dim Slider
            Text(
                text = "Wallpaper Dim: ${(uiState.preferences.wallpaperDim * 100).toInt()}%",
                color = colors.primaryText,
                fontSize = 14.sp
            )
            Slider(
                value = uiState.preferences.wallpaperDim,
                onValueChange = { viewModel.setWallpaperDim(it) },
                valueRange = 0.0f..1.0f,
                modifier = Modifier.fillMaxWidth()
            )

            // CRT Scanlines switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "CRT Scanline Filter", color = colors.primaryText, fontSize = 14.sp)
                    Text(text = "Subtle retro terminal scanlines effect", color = colors.secondaryText, fontSize = 12.sp)
                }
                Switch(
                    checked = uiState.preferences.crtScanlinesEnabled,
                    onCheckedChange = { viewModel.setCrtScanlines(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Behavior Section
          //  SettingsSectionHeader(title = "BEHAVIOR & LAUNCHER")

/*            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Default Launcher Mode", color = colors.primaryText, fontSize = 14.sp)
                    Text(text = "Open terminal or GUI app drawer on start", color = colors.secondaryText, fontSize = 12.sp)
                }
                Row {
                    OutlinedButton(
                        onClick = { viewModel.setDefaultMode("term") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (uiState.preferences.defaultMode == "term") colors.accent.copy(alpha = 0.2f) else Color.Transparent
                        )
                    ) {
                        Text("Terminal", color = colors.primaryText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = { viewModel.setDefaultMode("gui") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (uiState.preferences.defaultMode == "gui") colors.accent.copy(alpha = 0.2f) else Color.Transparent
                        )
                    ) {
                        Text("Drawer", color = colors.primaryText, fontSize = 12.sp)
                    }
                }
            }*/

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Kernel Boot Animation", color = colors.primaryText, fontSize = 14.sp)
                    Text(text = "Show retro startup sequence on launch", color = colors.secondaryText, fontSize = 12.sp)
                }
                Switch(
                    checked = uiState.preferences.bootAnimationEnabled,
                    onCheckedChange = { viewModel.setBootAnimation(it) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Auto-Launch Single Match", color = colors.primaryText, fontSize = 14.sp)
                    Text(text = "Launch app immediately when only one result matches", color = colors.secondaryText, fontSize = 12.sp)
                }
                Switch(
                    checked = uiState.preferences.autoLaunchSingleMatch,
                    onCheckedChange = { viewModel.setAutoLaunch(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Apps Customization Section
            SettingsSectionHeader(title = "APPS & CUSTOMIZATION")

            // Hidden Apps
            if (uiState.hiddenApps.isNotEmpty()) {
                Text(
                    text = "Hidden Apps (${uiState.hiddenApps.size}):",
                    color = colors.primaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                for (hidden in uiState.hiddenApps) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = hidden.label, color = colors.secondaryText, fontSize = 13.sp)
                        TextButton(onClick = { viewModel.unhideApp(hidden) }) {
                            Text("Unhide", color = colors.accent)
                        }
                    }
                }
            } else {
                Text(text = "No apps are hidden.", color = colors.secondaryText, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aliases
            if (uiState.aliases.isNotEmpty()) {
                Text(
                    text = "Configured Aliases (${uiState.aliases.size}):",
                    color = colors.primaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                for (alias in uiState.aliases) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${alias.name} → '${alias.command}'", color = colors.primaryText, fontFamily = MonospaceFontFamily, fontSize = 12.sp)
                        IconButton(onClick = { viewModel.removeAlias(alias.name) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = colors.errorText, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

/*
            Spacer(modifier = Modifier.height(24.dp))

            // Backup & Export Section
            SettingsSectionHeader(title = "BACKUP & RESTORE")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val json = viewModel.generateBackupJson()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(ClipData.newPlainText("Shell Home Backup", json))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Config JSON")
                }
            }
*/

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SettingsSectionHeader(title = "ABOUT SHELL HOME")
            Text(text = "Shell Home Launcher v1.0.0", color = colors.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "Developer: alsaeeddev.com", color = colors.secondaryText, fontFamily = MonospaceFontFamily, fontSize = 12.sp)
            Text(text = "Designed for power users, developers, and minimalist terminal enthusiasts.", color = colors.secondaryText, fontSize = 12.sp)
            Text(text = "Zero tracking • 100% On-device privacy • 16 KB Page Compatible", color = colors.successText, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val colors = LocalTerminalColors.current
    Text(
        text = title,
        color = colors.accent,
        fontFamily = MonospaceFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
