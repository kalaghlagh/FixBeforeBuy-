package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AIProvider
import com.example.model.AISettings
import com.example.ui.theme.HazardRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSettings: AISettings,
    connectionTestStatus: String?,
    connectionTestLoading: Boolean,
    onSaveSettings: (AISettings) -> Unit,
    onClearApiKey: () -> Unit,
    onTestConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var provider by remember(currentSettings) { mutableStateOf(currentSettings.provider) }
    var apiKey by remember(currentSettings) { mutableStateOf(currentSettings.apiKey) }
    var modelId by remember(currentSettings) { mutableStateOf(currentSettings.modelId) }
    var customEndpoint by remember(currentSettings) { mutableStateOf(currentSettings.customEndpoint) }
    var appTheme by remember { mutableStateOf(currentSettings.appTheme) }
    var language by remember { mutableStateOf(currentSettings.language) }

    LaunchedEffect(currentSettings.appTheme) {
        appTheme = currentSettings.appTheme
    }
    LaunchedEffect(currentSettings.language) {
        language = currentSettings.language
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var showClearKeyDialog by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf(false) }

    if (showClearKeyDialog) {
        AlertDialog(
            onDismissRequest = { showClearKeyDialog = false },
            title = { Text(stringResource(R.string.clear_key)) },
            text = { Text(stringResource(R.string.confirm_clear_key)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        apiKey = ""
                        onClearApiKey()
                        showClearKeyDialog = false
                    }
                ) {
                    Text(stringResource(R.string.clear_key), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearKeyDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // General Appearance & Language
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(stringResource(R.string.general_settings), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        // Language Segmented Buttons
                        Text(stringResource(R.string.language_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("en" to "English", "ar" to "العربية", "fr" to "Français").forEach { (code, label) ->
                                val isSelected = language.equals(code, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        language = code
                                        val updated = currentSettings.copy(
                                            provider = provider,
                                            apiKey = apiKey.trim(),
                                            modelId = modelId.trim(),
                                            customEndpoint = customEndpoint.trim(),
                                            appTheme = appTheme,
                                            language = code
                                        )
                                        onSaveSettings(updated)
                                    },
                                    label = {
                                        Text(
                                            label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("lang_$code")
                                )
                            }
                        }

                        // Theme Selection Buttons
                        Text(stringResource(R.string.theme_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("system", R.string.theme_system, Icons.Default.BrightnessAuto),
                                Triple("light", R.string.theme_light, Icons.Default.LightMode),
                                Triple("dark", R.string.theme_dark, Icons.Default.DarkMode)
                            ).forEach { (mode, strRes, icon) ->
                                val isSelected = appTheme.equals(mode, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        appTheme = mode
                                        val updated = currentSettings.copy(
                                            provider = provider,
                                            apiKey = apiKey.trim(),
                                            modelId = modelId.trim(),
                                            customEndpoint = customEndpoint.trim(),
                                            appTheme = mode,
                                            language = language
                                        )
                                        onSaveSettings(updated)
                                    },
                                    label = {
                                        Text(
                                            stringResource(strRes),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("theme_$mode")
                                )
                            }
                        }
                    }
                }
            }

            // AI Provider Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_config_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.ai_configuration), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        // Provider Switch
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AIProvider.entries.forEach { p ->
                                val isSelected = provider == p
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        provider = p
                                        if (p == AIProvider.GEMINI && modelId == "gpt-4o-mini") {
                                            modelId = "gemini-2.5-flash"
                                        } else if (p == AIProvider.OPENAI && modelId.startsWith("gemini")) {
                                            modelId = "gpt-4o-mini"
                                        }
                                    },
                                    label = { Text(p.displayName, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("provider_${p.name}")
                                )
                            }
                        }

                        // API Key Field with visibility toggle
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text(stringResource(R.string.api_key_label)) },
                            placeholder = { Text(stringResource(R.string.api_key_placeholder)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                    if (apiKey.isNotBlank()) {
                                        IconButton(onClick = { showClearKeyDialog = true }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear API Key", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Model Identifier
                        OutlinedTextField(
                            value = modelId,
                            onValueChange = { modelId = it },
                            label = { Text(stringResource(R.string.model_id_label)) },
                            modifier = Modifier.fillMaxWidth().testTag("model_id_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Custom Endpoint
                        OutlinedTextField(
                            value = customEndpoint,
                            onValueChange = { customEndpoint = it },
                            label = { Text(stringResource(R.string.custom_endpoint_label)) },
                            placeholder = { Text(if (provider == AIProvider.GEMINI) "https://generativelanguage.googleapis.com/v1beta" else "https://api.openai.com/v1/chat/completions") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_endpoint_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Test Connection Button
                        OutlinedButton(
                            onClick = onTestConnection,
                            enabled = !connectionTestLoading,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("test_connection_btn")
                        ) {
                            if (connectionTestLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.testing_connection))
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.test_connection))
                            }
                        }

                        // Connection Test Status
                        if (connectionTestStatus != null) {
                            val isSuccess = connectionTestStatus.startsWith("SUCCESS")
                            Surface(
                                color = if (isSuccess) SuccessGreen.copy(alpha = 0.12f) else HazardRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("connection_status_badge")
                            ) {
                                Text(
                                    text = connectionTestStatus,
                                    color = if (isSuccess) SuccessGreen else HazardRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Save Settings Action
                        Button(
                            onClick = {
                                val updated = AISettings(
                                    provider = provider,
                                    apiKey = apiKey.trim(),
                                    modelId = modelId.trim(),
                                    customEndpoint = customEndpoint.trim(),
                                    appTheme = appTheme,
                                    language = language
                                )
                                onSaveSettings(updated)
                                saveSuccessMessage = true
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.save_settings), fontWeight = FontWeight.Bold)
                        }

                        if (saveSuccessMessage) {
                            Text(
                                text = "✓ Settings saved successfully!",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Security & Architecture Notice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Client Security Architecture", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            text = stringResource(R.string.security_notice),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.app_version),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
