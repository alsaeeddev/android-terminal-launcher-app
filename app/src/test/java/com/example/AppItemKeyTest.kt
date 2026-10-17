package com.example

import com.example.domain.model.AppItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppItemKeyTest {

    @Test
    fun `uniqueKey produces distinct keys for same package with different activities`() {
        val app1 = AppItem(
            packageName = "com.whatsapp",
            activityName = "com.whatsapp.Main",
            label = "WhatsApp"
        )
        val app2 = AppItem(
            packageName = "com.whatsapp",
            activityName = "com.whatsapp.Conversation",
            label = "WhatsApp Conversation"
        )

        assertNotEquals(app1.uniqueKey, app2.uniqueKey)
    }

    @Test
    fun `uniqueKey produces distinct keys for work profile vs main profile`() {
        val appMain = AppItem(
            packageName = "com.whatsapp",
            activityName = "com.whatsapp.Main",
            label = "WhatsApp",
            isWorkProfile = false
        )
        val appWork = AppItem(
            packageName = "com.whatsapp",
            activityName = "com.whatsapp.Main",
            label = "WhatsApp (Work)",
            isWorkProfile = true
        )

        assertNotEquals(appMain.uniqueKey, appWork.uniqueKey)
    }

    @Test
    fun `distinctBy uniqueKey deduplicates exact duplicate app items`() {
        val app1 = AppItem(
            packageName = "com.whatsapp",
            activityName = "com.whatsapp.Main",
            label = "WhatsApp"
        )
        val app2 = AppItem(
            packageName = "com.whatsapp",
            activityName = "com.whatsapp.Main",
            label = "WhatsApp"
        )
        val app3 = AppItem(
            packageName = "com.example.other",
            activityName = "com.example.other.MainActivity",
            label = "Other App"
        )

        val list = listOf(app1, app2, app3)
        val deduplicated = list.distinctBy { it.uniqueKey }

        assertEquals(2, deduplicated.size)
        assertEquals(listOf(app1, app3), deduplicated)
    }
}
