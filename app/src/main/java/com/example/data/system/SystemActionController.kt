package com.example.data.system

import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.service.LockAccessibilityService

class SystemActionController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    fun isDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            return roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
        } else {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            return resolveInfo?.activityInfo?.packageName == context.packageName
        }
    }

    fun openDefaultLauncherSettings(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            } else {
                Intent(Settings.ACTION_HOME_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        } else {
            Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    fun lockScreen(): Result<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locked = LockAccessibilityService.lockScreen()
            return if (locked) {
                Result.success("Screen locked.")
            } else {
                Result.failure(Exception("Accessibility service not enabled. Please enable Shell Home in Accessibility Settings."))
            }
        } else {
            return Result.failure(Exception("Lock screen gesture requires Android 9 (API 28) or newer."))
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun expandNotificationShade() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusBarManager.getMethod("expandNotificationsPanel")
            expand.invoke(statusBarService)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun setTorch(enable: Boolean): Result<String> {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                ?: return Result.failure(Exception("Flashlight hardware not found."))
            cameraManager.setTorchMode(cameraId, enable)
            Result.success("Flashlight turned ${if (enable) "ON" else "OFF"}.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setVolume(stream: String, levelPercent: Int): Result<String> {
        val am = audioManager ?: return Result.failure(Exception("Audio service unavailable"))
        val streamType = when (stream.lowercase()) {
            "media", "music" -> AudioManager.STREAM_MUSIC
            "ring", "ringer" -> AudioManager.STREAM_RING
            "alarm" -> AudioManager.STREAM_ALARM
            "notification" -> AudioManager.STREAM_NOTIFICATION
            else -> AudioManager.STREAM_MUSIC
        }
        val maxVolume = am.getStreamMaxVolume(streamType)
        val targetIndex = (maxVolume * (levelPercent.coerceIn(0, 100) / 100.0)).toInt()
        am.setStreamVolume(streamType, targetIndex, AudioManager.FLAG_SHOW_UI)
        return Result.success("Volume set to $levelPercent% (Index $targetIndex/$maxVolume)")
    }

    fun getVolumeSummary(): String {
        val am = audioManager ?: return "Audio service unavailable"
        val musicMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val musicCur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val ringMax = am.getStreamMaxVolume(AudioManager.STREAM_RING)
        val ringCur = am.getStreamVolume(AudioManager.STREAM_RING)
        val alarmMax = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val alarmCur = am.getStreamVolume(AudioManager.STREAM_ALARM)
        return "Media: ${(musicCur * 100) / musicMax}% | Ring: ${(ringCur * 100) / ringMax}% | Alarm: ${(alarmCur * 100) / alarmMax}%"
    }

    fun openSettingsSection(section: String): Result<String> {
        val intentAction = when (section.lowercase()) {
            "wifi", "wireless" -> Settings.ACTION_WIFI_SETTINGS
            "bt", "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display", "screen" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound", "audio", "volume" -> Settings.ACTION_SOUND_SETTINGS
            "battery", "power" -> Intent.ACTION_POWER_USAGE_SUMMARY
            "apps", "applications" -> Settings.ACTION_APPLICATION_SETTINGS
            "storage", "disk" -> Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            "security" -> Settings.ACTION_SECURITY_SETTINGS
            "dnd" -> Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            "date", "time" -> Settings.ACTION_DATE_SETTINGS
            "loc", "location" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return try {
            val intent = Intent(intentAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Opened $section settings.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
