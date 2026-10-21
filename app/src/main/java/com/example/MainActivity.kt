package com.example

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DiagnosticSession
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.FixBeforeBuyTheme
import com.example.viewmodel.MainViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val settings by mainViewModel.settings.collectAsStateWithLifecycle()

            // Safe locale update
            val context = LocalContext.current
            LaunchedEffect(settings.language) {
                try {
                    val targetTag = when (settings.language) {
                        "ar" -> "ar"
                        "fr" -> "fr"
                        else -> "en"
                    }
                    val currentTag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        context.resources.configuration.locales.get(0)?.language ?: "en"
                    } else {
                        @Suppress("DEPRECATION")
                        context.resources.configuration.locale?.language ?: "en"
                    }
                    if (currentTag != targetTag) {
                        val locale = Locale.forLanguageTag(targetTag)
                        Locale.setDefault(locale)
                        val config = Configuration(context.resources.configuration)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            config.setLocales(LocaleList(locale))
                        } else {
                            @Suppress("DEPRECATION")
                            config.locale = locale
                        }
                        @Suppress("DEPRECATION")
                        context.resources.updateConfiguration(config, context.resources.displayMetrics)
                    }
                } catch (_: Exception) {
                    // Safe fallback
                }
            }

            val isDark = when (settings.appTheme.lowercase()) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val layoutDirection = if (settings.language == "ar") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
            CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
                FixBeforeBuyTheme(darkTheme = isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        FixBeforeBuyApp(viewModel = mainViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun FixBeforeBuyApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var previousScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val testStatus by viewModel.connectionTestStatus.collectAsStateWithLifecycle()
    val testLoading by viewModel.connectionTestLoading.collectAsStateWithLifecycle()

    val systemInDark = isSystemInDarkTheme()
    val isAppDark = when (settings.appTheme.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> systemInDark
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage ?: "")
            viewModel.clearError()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = if (currentScreen == Screen.CostCalculator) {
            previousScreen
        } else {
            Screen.Home
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            previousScreen = currentScreen
                            currentScreen = screen
                        },
                        icon = {
                            Icon(imageVector = screen.icon, contentDescription = screen.title)
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Home -> {
                    HomeScreen(
                        recentSessions = recentSessions,
                        isLoading = isLoading,
                        onStartDiagnosis = { category, desc, brand, model, age, warranty, uri, b64 ->
                            viewModel.startDiagnosis(
                                category = category,
                                description = desc,
                                brand = brand,
                                model = model,
                                ageYears = age,
                                warrantyStatus = warranty,
                                imageUri = uri,
                                imageBase64 = b64
                            )
                            previousScreen = Screen.Home
                            currentScreen = Screen.Diagnose
                        },
                        onResumeSession = { session ->
                            viewModel.resumeSession(session)
                            previousScreen = Screen.Home
                            currentScreen = Screen.Diagnose
                        },
                        onNavigateToCalculator = {
                            previousScreen = Screen.Home
                            currentScreen = Screen.CostCalculator
                        },
                        onNavigateToHistory = {
                            previousScreen = Screen.Home
                            currentScreen = Screen.History
                        },
                        onNavigateToSettings = {
                            previousScreen = Screen.Home
                            currentScreen = Screen.Settings
                        },
                        isDarkTheme = isAppDark,
                        onToggleTheme = {
                            viewModel.saveSettings(
                                settings.copy(
                                    appTheme = if (isAppDark) "light" else "dark"
                                )
                            )
                        }
                    )
                }

                Screen.Diagnose -> {
                    DiagnoseWizardScreen(
                        session = currentSession,
                        isLoading = isLoading,
                        onSendAnswer = { answer ->
                            viewModel.sendAnswer(answer)
                        },
                        onToggleStep = { stepNumber ->
                            viewModel.toggleRepairStep(stepNumber)
                        },
                        onNavigateToCalculator = {
                            previousScreen = Screen.Diagnose
                            currentScreen = Screen.CostCalculator
                        },
                        onStartOver = {
                            viewModel.clearCurrentSession()
                            currentScreen = Screen.Home
                        }
                    )
                }

                Screen.CostCalculator -> {
                    CostComparisonScreen(
                        initialCost = currentSession?.costComparison ?: com.example.model.CostComparison(),
                        onSaveCostComparison = { comparison ->
                            viewModel.updateCostComparison(comparison)
                        },
                        onNavigateBack = {
                            currentScreen = previousScreen
                        }
                    )
                }

                Screen.History -> {
                    HistoryScreen(
                        sessions = allSessions,
                        onResumeSession = { session ->
                            viewModel.resumeSession(session)
                            previousScreen = Screen.History
                            currentScreen = Screen.Diagnose
                        },
                        onDeleteSession = { id ->
                            viewModel.deleteSession(id)
                        },
                        onClearAll = {
                            viewModel.clearAllHistory()
                        }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        currentSettings = settings,
                        connectionTestStatus = testStatus,
                        connectionTestLoading = testLoading,
                        onSaveSettings = { updated ->
                            viewModel.saveSettings(updated)
                        },
                        onClearApiKey = {
                            viewModel.clearApiKey()
                        },
                        onTestConnection = {
                            viewModel.testConnection()
                        }
                    )
                }
            }
        }
    }
}
