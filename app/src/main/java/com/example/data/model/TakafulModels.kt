package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class LeadStatus(val labelEn: String, val labelUr: String) {
    NEW("New Prospect", "نیا ممکنہ کلائنٹ"),
    CONTACTED("Contacted", "رابطہ ہو چکا"),
    INTERESTED("Interested", "دلچسپی رکھتے ہیں"),
    FOLLOW_UP("Follow-up Scheduled", "فالو اپ مقرر"),
    COMPLETED("Completed Sale", "مکمل فروخت"),
    ARCHIVED("Archived", "محفوظ شدہ");

    companion object {
        fun fromString(value: String): LeadStatus =
            entries.firstOrNull { it.name == value } ?: NEW
    }
}

enum class LeadPermissionStatus(val labelEn: String, val labelUr: String) {
    VERIFIED_PUBLIC("Verified Public Business Info", "تصدیق شدہ عوامی کاروباری معلومات"),
    CONSENT_OBTAINED("Direct Consent Obtained", "براہِ راست اجازت حاصل"),
    REFERRAL_INTRO("Warm Referral Intro", "ریفرل تعارف"),
    DO_NOT_CONTACT("Do Not Contact", "رابطہ نہ کریں");

    companion object {
        fun fromString(value: String): LeadPermissionStatus =
            entries.firstOrNull { it.name == value } ?: VERIFIED_PUBLIC
    }
}

enum class ContactChannel(val labelEn: String, val labelUr: String) {
    WHATSAPP("WhatsApp", "واٹس ایپ"),
    PHONE("Phone Call", "فون کال"),
    EMAIL("Email", "ای میل"),
    MEETING("Meeting", "ملاقات"),
    OTHER("Other", "دیگر");

    companion object {
        fun fromString(value: String): ContactChannel =
            entries.firstOrNull { it.name == value } ?: WHATSAPP
    }
}

