package com.example.data.repository

import android.os.UserHandle
import com.example.core.common.DispatcherProvider
import com.example.core.util.FuzzyMatcher
import com.example.data.local.db.dao.AppDao
import com.example.data.local.db.entity.AppLabelOverrideEntity
import com.example.data.local.db.entity.AppUsageEntity
import com.example.data.local.db.entity.FavoriteAppEntity
import com.example.data.local.db.entity.HiddenAppEntity
import com.example.data.system.LauncherAppsManager
import com.example.domain.model.AppItem
import com.example.domain.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepositoryImpl(
    private val launcherAppsManager: LauncherAppsManager,
    private val appDao: AppDao,
    private val dispatcherProvider: DispatcherProvider,
    private val externalScope: CoroutineScope
) : AppRepository {

    private val _rawApps = MutableStateFlow<List<AppItem>>(emptyList())
    private val _appsFlow = MutableStateFlow<List<AppItem>>(emptyList())
    override val appsFlow: StateFlow<List<AppItem>> = _appsFlow.asStateFlow()

    init {
        // Initial load off the main thread
        externalScope.launch(dispatcherProvider.io) {
            refreshApps()
        }

        // Observe live package install / uninstall / update events from LauncherApps
        launcherAppsManager.observeAppChanges()
            .onEach {
                refreshApps()
            }
            .launchIn(externalScope)

        // Combine raw apps with Room data (favorites, hidden, overrides, usage)
        combine(
            _rawApps,
            appDao.getFavorites(),
            appDao.getHiddenApps(),
            appDao.getLabelOverrides(),
            appDao.getAllUsage()
        ) { raw, favs, hidden, overrides, usage ->
            val favMap = favs.associateBy { it.packageName }
            val hiddenSet = hidden.map { it.packageName }.toSet()
            val overrideMap = overrides.associate { it.packageName to it.customLabel }
            val usageMap = usage.associate { it.packageName to it }

            raw.map { app ->
                val overrideLabel = overrideMap[app.packageName]
                val use = usageMap[app.packageName]
                app.copy(
                    label = overrideLabel ?: app.label,
                    isFavorite = favMap.containsKey(app.packageName),
                    isHidden = hiddenSet.contains(app.packageName),
                    launchCount = use?.launchCount ?: 0,
                    lastLaunchedTimestamp = use?.lastLaunchedTimestamp ?: 0L
                )
            }.sortedWith(
                compareByDescending<AppItem> { it.isFavorite }
                    .thenBy { it.label.lowercase() }
            )
        }.onEach { enriched ->
            _appsFlow.value = enriched
        }.launchIn(externalScope)
    }

    override suspend fun refreshApps() = withContext(dispatcherProvider.io) {
        val installed = launcherAppsManager.getInstalledLaunchableApps()
        _rawApps.value = installed
    }

    override suspend fun launchApp(packageName: String, userHandle: UserHandle?, activityName: String?): Result<Unit> =
        withContext(dispatcherProvider.io) {
            val result = launcherAppsManager.launchApp(packageName, userHandle, activityName)
            if (result.isSuccess) {
                recordLaunch(packageName)
            }
            result
        }

    override suspend fun openAppInfo(packageName: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            launcherAppsManager.openAppInfo(packageName)
        }

    override suspend fun uninstallApp(packageName: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            launcherAppsManager.uninstallApp(packageName)
        }

    override suspend fun openPlayStore(packageName: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            launcherAppsManager.openPlayStore(packageName)
        }

    override suspend fun setFavorite(packageName: String, isFavorite: Boolean) =
        withContext(dispatcherProvider.io) {
            if (isFavorite) {
                appDao.addFavorite(FavoriteAppEntity(packageName = packageName))
            } else {
                appDao.removeFavorite(packageName)
            }
        }

    override suspend fun setHidden(packageName: String, isHidden: Boolean) =
        withContext(dispatcherProvider.io) {
            if (isHidden) {
                appDao.hideApp(HiddenAppEntity(packageName = packageName))
            } else {
                appDao.unhideApp(packageName)
            }
        }

    override suspend fun setLabelOverride(packageName: String, customLabel: String?) =
        withContext(dispatcherProvider.io) {
            if (customLabel.isNullOrBlank()) {
                appDao.removeLabelOverride(packageName)
            } else {
                appDao.setLabelOverride(AppLabelOverrideEntity(packageName, customLabel.trim()))
            }
        }

    override suspend fun findApp(query: String): AppItem? = withContext(dispatcherProvider.default) {
        val currentApps = _appsFlow.value
        val exact = currentApps.firstOrNull {
            it.label.equals(query, ignoreCase = true) || it.packageName.equals(query, ignoreCase = true)
        }
        if (exact != null) return@withContext exact

        // Fuzzy match
        val matches = FuzzyMatcher.filterAndSort(query, currentApps, { it.label }, limit = 1)
        matches.firstOrNull()
    }

    override suspend fun recordLaunch(packageName: String) = withContext(dispatcherProvider.io) {
        val now = System.currentTimeMillis()
        val updatedRows = appDao.incrementLaunch(packageName, now)
        if (updatedRows == 0) {
            appDao.insertOrUpdateUsage(
                AppUsageEntity(
                    packageName = packageName,
                    launchCount = 1,
                    lastLaunchedTimestamp = now
                )
            )
        }
    }

    override suspend fun getAppShortcuts(packageName: String): List<String> =
        withContext(dispatcherProvider.io) {
            launcherAppsManager.getAppShortcuts(packageName)
        }
}
