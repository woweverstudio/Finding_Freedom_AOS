package com.woweverstudio.exit_aos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.woweverstudio.exit_aos.domain.model.AssetSnapshot
import java.util.Date
import java.util.UUID

@Entity(tableName = "asset_snapshot")
data class AssetSnapshotEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val yearMonth: String = "",
    val amount: Double = 0.0,
    val snapshotDate: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): AssetSnapshot = AssetSnapshot(
        id = id,
        yearMonth = yearMonth,
        amount = amount,
        snapshotDate = Date(snapshotDate)
    )
    
    companion object {
        fun fromDomainModel(snapshot: AssetSnapshot): AssetSnapshotEntity = AssetSnapshotEntity(
            id = snapshot.id,
            yearMonth = snapshot.yearMonth,
            amount = snapshot.amount,
            snapshotDate = snapshot.snapshotDate.time
        )
    }
}

