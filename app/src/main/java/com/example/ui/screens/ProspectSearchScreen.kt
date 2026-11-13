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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.OwnerProfile
import com.example.ui.localization.UiStrings
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.Gold400
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.util.OutreachIntents

private val PRESET_CITIES = listOf(
    "Islamabad", "Rawalpindi", "Lahore", "Karachi", "Peshawar", "Faisalabad", "Multan"
)

private val PRESET_INDUSTRIES = listOf(
    "Property Developers",
    "Property Management Companies",
    "Housing Societies",
    "Construction Companies",
    "Business Owners",
    "Investors",
    "Doctors",
    "Hospitals",
    "Schools",
    "Corporate HR"
)

private val PRESET_DESIGNATIONS = listOf(
    "CEO",
    "Managing Director",
    "Owner",
    "Director",
    "HR Head",
    "Medical Superintendent",
    "Principal",
    "Chairman",
    "Investor",
    "Procurement Head"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProspectSearchScreen(
    strings: UiStrings,
    onCreateLeadFromSearch: (city: String, industry: String, designation: String) -> Unit
) {
    val context = LocalContext.current

    var city by remember { mutableStateOf(OwnerProfile.DEFAULT_MARKET_CITY) }
    var industry by remember { mutableStateOf(PRESET_INDUSTRIES.first()) }
    var designation by remember { mutableStateOf(PRESET_DESIGNATIONS.first()) }
    var extraKeywords by remember { mutableStateOf("") }

    val googleUrl = remember(city, industry, designation, extraKeywords) {
        OutreachIntents.buildGoogleSearchUrl(city, industry, designation, extraKeywords)
    }
    val bingUrl = remember(city, industry, designation, extraKeywords) {
        OutreachIntents.buildBingSearchUrl(city, industry, designation, extraKeywords)
    }
    val linkedInUrl = remember(city, industry, designation, extraKeywords) {
        OutreachIntents.buildLinkedInGoogleUrl(city, industry, designation, extraKeywords)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 980.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = strings.prospectSearchTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Surface(
                    color = AmberSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = AmberWarning)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.prospectSearchCompliance,
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberWarning
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = strings.searchVsVerifiedHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberWarning
                            )
                        }
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = strings.cityLabel, style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PRESET_CITIES.forEach { preset ->
                                FilterChip(
                                    selected = city.equals(preset, ignoreCase = true),
                                    onClick = { city = preset },
                                    label = { Text(preset) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text(strings.cityLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("search_city_input")
                        )

                        Text(text = strings.industryLabel, style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PRESET_INDUSTRIES.forEach { preset ->
                                FilterChip(
                                    selected = industry.equals(preset, ignoreCase = true),
                                    onClick = { industry = preset },
                                    label = { Text(preset) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = industry,
                            onValueChange = { industry = it },
                            label = { Text(strings.industryLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("search_industry_input")
                        )

                        Text(text = strings.designationLabel, style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PRESET_DESIGNATIONS.forEach { preset ->
                                FilterChip(
                                    selected = designation.equals(preset, ignoreCase = true),
                                    onClick = { designation = preset },
                                    label = { Text(preset) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = designation,
                            onValueChange = { designation = it },
                            label = { Text(strings.designationLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("search_designation_input")
                        )

                        OutlinedTextField(
                            value = extraKeywords,
                            onValueChange = { extraKeywords = it },
                            label = { Text(strings.keywordLabel) },
                            placeholder = { Text("e.g. DHA, Bahria Town, Blue Area") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("search_keywords_input")
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.searchLinksSectionTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = listOf(industry, designation, city, extraKeywords, "Pakistan")
                                    .filter { it.isNotBlank() }
                                    .joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Button(
                            onClick = { OutreachIntents.openUrl(context, googleUrl) },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("open_google_search_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = Gold400, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.openGoogleSearch)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { OutreachIntents.openUrl(context, bingUrl) },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("open_bing_search_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.openBingSearch)
                            }

                            OutlinedButton(
                                onClick = { OutreachIntents.openUrl(context, linkedInUrl) },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("open_linkedin_search_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.openLinkedInSearch)
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = { onCreateLeadFromSearch(city, industry, designation) },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_discovered_lead_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.saveDiscoveredLead)
                        }
                    }
                }
            }
        }
    }
}
