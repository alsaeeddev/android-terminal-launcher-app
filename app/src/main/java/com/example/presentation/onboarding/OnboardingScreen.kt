package com.example.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.TerminalThemeType
import com.example.data.system.SystemActionController
import com.example.domain.repository.SettingsRepository
import com.example.ui.theme.LocalTerminalColors
import com.example.ui.theme.MonospaceFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    settingsRepository: SettingsRepository,
    systemActionController: SystemActionController,
    onComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val colors = LocalTerminalColors.current
    val context = LocalContext.current

    val finishOnboarding = {
        CoroutineScope(Dispatchers.IO).launch {
            settingsRepository.setOnboardingCompleted(true)
        }
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (step < 3) {
                    TextButton(onClick = finishOnboarding, modifier = Modifier.testTag("onboarding_skip_button")) {
                        Text(text = "Skip", color = colors.secondaryText, fontFamily = MonospaceFontFamily)
                    }
                }
            }

            // Step Content
            AnimatedContent(targetState = step, label = "OnboardingStep") { currentStep ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (currentStep) {
                        0 -> {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Welcome to Shell Home",
                                color = colors.primaryText,
                                fontFamily = MonospaceFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Control your phone with a blazing fast command-line terminal interface. Type app names or commands to navigate without distraction.",
                                color = colors.secondaryText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                        1 -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.successText,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Default Launcher",
                                color = colors.primaryText,
                                fontFamily = MonospaceFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Set Shell Home as your default Android home app so pressing Home brings you directly to your command prompt.",
                                color = colors.secondaryText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    val intent = systemActionController.openDefaultLauncherSettings()
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Text("Choose Default Home App")
                            }
                        }
                        2 -> {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = colors.infoText,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Optional Permissions",
                                color = colors.primaryText,
                                fontFamily = MonospaceFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Double-tap to lock requires Accessibility. Notification counts require Notification Listener access. Both are completely optional and private.",
                                color = colors.secondaryText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedButton(onClick = { systemActionController.openAccessibilitySettings() }) {
                                Text("Enable Double-Tap Lock", color = colors.primaryText)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { systemActionController.openNotificationListenerSettings() }) {
                                Text("Enable Notification Badges", color = colors.primaryText)
                            }
                        }
                        3 -> {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Choose Your Theme",
                                color = colors.primaryText,
                                fontFamily = MonospaceFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Pick your preferred developer color scheme. You can change this anytime with 'theme set <name>' in terminal.",
                                color = colors.secondaryText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { CoroutineScope(Dispatchers.IO).launch { settingsRepository.setTheme(TerminalThemeType.DRACULA) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
                                ) {
                                    Text("Dracula", color = colors.primaryText)
                                }
                                Button(
                                    onClick = { CoroutineScope(Dispatchers.IO).launch { settingsRepository.setTheme(TerminalThemeType.MATRIX_GREEN) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
                                ) {
                                    Text("Matrix", color = colors.primaryText)
                                }
                                Button(
                                    onClick = { CoroutineScope(Dispatchers.IO).launch { settingsRepository.setTheme(TerminalThemeType.NORD) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
                                ) {
                                    Text("Nord", color = colors.primaryText)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) {
                        Text("Back", color = colors.secondaryText, fontFamily = MonospaceFontFamily)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) step++ else finishOnboarding()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.testTag("onboarding_next_button")
                ) {
                    Text(if (step < 3) "Next" else "Get Started")
                }
            }
        }
    }
}
