package com.example.data.system

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import com.example.domain.model.AppItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

class LauncherAppsManager(private val context: Context) {

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
    private val packageManager = context.packageManager

    fun getInstalledLaunchableApps(): List<AppItem> {
        val appList = mutableListOf<AppItem>()
        val currentUser = Process.myUserHandle()
        val profiles: List<UserHandle> = userManager?.userProfiles ?: listOf(currentUser)

        for (profile in profiles) {
            val isWorkProfile = profile != currentUser
            val activities: List<LauncherActivityInfo> = try {
                launcherApps?.getActivityList(null, profile) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            for (activityInfo in activities) {
                val pkgName = activityInfo.applicationInfo.packageName
                // Don't show our own launcher inside the app drawer/ls apps list if desired, or show it
                if (pkgName == context.packageName) continue

                val label = try {
                    activityInfo.label?.toString() ?: pkgName
                } catch (_: Exception) {
                    pkgName
                }

                val icon = try {
                    activityInfo.getBadgedIcon(0)
                } catch (_: Exception) {
                    null
                }

                val installedTime = try {
                    packageManager.getPackageInfo(pkgName, 0).firstInstallTime
                } catch (_: Exception) {
                    0L
                }

                appList.add(
                    AppItem(
                        packageName = pkgName,
                        activityName = activityInfo.componentName.className,
                        label = label,
                        icon = icon,
                        userHandle = profile,
                        isWorkProfile = isWorkProfile,
                        installedTimestamp = installedTime
                    )
                )
            }
        }

        return appList
            .distinctBy { it.uniqueKey }
            .sortedBy { it.label.lowercase() }
    }

    /**
     * Listens to live app install, uninstall, and change events via LauncherApps.Callback
     */
    fun observeAppChanges(): Flow<Unit> = callbackFlow {
        if (launcherApps == null) {
            awaitClose { }
            return@callbackFlow
        }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean
            ) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean
            ) {
                trySend(Unit)
            }

            override fun onPackagesSuspended(
                packageNames: Array<out String>?,
                user: UserHandle?
            ) {
                trySend(Unit)
            }

            override fun onPackagesUnsuspended(
                packageNames: Array<out String>?,
                user: UserHandle?
            ) {
                trySend(Unit)
            }
        }

        val mainHandler = Handler(Looper.getMainLooper())
        launcherApps.registerCallback(callback, mainHandler)
        awaitClose {
            try {
                launcherApps.unregisterCallback(callback)
            } catch (_: Exception) { }
        }
    }

    fun launchApp(packageName: String, userHandle: UserHandle? = null, activityName: String? = null): Result<Unit> {
        val targetUser = userHandle ?: Process.myUserHandle()
        return try {
            if (launcherApps != null) {
                val activities = launcherApps.getActivityList(packageName, targetUser)
                if (activities.isNotEmpty()) {
                    val activity = if (!activityName.isNullOrEmpty()) {
                        activities.firstOrNull { it.componentName.className == activityName } ?: activities[0]
                    } else {
                        activities[0]
                    }
                    launcherApps.startMainActivity(activity.componentName, targetUser, null, null)
                    Result.success(Unit)
                } else {
                    // Fallback to PackageManager launch intent
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        Result.success(Unit)
                    } else {
                        Result.failure(ActivityNotFoundException("No launchable activity found for $packageName"))
                    }
                }
            } else {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                    ?: return Result.failure(ActivityNotFoundException("Launch intent not found"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openAppInfo(packageName: String): Result<Unit> {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun uninstallApp(packageName: String): Result<Unit> {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openPlayStore(packageName: String): Result<Unit> {
        return try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
            Result.success(Unit)
        } catch (_: ActivityNotFoundException) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getAppShortcuts(packageName: String): List<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 || launcherApps == null) {
            return emptyList()
        }
        return try {
            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            }
            val shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle())
            shortcuts?.mapNotNull { it.shortLabel?.toString() ?: it.longLabel?.toString() } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
