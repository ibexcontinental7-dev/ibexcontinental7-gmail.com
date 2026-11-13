package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Lead
import com.example.data.model.OfficialTakafulProduct
import com.example.data.model.OwnerProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

enum class StudioMode(val labelEn: String, val labelUr: String) {
    OUTREACH_DRAFT("Outreach Draft", "تعارفی پیغام"),
    OBJECTION_HANDLER("Objection & Meeting Prep", "اعتراضات و میٹنگ تیاری")
}

enum class ObjectionPreset(val labelEn: String, val labelUr: String, val promptTopic: String) {
    TRUST_SHARIAH(
        "Shariah / Halal Trust",
        "شرعی و حلال تصدیق",
        "Client asks whether Family Takaful is genuinely Shariah-compliant and different from conventional insurance."
    ),
    EXISTING_SAVINGS(
        "Already Have Bank / Insurance",
        "پہلے سے بینک یا بیمہ موجود ہے",
        "Client says they already have bank savings, real estate plots, or conventional insurance and don't see why they need Takaful."
    ),
    INFLATION_COST(
        "Inflation / Monthly Budget",
        "مہنگائی اور ماہانہ بجٹ",
        "Client worries that committing to a long-term plan is tough during high inflation in Pakistan."
    ),
    LIQUIDITY_CONCERN(
        "Need Cash / Flexibility",
        "جلد رقم کی ضرورت یا لچک",
        "Client asks about early access / liquidity concerns without committing to unverified figures."
    ),
    MEETING_PREP_AGENDA(
        "15-Min Meeting Agenda",
        "15 منٹ میٹنگ تیاری",
        "Prepare a crisp 15-minute face-to-face GM executive meeting talking points & discovery questions for this prospect."
    )
}

enum class MessageFormat(val labelEn: String, val labelUr: String) {
    WHATSAPP_SHORT("Short WhatsApp Message", "مختصر واٹس ایپ پیغام"),
    PROFESSIONAL_INTRO("Professional Introduction", "پروفیشنل تعارف"),
    MEETING_REQUEST("Meeting Request", "ملاقات کی درخواست"),
    EMAIL_DRAFT("Email Draft", "ای میل ڈرافٹ")
}

enum class MessageLanguage(val labelEn: String, val labelUr: String) {
    URDU("Urdu (اردو)", "اردو"),
    ENGLISH("English", "انگریزی"),
    BILINGUAL("Bilingual (Urdu + English)", "دو لسانی (اردو + انگریزی)")
}

sealed interface AiDraftState {
    data object Idle : AiDraftState
    data object Loading : AiDraftState
    data class Streaming(val text: String) : AiDraftState
    data class Success(val subject: String, val body: String) : AiDraftState
    data class Error(val message: String) : AiDraftState
}

