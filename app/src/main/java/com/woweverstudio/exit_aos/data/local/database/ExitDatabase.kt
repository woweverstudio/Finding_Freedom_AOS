package com.woweverstudio.exit_aos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.woweverstudio.exit_aos.data.local.dao.AssetDao
import com.woweverstudio.exit_aos.data.local.dao.AssetSnapshotDao
import com.woweverstudio.exit_aos.data.local.dao.DepositReminderDao
import com.woweverstudio.exit_aos.data.local.dao.MonthlyUpdateDao
import com.woweverstudio.exit_aos.data.local.dao.UserProfileDao
import com.woweverstudio.exit_aos.data.local.entity.AssetEntity
import com.woweverstudio.exit_aos.data.local.entity.AssetSnapshotEntity
import com.woweverstudio.exit_aos.data.local.entity.DepositReminderEntity
import com.woweverstudio.exit_aos.data.local.entity.MonthlyUpdateEntity
import com.woweverstudio.exit_aos.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        AssetEntity::class,
        AssetSnapshotEntity::class,
        MonthlyUpdateEntity::class,
        DepositReminderEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ExitDatabase : RoomDatabase() {
    
    abstract fun userProfileDao(): UserProfileDao
    abstract fun assetDao(): AssetDao
    abstract fun assetSnapshotDao(): AssetSnapshotDao
    abstract fun monthlyUpdateDao(): MonthlyUpdateDao
    abstract fun depositReminderDao(): DepositReminderDao
    
    companion object {
        const val DATABASE_NAME = "exit_database"
    }
}

