package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Lead
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object OutreachIntents {

    fun buildGoogleSearchUrl(
        city: String,
        industry: String,
        designation: String,
        extraKeywords: String
    ): String {
        val query = listOf(industry, designation, city, extraKeywords, "Pakistan")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        return "https://www.google.com/search?q=$encoded"
    }

    fun buildBingSearchUrl(
        city: String,
        industry: String,
        designation: String,
        extraKeywords: String
    ): String {
        val query = listOf(industry, designation, city, extraKeywords, "Pakistan")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        return "https://www.bing.com/search?q=$encoded"
    }

    fun buildLinkedInGoogleUrl(
        city: String,
        industry: String,
        designation: String,
        extraKeywords: String
    ): String {
        val parts = listOfNotNull(
            "site:linkedin.com/in",
            designation.trim().takeIf { it.isNotBlank() }?.let { "\"$it\"" },
            industry.trim().takeIf { it.isNotBlank() },
            city.trim().takeIf { it.isNotBlank() },
            extraKeywords.trim().takeIf { it.isNotBlank() }
        )
        val encoded = URLEncoder.encode(parts.joinToString(" "), StandardCharsets.UTF_8.toString())
        return "https://www.google.com/search?q=$encoded"
    }

    fun openUrl(context: Context, url: String) {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open browser link.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsAppWithDraft(context: Context, rawPhone: String, message: String) {
        val cleaned = normalizePhoneForWhatsApp(rawPhone)
        val encodedText = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val url = if (cleaned.isNotBlank()) {
            "https://wa.me/$cleaned?text=$encodedText"
        } else {
            "https://wa.me/?text=$encodedText"
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open WhatsApp.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openEmailWithDraft(context: Context, recipientEmail: String, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${Uri.encode(recipientEmail.trim())}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No email app available.", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied to clipboard.", Toast.LENGTH_SHORT).show()
    }

    fun addFollowUpCalendarReminder(context: Context, lead: Lead) {
        try {
            val cal = Calendar.getInstance().apply {
                if (lead.followUpDate.isNotBlank()) {
                    try {
                        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(lead.followUpDate)
                        if (parsed != null) time = parsed
                    } catch (_: Exception) {}
                }
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
            }
            val startMillis = cal.timeInMillis
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Follow up: ${lead.name} (${lead.company.ifBlank { lead.city }})")
                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Askari Family Takaful follow-up\nDesignation: ${lead.designation}\nPhone: ${lead.contactPhone}\nNotes: ${lead.notes}"
                )
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 30 * 60 * 1000L)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open Calendar app.", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildLeadsCsv(leads: List<Lead>): String {
        val sb = StringBuilder()
        // UTF-8 BOM so Urdu script opens properly in Excel
        sb.append('\uFEFF')
        sb.appendLine("Name,Company,Designation,City,Industry,Contact Phone,Email,Website,LinkedIn URL,Source,Permission Status,Status,Follow-Up Date,Notes")
        for (lead in leads) {
            val row = listOf(
                lead.name,
                lead.company,
                lead.designation,
                lead.city,
                lead.industry,
                lead.contactPhone,
                lead.email,
                lead.website,
                lead.linkedInUrl,
                lead.source,
                lead.permissionStatus,
                lead.status,
                lead.followUpDate,
                lead.notes
            ).joinToString(",") { escapeCsv(it) }
            sb.appendLine(row)
        }
        return sb.toString()
    }

    fun exportLeadsCsv(context: Context, leads: List<Lead>) {
        try {
            val csvContent = buildLeadsCsv(leads)
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(java.util.Date())
            val csvFile = File(exportDir, "askari_takaful_leads_$timestamp.csv")
            csvFile.writeText(csvContent, StandardCharsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Askari Family Takaful Leads Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Leads CSV"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\r\n", " ").replace("\n", " ")
        return if (clean.contains(',') || clean.contains('"')) {
            "\"${clean.replace("\"", "\"\"")}\""
        } else {
            clean
        }
    }

    private fun normalizePhoneForWhatsApp(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("00") -> digits.drop(2)
            digits.startsWith("03") && digits.length == 11 -> "92${digits.drop(1)}"
            else -> digits
        }
    }
}
