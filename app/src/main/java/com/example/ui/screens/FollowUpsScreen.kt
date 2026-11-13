package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContactChannel
import com.example.data.model.ContactHistoryEntry
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.OutreachIntents
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun FollowUpsScreen(
    strings: UiStrings,
    language: AppLanguage,
    leadsState: UiState<List<Lead>>,
    observeHistory: (String) -> Flow<List<ContactHistoryEntry>>,
    onUpdateStatusAndDate: (leadId: String, status: LeadStatus, followUpDate: String) -> Unit,
    onAddContactLog: (leadId: String, channel: ContactChannel, summary: String, outcome: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeLogLead by remember { mutableStateOf<Lead?>(null) }
    var activeStatusLead by remember { mutableStateOf<Lead?>(null) }
    var expandedHistoryLeadId by remember { mutableStateOf<String?>(null) }

    val allLeads = (leadsState as? UiState.Success)?.data.orEmpty()
    val today = remember { MainViewModel.todayIsoDate() }

    val dueList = remember(allLeads, today) {
        allLeads.filter { MainViewModel.isFollowUpDue(it, today) }
            .sortedBy { it.followUpDate.ifBlank { "9999-12-31" } }
    }
    val upcomingList = remember(allLeads, today) {
        allLeads.filter {
            it.status != LeadStatus.COMPLETED.name &&
                it.status != LeadStatus.ARCHIVED.name &&
                it.followUpDate.isNotBlank() &&
                it.followUpDate > today
        }.sortedBy { it.followUpDate }
    }
    val completedList = remember(allLeads) {
        allLeads.filter { it.status == LeadStatus.COMPLETED.name }
    }

    val displayedLeads = when (selectedTab) {
        0 -> dueList
        1 -> upcomingList
        else -> completedList
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 980.dp)
        ) {
            Text(
                text = strings.followUpsTitle,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${strings.dueOverdueTab} (${dueList.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${strings.upcomingTab} (${upcomingList.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("${strings.completedTab} (${completedList.size})") }
                )
            }

            if (displayedLeads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noFollowUpsDue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedLeads, key = { it.id }) { lead ->
                        val isExpanded = expandedHistoryLeadId == lead.id
                        FollowUpLeadCard(
                            lead = lead,
                            today = today,
                            strings = strings,
                            language = language,
                            isHistoryExpanded = isExpanded,
                            observeHistory = observeHistory,
                            onToggleHistory = {
                                expandedHistoryLeadId = if (isExpanded) null else lead.id
                            },
                            onOpenStatusEditor = { activeStatusLead = lead },
                            onOpenLogEditor = { activeLogLead = lead },
                            onAddCalendarReminder = { OutreachIntents.addFollowUpCalendarReminder(context, lead) }
                        )
                    }
                }
            }
        }
    }

    activeLogLead?.let { lead ->
        ContactLogDialog(
            lead = lead,
            strings = strings,
            language = language,
            onDismiss = { activeLogLead = null },
            onSave = { channel, summary, outcome ->
                onAddContactLog(lead.id, channel, summary, outcome)
                activeLogLead = null
                expandedHistoryLeadId = lead.id
            }
        )
    }

    activeStatusLead?.let { lead ->
        FollowUpUpdateDialog(
            lead = lead,
            strings = strings,
            language = language,
            onDismiss = { activeStatusLead = null },
            onSave = { status, date ->
                onUpdateStatusAndDate(lead.id, status, date)
                activeStatusLead = null
            }
        )
    }
}

@Composable
private fun FollowUpLeadCard(
    lead: Lead,
    today: String,
    strings: UiStrings,
    language: AppLanguage,
    isHistoryExpanded: Boolean,
    observeHistory: (String) -> Flow<List<ContactHistoryEntry>>,
    onToggleHistory: () -> Unit,
    onOpenStatusEditor: () -> Unit,
    onOpenLogEditor: () -> Unit,
    onAddCalendarReminder: () -> Unit
) {
    val status = LeadStatus.fromString(lead.status)
    val isOverdue = lead.followUpDate.isNotBlank() && lead.followUpDate < today && status != LeadStatus.COMPLETED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isOverdue) AmberWarning.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = listOf(lead.designation, lead.company, lead.city).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = if (status == LeadStatus.COMPLETED) EmeraldSurface else AmberSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.URDU) status.labelUr else status.labelEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status == LeadStatus.COMPLETED) EmeraldSuccess else AmberWarning,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberWarning)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (lead.followUpDate.isNotBlank()) "${strings.fieldFollowUpDate}: ${lead.followUpDate}" else strings.noFollowUpDateSet,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isOverdue) AmberWarning else MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenLogEditor,
                    modifier = Modifier.weight(1f).testTag("log_contact_btn_${lead.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.addContactLog, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onOpenStatusEditor,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.reschedule, maxLines = 1)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddCalendarReminder,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.addCalendarReminder, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onToggleHistory,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.contactHistoryTitle, maxLines = 1)
                }
            }

            if (isHistoryExpanded) {
                HorizontalDivider()
                val history by observeHistory(lead.id).collectAsStateWithLifecycle(initialValue = emptyList())
                if (history.isEmpty()) {
                    Text(
                        text = strings.noContactHistoryYet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.forEach { entry ->
                            val channel = ContactChannel.fromString(entry.channel)
                            val dateStr = entry.createdAt?.toDate()?.let {
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(it)
                            } ?: "Just now"
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${if (language == AppLanguage.URDU) channel.labelUr else channel.labelEn} • $dateStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(text = entry.summary, style = MaterialTheme.typography.bodySmall)
                                    if (entry.outcome.isNotBlank()) {
                                        Text(
                                            text = "Outcome: ${entry.outcome}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactLogDialog(
    lead: Lead,
    strings: UiStrings,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (ContactChannel, String, String) -> Unit
) {
    var channel by remember { mutableStateOf(ContactChannel.WHATSAPP) }
    var summary by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${strings.addContactLog} — ${lead.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = if (language == AppLanguage.URDU) channel.labelUr else channel.labelEn,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.channelLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ContactChannel.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(if (language == AppLanguage.URDU) item.labelUr else item.labelEn) },
                                onClick = {
                                    channel = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = summary,
                    onValueChange = { if (it.length <= 1000) summary = it },
                    label = { Text(strings.interactionSummaryLabel) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = outcome,
                    onValueChange = { if (it.length <= 200) outcome = it },
                    label = { Text(strings.outcomeLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(channel, summary, outcome) },
                enabled = summary.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowUpUpdateDialog(
    lead: Lead,
    strings: UiStrings,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (LeadStatus, String) -> Unit
) {
    var status by remember { mutableStateOf(LeadStatus.fromString(lead.status)) }
    var date by remember { mutableStateOf(lead.followUpDate) }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${strings.updateFollowUpTitle} — ${lead.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = if (language == AppLanguage.URDU) status.labelUr else status.labelEn,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.fieldLeadStatus) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        LeadStatus.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(if (language == AppLanguage.URDU) item.labelUr else item.labelEn) },
                                onClick = {
                                    status = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { if (it.length <= 20) date = it },
                        label = { Text(strings.fieldFollowUpDate) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.Event, contentDescription = strings.fieldFollowUpDate)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(status, date) }) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            date = fmt.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
