package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GmMonthlyTarget
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.OwnerProfile
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoSurface
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun DashboardScreen(
    strings: UiStrings,
    language: AppLanguage,
    metrics: DashboardMetrics,
    leadsState: UiState<List<Lead>>,
    monthlyTargetState: UiState<GmMonthlyTarget> = UiState.Loading,
    onSaveMonthlyTarget: (sales: Long, outreach: Long, referrals: Long, month: String) -> Unit = { _, _, _, _ -> },
    onNavigateToLeads: (LeadStatus?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStudio: (Lead?) -> Unit,
    onNavigateToFollowUps: () -> Unit,
    onNavigateToProducts: () -> Unit
) {
    val allLeads = (leadsState as? UiState.Success)?.data.orEmpty()
    val dueLeads = allLeads.filter { MainViewModel.isFollowUpDue(it) }.take(5)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 980.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ExecutiveHeroCard(strings = strings)
            }

            item {
                Text(
                    text = strings.executiveBriefTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                MetricsGrid(
                    strings = strings,
                    metrics = metrics,
                    onMetricClick = { targetStatus ->
                        if (targetStatus == LeadStatus.FOLLOW_UP) {
                            onNavigateToFollowUps()
                        } else {
                            onNavigateToLeads(targetStatus)
                        }
                    }
                )
            }

            item {
                MonthlyTargetSection(
                    strings = strings,
                    language = language,
                    metrics = metrics,
                    targetState = monthlyTargetState,
                    onSaveTargets = onSaveMonthlyTarget
                )
            }

            item {
                Text(
                    text = strings.quickActionsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("dashboard_search_cta"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = Gold400, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.navSearch, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    FilledTonalButton(
                        onClick = { onNavigateToStudio(null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("dashboard_studio_cta"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.navStudio, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onNavigateToLeads(null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dashboard_leads_cta"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.navLeads, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    FilledTonalButton(
                        onClick = onNavigateToProducts,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dashboard_products_cta"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.navProducts, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.dueFollowUpsTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        color = if (metrics.followUpsDue > 0) AmberSurface else EmeraldSurface,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { onNavigateToFollowUps() }
                    ) {
                        Text(
                            text = "${metrics.followUpsDue}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (metrics.followUpsDue > 0) AmberWarning else EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            when (leadsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = leadsState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (dueLeads.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = strings.noFollowUpsDue,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(dueLeads, key = { it.id }) { lead ->
                            DueLeadPreviewCard(
                                lead = lead,
                                language = language,
                                onOpenFollowUps = onNavigateToFollowUps,
                                onDraftAi = { onNavigateToStudio(lead) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutiveHeroCard(strings: UiStrings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner_1794594951006),
                contentDescription = "Islamabad corporate skyline",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Navy950.copy(alpha = 0.92f),
                                Navy900.copy(alpha = 0.78f),
                                Navy800.copy(alpha = 0.58f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.takaful_launcher_icon_1794594951045),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = OwnerProfile.COMPANY_EN.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Gold400
                        )
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Column {
                    Text(
                        text = strings.gmSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite.copy(alpha = 0.92f)
                    )
                    Text(
                        text = "WhatsApp: ${OwnerProfile.WHATSAPP_NUMBER}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gold400
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsGrid(
    strings: UiStrings,
    metrics: DashboardMetrics,
    onMetricClick: (LeadStatus?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                title = strings.metricTotalLeads,
                count = metrics.totalLeads,
                icon = Icons.Filled.Groups,
                accentColor = Navy900,
                badgeBg = Navy100,
                modifier = Modifier.weight(1f).testTag("metric_total_leads"),
                onClick = { onMetricClick(null) }
            )
            MetricTile(
                title = strings.metricNewProspects,
                count = metrics.newProspects,
                icon = Icons.Filled.PersonAdd,
                accentColor = InfoBlue,
                badgeBg = InfoSurface,
                modifier = Modifier.weight(1f).testTag("metric_new_prospects"),
                onClick = { onMetricClick(LeadStatus.NEW) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                title = strings.metricContacted,
                count = metrics.contactedClients,
                icon = Icons.Filled.MarkEmailRead,
                accentColor = Navy700,
                badgeBg = Navy100,
                modifier = Modifier.weight(1f).testTag("metric_contacted"),
                onClick = { onMetricClick(LeadStatus.CONTACTED) }
            )
            MetricTile(
                title = strings.metricInterested,
                count = metrics.interestedClients,
                icon = Icons.Filled.Star,
                accentColor = Color(0xFF9F7A16),
                badgeBg = Color(0xFFFDF5DD),
                modifier = Modifier.weight(1f).testTag("metric_interested"),
                onClick = { onMetricClick(LeadStatus.INTERESTED) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                title = strings.metricFollowUpsDue,
                count = metrics.followUpsDue,
                icon = Icons.Filled.Schedule,
                accentColor = AmberWarning,
                badgeBg = AmberSurface,
                modifier = Modifier.weight(1f).testTag("metric_follow_ups_due"),
                onClick = { onMetricClick(LeadStatus.FOLLOW_UP) }
            )
            MetricTile(
                title = strings.metricCompletedSales,
                count = metrics.completedSales,
                icon = Icons.Filled.CheckCircle,
                accentColor = EmeraldSuccess,
                badgeBg = EmeraldSurface,
                modifier = Modifier.weight(1f).testTag("metric_completed_sales"),
                onClick = { onMetricClick(LeadStatus.COMPLETED) }
            )
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DueLeadPreviewCard(
    lead: Lead,
    language: AppLanguage,
    onOpenFollowUps: () -> Unit,
    onDraftAi: () -> Unit
) {
    val statusEnum = LeadStatus.fromString(lead.status)
    val statusText = if (language == AppLanguage.URDU) statusEnum.labelUr else statusEnum.labelEn

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenFollowUps),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lead.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(lead.designation, lead.company, lead.city).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberWarning)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${lead.followUpDate.ifBlank { "Scheduled" }} • $statusText",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberWarning
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onDraftAi,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}
