package com.example.core.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

object DeviceInfoHelper {

    fun getAndroidVersionName(): String {
        val release = Build.VERSION.RELEASE ?: "Unknown"
        val sdk = Build.VERSION.SDK_INT
        return "Android $release (API $sdk)"
    }

    fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"
    }

    fun getUptimeString(): String {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val days = TimeUnit.MILLISECONDS.toDays(uptimeMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    fun getRamInfo(context: Context): Pair<Long, Long> { // (availableBytes, totalBytes)
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        return Pair(memInfo.availMem, memInfo.totalMem)
    }

    fun getStorageInfo(): Pair<Long, Long> { // (availableBytes, totalBytes)
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        return Pair(availableBlocks * blockSize, totalBlocks * blockSize)
    }

    fun formatBytes(bytes: Long): String {
        val gb = bytes.toDouble() / (1024 * 1024 * 1024)
        return if (gb >= 1.0) {
            String.format(Locale.US, "%.1f GB", gb)
        } else {
            val mb = bytes.toDouble() / (1024 * 1024)
            String.format(Locale.US, "%.0f MB", mb)
        }
    }

    data class BatteryStatus(
        val percentage: Int,
        val isCharging: Boolean,
        val statusString: String
    )

    fun getBatteryStatus(context: Context): BatteryStatus {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 50
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        val statusDesc = if (isCharging) "Charging" else "Discharging"
        return BatteryStatus(percentage = pct, isCharging = isCharging, statusString = statusDesc)
    }

    fun getNetworkType(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "Offline"
        val activeNetwork = cm.activeNetwork ?: return "Offline"
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return "Offline"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
            else -> "Connected"
        }
    }

    fun getNeofetchHeader(context: Context, username: String = "user", hostname: String = "shellhome"): List<String> {
        val device = getDeviceModel()
        val os = getAndroidVersionName()
        val uptime = getUptimeString()
        val (availRam, totalRam) = getRamInfo(context)
        val usedRam = totalRam - availRam
        val (availStorage, totalStorage) = getStorageInfo()
        val usedStorage = totalStorage - availStorage
        val battery = getBatteryStatus(context)
        val net = getNetworkType(context)

        val banner = listOf(
            "       _.-;;-._        $username@$hostname",
            "'-..-'|   ||   |       --------------------------------",
            "'-..-'|_.-;;-._|       OS:      $os",
            "'-..-'|   ||   |       Host:    $device",
            "'-..-'|_.-''-._|       Uptime:  $uptime",
            "                       Memory:  ${formatBytes(usedRam)} / ${formatBytes(totalRam)}",
            "                       Storage: ${formatBytes(usedStorage)} / ${formatBytes(totalStorage)}",
            "                       Battery: ${battery.percentage}% [${battery.statusString}]",
            "                       Network: $net"
        )
        return banner
    }
}
