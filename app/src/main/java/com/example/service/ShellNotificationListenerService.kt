package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShellNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _badgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val badgeCounts: StateFlow<Map<String, Int>> = _badgeCounts.asStateFlow()

        @Volatile
        var isConnected: Boolean = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        updateCounts()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        updateCounts()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        updateCounts()
    }

    private fun updateCounts() {
        try {
            val active = activeNotifications ?: return
            val counts = mutableMapOf<String, Int>()
            for (notif in active) {
                if (!notif.isOngoing) {
                    val pkg = notif.packageName
                    counts[pkg] = (counts[pkg] ?: 0) + 1
                }
            }
            _badgeCounts.value = counts
        } catch (_: Exception) {
        }
    }
}
