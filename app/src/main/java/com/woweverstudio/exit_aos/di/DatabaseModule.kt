package com.woweverstudio.exit_aos.di

import android.content.Context
import androidx.room.Room
import com.woweverstudio.exit_aos.data.local.dao.AssetDao
import com.woweverstudio.exit_aos.data.local.dao.AssetSnapshotDao
import com.woweverstudio.exit_aos.data.local.dao.DepositReminderDao
import com.woweverstudio.exit_aos.data.local.dao.MonthlyUpdateDao
import com.woweverstudio.exit_aos.data.local.dao.UserProfileDao
import com.woweverstudio.exit_aos.data.local.database.ExitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExitDatabase {
        return Room.databaseBuilder(
            context,
            ExitDatabase::class.java,
            ExitDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideUserProfileDao(database: ExitDatabase): UserProfileDao {
        return database.userProfileDao()
    }
    
    @Provides
    @Singleton
    fun provideAssetDao(database: ExitDatabase): AssetDao {
        return database.assetDao()
    }
    
    @Provides
    @Singleton
    fun provideAssetSnapshotDao(database: ExitDatabase): AssetSnapshotDao {
        return database.assetSnapshotDao()
    }
    
    @Provides
    @Singleton
    fun provideMonthlyUpdateDao(database: ExitDatabase): MonthlyUpdateDao {
        return database.monthlyUpdateDao()
    }
    
    @Provides
    @Singleton
    fun provideDepositReminderDao(database: ExitDatabase): DepositReminderDao {
        return database.depositReminderDao()
    }
}

