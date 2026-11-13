package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Lead
import com.example.data.model.LeadPermissionStatus
import com.example.data.model.LeadStatus
import com.example.data.model.OwnerProfile
import com.example.data.model.Prospect
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.CrimsonSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.Gold400
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.OutreachIntents

@Composable
fun LeadsScreen(
    strings: UiStrings,
    language: AppLanguage,
    leadsState: UiState<List<Lead>>,
    prospectsState: UiState<List<Prospect>> = UiState.Success(emptyList()),
    selectedStatusFilter: LeadStatus?,
    onSelectStatusFilter: (LeadStatus?) -> Unit,
    prefilledLeadDraft: Lead?,
    onConsumePrefilledDraft: () -> Unit,
    onSaveLead: (Lead) -> Unit,
    onDeleteLead: (String) -> Unit,
    onDeleteProspect: (String) -> Unit = {},
    onUpgradeProspectToLead: (prospectId: String, lead: Lead) -> Unit = { _, _ -> },
    onOpenAddNewProspect: () -> Unit = {},
    onOpenAiStudio: (Lead) -> Unit,
    onOpenFollowUps: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var cityFilter by remember { mutableStateOf("ALL") }
    var editorLead by remember(prefilledLeadDraft) { mutableStateOf(prefilledLeadDraft) }
    var upgradingProspectId by remember { mutableStateOf<String?>(null) }
    var deleteCandidate by remember { mutableStateOf<Lead?>(null) }

    val allLeads = (leadsState as? UiState.Success)?.data.orEmpty()
    val allProspects = (prospectsState as? UiState.Success)?.data.orEmpty()
    val availableCities = remember(allLeads) {
        listOf("ALL") + allLeads.map { it.city }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredLeads = remember(allLeads, searchQuery, selectedStatusFilter, cityFilter) {
        MainViewModel.filterLeads(
            leads = allLeads,
            query = searchQuery,
            selectedStatus = selectedStatusFilter,
            cityFilter = cityFilter
        )
    }

    val filteredProspects = remember(allProspects, searchQuery, selectedStatusFilter) {
        MainViewModel.filterProspects(
            prospects = allProspects,
            query = searchQuery,
            selectedStatus = selectedStatusFilter
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 980.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.leadsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { OutreachIntents.exportLeadsCsv(context, filteredLeads) },
                    enabled = filteredLeads.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("export_csv_button")
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.exportCsv)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_leads_search_button")
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = strings.cancel)
                        }
                    }
                },
                placeholder = { Text(strings.searchLeadsHint) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("leads_search_input")
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { onSelectStatusFilter(null) },
                    label = { Text(if (language == AppLanguage.URDU) "تمام" else "All (${allLeads.size})") }
                )
                LeadStatus.entries.forEach { st ->
                    FilterChip(
                        selected = selectedStatusFilter == st,
                        onClick = { onSelectStatusFilter(if (selectedStatusFilter == st) null else st) },
                        label = { Text(if (language == AppLanguage.URDU) st.labelUr else st.labelEn) }
                    )
                }
            }

            if (availableCities.size > 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCities.forEach { city ->
                        FilterChip(
                            selected = cityFilter == city,
                            onClick = { cityFilter = city },
                            label = { Text(if (city == "ALL") (if (language == AppLanguage.URDU) "تمام شہر" else "All Cities") else city) }
                        )
                    }
                }
            }

            when (leadsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text(
                            text = leadsState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is UiState.Success -> {
                    if (filteredLeads.isEmpty() && filteredProspects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = strings.noLeadsYet,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 92.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProspects, key = { "prospect_${it.id}" }) { prospect ->
                                val dialablePhone = OutreachIntents.extractPhoneOrNull(prospect.contact)
                                QuickProspectCard(
                                    prospect = prospect,
                                    strings = strings,
                                    canCall = dialablePhone != null,
                                    onCall = { dialablePhone?.let { OutreachIntents.dialPhone(context, it) } },
                                    onUpgradeToLead = {
                                        upgradingProspectId = prospect.id
                                        val matchedStatus = LeadStatus.entries.firstOrNull {
                                            it.name.equals(prospect.status, ignoreCase = true) ||
                                                it.labelEn.equals(prospect.status, ignoreCase = true) ||
                                                it.labelUr.equals(prospect.status, ignoreCase = true)
                                        } ?: LeadStatus.NEW
                                        editorLead = Lead(
                                            name = prospect.name,
                                            contactPhone = dialablePhone.orEmpty(),
                                            email = OutreachIntents.extractEmailOrNull(prospect.contact).orEmpty(),
                                            city = OwnerProfile.DEFAULT_MARKET_CITY,
                                            source = "Quick Prospect",
                                            notes = prospect.notes,
                                            status = matchedStatus.name
                                        )
                                    },
                                    onDelete = { onDeleteProspect(prospect.id) },
                                    onAiStudio = {
                                        onOpenAiStudio(
                                            Lead(
                                                name = prospect.name,
                                                contactPhone = dialablePhone ?: prospect.contact,
                                                email = OutreachIntents.extractEmailOrNull(prospect.contact).orEmpty(),
                                                notes = prospect.notes,
                                                status = prospect.status
                                            )
                                        )
                                    }
                                )
                            }
                            items(filteredLeads, key = { "lead_${it.id}" }) { lead ->
                                LeadCard(
                                    lead = lead,
                                    strings = strings,
                                    language = language,
                                    onEdit = {
                                        upgradingProspectId = null
                                        editorLead = lead
                                    },
                                    onDelete = { deleteCandidate = lead },
                                    onCall = { OutreachIntents.dialPhone(context, lead.contactPhone) },
                                    onLogReferral = {
                                        upgradingProspectId = null
                                        editorLead = Lead(
                                            city = lead.city.ifBlank { OwnerProfile.DEFAULT_MARKET_CITY },
                                            source = "Referral from ${lead.name}",
                                            permissionStatus = LeadPermissionStatus.REFERRAL_INTRO.name,
                                            status = LeadStatus.NEW.name,
                                            notes = "Warm referral introduced by ${lead.name} (${lead.company.ifBlank { lead.city }})"
                                        )
                                    },
                                    onAiStudio = { onOpenAiStudio(lead) },
                                    onOpenWebsite = { url -> OutreachIntents.openUrl(context, url) },
                                    onFollowUp = onOpenFollowUps
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onOpenAddNewProspect,
            containerColor = Navy900,
            contentColor = Gold400,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_lead_fab")
        ) {
            Icon(Icons.Filled.Add, contentDescription = strings.addNewProspectTitle)
        }
    }

    editorLead?.let { current ->
        LeadEditorDialog(
            initialLead = current,
            strings = strings,
            language = language,
            onDismiss = {
                editorLead = null
                upgradingProspectId = null
                onConsumePrefilledDraft()
            },
            onSave = { updated ->
                val upgradeId = upgradingProspectId
                if (upgradeId != null) {
                    onUpgradeProspectToLead(upgradeId, updated)
                } else {
                    onSaveLead(updated)
                }
                editorLead = null
                upgradingProspectId = null
                onConsumePrefilledDraft()
            }
        )
    }

    deleteCandidate?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(strings.deleteConfirmTitle) },
            text = { Text("${strings.deleteConfirmBody}\n\n${target.name}") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteLead(target.id)
                        deleteCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun QuickProspectCard(
    prospect: Prospect,
    strings: UiStrings,
    canCall: Boolean,
    onCall: () -> Unit,
    onUpgradeToLead: () -> Unit,
    onDelete: () -> Unit,
    onAiStudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prospect_card_${prospect.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Gold400.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prospect.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (prospect.contact.isNotBlank()) {
                        Text(
                            text = prospect.contact,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(color = Navy100, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        text = prospect.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = Navy900,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (prospect.notes.isNotBlank()) {
                Text(
                    text = prospect.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onAiStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Gold400, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI", style = MaterialTheme.typography.labelMedium)
                    }
                    if (canCall) {
                        OutlinedButton(
                            onClick = onCall,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("call_prospect_${prospect.id}")
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = strings.callProspect, modifier = Modifier.size(15.dp))
                        }
                    }
                    OutlinedButton(
                        onClick = onUpgradeToLead,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("upgrade_prospect_${prospect.id}")
                    ) {
                        Icon(Icons.Filled.Upgrade, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.upgradeToLead, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete prospect", tint = CrimsonError)
                }
            }
        }
    }
}

@Composable
private fun LeadCard(
    lead: Lead,
    strings: UiStrings,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onLogReferral: () -> Unit,
    onAiStudio: () -> Unit,
    onOpenWebsite: (String) -> Unit,
    onFollowUp: () -> Unit
) {
    val status = LeadStatus.fromString(lead.status)
    val perm = LeadPermissionStatus.fromString(lead.permissionStatus)
    val isArchived = status == LeadStatus.ARCHIVED
    val canCall = lead.contactPhone.isNotBlank() && perm != LeadPermissionStatus.DO_NOT_CONTACT
    val canLogReferral = status == LeadStatus.COMPLETED || status == LeadStatus.INTERESTED

    val (statusBg, statusFg) = when (status) {
        LeadStatus.COMPLETED -> EmeraldSurface to EmeraldSuccess
        LeadStatus.INTERESTED -> AmberSurface to AmberWarning
        LeadStatus.ARCHIVED -> CrimsonSurface to CrimsonError
        else -> Navy100 to Navy900
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("lead_card_${lead.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(lead.designation, lead.company).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(color = statusBg, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        text = if (language == AppLanguage.URDU) status.labelUr else status.labelEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "${lead.city}${if (lead.industry.isNotBlank()) " • ${lead.industry}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = if (perm == LeadPermissionStatus.DO_NOT_CONTACT) CrimsonSurface else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.URDU) perm.labelUr else perm.labelEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (perm == LeadPermissionStatus.DO_NOT_CONTACT) CrimsonError else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (lead.contactPhone.isNotBlank() || lead.email.isNotBlank()) {
                Text(
                    text = listOf(lead.contactPhone, lead.email).filter { it.isNotBlank() }.joinToString("  |  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (lead.followUpDate.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberWarning)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Follow-up: ${lead.followUpDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberWarning
                    )
                }
            }

            if (lead.notes.isNotBlank()) {
                Text(
                    text = lead.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onAiStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Gold400, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI", style = MaterialTheme.typography.labelMedium)
                    }
                    if (canCall) {
                        OutlinedButton(
                            onClick = onCall,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("call_lead_${lead.id}")
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = strings.callProspect, modifier = Modifier.size(15.dp))
                        }
                    }
                    if (lead.website.isNotBlank() || lead.linkedInUrl.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onOpenWebsite(lead.linkedInUrl.ifBlank { lead.website }) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open link", modifier = Modifier.size(15.dp))
                        }
                    }
                    OutlinedButton(
                        onClick = onFollowUp,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Event, contentDescription = strings.navFollowUps, modifier = Modifier.size(15.dp))
                    }
                }

                Row {
                    if (canLogReferral) {
                        IconButton(
                            onClick = onLogReferral,
                            modifier = Modifier.testTag("log_referral_${lead.id}")
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = strings.logReferral, tint = EmeraldSuccess)
                        }
                    }
                    IconButton(onClick = onEdit, enabled = !isArchived) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit lead")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete lead", tint = CrimsonError)
                    }
                }
            }
        }
    }
}
