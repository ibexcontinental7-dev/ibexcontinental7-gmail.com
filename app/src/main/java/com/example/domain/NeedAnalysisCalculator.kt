package com.example.domain

import com.example.data.model.OfficialProductCatalog
import com.example.data.model.OfficialTakafulProduct

enum class NeedGoal(val labelEn: String, val labelUr: String) {
    FAMILY_PROTECTION("Family Protection", "خاندانی مالی تحفظ"),
    CHILD_EDUCATION("Child Education", "بچوں کی تعلیم"),
    CHILD_MARRIAGE("Marriage Planning", "بچوں کی شادی"),
    RETIREMENT("Retirement Reserve", "ریٹائرمنٹ اور پُرسکون کل"),
    LONG_TERM_WEALTH("Executive Wealth Reserve", "طویل مدتی بچت اور اثاثہ")
}

data class NeedAnalysisInput(
    val monthlySavingsPkr: Long,
    val horizonYears: Int,
    val dependentsCount: Int,
    val goal: NeedGoal
)

data class NeedAnalysisResult(
    val primaryProduct: OfficialTakafulProduct,
    val secondaryProduct: OfficialTakafulProduct,
    val annualContributionPkr: Long,
    val cumulativeHorizonContributionPkr: Long,
    val planningProtectionBenchmarkPkr: Long,
    val rationaleEn: String,
    val rationaleUr: String
)

object NeedAnalysisCalculator {

    fun calculate(input: NeedAnalysisInput): NeedAnalysisResult {
        val safeMonthly = input.monthlySavingsPkr.coerceIn(5_000L, 10_000_000L)
        val safeYears = input.horizonYears.coerceIn(3, 30)
        val safeDependents = input.dependentsCount.coerceIn(0, 15)

        val annualContribution = safeMonthly * 12L
        val cumulativeContribution = annualContribution * safeYears.toLong()
        // General financial-planning protection benchmark (NOT an official insurer quote or profit rate)
        val protectionYearsFactor = (5 + safeDependents * 2).coerceIn(5, 18)
        val protectionBenchmark = annualContribution * protectionYearsFactor.toLong()

        val byId = OfficialProductCatalog.products.associateBy { it.id }
        val goldenPath = byId.getValue("golden_path")
        val muhafiz = byId.getValue("muhafiz")
        val iqra = byId.getValue("iqra")
        val humrahi = byId.getValue("humrahi")
        val pursukoonKal = byId.getValue("pursukoon_kal")
        val sarbuland = byId.getValue("sarbuland")
        val kanzUlAskari = byId.getValue("kanz_ul_askari")

        val (primary, secondary, rationaleEn, rationaleUr) = when (input.goal) {
            NeedGoal.CHILD_EDUCATION -> Quad(
                iqra,
                goldenPath,
                "Aligned by product theme with Children's Education planning ($safeYears-year horizon, $safeDependents dependents). Confirm official eligibility and contribution tiers on askarilife.com.",
                "بچوں کی تعلیم کے ہدف ($safeYears سال، $safeDependents زیرِ کفالت افراد) کے لیے پلان کے نام کی مناسبت سے تجویز۔ سرکاری شرائط askarilife.com پر تصدیق کریں۔"
            )
            NeedGoal.CHILD_MARRIAGE -> Quad(
                humrahi,
                iqra,
                "Aligned by product theme with Family Milestone & Marriage planning over $safeYears years. Confirm official schedules and brochure terms on askarilife.com.",
                "$safeYears سالہ خاندانی و شادی کی منصوبہ بندی کے لیے پلان کی مناسبت سے تجویز۔ سرکاری تفصیلات askarilife.com سے چیک کریں۔"
            )
            NeedGoal.RETIREMENT -> Quad(
                pursukoonKal,
                kanzUlAskari,
                "Aligned by product theme with Retirement & long-term peace-of-mind planning ($safeYears years). Verify official product brochure on askarilife.com before quoting.",
                "$safeYears سالہ ریٹائرمنٹ اور پُرسکون مستقبل کے ہدف کے لیے تجویز۔ کوٹ دینے سے پہلے askarilife.com پر سرکاری بروشر دیکھیں۔"
            )
            NeedGoal.FAMILY_PROTECTION -> {
                val primaryChoice = if (safeDependents >= 3) muhafiz else sarbuland
                val secondaryChoice = if (primaryChoice.id == muhafiz.id) sarbuland else muhafiz
                Quad(
                    primaryChoice,
                    goldenPath,
                    "Prioritizes family financial protection for $safeDependents dependents over a $safeYears-year horizon. General planning benchmark only—confirm official Takaful terms at askarilife.com.",
                    "$safeDependents زیرِ کفالت افراد کے مالی تحفظ اور $safeYears سالہ منصوبہ بندی کے لیے تجویز۔ یہ صرف عمومی اندازہ ہے—سرکاری شرائط askarilife.com پر دیکھیں۔"
                )
            }
            NeedGoal.LONG_TERM_WEALTH -> {
                val primaryChoice = if (safeMonthly >= 50_000L || safeYears >= 12) kanzUlAskari else goldenPath
                val secondaryChoice = if (primaryChoice.id == kanzUlAskari.id) goldenPath else sarbuland
                Quad(
                    primaryChoice,
                    secondaryChoice,
                    "Suited by theme for long-term disciplined Shariah-compliant saving ($safeYears years). Does not project returns—verify official facts on askarilife.com.",
                    "طویل مدتی شرعی بچت ($safeYears سال) کے ہدف کے لیے تجویز۔ اس میں کوئی فرضی منافع شامل نہیں—سرکاری حقائق askarilife.com پر دیکھیں۔"
                )
            }
        }

        return NeedAnalysisResult(
            primaryProduct = primary,
            secondaryProduct = secondary,
            annualContributionPkr = annualContribution,
            cumulativeHorizonContributionPkr = cumulativeContribution,
            planningProtectionBenchmarkPkr = protectionBenchmark,
            rationaleEn = rationaleEn,
            rationaleUr = rationaleUr
        )
    }

    private data class Quad<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
