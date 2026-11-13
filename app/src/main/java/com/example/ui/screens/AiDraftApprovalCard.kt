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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ContactChannel
import com.example.data.model.Lead
import com.example.ui.localization.UiStrings
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.MessageFormat
import com.example.util.OutreachIntents

@Composable
fun AiDraftApprovalCard(
    strings: UiStrings,
    selectedLead: Lead?,
    selectedFormat: MessageFormat,
    showEmailSubject: Boolean,
    editableSubject: String,
    editableBody: String,
    isApproved: Boolean,
    isDoNotContact: Boolean,
    logToHistoryChecked: Boolean,
    onSubjectChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onApprovedChange: (Boolean) -> Unit,
    onLogHistoryChange: (Boolean) -> Unit,
    onLogOutreach: (leadId: String, channel: ContactChannel, summary: String) -> Unit
) {
    val context = LocalContext.current

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

            if (showEmailSubject && (selectedFormat == MessageFormat.EMAIL_DRAFT || editableSubject.isNotBlank())) {
                OutlinedTextField(
                    value = editableSubject,
                    onValueChange = onSubjectChange,
                    label = { Text(strings.emailSubjectLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = editableBody,
                onValueChange = onBodyChange,
                label = { Text(strings.draftEditorLabel) },
                minLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_draft_editor")
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
                        onCheckedChange = onApprovedChange,
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
                        onCheckedChange = onLogHistoryChange
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
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_whatsapp_button"),
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
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_email_button"),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.copyMessage)
            }
        }
    }
}
