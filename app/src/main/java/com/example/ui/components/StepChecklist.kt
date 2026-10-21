package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RepairPlan
import com.example.ui.theme.TealAccent

@Composable
fun StepChecklist(
    repairPlan: RepairPlan,
    onToggleStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSteps = repairPlan.steps.size
    val completedSteps = repairPlan.steps.count { it.isCompleted }
    val progressPct = if (totalSteps > 0) (completedSteps * 100 / totalSteps) else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
            .testTag("repair_checklist")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Repair Steps ($completedSteps / $totalSteps)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$progressPct%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TealAccent
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        repairPlan.steps.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleStep(step.stepNumber) }
                    .padding(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = step.isCompleted,
                    onCheckedChange = { onToggleStep(step.stepNumber) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = TealAccent,
                        checkmarkColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("step_checkbox_${step.stepNumber}")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    Text(
                        text = "${step.stepNumber}. ${step.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (step.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (step.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (step.description.isNotBlank()) {
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = if (step.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
