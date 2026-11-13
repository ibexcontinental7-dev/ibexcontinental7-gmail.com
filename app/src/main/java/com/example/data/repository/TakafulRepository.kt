package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.ContactChannel
import com.example.data.model.ContactHistoryEntry
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.ProductVerificationNote
import com.example.data.model.Prospect
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TakafulRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeLeads(userId: String): Flow<List<Lead>> = flow {
        val path = "users/$userId/leads"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("leads")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Lead::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun getLead(leadId: String): Result<Lead?> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("leads").document(leadId)
        return try {
            val snap = docRef.get().await()
            val lead = snap.toObject(
                Lead::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(lead)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun addLead(lead: Lead): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("leads").document()
        val payload = mapOf(
            "userId" to uid,
            "name" to lead.name.trim(),
            "company" to lead.company.trim(),
            "designation" to lead.designation.trim(),
            "city" to lead.city.trim().ifBlank { "Islamabad" },
            "industry" to lead.industry.trim(),
            "contactPhone" to lead.contactPhone.trim(),
            "email" to lead.email.trim(),
            "website" to lead.website.trim(),
            "linkedInUrl" to lead.linkedInUrl.trim(),
            "source" to lead.source.trim().ifBlank { "Public Search" },
            "notes" to lead.notes.trim(),
            "permissionStatus" to lead.permissionStatus,
            "status" to lead.status,
            "followUpDate" to lead.followUpDate.trim(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateLead(lead: Lead): Result<Unit> {
        val uid = requireUserId()
        require(lead.id.isNotBlank()) { "Lead ID must not be blank for update" }
        val docRef = db.collection("users").document(uid).collection("leads").document(lead.id)
        val payload = mapOf(
            "name" to lead.name.trim(),
            "company" to lead.company.trim(),
            "designation" to lead.designation.trim(),
            "city" to lead.city.trim().ifBlank { "Islamabad" },
            "industry" to lead.industry.trim(),
            "contactPhone" to lead.contactPhone.trim(),
            "email" to lead.email.trim(),
            "website" to lead.website.trim(),
            "linkedInUrl" to lead.linkedInUrl.trim(),
            "source" to lead.source.trim(),
            "notes" to lead.notes.trim(),
            "permissionStatus" to lead.permissionStatus,
            "status" to lead.status,
            "followUpDate" to lead.followUpDate.trim(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateLeadStatusAndFollowUp(
        leadId: String,
        status: LeadStatus,
        followUpDate: String
    ): Result<Unit> {
        val uid = requireUserId()
        require(leadId.isNotBlank()) { "Lead ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("leads").document(leadId)
        val payload = mapOf(
            "status" to status.name,
            "followUpDate" to followUpDate.trim(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteLead(leadId: String): Result<Unit> {
        val uid = requireUserId()
        require(leadId.isNotBlank()) { "Lead ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("leads").document(leadId)
        return try {
            // Delete child history entries first, then lead document
            val historySnap = docRef.collection("history").get().await()
            for (doc in historySnap.documents) {
                doc.reference.delete().await()
            }
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            Result.failure(e)
        }
    }

    fun observeHistory(userId: String, leadId: String): Flow<List<ContactHistoryEntry>> = flow {
        val path = "users/$userId/leads/$leadId/history"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("leads")
                .document(leadId)
                .collection("history")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ContactHistoryEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun addContactHistory(
        leadId: String,
        channel: ContactChannel,
        summary: String,
        outcome: String
    ): Result<String> {
        val uid = requireUserId()
        require(leadId.isNotBlank()) { "Lead ID must not be blank" }
        val docRef = db.collection("users")
            .document(uid)
            .collection("leads")
            .document(leadId)
            .collection("history")
            .document()

        val payload = mapOf(
            "userId" to uid,
            "leadId" to leadId,
            "channel" to channel.name,
            "summary" to summary.trim(),
            "outcome" to outcome.trim(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    fun observeProductNotes(userId: String): Flow<List<ProductVerificationNote>> = flow {
        val path = "users/$userId/productNotes"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("productNotes")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ProductVerificationNote::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun saveProductNote(note: ProductVerificationNote): Result<Unit> {
        val uid = requireUserId()
        require(note.productId.isNotBlank()) { "Product ID must not be blank" }
        val docRef = db.collection("users")
            .document(uid)
            .collection("productNotes")
            .document(note.productId)

        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updatePayload = mapOf(
                    "verifiedSummary" to note.verifiedSummary.trim(),
                    "verifiedEligibility" to note.verifiedEligibility.trim(),
                    "verifiedSourceDate" to note.verifiedSourceDate.trim(),
                    "isVerifiedByUser" to note.isVerifiedByUser,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "productId" to note.productId,
                    "verifiedSummary" to note.verifiedSummary.trim(),
                    "verifiedEligibility" to note.verifiedEligibility.trim(),
                    "verifiedSourceDate" to note.verifiedSourceDate.trim(),
                    "isVerifiedByUser" to note.isVerifiedByUser,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            Result.failure(e)
        }
    }

    fun observeProspects(userId: String): Flow<List<Prospect>> = flow {
        val path = "prospects"
        emitAll(
            db.collection("prospects")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Prospect::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun addProspect(prospect: Prospect): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("prospects").document()
        val payload = mapOf(
            "userId" to uid,
            "name" to prospect.name.trim(),
            "contact" to prospect.contact.trim(),
            "status" to prospect.status.trim().ifBlank { LeadStatus.NEW.labelEn },
            "notes" to prospect.notes.trim(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteProspect(prospectId: String): Result<Unit> {
        val uid = requireUserId()
        require(prospectId.isNotBlank()) { "Prospect ID must not be blank" }
        val docRef = db.collection("prospects").document(prospectId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            Result.failure(e)
        }
    }

    private fun logStreamErrorIfActive(
        error: Throwable,
        operationType: OperationType,
        path: String,
        expectedUid: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, operationType, path, auth)
        }
    }
}
