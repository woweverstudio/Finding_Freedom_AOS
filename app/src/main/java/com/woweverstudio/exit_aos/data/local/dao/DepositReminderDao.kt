package com.woweverstudio.exit_aos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.woweverstudio.exit_aos.data.local.entity.DepositReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositReminderDao {
    
    @Query("SELECT * FROM deposit_reminder ORDER BY createdAt ASC")
    fun getAllReminders(): Flow<List<DepositReminderEntity>>
    
    @Query("SELECT * FROM deposit_reminder ORDER BY createdAt ASC")
    suspend fun getAllRemindersSync(): List<DepositReminderEntity>
    
    @Query("SELECT * FROM deposit_reminder WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: String): DepositReminderEntity?
    
    @Query("SELECT * FROM deposit_reminder WHERE isEnabled = 1")
    suspend fun getEnabledReminders(): List<DepositReminderEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: DepositReminderEntity)
    
    @Update
    suspend fun update(reminder: DepositReminderEntity)
    
    @Delete
    suspend fun delete(reminder: DepositReminderEntity)
    
    @Query("DELETE FROM deposit_reminder")
    suspend fun deleteAll()
}

