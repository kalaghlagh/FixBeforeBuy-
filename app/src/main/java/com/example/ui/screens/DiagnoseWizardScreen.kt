package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DiagnosticMessage
import com.example.model.DiagnosticSession
import com.example.model.RepairPlan
import com.example.ui.components.ConfidenceIndicator
import com.example.ui.components.SafetyBanner
import com.example.ui.components.StepChecklist
import com.example.ui.theme.HazardRed
import com.example.ui.theme.TealAccent
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnoseWizardScreen(
    session: DiagnosticSession?,
    isLoading: Boolean,
    onSendAnswer: (String) -> Unit,
    onToggleStep: (Int) -> Unit,
    onNavigateToCalculator: () -> Unit,
    onStartOver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textAnswer by remember { mutableStateOf("") }
    var showStartOverDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(session?.messages?.size) {
        if (session != null && session.messages.isNotEmpty()) {
            listState.animateScrollToItem(session.messages.size)
        }
    }

    if (session == null) {
        // Empty placeholder state
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.BuildCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No Active Diagnosis Session",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Go to the Home tab to start a new diagnosis with problem details or photos.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    if (showStartOverDialog) {
        AlertDialog(
            onDismissRequest = { showStartOverDialog = false },
            title = { Text(stringResource(R.string.start_over)) },
            text = { Text(stringResource(R.string.confirm_start_over)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStartOverDialog = false
                        onStartOver()
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartOverDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(session.category.titleRes),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showStartOverDialog = true },
                        modifier = Modifier.testTag("start_over_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Start Over")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Interactive Bottom Answer Bar
            if (!session.isCompleted && !session.technicianRecommended) {
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick-Answer chips if available in latest message
                        val latestMsg = session.messages.lastOrNull()
                        if (latestMsg?.quickOptions?.isNotEmpty() == true) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(latestMsg.quickOptions) { option ->
                                    ElevatedFilterChip(
                                        selected = false,
                                        onClick = {
                                            onSendAnswer(option)
                                        },
                                        label = { Text(option, fontWeight = FontWeight.SemiBold) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("quick_option_$option")
                                    )
                                }
                            }
                        }

                        // Text input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = textAnswer,
                                onValueChange = { textAnswer = it },
                                placeholder = { Text(stringResource(R.string.type_answer_placeholder), fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("wizard_answer_input"),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )
                            IconButton(
                                onClick = {
                                    if (textAnswer.isNotBlank()) {
                                        val answer = textAnswer
                                        textAnswer = ""
                                        onSendAnswer(answer)
                                    }
                                },
                                enabled = textAnswer.isNotBlank() && !isLoading,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (textAnswer.isNotBlank()) TealAccent else MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("wizard_send_btn")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send Answer",
                                        tint = if (textAnswer.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
        ) {
            // Safety or Technician Banner
            if (session.technicianRecommended) {
                item {
                    SafetyBanner(
                        title = stringResource(R.string.technician_warning),
                        message = session.technicianReason ?: stringResource(R.string.technician_explanation),
                        isCritical = true
                    )
                }
            } else if (session.safetyWarnings.isNotEmpty()) {
                item {
                    SafetyBanner(
                        title = "Required Safety Precautions",
                        message = session.safetyWarnings.first(),
                        isCritical = false
                    )
                }
            }

            // Confidence Indicator Card
            item {
                ConfidenceIndicator(confidence = session.confidence)
            }

            // Possible Causes Section
            if (session.possibleCauses.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("possible_causes_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.possible_causes),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            session.possibleCauses.forEach { cause ->
                                val badgeColor = when (cause.likelihood.lowercase()) {
                                    "high" -> HazardRed
                                    "medium" -> WarningAmber
                                    else -> Color(0xFF64748B)
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cause.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Surface(
                                            color = badgeColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = cause.likelihood.uppercase(),
                                                color = badgeColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (cause.reason.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = cause.reason,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Safe Test Inspection Card
            if (session.safeTest != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("safe_test_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = session.safeTest.title.ifBlank { stringResource(R.string.safe_inspection) },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    color = TealAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Risk: ${session.safeTest.riskLevel.uppercase()}",
                                        color = TealAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            session.safeTest.instructions.forEachIndexed { i, instruction ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("${i + 1}.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(instruction, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                }
                            }

                            if (session.safeTest.expectedObservations.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Expected observation: ${session.safeTest.expectedObservations.joinToString(", ")}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }

            // Step Checklist & Repair Guide
            if (session.repairPlan != null && !session.technicianRecommended) {
                item {
                    StepChecklist(
                        repairPlan = session.repairPlan,
                        onToggleStep = onToggleStep
                    )
                }

                // Tools & Parts Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Tools & Parts Required", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                            if (session.repairPlan.requiredTools.isNotEmpty()) {
                                Text(stringResource(R.string.required_tools), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(session.repairPlan.requiredTools) { tool ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(tool, fontSize = 11.sp) },
                                            icon = { Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                            }

                            if (session.repairPlan.requiredParts.isNotEmpty()) {
                                Text(stringResource(R.string.required_parts), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(session.repairPlan.requiredParts) { part ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(part, fontSize = 11.sp) },
                                            icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action to Open Cost Comparison
            item {
                Button(
                    onClick = onNavigateToCalculator,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("compare_costs_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Compare Fix vs Replace Cost",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Chat Messages History
            item {
                Text(
                    text = "Diagnostic Conversation",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(session.messages) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: DiagnosticMessage) {
    val isUser = message.sender.lowercase() == "user"
    val isSystem = message.sender.lowercase() == "system"

    val alignment = when {
        isUser -> Alignment.End
        else -> Alignment.Start
    }

    val bubbleBg = when {
        isUser -> TealAccent
        isSystem -> Color(0xFFFEF2F2)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isUser -> Color.White
        isSystem -> Color(0xFF991B1B)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 14.dp
                    )
                )
                .background(bubbleBg)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = textColor,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
