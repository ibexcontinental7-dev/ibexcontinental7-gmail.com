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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    onOpenAddNewProspect: () -> Unit = {},
    onOpenAiStudio: (Lead) -> Unit,
    onOpenFollowUps: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var cityFilter by remember { mutableStateOf("ALL") }
    var editorLead by remember(prefilledLeadDraft) { mutableStateOf(prefilledLeadDraft) }
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
                                QuickProspectCard(
                                    prospect = prospect,
                                    onDelete = { onDeleteProspect(prospect.id) },
                                    onAiStudio = {
                                        onOpenAiStudio(
                                            Lead(
                                                name = prospect.name,
                                                contactPhone = prospect.contact,
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
                                    language = language,
                                    onEdit = { editorLead = lead },
                                    onDelete = { deleteCandidate = lead },
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
                onConsumePrefilledDraft()
            },
            onSave = { updated ->
                onSaveLead(updated)
                editorLead = null
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
                Button(
                    onClick = onAiStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Gold400, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AI", style = MaterialTheme.typography.labelMedium)
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
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAiStudio: () -> Unit,
    onOpenWebsite: (String) -> Unit,
    onFollowUp: () -> Unit
) {
    val status = LeadStatus.fromString(lead.status)
    val perm = LeadPermissionStatus.fromString(lead.permissionStatus)
    val isArchived = status == LeadStatus.ARCHIVED

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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Gold400, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("AI", style = MaterialTheme.typography.labelMedium)
                    }
                    if (lead.website.isNotBlank() || lead.linkedInUrl.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onOpenWebsite(lead.linkedInUrl.ifBlank { lead.website }) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                    }
                    OutlinedButton(
                        onClick = onFollowUp,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(15.dp))
                    }
                }

                Row {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadEditorDialog(
    initialLead: Lead,
    strings: UiStrings,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Lead) -> Unit
) {
    var name by remember { mutableStateOf(initialLead.name) }
    var company by remember { mutableStateOf(initialLead.company) }
    var designation by remember { mutableStateOf(initialLead.designation) }
    var city by remember { mutableStateOf(initialLead.city.ifBlank { OwnerProfile.DEFAULT_MARKET_CITY }) }
    var industry by remember { mutableStateOf(initialLead.industry) }
    var phone by remember { mutableStateOf(initialLead.contactPhone) }
    var email by remember { mutableStateOf(initialLead.email) }
    var website by remember { mutableStateOf(initialLead.website) }
    var linkedInUrl by remember { mutableStateOf(initialLead.linkedInUrl) }
    var source by remember { mutableStateOf(initialLead.source.ifBlank { "Public Search" }) }
    var notes by remember { mutableStateOf(initialLead.notes) }
    var followUpDate by remember { mutableStateOf(initialLead.followUpDate) }
    var permissionStatus by remember { mutableStateOf(LeadPermissionStatus.fromString(initialLead.permissionStatus)) }
    var status by remember { mutableStateOf(LeadStatus.fromString(initialLead.status)) }
    var showDatePicker by remember { mutableStateOf(false) }

    var statusExpanded by remember { mutableStateOf(false) }
    var permExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialLead.id.isBlank()) strings.addLead else strings.editLead)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 120) name = it },
                    label = { Text(strings.fieldNameReq) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lead_name_input")
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { if (it.length <= 150) company = it },
                    label = { Text(strings.fieldCompany) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lead_company_input")
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { if (it.length <= 120) designation = it },
                    label = { Text(strings.fieldDesignation) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lead_designation_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { if (it.length <= 80) city = it },
                        label = { Text(strings.fieldCityReq) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("lead_city_input")
                    )
                    OutlinedTextField(
                        value = industry,
                        onValueChange = { if (it.length <= 120) industry = it },
                        label = { Text(strings.fieldIndustry) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 40) phone = it },
                    label = { Text(strings.fieldPhone) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lead_phone_input")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (it.length <= 150) email = it },
                    label = { Text(strings.fieldEmail) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { if (it.length <= 300) website = it },
                    label = { Text(strings.fieldWebsite) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = linkedInUrl,
                    onValueChange = { if (it.length <= 300) linkedInUrl = it },
                    label = { Text(strings.fieldLinkedIn) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { if (it.length <= 100) source = it },
                    label = { Text(strings.fieldSource) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = permExpanded,
                    onExpandedChange = { permExpanded = !permExpanded }
                ) {
                    OutlinedTextField(
                        value = if (language == AppLanguage.URDU) permissionStatus.labelUr else permissionStatus.labelEn,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.fieldPermissionStatus) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = permExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = permExpanded, onDismissRequest = { permExpanded = false }) {
                        LeadPermissionStatus.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(if (language == AppLanguage.URDU) item.labelUr else item.labelEn) },
                                onClick = {
                                    permissionStatus = item
                                    permExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = if (language == AppLanguage.URDU) status.labelUr else status.labelEn,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.fieldLeadStatus) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        LeadStatus.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(if (language == AppLanguage.URDU) item.labelUr else item.labelEn) },
                                onClick = {
                                    status = item
                                    statusExpanded = false
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
                        value = followUpDate,
                        onValueChange = { if (it.length <= 20) followUpDate = it },
                        label = { Text(strings.fieldFollowUpDate) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.Event, contentDescription = strings.fieldFollowUpDate)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 2000) notes = it },
                    label = { Text(strings.fieldNotes) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialLead.copy(
                            name = name,
                            company = company,
                            designation = designation,
                            city = city.ifBlank { OwnerProfile.DEFAULT_MARKET_CITY },
                            industry = industry,
                            contactPhone = phone,
                            email = email,
                            website = website,
                            linkedInUrl = linkedInUrl,
                            source = source,
                            notes = notes,
                            permissionStatus = permissionStatus.name,
                            status = status.name,
                            followUpDate = followUpDate
                        )
                    )
                },
                enabled = name.isNotBlank() && city.isNotBlank(),
                modifier = Modifier.testTag("save_lead_dialog_button")
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
                            followUpDate = fmt.format(Date(millis))
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
