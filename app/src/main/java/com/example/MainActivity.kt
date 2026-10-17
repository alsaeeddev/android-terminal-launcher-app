package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.drawer.GuiDrawerScreen
import com.example.presentation.drawer.GuiDrawerViewModel
import com.example.presentation.home.TerminalHomeScreen
import com.example.presentation.home.TerminalViewModel
import com.example.presentation.navigation.Routes
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.settings.SettingsViewModel
import com.example.ui.theme.ShellHomeTheme

class MainActivity : ComponentActivity() {

    private lateinit var terminalViewModel: TerminalViewModel
    private lateinit var guiDrawerViewModel: GuiDrawerViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as ShellHomeApplication).container

        // Instantiate ViewModels with application dependencies
        terminalViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TerminalViewModel(
                    executeCommandUseCase = container.executeCommandUseCase,
                    commandRegistry = container.commandRegistry,
                    appRepository = container.appRepository,
                    settingsRepository = container.settingsRepository,
                    productivityRepository = container.productivityRepository,
                    systemActionController = container.systemActionController
                ) as T
            }
        })[TerminalViewModel::class.java]

        guiDrawerViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GuiDrawerViewModel(container.appRepository) as T
            }
        })[GuiDrawerViewModel::class.java]

        settingsViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    appRepository = container.appRepository,
                    productivityRepository = container.productivityRepository,
                    systemActionController = container.systemActionController
                ) as T
            }
        })[SettingsViewModel::class.java]

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()
            val prefs = settingsState.preferences

            ShellHomeTheme(
                themeType = prefs.theme,
                fontSize = prefs.fontSize
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val navController = rememberNavController()

                    val startDestination = remember(prefs.onboardingCompleted, prefs.defaultMode) {
                        if (!prefs.onboardingCompleted) {
                            Routes.ONBOARDING
                        } else if (prefs.defaultMode == "gui") {
                            Routes.DRAWER
                        } else {
                            Routes.TERMINAL
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Routes.TERMINAL) {
                            // On Terminal, back button does nothing (launcher root)
                            BackHandler {
                                // Keep launcher alive
                            }
                            TerminalHomeScreen(
                                viewModel = terminalViewModel,
                                onNavigateToDrawer = {
                                    navController.navigate(Routes.DRAWER)
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Routes.SETTINGS)
                                }
                            )
                        }

                        composable(Routes.DRAWER) {
                            GuiDrawerScreen(
                                viewModel = guiDrawerViewModel,
                                onBackToTerminal = {
                                    navController.popBackStack(Routes.TERMINAL, inclusive = false)
                                }
                            )
                        }

                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Routes.ONBOARDING) {
                            OnboardingScreen(
                                settingsRepository = container.settingsRepository,
                                systemActionController = container.systemActionController,
                                onComplete = {
                                    navController.navigate(Routes.TERMINAL) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // HOME button re-entry: If user was in Settings or Drawer, pressing Home returns to Terminal
    }
}
