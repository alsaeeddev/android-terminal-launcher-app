package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.entity.AppLabelOverrideEntity
import com.example.data.local.db.entity.AppUsageEntity
import com.example.data.local.db.entity.FavoriteAppEntity
import com.example.data.local.db.entity.HiddenAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Usage stats
    @Query("SELECT * FROM app_usage ORDER BY launchCount DESC")
    fun getAllUsage(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName LIMIT 1")
    suspend fun getUsage(packageName: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsage(usage: AppUsageEntity)

    @Query("UPDATE app_usage SET launchCount = launchCount + 1, lastLaunchedTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun incrementLaunch(packageName: String, timestamp: Long): Int

    // Favorites
    @Query("SELECT * FROM favorite_app ORDER BY orderIndex ASC")
    fun getFavorites(): Flow<List<FavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteAppEntity)

    @Query("DELETE FROM favorite_app WHERE packageName = :packageName")
    suspend fun removeFavorite(packageName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_app WHERE packageName = :packageName)")
    suspend fun isFavorite(packageName: String): Boolean

    // Hidden apps
    @Query("SELECT * FROM hidden_app")
    fun getHiddenApps(): Flow<List<HiddenAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideApp(hidden: HiddenAppEntity)

    @Query("DELETE FROM hidden_app WHERE packageName = :packageName")
    suspend fun unhideApp(packageName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM hidden_app WHERE packageName = :packageName)")
    suspend fun isHidden(packageName: String): Boolean

    // Label overrides
    @Query("SELECT * FROM app_label_override")
    fun getLabelOverrides(): Flow<List<AppLabelOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLabelOverride(override: AppLabelOverrideEntity)

    @Query("DELETE FROM app_label_override WHERE packageName = :packageName")
    suspend fun removeLabelOverride(packageName: String)
}
