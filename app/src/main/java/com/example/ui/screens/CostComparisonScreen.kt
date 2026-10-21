package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CostComparison
import com.example.ui.theme.HazardRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostComparisonScreen(
    initialCost: CostComparison = CostComparison(),
    onSaveCostComparison: (CostComparison) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var partsCostText by remember { mutableStateOf(if (initialCost.partsCost > 0) initialCost.partsCost.toString() else "") }
    var laborCostText by remember { mutableStateOf(if (initialCost.laborCost > 0) initialCost.laborCost.toString() else "") }
    var extraRepairCostText by remember { mutableStateOf(if (initialCost.extraRepairCost > 0) initialCost.extraRepairCost.toString() else "") }

    var replacementPriceText by remember { mutableStateOf(if (initialCost.replacementCost > 0) initialCost.replacementCost.toString() else "") }
    var extraPurchaseText by remember { mutableStateOf(if (initialCost.extraPurchaseCost > 0) initialCost.extraPurchaseCost.toString() else "") }

    var repairLifeText by remember { mutableStateOf(initialCost.repairUsefulLifeYears.toString()) }
    var replaceLifeText by remember { mutableStateOf(initialCost.replaceUsefulLifeYears.toString()) }

    var savedSnackbarVisible by remember { mutableStateOf(false) }

    // Parse values safely
    val parts = partsCostText.toDoubleOrNull() ?: 0.0
    val labor = laborCostText.toDoubleOrNull() ?: 0.0
    val extraRepair = extraRepairCostText.toDoubleOrNull() ?: 0.0

    val replacePrice = replacementPriceText.toDoubleOrNull() ?: 0.0
    val extraPurchase = extraPurchaseText.toDoubleOrNull() ?: 0.0

    val repairLife = (repairLifeText.toDoubleOrNull() ?: 2.0).coerceAtLeast(0.5)
    val replaceLife = (replaceLifeText.toDoubleOrNull() ?: 5.0).coerceAtLeast(0.5)

    val currentComparison = remember(parts, labor, extraRepair, replacePrice, extraPurchase, repairLife, replaceLife) {
        CostComparison(
            partsCost = parts,
            laborCost = labor,
            extraRepairCost = extraRepair,
            replacementCost = replacePrice,
            extraPurchaseCost = extraPurchase,
            repairUsefulLifeYears = repairLife,
            replaceUsefulLifeYears = replaceLife
        )
    }

    val totalRepair = currentComparison.totalRepairCost
    val totalReplace = currentComparison.totalReplacementCost
    val immediateSavings = currentComparison.immediateSavings
    val isRepairFavorable = currentComparison.isRepairEconomical

    val resultBgColor by animateColorAsState(
        targetValue = if (isRepairFavorable) SuccessGreen.copy(alpha = 0.12f) else HazardRed.copy(alpha = 0.12f),
        label = "resultBg"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cost_calculator_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("cost_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            // Header Description
            item {
                Text(
                    text = stringResource(R.string.cost_calc_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            // Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("cost_result_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Verdict Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(resultBgColor)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRepairFavorable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isRepairFavorable) SuccessGreen else HazardRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isRepairFavorable) stringResource(R.string.repair_recommended) else stringResource(R.string.replace_recommended),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRepairFavorable) SuccessGreen else HazardRed
                                        )
                                    )
                                    Text(
                                        text = if (isRepairFavorable) {
                                            "You save $${String.format(Locale.US, "%.2f", immediateSavings)} immediately by repairing."
                                        } else {
                                            "Buying new is $${String.format(Locale.US, "%.2f", -immediateSavings)} cheaper than total repair cost."
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        // Side by side totals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(stringResource(R.string.total_repair_cost), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$${String.format(Locale.US, "%.2f", totalRepair)}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRepairFavorable) TealAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stringResource(R.string.total_replacement_cost), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$${String.format(Locale.US, "%.2f", totalReplace)}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Annualized Life Comparison
                        Text(
                            text = "Estimated Useful-Life Cost Analysis:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Repair: $${String.format(Locale.US, "%.2f", currentComparison.annualCostRepair)}/yr ($repairLife yrs)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "New: $${String.format(Locale.US, "%.2f", currentComparison.annualCostReplace)}/yr ($replaceLife yrs)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Repair Expenses Input Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("1. Repair Cost Factors", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = partsCostText,
                            onValueChange = { partsCostText = it },
                            label = { Text(stringResource(R.string.parts_cost_label)) },
                            placeholder = { Text("e.g. 25.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("parts_cost_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = laborCostText,
                            onValueChange = { laborCostText = it },
                            label = { Text(stringResource(R.string.labor_cost_label)) },
                            placeholder = { Text("e.g. 0.00 (DIY) or 40.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("labor_cost_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = extraRepairCostText,
                            onValueChange = { extraRepairCostText = it },
                            label = { Text(stringResource(R.string.extra_repair_cost_label)) },
                            placeholder = { Text("e.g. Shipping / Adhesive / Consumables") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = repairLifeText,
                            onValueChange = { repairLifeText = it },
                            label = { Text(stringResource(R.string.useful_life_repair_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Replacement Cost Input Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("2. Replacement Product Factors", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = replacementPriceText,
                            onValueChange = { replacementPriceText = it },
                            label = { Text(stringResource(R.string.replacement_price_label)) },
                            placeholder = { Text("e.g. 199.99") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("replacement_price_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = extraPurchaseText,
                            onValueChange = { extraPurchaseText = it },
                            label = { Text(stringResource(R.string.extra_purchase_cost_label)) },
                            placeholder = { Text("e.g. Taxes / Shipping / Setup") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = replaceLifeText,
                            onValueChange = { replaceLifeText = it },
                            label = { Text(stringResource(R.string.useful_life_replace_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Save to Session Action
            item {
                Button(
                    onClick = {
                        onSaveCostComparison(currentComparison)
                        savedSnackbarVisible = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_cost_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.save_to_session), fontWeight = FontWeight.Bold)
                }

                if (savedSnackbarVisible) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ Cost comparison saved to active diagnosis session!",
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Disclaimer
            item {
                Text(
                    text = stringResource(R.string.cost_disclaimer),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
    }
}
