package com.woweverstudio.exit_aos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.woweverstudio.exit_aos.domain.model.Asset
import java.util.Date
import java.util.UUID

@Entity(tableName = "asset")
data class AssetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Asset = Asset(
        id = id,
        amount = amount,
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt)
    )
    
    companion object {
        fun fromDomainModel(asset: Asset): AssetEntity = AssetEntity(
            id = asset.id,
            amount = asset.amount,
            createdAt = asset.createdAt.time,
            updatedAt = asset.updatedAt.time
        )
    }
}

