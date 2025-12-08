package com.woweverstudio.exit_aos.data.repository

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
import com.woweverstudio.exit_aos.domain.model.Asset
import com.woweverstudio.exit_aos.domain.model.AssetSnapshot
import com.woweverstudio.exit_aos.domain.model.DepositReminder
import com.woweverstudio.exit_aos.domain.model.MonthlyUpdate
import com.woweverstudio.exit_aos.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExitRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val assetDao: AssetDao,
    private val assetSnapshotDao: AssetSnapshotDao,
    private val monthlyUpdateDao: MonthlyUpdateDao,
    private val depositReminderDao: DepositReminderDao
) {
    // MARK: - UserProfile
    
    fun getUserProfile(): Flow<UserProfile?> =
        userProfileDao.getUserProfile().map { it?.toDomainModel() }
    
    suspend fun getUserProfileSync(): UserProfile? =
        userProfileDao.getUserProfileSync()?.toDomainModel()
    
    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insert(UserProfileEntity.fromDomainModel(profile))
    }
    
    suspend fun updateUserProfile(profile: UserProfile) {
        userProfileDao.update(UserProfileEntity.fromDomainModel(profile))
    }
    
    // MARK: - Asset
    
    fun getAsset(): Flow<Asset?> =
        assetDao.getAsset().map { it?.toDomainModel() }
    
    suspend fun getAssetSync(): Asset? =
        assetDao.getAssetSync()?.toDomainModel()
    
    suspend fun saveAsset(asset: Asset) {
        assetDao.insert(AssetEntity.fromDomainModel(asset))
    }
    
    suspend fun updateAsset(asset: Asset) {
        assetDao.update(AssetEntity.fromDomainModel(asset))
    }
    
    // MARK: - AssetSnapshot
    
    fun getAssetSnapshots(): Flow<List<AssetSnapshot>> =
        assetSnapshotDao.getAllSnapshots().map { list -> list.map { it.toDomainModel() } }
    
    suspend fun getAssetSnapshotsSync(): List<AssetSnapshot> =
        assetSnapshotDao.getAllSnapshotsSync().map { it.toDomainModel() }
    
    suspend fun getSnapshotByYearMonth(yearMonth: String): AssetSnapshot? =
        assetSnapshotDao.getSnapshotByYearMonth(yearMonth)?.toDomainModel()
    
    suspend fun saveSnapshot(snapshot: AssetSnapshot) {
        assetSnapshotDao.insert(AssetSnapshotEntity.fromDomainModel(snapshot))
    }
    
    suspend fun updateSnapshot(snapshot: AssetSnapshot) {
        assetSnapshotDao.update(AssetSnapshotEntity.fromDomainModel(snapshot))
    }
    
    // MARK: - MonthlyUpdate
    
    fun getMonthlyUpdates(): Flow<List<MonthlyUpdate>> =
        monthlyUpdateDao.getAllUpdates().map { list -> list.map { it.toDomainModel() } }
    
    suspend fun getMonthlyUpdatesSync(): List<MonthlyUpdate> =
        monthlyUpdateDao.getAllUpdatesSync().map { it.toDomainModel() }
    
    suspend fun getUpdateByYearMonth(yearMonth: String): MonthlyUpdate? =
        monthlyUpdateDao.getUpdateByYearMonth(yearMonth)?.toDomainModel()
    
    fun getUpdatesByYear(year: Int): Flow<List<MonthlyUpdate>> =
        monthlyUpdateDao.getUpdatesByYear(year.toString()).map { list -> list.map { it.toDomainModel() } }
    
    suspend fun saveMonthlyUpdate(update: MonthlyUpdate) {
        monthlyUpdateDao.insert(MonthlyUpdateEntity.fromDomainModel(update))
    }
    
    suspend fun updateMonthlyUpdate(update: MonthlyUpdate) {
        monthlyUpdateDao.update(MonthlyUpdateEntity.fromDomainModel(update))
    }
    
    suspend fun deleteMonthlyUpdate(update: MonthlyUpdate) {
        monthlyUpdateDao.delete(MonthlyUpdateEntity.fromDomainModel(update))
    }
    
    // MARK: - DepositReminder
    
    fun getDepositReminders(): Flow<List<DepositReminder>> =
        depositReminderDao.getAllReminders().map { list -> list.map { it.toDomainModel() } }
    
    suspend fun getDepositRemindersSync(): List<DepositReminder> =
        depositReminderDao.getAllRemindersSync().map { it.toDomainModel() }
    
    suspend fun getEnabledReminders(): List<DepositReminder> =
        depositReminderDao.getEnabledReminders().map { it.toDomainModel() }
    
    suspend fun saveReminder(reminder: DepositReminder) {
        depositReminderDao.insert(DepositReminderEntity.fromDomainModel(reminder))
    }
    
    suspend fun updateReminder(reminder: DepositReminder) {
        depositReminderDao.update(DepositReminderEntity.fromDomainModel(reminder))
    }
    
    suspend fun deleteReminder(reminder: DepositReminder) {
        depositReminderDao.delete(DepositReminderEntity.fromDomainModel(reminder))
    }
    
    // MARK: - Clear All Data
    
    suspend fun deleteAllData() {
        userProfileDao.deleteAll()
        assetDao.deleteAll()
        assetSnapshotDao.deleteAll()
        monthlyUpdateDao.deleteAll()
        depositReminderDao.deleteAll()
    }
}