class AiMessageViewModel : ViewModel() {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are an executive sales communications assistant for a General Manager at Askari Family Takaful in Pakistan (Islamabad market).
                    STRICT INTEGRITY RULES:
                    1. Always include the exact sender signature/introduction:
                       - In English: General Manager, Askari Family Takaful, WhatsApp: +923160894696
                       - In Urdu: جنرل منیجر، عسکری فیملی تکافل، واٹس ایپ: +923160894696
                    2. NEVER invent product benefits, return percentages, profit rates, contribution amounts, policy terms, exclusions, or guarantees.
                    3. Speak respectfully and professionally in Pakistani business Urdu or polished corporate English as requested.
                    4. Keep messages ready for human review (do not claim any message has been sent).
                    """.trimIndent()
                )
            }
        )
    }

    private val _draftState = MutableStateFlow<AiDraftState>(AiDraftState.Idle)
    val draftState: StateFlow<AiDraftState> = _draftState.asStateFlow()

    private val _editableSubject = MutableStateFlow("")
    val editableSubject: StateFlow<String> = _editableSubject.asStateFlow()

    private val _editableBody = MutableStateFlow("")
    val editableBody: StateFlow<String> = _editableBody.asStateFlow()

    private val _isApproved = MutableStateFlow(false)
    val isApproved: StateFlow<Boolean> = _isApproved.asStateFlow()

    fun updateSubject(value: String) {
        _editableSubject.value = value
        _isApproved.value = false
    }

    fun updateBody(value: String) {
        _editableBody.value = value
        _isApproved.value = false
    }

    fun setApproved(approved: Boolean) {
        _isApproved.value = approved
    }

    fun generateDraft(
        lead: Lead?,
        customName: String,
        customCompany: String,
        customDesignation: String,
        customIndustry: String,
        customCity: String,
        format: MessageFormat,
        language: MessageLanguage,
        product: OfficialTakafulProduct?,
        extraContext: String
    ) {
        val targetName = lead?.name?.takeIf { it.isNotBlank() } ?: customName.trim().ifBlank { "Respected Client" }
        val targetCompany = lead?.company?.takeIf { it.isNotBlank() } ?: customCompany.trim()
        val targetDesignation = lead?.designation?.takeIf { it.isNotBlank() } ?: customDesignation.trim()
        val targetIndustry = lead?.industry?.takeIf { it.isNotBlank() } ?: customIndustry.trim()
        val targetCity = lead?.city?.takeIf { it.isNotBlank() } ?: customCity.trim().ifBlank { OwnerProfile.DEFAULT_MARKET_CITY }

        val productLine = if (product != null) {
            "Optional Product Mention: ${product.nameEn} (${product.nameUr}). Do NOT invent specific financial returns, contribution amounts, or guarantees—only mention it by name as an official Askari Family Takaful plan and invite a consultation."
        } else {
            "Focus on Shariah-compliant family and executive financial protection with Askari Family Takaful."
        }

        val formatInstruction = when (format) {
            MessageFormat.WHATSAPP_SHORT -> "Write a concise, respectful WhatsApp message (around 60-90 words) suitable for busy executives."
            MessageFormat.PROFESSIONAL_INTRO -> "Write a dignified executive introduction message highlighting trust, Shariah-compliant family takaful protection, and advisory value."
            MessageFormat.MEETING_REQUEST -> "Write a polite request for a brief 15-minute introductory call or office meeting in $targetCity."
            MessageFormat.EMAIL_DRAFT -> "Write a formal corporate email. Start the very first line with 'Subject: <clear subject line>' followed by a blank line and the email body."
        }

        val languageInstruction = when (language) {
            MessageLanguage.URDU -> "Write entirely in polite, natural Pakistani business Urdu script (اردو), keeping the WhatsApp number +923160894696 clear."
            MessageLanguage.ENGLISH -> "Write in polished, concise Pakistani corporate English."
            MessageLanguage.BILINGUAL -> "Provide a crisp Urdu (اردو) version first, followed by a divider '---', and then a concise English version."
        }

        val prompt = """
            Draft a personalized B2B / executive outreach message for:
            - Prospect Name: $targetName
            - Company / Organization: ${targetCompany.ifBlank { "Not specified" }}
            - Designation: ${targetDesignation.ifBlank { "Decision Maker" }}
            - Industry / Sector: ${targetIndustry.ifBlank { "Corporate / Business" }}
            - City: $targetCity
            - Extra Context: ${extraContext.trim().ifBlank { "Initial executive outreach" }}

            Format Requirement: $formatInstruction
            Language Requirement: $languageInstruction
            Product Guidance: $productLine

