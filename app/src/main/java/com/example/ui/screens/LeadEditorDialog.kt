package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Lead
import com.example.data.model.LeadPermissionStatus
import com.example.data.model.LeadStatus
import com.example.data.model.OwnerProfile
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