@IgnoreExtraProperties
data class Lead(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val company: String = "",
    val designation: String = "",
    val city: String = "Islamabad",
    val industry: String = "",
    val contactPhone: String = "",
    val email: String = "",
    val website: String = "",
    val linkedInUrl: String = "",
    val source: String = "Public Search",
    val notes: String = "",
    val permissionStatus: String = LeadPermissionStatus.VERIFIED_PUBLIC.name,
    val status: String = LeadStatus.NEW.name,
    val followUpDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Prospect(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val contact: String = "",
    val status: String = LeadStatus.NEW.labelEn,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ContactHistoryEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val leadId: String = "",
    val channel: String = ContactChannel.WHATSAPP.name,
    val summary: String = "",
    val outcome: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProductVerificationNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val verifiedSummary: String = "",
    val verifiedEligibility: String = "",
    val verifiedSourceDate: String = "",
    @get:PropertyName("isVerifiedByUser")
    @set:PropertyName("isVerifiedByUser")
    var isVerifiedByUser: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class OfficialTakafulProduct(
    val id: String,
    val nameEn: String,
    val nameUr: String,
    val officialPortalUrl: String,
    val unverifiedNoticeEn: String,
    val unverifiedNoticeUr: String
)

object OwnerProfile {
    const val DESIGNATION_EN = "General Manager"
    const val DESIGNATION_UR = "جنرل منیجر"
    const val COMPANY_EN = "Askari Family Takaful"
    const val COMPANY_UR = "عسکری فیملی تکافل"
    const val WHATSAPP_NUMBER = "+923160894696"
    const val DEFAULT_MARKET_CITY = "Islamabad"
    const val OFFICIAL_TAKAFUL_URL = "https://askarilife.com/individual-family-takaful/"
}

object OfficialProductCatalog {
    val products: List<OfficialTakafulProduct> = listOf(
        OfficialTakafulProduct(
            id = "golden_path",
            nameEn = "Golden Path",
            nameUr = "گولڈن پاتھ",
            officialPortalUrl = OwnerProfile.OFFICIAL_TAKAFUL_URL,
            unverifiedNoticeEn = "Official Askari Family Takaful plan name listed. Specific benefits, contribution limits, returns, exclusions, and terms are NOT verified in-app—confirm directly on askarilife.com before quoting.",
            unverifiedNoticeUr = "عسکری فیملی تکافل کا سرکاری پلان درج ہے۔ مخصوص فوائد، ادائیگی کی رقم، منافع، استثنیٰ اور شرائط ایپ میں تصدیق شدہ نہیں ہیں—کسی کلائنٹ کو تفصیل دینے سے پہلے askarilife.com پر تصدیق کریں۔"
        ),
        OfficialTakafulProduct(
            id = "muhafiz",
            nameEn = "Muhafiz",
            nameUr = "محافظ",
            officialPortalUrl = OwnerProfile.OFFICIAL_TAKAFUL_URL,
            unverifiedNoticeEn = "Official Askari Family Takaful plan name listed. Specific benefits, contribution limits, returns, exclusions, and terms are NOT verified in-app—confirm directly on askarilife.com before quoting.",
            unverifiedNoticeUr = "عسکری فیملی تکافل کا سرکاری پلان درج ہے۔ مخصوص فوائد، ادائیگی کی رقم، منافع، استثنیٰ اور شرائط ایپ میں تصدیق شدہ نہیں ہیں—کسی کلائنٹ کو تفصیل دینے سے پہلے askarilife.com پر تصدیق کریں۔"
        ),
        OfficialTakafulProduct(
            id = "iqra",
            nameEn = "Iqra",
            nameUr = "اقراء",
            officialPortalUrl = OwnerProfile.OFFICIAL_TAKAFUL_URL,
            unverifiedNoticeEn = "Official Askari Family Takaful plan name listed. Specific benefits, contribution limits, returns, exclusions, and terms are NOT verified in-app—confirm directly on askarilife.com before quoting.",
            unverifiedNoticeUr = "عسکری فیملی تکافل کا سرکاری پلان درج ہے۔ مخصوص فوائد، ادائیگی کی رقم، منافع، استثنیٰ اور شرائط ایپ میں تصدیق شدہ نہیں ہیں—کسی کلائنٹ کو تفصیل دینے سے پہلے askarilife.com پر تصدیق کریں۔"
        ),
        OfficialTakafulProduct(
            id = "humrahi",
            nameEn = "Humrahi",
            nameUr = "ہمراہی",
            officialPortalUrl = OwnerProfile.OFFICIAL_TAKAFUL_URL,
            unverifiedNoticeEn = "Official Askari Family Takaful plan name listed. Specific benefits, contribution limits, returns, exclusions, and terms are NOT verified in-app—confirm directly on askarilife.com before quoting.",
            unverifiedNoticeUr = "عسکری فیملی تکافل کا سرکاری پلان درج ہے۔ مخصوص فوائد، ادائیگی کی رقم، منافع، استثنیٰ اور شرائط ایپ میں تصدیق شدہ نہیں ہیں—کسی کلائنٹ کو تفصیل دینے سے پہلے askarilife.com پر تصدیق کریں۔"
        ),
        OfficialTakafulProduct(
            id = "pursukoon_kal",
            nameEn = "PurSukoon Kal",
            nameUr = "پُرسکون کل",
            officialPortalUrl = OwnerProfile.OFFICIAL_TAKAFUL_URL,
            unverifiedNoticeEn = "Official Askari Family Takaful plan name listed. Specific benefits, contribution limits, returns, exclusions, and terms are NOT verified in-app—confirm directly on askarilife.com before quoting.",
            unverifiedNoticeUr = "عسکری فیملی تکافل کا سرکاری پلان درج ہے۔ مخصوص فوائد، ادائیگی کی رقم، منافع، استثنیٰ اور شرائط ایپ میں تصدیق شدہ نہیں ہیں—کسی کلائنٹ کو تفصیل دینے سے پہلے askarilife.com پر تصدیق کریں۔"
        ),
        OfficialTakafulProduct(
            id = "sarbuland",
            nameEn = "Sarbuland",
            nameUr = "سربلند",
            officialPortalUrl = OwnerProfile.OFFICIAL_TAKAFUL_URL,
            unverifiedNoticeEn = "Official Askari Family Takaful plan name listed. Specific benefits, contribution limits, returns, exclusions, and terms are NOT verified in-app—confirm directly on askarilife.com before quoting.",
            unverifiedNoticeUr = "عسکری فیملی تکافل کا سرکاری پلان درج ہے۔ مخصوص فوائد، ادائیگی کی رقم، منافع، استثنیٰ اور شرائط ایپ میں تصدیق شدہ نہیں ہیں—کسی کلائنٹ کو تفصیل دینے سے پہلے askarilife.com پر تصدیق کریں۔"
        ),
        OfficialTakafulProduct(
            id = "kanz_ul_askari",
            nameEn = "Kanz ul Askari",
            nameUr = "کنز العسکری",
            officialPortalUrl = OwnerProfile.OFFICIAL_TAKAFUL_URL,
            unverifiedNoticeEn = "Official Askari Family Takaful plan name listed. Specific benefits, contribution limits, returns, exclusions, and terms are NOT verified in-app—confirm directly on askarilife.com before quoting.",
            unverifiedNoticeUr = "عسکری فیملی تکافل کا سرکاری پلان درج ہے۔ مخصوص فوائد، ادائیگی کی رقم، منافع، استثنیٰ اور شرائط ایپ میں تصدیق شدہ نہیں ہیں—کسی کلائنٹ کو تفصیل دینے سے پہلے askarilife.com پر تصدیق کریں۔"
        )
    )
}
