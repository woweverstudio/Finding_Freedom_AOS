package com.woweverstudio.exit_aos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.woweverstudio.exit_aos.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    
    @Query("SELECT * FROM asset LIMIT 1")
    fun getAsset(): Flow<AssetEntity?>
    
    @Query("SELECT * FROM asset LIMIT 1")
    suspend fun getAssetSync(): AssetEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: AssetEntity)
    
    @Update
    suspend fun update(asset: AssetEntity)
    
    @Delete
    suspend fun delete(asset: AssetEntity)
    
    @Query("DELETE FROM asset")
    suspend fun deleteAll()
}

