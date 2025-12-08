package com.woweverstudio.exit_aos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.woweverstudio.exit_aos.data.local.entity.AssetSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetSnapshotDao {
    
    @Query("SELECT * FROM asset_snapshot ORDER BY yearMonth DESC")
    fun getAllSnapshots(): Flow<List<AssetSnapshotEntity>>
    
    @Query("SELECT * FROM asset_snapshot ORDER BY yearMonth DESC")
    suspend fun getAllSnapshotsSync(): List<AssetSnapshotEntity>
    
    @Query("SELECT * FROM asset_snapshot WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getSnapshotByYearMonth(yearMonth: String): AssetSnapshotEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: AssetSnapshotEntity)
    
    @Update
    suspend fun update(snapshot: AssetSnapshotEntity)
    
    @Delete
    suspend fun delete(snapshot: AssetSnapshotEntity)
    
    @Query("DELETE FROM asset_snapshot")
    suspend fun deleteAll()
}

