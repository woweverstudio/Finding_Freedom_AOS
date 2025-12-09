package com.woweverstudio.exit_aos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.woweverstudio.exit_aos.domain.model.UserProfile
import java.util.Date
import java.util.UUID

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val desiredMonthlyIncome: Double = 3_000_000.0,
    val currentNetAssets: Double = 0.0,
    val monthlyInvestment: Double = 500_000.0,
    val preRetirementReturnRate: Double = 6.5,
    val postRetirementReturnRate: Double = 4.0,
    @Deprecated("물가상승률 개념 삭제됨 - 사용자가 수익률에 직접 반영")
    val inflationRate: Double = 2.5,  // DB 호환성을 위해 유지
    val hasCompletedOnboarding: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): UserProfile = UserProfile(
        id = id,
        desiredMonthlyIncome = desiredMonthlyIncome,
        currentNetAssets = currentNetAssets,
        monthlyInvestment = monthlyInvestment,
        preRetirementReturnRate = preRetirementReturnRate,
        postRetirementReturnRate = postRetirementReturnRate,
        hasCompletedOnboarding = hasCompletedOnboarding,
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt)
    )
    
    companion object {
        @Suppress("DEPRECATION")
        fun fromDomainModel(profile: UserProfile): UserProfileEntity = UserProfileEntity(
            id = profile.id,
            desiredMonthlyIncome = profile.desiredMonthlyIncome,
            currentNetAssets = profile.currentNetAssets,
            monthlyInvestment = profile.monthlyInvestment,
            preRetirementReturnRate = profile.preRetirementReturnRate,
            postRetirementReturnRate = profile.postRetirementReturnRate,
            inflationRate = 2.5,  // DB 호환성을 위해 기본값 유지
            hasCompletedOnboarding = profile.hasCompletedOnboarding,
            createdAt = profile.createdAt.time,
            updatedAt = profile.updatedAt.time
        )
    }
}

