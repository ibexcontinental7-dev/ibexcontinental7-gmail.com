package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LeadStatus
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.CrimsonSurface
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddNewProspectScreen(
    strings: UiStrings,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onSaveProspect: (
        name: String,
        contact: String,
        status: String,
        notes: String,
        onResult: (Result<String>) -> Unit
    ) -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var status by remember {
        mutableStateOf(
            if (language == AppLanguage.URDU) LeadStatus.NEW.labelUr else LeadStatus.NEW.labelEn
        )
    }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val statusPresets = remember(language) {
        listOf(
            LeadStatus.NEW,
            LeadStatus.CONTACTED,
            LeadStatus.INTERESTED,
            LeadStatus.FOLLOW_UP,
            LeadStatus.COMPLETED
        ).map { if (language == AppLanguage.URDU) it.labelUr else it.labelEn }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("add_prospect_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.cancel
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.addNewProspectTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = strings.addNewProspectSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Gold500.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = strings.prospectCollectionBadge,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 120) name = it },
                        label = { Text(strings.fieldNameReq) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prospect_name_input")
                    )

                    OutlinedTextField(
                        value = contact,
                        onValueChange = { if (it.length <= 150) contact = it },
                        label = { Text(strings.prospectContactLabel) },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prospect_contact_input")
                    )

                    OutlinedTextField(
                        value = status,
                        onValueChange = { if (it.length <= 60) status = it },
                        label = { Text(strings.prospectStatusLabel) },
                        leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prospect_status_input")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statusPresets.forEach { preset ->
                            FilterChip(
                                selected = status.equals(preset, ignoreCase = true),
                                onClick = { status = preset },
                                label = { Text(preset) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { if (it.length <= 2000) notes = it },
                        label = { Text(strings.prospectNotesLabel) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prospect_notes_input")
                    )

                    if (errorMessage != null) {
                        Surface(
                            color = CrimsonSurface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = CrimsonError,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            enabled = !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(strings.cancel)
                        }

                        Button(
                            onClick = {
                                errorMessage = null
                                isSaving = true
                                onSaveProspect(name, contact, status, notes) { result ->
                                    isSaving = false
                                    result.onSuccess {
                                        onNavigateBack()
                                    }.onFailure { err ->
                                        errorMessage = err.localizedMessage ?: "Failed to save prospect."
                                    }
                                }
                            },
                            enabled = !isSaving && name.isNotBlank() && status.isNotBlank(),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(52.dp)
                                .testTag("save_new_prospect_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Navy900,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Gold400,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(strings.saveProspectButton)
                            }
                        }
                    }
                }
            }
        }
    }
}
