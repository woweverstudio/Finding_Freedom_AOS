package com.woweverstudio.exit_aos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.woweverstudio.exit_aos.data.local.entity.MonthlyUpdateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyUpdateDao {
    
    @Query("SELECT * FROM monthly_update ORDER BY yearMonth DESC")
    fun getAllUpdates(): Flow<List<MonthlyUpdateEntity>>
    
    @Query("SELECT * FROM monthly_update ORDER BY yearMonth DESC")
    suspend fun getAllUpdatesSync(): List<MonthlyUpdateEntity>
    
    @Query("SELECT * FROM monthly_update WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getUpdateByYearMonth(yearMonth: String): MonthlyUpdateEntity?
    
    @Query("SELECT * FROM monthly_update WHERE yearMonth LIKE :yearPrefix || '%' ORDER BY yearMonth DESC")
    fun getUpdatesByYear(yearPrefix: String): Flow<List<MonthlyUpdateEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(update: MonthlyUpdateEntity)
    
    @Update
    suspend fun update(update: MonthlyUpdateEntity)
    
    @Delete
    suspend fun delete(update: MonthlyUpdateEntity)
    
    @Query("DELETE FROM monthly_update")
    suspend fun deleteAll()
}

