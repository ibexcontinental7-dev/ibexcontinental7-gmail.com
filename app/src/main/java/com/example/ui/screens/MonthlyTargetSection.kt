package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.GmMonthlyTarget
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun MonthlyTargetSection(
    strings: UiStrings,
    language: AppLanguage,
    metrics: DashboardMetrics,
    targetState: UiState<GmMonthlyTarget>,
    onSaveTargets: (salesTarget: Long, outreachTarget: Long, referralTarget: Long, month: String) -> Unit
) {
    val activeTarget = (targetState as? UiState.Success)?.data ?: GmMonthlyTarget(
        targetMonth = MainViewModel.currentYearMonth()
    )
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_targets_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Navy900,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Gold400,
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.monthlyTargetsTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = activeTarget.targetMonth.ifBlank { MainViewModel.currentYearMonth() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("edit_monthly_targets_button")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.editTargetsButton, style = MaterialTheme.typography.labelMedium)
                }
            }

            TargetProgressBarRow(
                label = strings.metricCompletedSales,
                current = metrics.completedSales,
                target = activeTarget.salesTarget.toInt().coerceAtLeast(1),
                barColor = EmeraldSuccess
            )
            TargetProgressBarRow(
                label = if (language == AppLanguage.URDU) "فعال رابطے (آؤٹ ریچ)" else "Active Outreach Pipeline",
                current = metrics.activeOutreachCount,
                target = activeTarget.outreachTarget.toInt().coerceAtLeast(1),
                barColor = Navy900
            )
            TargetProgressBarRow(
                label = strings.warmReferralsLabel,
                current = metrics.warmReferrals,
                target = activeTarget.referralTarget.toInt().coerceAtLeast(1),
                barColor = InfoBlue
            )
        }
    }

    if (showEditDialog) {
        EditMonthlyTargetsDialog(
            strings = strings,
            initial = activeTarget,
            onDismiss = { showEditDialog = false },
            onSave = { sales, outreach, referrals ->
                onSaveTargets(
                    sales,
                    outreach,
                    referrals,
                    activeTarget.targetMonth.ifBlank { MainViewModel.currentYearMonth() }
                )
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun TargetProgressBarRow(
    label: String,
    current: Int,
    target: Int,
    barColor: Color
) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$current / $target",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = barColor
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            color = barColor,
            trackColor = Navy100,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun EditMonthlyTargetsDialog(
    strings: UiStrings,
    initial: GmMonthlyTarget,
    onDismiss: () -> Unit,
    onSave: (salesTarget: Long, outreachTarget: Long, referralTarget: Long) -> Unit
) {
    var salesText by remember { mutableStateOf(initial.salesTarget.toString()) }
    var outreachText by remember { mutableStateOf(initial.outreachTarget.toString()) }
    var referralsText by remember { mutableStateOf(initial.referralTarget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.monthlyTargetsTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = salesText,
                    onValueChange = { if (it.length <= 5) salesText = it.filter(Char::isDigit) },
                    label = { Text(strings.targetSalesLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = outreachText,
                    onValueChange = { if (it.length <= 5) outreachText = it.filter(Char::isDigit) },
                    label = { Text(strings.targetOutreachLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = referralsText,
                    onValueChange = { if (it.length <= 5) referralsText = it.filter(Char::isDigit) },
                    label = { Text(strings.targetReferralsLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = (salesText.toLongOrNull() ?: 5L).coerceIn(1L, 10_000L)
                    val o = (outreachText.toLongOrNull() ?: 25L).coerceIn(1L, 10_000L)
                    val r = (referralsText.toLongOrNull() ?: 10L).coerceIn(1L, 10_000L)
                    onSave(s, o, r)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite)
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
