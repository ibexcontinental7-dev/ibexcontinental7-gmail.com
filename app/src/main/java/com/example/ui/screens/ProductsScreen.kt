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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.example.data.model.OfficialProductCatalog
import com.example.data.model.OfficialTakafulProduct
import com.example.data.model.OwnerProfile
import com.example.data.model.ProductVerificationNote
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

@Composable
fun ProductsScreen(
    strings: UiStrings,
    language: AppLanguage,
    notesState: UiState<Map<String, ProductVerificationNote>>,
    onSaveProductNote: (ProductVerificationNote) -> Unit,
    onOpenAiWithProduct: (OfficialTakafulProduct) -> Unit = {}
) {
    val context = LocalContext.current
    val notesMap = (notesState as? UiState.Success)?.data.orEmpty()
    var editingProduct by remember { mutableStateOf<OfficialTakafulProduct?>(null) }

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
                    text = strings.productsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = strings.productsOfficialSource,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                Surface(
                    color = AmberSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = AmberWarning)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.productsGlobalDisclaimer,
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberWarning
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { OutreachIntents.openUrl(context, OwnerProfile.OFFICIAL_TAKAFUL_URL) },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_official_askari_portal")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.openOfficialPortal)
                        }
                    }
                }
            }

            item {
                NeedAnalysisSection(
                    strings = strings,
                    language = language,
                    onDraftAiForProduct = onOpenAiWithProduct
                )
            }

            items(OfficialProductCatalog.products, key = { it.id }) { product ->
                val note = notesMap[product.id]
                val isVerified = note?.isVerifiedByUser == true
                ProductCard(
                    product = product,
                    note = note,
                    isVerified = isVerified,
                    strings = strings,
                    language = language,
                    onOpenOfficial = { OutreachIntents.openUrl(context, product.officialPortalUrl) },
                    onEditNotes = { editingProduct = product }
                )
            }
        }
    }

    editingProduct?.let { product ->
        val existingNote = notesMap[product.id]
        ProductNoteEditorDialog(
            product = product,
            existingNote = existingNote,
            strings = strings,
            onDismiss = { editingProduct = null },
            onSave = { updated ->
                onSaveProductNote(updated)
                editingProduct = null
            }
        )
    }
}

@Composable
private fun ProductCard(
    product: OfficialTakafulProduct,
    note: ProductVerificationNote?,
    isVerified: Boolean,
    strings: UiStrings,
    language: AppLanguage,
    onOpenOfficial: () -> Unit,
    onEditNotes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isVerified) EmeraldSuccess.copy(alpha = 0.45f) else AmberWarning.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.URDU) "${product.nameUr} (${product.nameEn})" else "${product.nameEn} (${product.nameUr})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Askari Family Takaful",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Surface(
                color = if (isVerified) EmeraldSurface else AmberSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Filled.Verified else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (isVerified) EmeraldSuccess else AmberWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isVerified) {
                            "${strings.verifiedBadge} (${note?.verifiedSourceDate.orEmpty()})"
                        } else {
                            strings.unverifiedBadge
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isVerified) EmeraldSuccess else AmberWarning
                    )
                }
            }

            if (isVerified && note != null && note.verifiedSummary.isNotBlank()) {
                Text(
                    text = note.verifiedSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (note.verifiedEligibility.isNotBlank()) {
                    Text(
                        text = note.verifiedEligibility,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = if (language == AppLanguage.URDU) product.unverifiedNoticeUr else product.unverifiedNoticeEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenOfficial,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.openOfficialPortal, maxLines = 1)
                }
                Button(
                    onClick = onEditNotes,
                    modifier = Modifier.weight(1f).testTag("edit_product_note_${product.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.recordVerifiedNotes, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ProductNoteEditorDialog(
    product: OfficialTakafulProduct,
    existingNote: ProductVerificationNote?,
    strings: UiStrings,
    onDismiss: () -> Unit,
    onSave: (ProductVerificationNote) -> Unit
) {
    var summary by remember { mutableStateOf(existingNote?.verifiedSummary.orEmpty()) }
    var eligibility by remember { mutableStateOf(existingNote?.verifiedEligibility.orEmpty()) }
    var sourceDate by remember {
        mutableStateOf(existingNote?.verifiedSourceDate?.ifBlank { MainViewModel.todayIsoDate() } ?: MainViewModel.todayIsoDate())
    }
    var verifiedChecked by remember { mutableStateOf(existingNote?.isVerifiedByUser == true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${product.nameEn} (${product.nameUr})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.productNoteWarning,
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberWarning
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { if (it.length <= 2000) summary = it },
                    label = { Text(strings.productNoteSummaryLabel) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eligibility,
                    onValueChange = { if (it.length <= 500) eligibility = it },
                    label = { Text(strings.productNoteEligibilityLabel) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sourceDate,
                    onValueChange = { if (it.length <= 40) sourceDate = it },
                    label = { Text(strings.productNoteDateLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = verifiedChecked, onCheckedChange = { verifiedChecked = it })
                    Text(
                        text = strings.productNoteConfirmCheckbox,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ProductVerificationNote(
                            productId = product.id,
                            verifiedSummary = summary,
                            verifiedEligibility = eligibility,
                            verifiedSourceDate = sourceDate,
                            isVerifiedByUser = verifiedChecked
                        )
                    )
                }
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
