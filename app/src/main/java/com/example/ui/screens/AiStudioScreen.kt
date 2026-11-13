package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContactChannel
import com.example.data.model.Lead
import com.example.data.model.LeadPermissionStatus
import com.example.data.model.OfficialProductCatalog
import com.example.data.model.OfficialTakafulProduct
import com.example.data.model.OwnerProfile
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.CrimsonSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.Gold400
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AiDraftState
import com.example.ui.viewmodel.AiMessageViewModel
import com.example.ui.viewmodel.MessageFormat
import com.example.ui.viewmodel.MessageLanguage
import com.example.ui.viewmodel.UiState
import com.example.util.OutreachIntents

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiStudioScreen(
    strings: UiStrings,
    language: AppLanguage,
    leadsState: UiState<List<Lead>>,
    preselectedLead: Lead?,
    aiViewModel: AiMessageViewModel,
    onLogOutreach: (leadId: String, channel: ContactChannel, summary: String) -> Unit
) {
    val context = LocalContext.current
    val allLeads = (leadsState as? UiState.Success)?.data.orEmpty()

    var selectedLead by remember(preselectedLead) { mutableStateOf(preselectedLead) }
    var leadMenuExpanded by remember { mutableStateOf(false) }

    var customName by remember { mutableStateOf("") }
    var customCompany by remember { mutableStateOf("") }
    var customDesignation by remember { mutableStateOf("") }
    var customIndustry by remember { mutableStateOf("") }
    var customCity by remember { mutableStateOf(OwnerProfile.DEFAULT_MARKET_CITY) }

    var selectedFormat by remember { mutableStateOf(MessageFormat.WHATSAPP_SHORT) }
    var selectedMessageLanguage by remember {
        mutableStateOf(if (language == AppLanguage.URDU) MessageLanguage.URDU else MessageLanguage.ENGLISH)
    }
    var selectedProduct by remember { mutableStateOf<OfficialTakafulProduct?>(null) }
    var extraContext by remember { mutableStateOf("") }
    var logToHistoryChecked by remember { mutableStateOf(true) }

    val draftState by aiViewModel.draftState.collectAsStateWithLifecycle()
    val editableSubject by aiViewModel.editableSubject.collectAsStateWithLifecycle()
    val editableBody by aiViewModel.editableBody.collectAsStateWithLifecycle()
    val isApproved by aiViewModel.isApproved.collectAsStateWithLifecycle()

    val isDoNotContact = selectedLead?.permissionStatus == LeadPermissionStatus.DO_NOT_CONTACT.name

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 980.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = strings.studioTitle,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = strings.studioSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${OwnerProfile.DESIGNATION_EN} • ${OwnerProfile.COMPANY_EN} • WhatsApp: ${OwnerProfile.WHATSAPP_NUMBER}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = leadMenuExpanded,
                            onExpandedChange = { leadMenuExpanded = !leadMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedLead?.let { "${it.name} (${it.company.ifBlank { it.city }})" }
                                    ?: strings.customProspectOption,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.selectLeadLabel) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leadMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("studio_lead_selector")
                            )
                            ExposedDropdownMenu(
                                expanded = leadMenuExpanded,
                                onDismissRequest = { leadMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.customProspectOption) },
                                    onClick = {
                                        selectedLead = null
                                        leadMenuExpanded = false
                                    }
                                )
                                allLeads.forEach { lead ->
                                    DropdownMenuItem(
                                        text = { Text("${lead.name} — ${lead.designation} (${lead.city})") },
                                        onClick = {
                                            selectedLead = lead
                                            leadMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isDoNotContact) {
                            Surface(
                                color = CrimsonSurface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonError)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = strings.doNotContactWarning,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CrimsonError
                                    )
                                }
                            }
                        }

                        if (selectedLead == null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    label = { Text(strings.fieldNameReq) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customCity,
                                    onValueChange = { customCity = it },
                                    label = { Text(strings.fieldCityReq) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customCompany,
                                    onValueChange = { customCompany = it },
                                    label = { Text(strings.fieldCompany) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customDesignation,
                                    onValueChange = { customDesignation = it },
                                    label = { Text(strings.fieldDesignation) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = customIndustry,
                                onValueChange = { customIndustry = it },
                                label = { Text(strings.fieldIndustry) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(text = strings.messageTypeLabel, style = MaterialTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MessageFormat.entries.forEach { fmt ->
                                FilterChip(
                                    selected = selectedFormat == fmt,
                                    onClick = { selectedFormat = fmt },
                                    label = { Text(if (language == AppLanguage.URDU) fmt.labelUr else fmt.labelEn) }
                                )
                            }
                        }

                        Text(text = strings.messageLanguageLabel, style = MaterialTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MessageLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = selectedMessageLanguage == lang,
                                    onClick = { selectedMessageLanguage = lang },
                                    label = { Text(if (language == AppLanguage.URDU) lang.labelUr else lang.labelEn) }
                                )
                            }
                        }

                        Text(text = strings.productFocusLabel, style = MaterialTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedProduct == null,
                                onClick = { selectedProduct = null },
                                label = { Text(if (language == AppLanguage.URDU) "عمومی تکافل" else "General Family Takaful") }
                            )
                            OfficialProductCatalog.products.forEach { product ->
                                FilterChip(
                                    selected = selectedProduct?.id == product.id,
                                    onClick = { selectedProduct = product },
                                    label = { Text(if (language == AppLanguage.URDU) product.nameUr else product.nameEn) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = extraContext,
                            onValueChange = { extraContext = it },
                            label = { Text(strings.extraContextLabel) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("studio_context_input")
                        )

                        Button(
                            onClick = {
                                aiViewModel.generateDraft(
                                    lead = selectedLead,
                                    customName = customName,
                                    customCompany = customCompany,
                                    customDesignation = customDesignation,
                                    customIndustry = customIndustry,
                                    customCity = customCity,
                                    format = selectedFormat,
                                    language = selectedMessageLanguage,
                                    product = selectedProduct,
                                    extraContext = extraContext
                                )
                            },
                            enabled = draftState !is AiDraftState.Loading && draftState !is AiDraftState.Streaming,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_gemini_draft_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (draftState is AiDraftState.Loading || draftState is AiDraftState.Streaming) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Gold400,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(strings.generatingText)
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Gold400, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(strings.generateWithGemini)
                            }
                        }
                    }
                }
            }

            if (draftState is AiDraftState.Error) {
                item {
                    Surface(
                        color = CrimsonSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (draftState as AiDraftState.Error).message,
                            color = CrimsonError,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (editableBody.isNotBlank() || draftState is AiDraftState.Success || draftState is AiDraftState.Streaming) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.5.dp,
                            if (isApproved) EmeraldSuccess else AmberWarning
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isApproved) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (isApproved) EmeraldSuccess else AmberWarning
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = strings.approvalGateTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (selectedFormat == MessageFormat.EMAIL_DRAFT || editableSubject.isNotBlank()) {
                                OutlinedTextField(
                                    value = editableSubject,
                                    onValueChange = { aiViewModel.updateSubject(it) },
                                    label = { Text(strings.emailSubjectLabel) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutlinedTextField(
                                value = editableBody,
                                onValueChange = { aiViewModel.updateBody(it) },
                                label = { Text(strings.draftEditorLabel) },
                                minLines = 6,
                                modifier = Modifier.fillMaxWidth().testTag("studio_draft_editor")
                            )

                            Surface(
                                color = if (isApproved) EmeraldSurface else AmberSurface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isApproved,
                                        onCheckedChange = { aiViewModel.setApproved(it) },
                                        modifier = Modifier.testTag("studio_approve_checkbox")
                                    )
                                    Text(
                                        text = if (isApproved) strings.approvalCheckboxLabel else strings.approvalRequiredWarning,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isApproved) EmeraldSuccess else AmberWarning
                                    )
                                }
                            }

                            if (selectedLead != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = logToHistoryChecked,
                                        onCheckedChange = { logToHistoryChecked = it }
                                    )
                                    Text(
                                        text = strings.logToHistoryCheck,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val lead = selectedLead
                                        if (lead != null && logToHistoryChecked) {
                                            onLogOutreach(lead.id, ContactChannel.WHATSAPP, editableBody.take(500))
                                        }
                                        OutreachIntents.openWhatsAppWithDraft(
                                            context = context,
                                            rawPhone = lead?.contactPhone.orEmpty(),
                                            message = editableBody
                                        )
                                    },
                                    enabled = isApproved && editableBody.isNotBlank() && !isDoNotContact,
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("open_whatsapp_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = PureWhite),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.openWhatsApp, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val lead = selectedLead
                                        if (lead != null && logToHistoryChecked) {
                                            onLogOutreach(lead.id, ContactChannel.EMAIL, editableBody.take(500))
                                        }
                                        OutreachIntents.openEmailWithDraft(
                                            context = context,
                                            recipientEmail = lead?.email.orEmpty(),
                                            subject = editableSubject.ifBlank { "Askari Family Takaful Introduction" },
                                            body = editableBody
                                        )
                                    },
                                    enabled = isApproved && editableBody.isNotBlank() && !isDoNotContact,
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("open_email_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.openEmail, maxLines = 1)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    OutreachIntents.copyToClipboard(context, "Askari Takaful Draft", editableBody)
                                },
                                enabled = isApproved && editableBody.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.copyMessage)
                            }
                        }
                    }
                }
            }
        }
    }
}
