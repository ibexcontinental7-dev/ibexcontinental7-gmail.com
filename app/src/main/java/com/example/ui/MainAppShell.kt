package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.OfficialTakafulProduct
import com.example.data.repository.TakafulRepository
import com.example.ui.auth.signOut
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.UiStrings
import com.example.ui.screens.AddNewProspectScreen
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FollowUpsScreen
import com.example.ui.screens.LeadsScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.ProspectSearchScreen
import com.example.ui.theme.Gold400
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AiMessageViewModel
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore

enum class AppTab(val icon: ImageVector) {
    DASHBOARD(Icons.Filled.Dashboard),
    SEARCH(Icons.Filled.Search),
    LEADS(Icons.Filled.Groups),
    STUDIO(Icons.AutoMirrored.Filled.Send),
    PRODUCTS(Icons.Filled.VerifiedUser),
    FOLLOW_UPS(Icons.Filled.EventRepeat)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    currentUserId: String,
    strings: UiStrings,
    language: AppLanguage,
    onToggleLanguage: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val mainViewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainViewModel(TakafulRepository(db), currentUserId)
            }
        }
    )

    val aiViewModel: AiMessageViewModel = viewModel(key = currentUserId)

    val leadsState by mainViewModel.leadsState.collectAsStateWithLifecycle()
    val prospectsState by mainViewModel.prospectsState.collectAsStateWithLifecycle()
    val metrics by mainViewModel.metricsState.collectAsStateWithLifecycle()
    val productNotesState by mainViewModel.productNotesState.collectAsStateWithLifecycle()
    val monthlyTargetState by mainViewModel.monthlyTargetState.collectAsStateWithLifecycle()
    val actionMessage by mainViewModel.actionMessage.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showAddProspect by remember { mutableStateOf(false) }
    var leadsFilter by remember { mutableStateOf<LeadStatus?>(null) }
    var prefilledLeadDraft by remember { mutableStateOf<Lead?>(null) }
    var studioSelectedLead by remember { mutableStateOf<Lead?>(null) }
    var studioSelectedProduct by remember { mutableStateOf<OfficialTakafulProduct?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            mainViewModel.clearActionMessage()
        }
    }

    BackHandler(enabled = !showAddProspect && currentTab != AppTab.DASHBOARD) {
        currentTab = AppTab.DASHBOARD
    }

    fun tabLabel(tab: AppTab): String = when (tab) {
        AppTab.DASHBOARD -> strings.navDashboard
        AppTab.SEARCH -> strings.navSearch
        AppTab.LEADS -> strings.navLeads
        AppTab.STUDIO -> strings.navStudio
        AppTab.PRODUCTS -> strings.navProducts
        AppTab.FOLLOW_UPS -> strings.navFollowUps
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.appTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy900,
                        titleContentColor = PureWhite,
                        actionIconContentColor = PureWhite
                    ),
                    actions = {
                        if (!isWideScreen) {
                            IconButton(
                                onClick = {
                                    showAddProspect = false
                                    currentTab = AppTab.PRODUCTS
                                },
                                modifier = Modifier.testTag("topbar_products_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = strings.navProducts,
                                    tint = if (currentTab == AppTab.PRODUCTS) Gold400 else PureWhite
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = onToggleLanguage,
                            modifier = Modifier.testTag("language_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (language == AppLanguage.URDU) "EN" else "اردو")
                        }
                        IconButton(
                            onClick = { signOut(credentialManager, scope) },
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = strings.signOut
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    val compactTabs = listOf(
                        AppTab.DASHBOARD,
                        AppTab.SEARCH,
                        AppTab.LEADS,
                        AppTab.STUDIO,
                        AppTab.FOLLOW_UPS
                    )
                    NavigationBar {
                        compactTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = !showAddProspect && currentTab == tab,
                                onClick = {
                                    showAddProspect = false
                                    currentTab = tab
                                },
                                icon = { Icon(tab.icon, contentDescription = tabLabel(tab)) },
                                label = {
                                    Text(
                                        text = tabLabel(tab),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail {
                        AppTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = !showAddProspect && currentTab == tab,
                                onClick = {
                                    showAddProspect = false
                                    currentTab = tab
                                },
                                icon = { Icon(tab.icon, contentDescription = tabLabel(tab)) },
                                label = { Text(tabLabel(tab), maxLines = 1) },
                                modifier = Modifier.testTag("rail_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    if (showAddProspect) {
                        AddNewProspectScreen(
                            strings = strings,
                            language = language,
                            onNavigateBack = { showAddProspect = false },
                            onSaveProspect = { name, contact, status, notes, onResult ->
                                mainViewModel.saveProspect(name, contact, status, notes, onResult)
                            }
                        )
                    } else {
                    when (currentTab) {
                        AppTab.DASHBOARD -> DashboardScreen(
                            strings = strings,
                            language = language,
                            metrics = metrics,
                            leadsState = leadsState,
                            monthlyTargetState = monthlyTargetState,
                            onSaveMonthlyTarget = { sales, outreach, referrals, month ->
                                mainViewModel.saveMonthlyTarget(
                                    salesTarget = sales,
                                    outreachTarget = outreach,
                                    referralTarget = referrals,
                                    targetMonth = month
                                )
                            },
                            onNavigateToLeads = { filter ->
                                leadsFilter = filter
                                currentTab = AppTab.LEADS
                            },
                            onNavigateToSearch = { currentTab = AppTab.SEARCH },
                            onNavigateToStudio = { lead ->
                                studioSelectedLead = lead
                                currentTab = AppTab.STUDIO
                            },
                            onNavigateToFollowUps = { currentTab = AppTab.FOLLOW_UPS },
                            onNavigateToProducts = { currentTab = AppTab.PRODUCTS }
                        )

                        AppTab.SEARCH -> ProspectSearchScreen(
                            strings = strings,
                            onCreateLeadFromSearch = { city, industry, designation ->
                                prefilledLeadDraft = Lead(
                                    city = city,
                                    industry = industry,
                                    designation = designation,
                                    source = "Public Search"
                                )
                                currentTab = AppTab.LEADS
                            }
                        )

                        AppTab.LEADS -> LeadsScreen(
                            strings = strings,
                            language = language,
                            leadsState = leadsState,
                            prospectsState = prospectsState,
                            selectedStatusFilter = leadsFilter,
                            onSelectStatusFilter = { leadsFilter = it },
                            prefilledLeadDraft = prefilledLeadDraft,
                            onConsumePrefilledDraft = { prefilledLeadDraft = null },
                            onSaveLead = { mainViewModel.saveLead(it) },
                            onDeleteLead = { mainViewModel.deleteLead(it) },
                            onDeleteProspect = { mainViewModel.deleteProspect(it) },
                            onUpgradeProspectToLead = { prospectId, lead ->
                                mainViewModel.upgradeProspectToLead(prospectId, lead)
                            },
                            onOpenAddNewProspect = { showAddProspect = true },
                            onOpenAiStudio = { lead ->
                                studioSelectedLead = lead
                                currentTab = AppTab.STUDIO
                            },
                            onOpenFollowUps = { currentTab = AppTab.FOLLOW_UPS }
                        )

                        AppTab.STUDIO -> AiStudioScreen(
                            strings = strings,
                            language = language,
                            leadsState = leadsState,
                            preselectedLead = studioSelectedLead,
                            preselectedProduct = studioSelectedProduct,
                            onConsumePreselectedProduct = { studioSelectedProduct = null },
                            aiViewModel = aiViewModel,
                            onLogOutreach = { leadId, channel, summary ->
                                mainViewModel.logContactHistory(
                                    leadId = leadId,
                                    channel = channel,
                                    summary = summary,
                                    outcome = "Draft approved & opened via ${channel.labelEn}"
                                )
                            }
                        )

                        AppTab.PRODUCTS -> ProductsScreen(
                            strings = strings,
                            language = language,
                            notesState = productNotesState,
                            onSaveProductNote = { mainViewModel.saveProductNote(it) },
                            onOpenAiWithProduct = { product ->
                                studioSelectedProduct = product
                                currentTab = AppTab.STUDIO
                            }
                        )

                        AppTab.FOLLOW_UPS -> FollowUpsScreen(
                            strings = strings,
                            language = language,
                            leadsState = leadsState,
                            observeHistory = { leadId -> mainViewModel.observeLeadHistory(leadId) },
                            onUpdateStatusAndDate = { id, status, date ->
                                mainViewModel.updateLeadStatusAndFollowUp(id, status, date)
                            },
                            onAddContactLog = { id, channel, summary, outcome ->
                                mainViewModel.logContactHistory(id, channel, summary, outcome)
                            }
                        )
                    }
                    }
                }
            }
        }
    }
}
