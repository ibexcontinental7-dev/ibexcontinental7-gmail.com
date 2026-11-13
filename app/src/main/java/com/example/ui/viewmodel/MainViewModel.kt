package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactChannel
import com.example.data.model.ContactHistoryEntry
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.ProductVerificationNote
import com.example.data.model.Prospect
import com.example.data.repository.TakafulRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DashboardMetrics(
    val totalLeads: Int = 0,
    val newProspects: Int = 0,
    val contactedClients: Int = 0,
    val interestedClients: Int = 0,
    val followUpsDue: Int = 0,
    val completedSales: Int = 0
)

class MainViewModel(
    private val repository: TakafulRepository,
    private val currentUserId: String
) : ViewModel() {

    val leadsState: StateFlow<UiState<List<Lead>>> = repository.observeLeads(currentUserId)
        .map<List<Lead>, UiState<List<Lead>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing leads", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load leads"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val metricsState: StateFlow<DashboardMetrics> = leadsState
        .map { state ->
            if (state is UiState.Success) computeMetrics(state.data) else DashboardMetrics()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DashboardMetrics()
        )

    val productNotesState: StateFlow<UiState<Map<String, ProductVerificationNote>>> =
        repository.observeProductNotes(currentUserId)
            .map<List<ProductVerificationNote>, UiState<Map<String, ProductVerificationNote>>> { list ->
                UiState.Success(list.associateBy { it.productId })
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing product notes", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load product notes"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val prospectsState: StateFlow<UiState<List<Prospect>>> = repository.observeProspects(currentUserId)
        .map<List<Prospect>, UiState<List<Prospect>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing prospects", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load prospects"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun observeLeadHistory(leadId: String): Flow<List<ContactHistoryEntry>> {
        return repository.observeHistory(currentUserId, leadId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing history for $leadId", firestoreError ?: error)
                emit(emptyList())
            }
    }

    fun saveLead(lead: Lead, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = if (lead.id.isBlank()) {
                repository.addLead(lead)
            } else {
                repository.updateLead(lead).map { lead.id }
            }
            result.onSuccess {
                _actionMessage.value = "Lead saved."
                onComplete(true)
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Unable to save lead."
                onComplete(false)
            }
        }
    }

    fun updateLeadStatusAndFollowUp(
        leadId: String,
        status: LeadStatus,
        followUpDate: String
    ) {
        viewModelScope.launch {
            repository.updateLeadStatusAndFollowUp(leadId, status, followUpDate)
                .onSuccess { _actionMessage.value = "Follow-up updated." }
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Update failed." }
        }
    }

    fun deleteLead(leadId: String) {
        viewModelScope.launch {
            repository.deleteLead(leadId)
                .onSuccess { _actionMessage.value = "Lead deleted." }
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Delete failed." }
        }
    }

    fun logContactHistory(
        leadId: String,
        channel: ContactChannel,
        summary: String,
        outcome: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addContactHistory(leadId, channel, summary, outcome)
                .onSuccess {
                    _actionMessage.value = "Contact logged."
                    onDone()
                }
                .onFailure {
                    _actionMessage.value = it.localizedMessage ?: "Failed to log contact."
                }
        }
    }

    fun saveProductNote(note: ProductVerificationNote, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveProductNote(note)
                .onSuccess {
                    _actionMessage.value = "Official product notes saved."
                    onDone()
                }
                .onFailure {
                    _actionMessage.value = it.localizedMessage ?: "Failed to save product notes."
                }
        }
    }

    fun saveProspect(
        name: String,
        contact: String,
        status: String,
        notes: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addProspect(
                Prospect(
                    name = name,
                    contact = contact,
                    status = status,
                    notes = notes
                )
            )
            result.onSuccess {
                _actionMessage.value = "Prospect saved to Firestore 'prospects'."
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Unable to save prospect."
            }
            onResult(result)
        }
    }

    fun deleteProspect(prospectId: String) {
        viewModelScope.launch {
            repository.deleteProspect(prospectId)
                .onSuccess { _actionMessage.value = "Prospect deleted." }
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Delete failed." }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun todayIsoDate(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        fun isFollowUpDue(lead: Lead, todayIso: String = todayIsoDate()): Boolean {
            if (lead.status == LeadStatus.COMPLETED.name || lead.status == LeadStatus.ARCHIVED.name) {
                return false
            }
            val dueByDate = lead.followUpDate.isNotBlank() && lead.followUpDate <= todayIso
            val dueByStatus = lead.status == LeadStatus.FOLLOW_UP.name
            return dueByDate || dueByStatus
        }

        fun computeMetrics(leads: List<Lead>): DashboardMetrics {
            val today = todayIsoDate()
            return DashboardMetrics(
                totalLeads = leads.size,
                newProspects = leads.count { it.status == LeadStatus.NEW.name },
                contactedClients = leads.count { it.status == LeadStatus.CONTACTED.name },
                interestedClients = leads.count { it.status == LeadStatus.INTERESTED.name },
                followUpsDue = leads.count { isFollowUpDue(it, today) },
                completedSales = leads.count { it.status == LeadStatus.COMPLETED.name }
            )
        }

        fun filterLeads(
            leads: List<Lead>,
            query: String,
            selectedStatus: LeadStatus?,
            cityFilter: String = "ALL"
        ): List<Lead> {
            val q = query.trim()
            return leads.filter { lead ->
                val statusEnum = LeadStatus.fromString(lead.status)
                val matchesStatus = selectedStatus == null || lead.status == selectedStatus.name
                val matchesCity = cityFilter == "ALL" || lead.city.equals(cityFilter, ignoreCase = true)
                val matchesQuery = q.isBlank() ||
                    lead.name.contains(q, ignoreCase = true) ||
                    lead.status.contains(q, ignoreCase = true) ||
                    statusEnum.labelEn.contains(q, ignoreCase = true) ||
                    statusEnum.labelUr.contains(q, ignoreCase = true) ||
                    lead.company.contains(q, ignoreCase = true) ||
                    lead.designation.contains(q, ignoreCase = true) ||
                    lead.city.contains(q, ignoreCase = true) ||
                    lead.industry.contains(q, ignoreCase = true)
                matchesStatus && matchesCity && matchesQuery
            }
        }

        fun filterProspects(
            prospects: List<Prospect>,
            query: String,
            selectedStatus: LeadStatus?
        ): List<Prospect> {
            val q = query.trim()
            return prospects.filter { prospect ->
                val matchesChip = selectedStatus == null ||
                    prospect.status.equals(selectedStatus.name, ignoreCase = true) ||
                    prospect.status.equals(selectedStatus.labelEn, ignoreCase = true) ||
                    prospect.status.equals(selectedStatus.labelUr, ignoreCase = true)
                val matchesQuery = q.isBlank() ||
                    prospect.name.contains(q, ignoreCase = true) ||
                    prospect.status.contains(q, ignoreCase = true) ||
                    prospect.contact.contains(q, ignoreCase = true) ||
                    prospect.notes.contains(q, ignoreCase = true)
                matchesChip && matchesQuery
            }
        }
    }
}
