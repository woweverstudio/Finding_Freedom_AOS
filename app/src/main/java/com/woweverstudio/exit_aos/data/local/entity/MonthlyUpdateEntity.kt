package com.woweverstudio.exit_aos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.woweverstudio.exit_aos.domain.model.MonthlyUpdate
import java.util.Date
import java.util.UUID

@Entity(tableName = "monthly_update")
data class MonthlyUpdateEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val yearMonth: String = "",
    val depositAmount: Double = 0.0,
    val passiveIncome: Double = 0.0,
    val salaryAmount: Double = 0.0,
    val dividendAmount: Double = 0.0,
    val interestAmount: Double = 0.0,
    val rentAmount: Double = 0.0,
    val otherAmount: Double = 0.0,
    val totalAssets: Double = 0.0,
    val depositDate: Long = System.currentTimeMillis(),
    val recordedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): MonthlyUpdate = MonthlyUpdate(
        id = id,
        yearMonth = yearMonth,
        depositAmount = depositAmount,
        passiveIncome = passiveIncome,
        salaryAmount = salaryAmount,
        dividendAmount = dividendAmount,
        interestAmount = interestAmount,
        rentAmount = rentAmount,
        otherAmount = otherAmount,
        totalAssets = totalAssets,
        depositDate = Date(depositDate),
        recordedAt = Date(recordedAt)
    )
    
    companion object {
        fun fromDomainModel(update: MonthlyUpdate): MonthlyUpdateEntity = MonthlyUpdateEntity(
            id = update.id,
            yearMonth = update.yearMonth,
            depositAmount = update.depositAmount,
            passiveIncome = update.passiveIncome,
            salaryAmount = update.salaryAmount,
            dividendAmount = update.dividendAmount,
            interestAmount = update.interestAmount,
            rentAmount = update.rentAmount,
            otherAmount = update.otherAmount,
            totalAssets = update.totalAssets,
            depositDate = update.depositDate.time,
            recordedAt = update.recordedAt.time
        )
    }
}

