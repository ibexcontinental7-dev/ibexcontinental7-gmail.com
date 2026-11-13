package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ContactChannel
import com.example.data.model.GmMonthlyTarget
import com.example.data.model.Lead
import com.example.data.model.LeadPermissionStatus
import com.example.data.model.LeadStatus
import com.example.data.model.ProductVerificationNote
import com.example.data.model.Prospect
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class TakafulRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addAndObserveLead_authenticatedOwner_succeedsAndMapsFields() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = TakafulRepository(firestore, auth)

        val uniqueName = "Tariq ${UUID.randomUUID().toString().take(6)}"
        val lead = Lead(
            name = uniqueName,
            company = "Capital Estates",
            designation = "CEO",
            city = "Islamabad",
            industry = "Property Developers",
            contactPhone = "+923001112233",
            permissionStatus = LeadPermissionStatus.VERIFIED_PUBLIC.name,
            status = LeadStatus.NEW.name,
            followUpDate = "2026-11-22"
        )

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.addLead(lead) }
        assertTrue("Expected addLead success: ${addResult.exceptionOrNull()}", addResult.isSuccess)
        val leadId = addResult.getOrThrow()
        assertTrue(leadId.isNotBlank())

        val leads = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeLeads(uid).first { list -> list.any { it.id == leadId } }
        }
        val saved = leads.first { it.id == leadId }
        assertEquals(uniqueName, saved.name)
        assertEquals("Islamabad", saved.city)
        assertNotNull(saved.createdAt)
    }

    @Test
    fun updateLeadAndAddHistory_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = TakafulRepository(firestore, auth)

        val leadId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addLead(
                Lead(
                    name = "Dr. Sara Khan",
                    company = "Shifa Clinic",
                    designation = "Medical Director",
                    city = "Islamabad",
                    industry = "Doctors"
                )
            ).getOrThrow()
        }

        val statusResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateLeadStatusAndFollowUp(leadId, LeadStatus.INTERESTED, "2026-11-25")
        }
        assertTrue(statusResult.isSuccess)

        val histResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addContactHistory(
                leadId = leadId,
                channel = ContactChannel.WHATSAPP,
                summary = "Approved Urdu intro sent manually.",
                outcome = "Requested brochure"
            )
        }
        assertTrue(histResult.isSuccess)

        val history = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeHistory(uid, leadId).first { it.isNotEmpty() }
        }
        assertEquals(ContactChannel.WHATSAPP.name, history.first().channel)
    }

    @Test
    fun saveProductNote_officialProduct_persistsVerifiedState() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = TakafulRepository(firestore, auth)

        val note = ProductVerificationNote(
            productId = "golden_path",
            verifiedSummary = "Verified official summary from askarilife.com.",
            verifiedEligibility = "Verified age band.",
            verifiedSourceDate = "2026-11-13",
            isVerifiedByUser = true
        )
        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveProductNote(note) }
        assertTrue("Expected saveProductNote success: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

        val notes = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeProductNotes(uid).first { list -> list.any { it.productId == "golden_path" } }
        }
        assertTrue(notes.first { it.productId == "golden_path" }.isVerifiedByUser)
    }

    @Test
    fun crossUserRead_differentUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = TakafulRepository(firestore, auth)
        val leadId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addLead(Lead(name = "Private Lead", city = "Islamabad")).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("leads").document(leadId)
                    .get().await()
            }
            fail("Expected PERMISSION_DENIED for cross-user read")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }
    }

    @Test
    fun fullEditAndDeleteLead_withHistory_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = TakafulRepository(firestore, auth)

        val leadId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addLead(
                Lead(
                    name = "Bilal Ahmed",
                    company = "Margalla Builders",
                    designation = "Director",
                    city = "Islamabad",
                    industry = "Construction Companies",
                    contactPhone = "+923005551234",
                    permissionStatus = LeadPermissionStatus.VERIFIED_PUBLIC.name,
                    status = LeadStatus.NEW.name,
                    followUpDate = "2026-11-20"
                )
            ).getOrThrow()
        }

        val histResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addContactHistory(
                leadId = leadId,
                channel = ContactChannel.PHONE,
                summary = "Initial reception call.",
                outcome = "Follow up with GM intro"
            )
        }
        assertTrue("Expected addContactHistory success: ${histResult.exceptionOrNull()}", histResult.isSuccess)

        val savedLead = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeLeads(uid).first { list -> list.any { it.id == leadId } }.first { it.id == leadId }
        }
        val editedLead = savedLead.copy(
            designation = "Managing Director",
            notes = "Requested Iqra & Golden Path overview.",
            permissionStatus = LeadPermissionStatus.CONSENT_OBTAINED.name,
            status = LeadStatus.INTERESTED.name,
            followUpDate = "2026-11-28"
        )
        val editResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.updateLead(editedLead) }
        assertTrue("Expected updateLead success: ${editResult.exceptionOrNull()}", editResult.isSuccess)

        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeLeads(uid).first { list ->
                list.any { it.id == leadId && it.designation == "Managing Director" }
            }.first { it.id == leadId }
        }
        assertEquals(LeadStatus.INTERESTED.name, updated.status)
        assertEquals(LeadPermissionStatus.CONSENT_OBTAINED.name, updated.permissionStatus)

        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.deleteLead(leadId) }
        assertTrue("Expected deleteLead success: ${deleteResult.exceptionOrNull()}", deleteResult.isSuccess)

        val remainingLeads = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeLeads(uid).first { list -> list.none { it.id == leadId } }
        }
        assertTrue(remainingLeads.none { it.id == leadId })
    }

    @Test
    fun observeLeads_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = TakafulRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeLeads("some_uid_123").first()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
    }
    }

    @Test
    fun addObserveAndDeleteProspect_enforcesOwnerIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = TakafulRepository(firestore, auth)

        val uniqueName = "Usman ${UUID.randomUUID().toString().take(6)}"
        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addProspect(
                Prospect(
                    name = uniqueName,
                    contact = "+923009876543",
                    status = "Interested",
                    notes = "Met in Islamabad Blue Area"
                )
            )
        }
        assertTrue("Expected addProspect success: ${addResult.exceptionOrNull()}", addResult.isSuccess)
        val prospectId = addResult.getOrThrow()

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeProspects(aliceUid).first { list -> list.any { it.id == prospectId } }
        }
        val saved = observed.first { it.id == prospectId }
        assertEquals(uniqueName, saved.name)
        assertEquals("+923009876543", saved.contact)
        assertEquals("Interested", saved.status)
        assertNotNull(saved.createdAt)

        // Cross-user read on prospects fails
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("prospects").document(prospectId).get().await()
            }
            fail("Expected PERMISSION_DENIED for cross-user prospect read")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }

        // Unauthenticated observeProspects fails
        auth.signOut()
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                aliceRepo.observeProspects(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated prospect list")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }

        // Owner delete succeeds
        signInTestUser(ALICE_EMAIL)
        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.deleteProspect(prospectId) }
        assertTrue("Expected deleteProspect success: ${deleteResult.exceptionOrNull()}", deleteResult.isSuccess)
    }

    @Test
    fun observeAndSaveMonthlyTarget_enforcesOwnerIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = TakafulRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveMonthlyTarget(
                GmMonthlyTarget(
                    userId = aliceUid,
                    targetMonth = "2026-11",
                    salesTarget = 8L,
                    outreachTarget = 40L,
                    referralTarget = 12L
                )
            )
        }
        assertTrue("Expected initial saveMonthlyTarget success: ${createResult.exceptionOrNull()}", createResult.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeMonthlyTarget(aliceUid, "2026-11").first { it.salesTarget == 8L }
        }
        assertEquals(40L, observed.outreachTarget)
        assertEquals(12L, observed.referralTarget)

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveMonthlyTarget(observed.copy(salesTarget = 10L))
        }
        assertTrue("Expected update saveMonthlyTarget success: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeMonthlyTarget(aliceUid, "2026-11").first { it.salesTarget == 10L }
        }
        assertEquals(10L, updated.salesTarget)

        // Cross-user read on gmSettings/monthly_targets fails with PERMISSION_DENIED
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("gmSettings").document("monthly_targets")
                    .get().await()
            }
            fail("Expected PERMISSION_DENIED for cross-user monthly_targets read")
        } catch (e: Exception) {
            val fsException = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice.gm@askari-test.com"
        const val BOB_EMAIL = "bob.gm@askari-test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
