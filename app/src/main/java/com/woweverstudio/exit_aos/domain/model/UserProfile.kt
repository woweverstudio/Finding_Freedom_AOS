package com.woweverstudio.exit_aos.domain.model

import java.util.Date
import java.util.UUID

/**
 * 사용자 프로필 도메인 모델
 * 은퇴 계획에 필요한 사용자 설정 정보
 */
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    
    /** 은퇴 후 희망 월 수입 (원 단위) */
    val desiredMonthlyIncome: Double = 3_000_000.0,
    
    /** 현재 순자산 - 투자 가능 자산만 (원 단위) */
    val currentNetAssets: Double = 0.0,
    
    /** 월 평균 저축·투자 금액 (원 단위) */
    val monthlyInvestment: Double = 500_000.0,
    
    /** 은퇴 전 연 목표 수익률 (%) */
    val preRetirementReturnRate: Double = 6.5,
    
    /** 은퇴 후 연 목표 수익률 (%) - 물가상승률을 고려하여 사용자가 직접 설정 */
    val postRetirementReturnRate: Double = 4.0,
    
    /** 온보딩 완료 여부 */
    val hasCompletedOnboarding: Boolean = false,
    
    /** 생성일 */
    val createdAt: Date = Date(),
    
    /** 마지막 업데이트일 */
    val updatedAt: Date = Date()
) {
    /**
     * 설정 업데이트
     */
    fun updateSettings(
        desiredMonthlyIncome: Double? = null,
        monthlyInvestment: Double? = null,
        preRetirementReturnRate: Double? = null,
        postRetirementReturnRate: Double? = null
    ): UserProfile = copy(
        desiredMonthlyIncome = desiredMonthlyIncome ?: this.desiredMonthlyIncome,
        monthlyInvestment = monthlyInvestment ?: this.monthlyInvestment,
        preRetirementReturnRate = preRetirementReturnRate ?: this.preRetirementReturnRate,
        postRetirementReturnRate = postRetirementReturnRate ?: this.postRetirementReturnRate,
        updatedAt = Date()
    )
}

