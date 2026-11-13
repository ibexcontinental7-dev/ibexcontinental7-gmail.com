package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.OfficialTakafulProduct
import com.example.domain.NeedAnalysisCalculator
import com.example.domain.NeedAnalysisInput
import com.example.domain.NeedGoal
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeedAnalysisSection(
    strings: UiStrings,
    language: AppLanguage,
    onDraftAiForProduct: (OfficialTakafulProduct) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var monthlyBudgetText by remember { mutableStateOf("50000") }
    var horizonYears by remember { mutableIntStateOf(10) }
    var dependentsCount by remember { mutableIntStateOf(2) }
    var selectedGoal by remember { mutableStateOf(NeedGoal.FAMILY_PROTECTION) }

    val parsedMonthly = monthlyBudgetText.filter { it.isDigit() }.toLongOrNull() ?: 50_000L
    val result = remember(parsedMonthly, horizonYears, dependentsCount, selectedGoal) {
        NeedAnalysisCalculator.calculate(
            NeedAnalysisInput(
                monthlySavingsPkr = parsedMonthly,
                horizonYears = horizonYears,
                dependentsCount = dependentsCount,
                goal = selectedGoal
            )
        )
    }

    val pkrFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("need_analysis_calculator_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Gold500.copy(alpha = 0.65f))
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Navy900,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Calculate,
                            contentDescription = null,
                            tint = Gold400,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.needCalcTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = strings.needCalcSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = strings.needCalcTitle
                    )
                }
            }

            if (expanded) {
                OutlinedTextField(
                    value = monthlyBudgetText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }
                        if (digits.length <= 8) monthlyBudgetText = digits
                    },
                    label = { Text(strings.needCalcMonthlyLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("need_calc_monthly_input")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(25_000L, 50_000L, 100_000L, 200_000L).forEach { preset ->
                        FilterChip(
                            selected = parsedMonthly == preset,
                            onClick = { monthlyBudgetText = preset.toString() },
                            label = { Text("PKR ${pkrFormat.format(preset)}") }
                        )
                    }
                }

                Text(text = strings.needCalcYearsLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { yrs ->
                        FilterChip(
                            selected = horizonYears == yrs,
                            onClick = { horizonYears = yrs },
                            label = {
                                Text(if (language == AppLanguage.URDU) "$yrs سال" else "$yrs yrs")
                            }
                        )
                    }
                }

                Text(text = strings.needCalcDependentsLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 1, 2, 3, 4).forEach { dep ->
                        FilterChip(
                            selected = dependentsCount == dep,
                            onClick = { dependentsCount = dep },
                            label = { Text(if (dep == 4) "4+" else "$dep") }
                        )
                    }
                }

                Text(text = strings.needCalcGoalLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NeedGoal.entries.forEach { goal ->
                        FilterChip(
                            selected = selectedGoal == goal,
                            onClick = { selectedGoal = goal },
                            label = { Text(if (language == AppLanguage.URDU) goal.labelUr else goal.labelEn) }
                        )
                    }
                }

                HorizontalDivider()

                Surface(
                    color = Navy100.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val primaryName = if (language == AppLanguage.URDU) {
                            "${result.primaryProduct.nameUr} (${result.primaryProduct.nameEn})"
                        } else {
                            "${result.primaryProduct.nameEn} (${result.primaryProduct.nameUr})"
                        }
                        val secondaryName = if (language == AppLanguage.URDU) {
                            "${result.secondaryProduct.nameUr} (${result.secondaryProduct.nameEn})"
                        } else {
                            "${result.secondaryProduct.nameEn} (${result.secondaryProduct.nameUr})"
                        }

                        Text(
                            text = "${strings.needCalcPrimaryPlan}: $primaryName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Navy900
                        )
                        Text(
                            text = "${strings.needCalcSecondaryPlan}: $secondaryName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(2.dp))
                        MetricSummaryRow(
                            label = strings.needCalcAnnualTotal,
                            value = "PKR ${pkrFormat.format(result.annualContributionPkr)}"
                        )
                        MetricSummaryRow(
                            label = strings.needCalcHorizonTotal,
                            value = "PKR ${pkrFormat.format(result.cumulativeHorizonContributionPkr)}"
                        )
                        MetricSummaryRow(
                            label = strings.needCalcBenchmarkCover,
                            value = "PKR ${pkrFormat.format(result.planningProtectionBenchmarkPkr)}"
                        )

                        Text(
                            text = if (language == AppLanguage.URDU) result.rationaleUr else result.rationaleEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    color = AmberSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.needCalcDisclaimer,
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberWarning,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Button(
                    onClick = { onDraftAiForProduct(result.primaryProduct) },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("need_calc_draft_ai_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Gold400, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${strings.draftAiPitchForPlan} • ${result.primaryProduct.nameEn}")
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Navy900
        )
    }
}
