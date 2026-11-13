package com.example

import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.OfficialProductCatalog
import com.example.data.model.OwnerProfile
import com.example.data.model.Prospect
import com.example.ui.viewmodel.MainViewModel
import com.example.util.OutreachIntents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TakafulLogicUnitTest {

    @Test
    fun `official catalog contains exact seven Askari Family Takaful products`() {
        val names = OfficialProductCatalog.products.map { it.nameEn }
        assertEquals(
            listOf(
                "Golden Path",
                "Muhafiz",
                "Iqra",
                "Humrahi",
                "PurSukoon Kal",
                "Sarbuland",
                "Kanz ul Askari"
            ),
            names
        )
    }

    @Test
    fun `owner profile matches GM defaults`() {
        assertEquals("General Manager", OwnerProfile.DESIGNATION_EN)
        assertEquals("Askari Family Takaful", OwnerProfile.COMPANY_EN)
        assertEquals("+923160894696", OwnerProfile.WHATSAPP_NUMBER)
        assertEquals("Islamabad", OwnerProfile.DEFAULT_MARKET_CITY)
    }

    @Test
    fun `dashboard metrics count all pipeline stages and due follow-ups`() {
        val leads = listOf(
            Lead(id = "1", name = "A", status = LeadStatus.NEW.name, followUpDate = "2020-01-01"),
            Lead(id = "2", name = "B", status = LeadStatus.CONTACTED.name, followUpDate = "2099-12-31"),
            Lead(id = "3", name = "C", status = LeadStatus.INTERESTED.name, followUpDate = ""),
            Lead(id = "4", name = "D", status = LeadStatus.FOLLOW_UP.name, followUpDate = "2099-12-31"),
            Lead(id = "5", name = "E", status = LeadStatus.COMPLETED.name, followUpDate = "2020-01-01")
        )
        val metrics = MainViewModel.computeMetrics(leads)
        assertEquals(5, metrics.totalLeads)
        assertEquals(1, metrics.newProspects)
        assertEquals(1, metrics.contactedClients)
        assertEquals(1, metrics.interestedClients)
        assertEquals(2, metrics.followUpsDue) // Lead 1 (past due) + Lead 4 (FOLLOW_UP status)
        assertEquals(1, metrics.completedSales)
    }

    @Test
    fun `search URLs encode city industry and designation properly`() {
        val google = OutreachIntents.buildGoogleSearchUrl("Islamabad", "Property Developers", "CEO", "DHA")
        val bing = OutreachIntents.buildBingSearchUrl("Islamabad", "Hospitals", "Medical Superintendent", "")
        assertTrue(google.startsWith("https://www.google.com/search?q="))
        assertTrue(google.contains("Islamabad"))
        assertTrue(bing.startsWith("https://www.bing.com/search?q="))
        assertTrue(bing.contains("Hospitals"))
    }

    @Test
    fun `csv export escapes commas and includes BOM`() {
        val csv = OutreachIntents.buildLeadsCsv(
            listOf(
                Lead(
                    id = "1",
                    name = "Tariq, Jr.",
                    company = "Capital \"Builders\"",
                    city = "Islamabad"
                )
            )
        )
        assertTrue(csv.startsWith("\uFEFFName,Company"))
        assertTrue(csv.contains("\"Tariq, Jr.\""))
        assertTrue(csv.contains("\"Capital \"\"Builders\"\"\""))
    }

    @Test
    fun `filterLeads matches prospect by name and by English or Urdu status`() {
        val leads = listOf(
            Lead(id = "1", name = "Tariq Mahmood", city = "Islamabad", status = LeadStatus.NEW.name),
            Lead(id = "2", name = "Dr. Sara Khan", city = "Rawalpindi", status = LeadStatus.INTERESTED.name),
            Lead(id = "3", name = "Bilal Ahmed", city = "Lahore", status = LeadStatus.FOLLOW_UP.name)
        )

        val byName = MainViewModel.filterLeads(leads, query = "sara", selectedStatus = null)
        assertEquals(listOf("2"), byName.map { it.id })

        val byStatusEn = MainViewModel.filterLeads(leads, query = "Interested", selectedStatus = null)
        assertEquals(listOf("2"), byStatusEn.map { it.id })

        val byStatusUr = MainViewModel.filterLeads(leads, query = LeadStatus.FOLLOW_UP.labelUr, selectedStatus = null)
        assertEquals(listOf("3"), byStatusUr.map { it.id })

        val byChip = MainViewModel.filterLeads(leads, query = "", selectedStatus = LeadStatus.NEW)
        assertEquals(listOf("1"), byChip.map { it.id })
    }

    @Test
    fun `filterProspects filters by name contact or status`() {
        val prospects = listOf(
            Prospect(id = "p1", name = "Kamran Malik", contact = "03001234567", status = "Interested", notes = "DHA"),
            Prospect(id = "p2", name = "Nadia Hussain", contact = "nadia@org.pk", status = "Contacted", notes = "Hospital")
        )

        assertEquals(listOf("p1"), MainViewModel.filterProspects(prospects, "kamran", null).map { it.id })
        assertEquals(listOf("p2"), MainViewModel.filterProspects(prospects, "Contacted", null).map { it.id })
        assertEquals(listOf("p2"), MainViewModel.filterProspects(prospects, "", LeadStatus.CONTACTED).map { it.id })
    }
}