            Mandatory Sender Signature:
            ${OwnerProfile.DESIGNATION_EN} (${OwnerProfile.DESIGNATION_UR})
            ${OwnerProfile.COMPANY_EN} (${OwnerProfile.COMPANY_UR})
            WhatsApp: ${OwnerProfile.WHATSAPP_NUMBER}
        """.trimIndent()

        viewModelScope.launch {
            _isApproved.value = false
            _draftState.value = AiDraftState.Loading
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    model.generateContentStream(prompt).collect { chunk ->
                        val part = chunk.text.orEmpty()
                        accumulated.append(part)
                        val currentText = accumulated.toString()
                        _draftState.value = AiDraftState.Streaming(currentText)
                        _editableBody.value = currentText
                    }
                }
                val finalText = accumulated.toString().trim()
                if (finalText.isBlank()) {
                    _draftState.value = AiDraftState.Error("Gemini returned an empty draft. Please try again.")
                    return@launch
                }

                val (parsedSubject, parsedBody) = parseEmailSubjectIfPresent(finalText, format, targetCompany)
                _editableSubject.value = parsedSubject
                _editableBody.value = parsedBody
                _draftState.value = AiDraftState.Success(parsedSubject, parsedBody)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini generation timed out", e)
                _draftState.value = AiDraftState.Error("AI generation timed out after 60 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI generation failed", e)
                _draftState.value = AiDraftState.Error(e.localizedMessage ?: "Failed to generate message with Gemini AI.")
            }
        }
    }

    fun generateObjectionResponse(
        lead: Lead?,
        customName: String,
        customCompany: String,
        customDesignation: String,
        customIndustry: String,
        customCity: String,
        preset: ObjectionPreset,
        customObjection: String,
        language: MessageLanguage,
        product: OfficialTakafulProduct?
    ) {
        val targetName = lead?.name?.takeIf { it.isNotBlank() } ?: customName.trim().ifBlank { "Respected Client" }
        val targetCompany = lead?.company?.takeIf { it.isNotBlank() } ?: customCompany.trim()
        val targetDesignation = lead?.designation?.takeIf { it.isNotBlank() } ?: customDesignation.trim()
        val targetIndustry = lead?.industry?.takeIf { it.isNotBlank() } ?: customIndustry.trim()
        val targetCity = lead?.city?.takeIf { it.isNotBlank() } ?: customCity.trim().ifBlank { OwnerProfile.DEFAULT_MARKET_CITY }

        val productLine = if (product != null) {
            "Referenced Official Plan: ${product.nameEn} (${product.nameUr}). Do NOT invent profit rates, guaranteed returns, contribution tables, or policy exclusions—advise verifying official brochure terms at ${OwnerProfile.OFFICIAL_TAKAFUL_URL}."
        } else {
            "Keep product discussion at general Askari Family Takaful advisory level without inventing unverified returns or terms."
        }

        val languageInstruction = when (language) {
            MessageLanguage.URDU -> "Write in dignified, persuasive Pakistani business Urdu script (اردو)."
            MessageLanguage.ENGLISH -> "Write in polished, empathetic Pakistani corporate English."
            MessageLanguage.BILINGUAL -> "Provide crisp Urdu (اردو) talking points & WhatsApp reply first, followed by '---', and then concise English."
        }

        val prompt = """
            Prepare a respectful GM executive response and talking points for a client objection / meeting prep:
            - Prospect: $targetName (${targetDesignation.ifBlank { "Decision Maker" }})
            - Organization: ${targetCompany.ifBlank { "Not specified" }} (${targetIndustry.ifBlank { "Business" }}, $targetCity)
            - Objection / Prep Scenario: ${preset.labelEn} — ${preset.promptTopic}
            - Specific Client Question / Notes: ${customObjection.trim().ifBlank { "Use the standard executive objection scenario above." }}

            Requirements:
            1. Start with 2-3 brief verbal talking points for the GM to use on a phone call or in a meeting.
            2. Follow with a ready-to-send polite follow-up reply (WhatsApp-friendly) that includes the mandatory GM signature.
            3. STRICTLY DO NOT fabricate profit percentages, return figures, surrender values, or guaranteed payouts.
            4. Language: $languageInstruction
            5. Product Guidance: $productLine

            Mandatory Sender Signature:
            ${OwnerProfile.DESIGNATION_EN} (${OwnerProfile.DESIGNATION_UR})
            ${OwnerProfile.COMPANY_EN} (${OwnerProfile.COMPANY_UR})
            WhatsApp: ${OwnerProfile.WHATSAPP_NUMBER}
        """.trimIndent()

        viewModelScope.launch {
            _isApproved.value = false
            _draftState.value = AiDraftState.Loading
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    model.generateContentStream(prompt).collect { chunk ->
                        val part = chunk.text.orEmpty()
                        accumulated.append(part)
                        val currentText = accumulated.toString()
                        _draftState.value = AiDraftState.Streaming(currentText)
                        _editableBody.value = currentText
                    }
                }
                val finalText = accumulated.toString().trim()
                if (finalText.isBlank()) {
                    _draftState.value = AiDraftState.Error("Gemini returned an empty response. Please try again.")
                    return@launch
                }
                _editableSubject.value = ""
                _editableBody.value = finalText
                _draftState.value = AiDraftState.Success("", finalText)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini objection prep timed out", e)
                _draftState.value = AiDraftState.Error("AI generation timed out after 60 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI objection prep failed", e)
                _draftState.value = AiDraftState.Error(e.localizedMessage ?: "Failed to generate objection response.")
            }
        }
    }

    private fun parseEmailSubjectIfPresent(
        text: String,
        format: MessageFormat,
        company: String
    ): Pair<String, String> {
        val lines = text.lines()
        val firstLine = lines.firstOrNull()?.trim().orEmpty()
        if (firstLine.startsWith("Subject:", ignoreCase = true)) {
            val subject = firstLine.substringAfter(":").trim()
            val body = lines.drop(1).dropWhile { it.isBlank() }.joinToString("\n").trim()
            return subject to body
        }
        val defaultSubject = if (format == MessageFormat.EMAIL_DRAFT) {
            if (company.isNotBlank()) "Askari Family Takaful — Executive Introduction for $company"
            else "Askari Family Takaful — Executive Consultation"
        } else ""
        return defaultSubject to text
    }

    private companion object {
        const val TAG = "AiMessageViewModel"
    }
}
