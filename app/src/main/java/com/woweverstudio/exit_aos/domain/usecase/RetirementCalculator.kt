package com.woweverstudio.exit_aos.domain.usecase

import com.woweverstudio.exit_aos.domain.model.UserProfile
import kotlin.math.min
import kotlin.math.pow

/**
 * 은퇴 계산 결과
 */
data class RetirementCalculationResult(
    /** 은퇴 시 필요 자산 (원 단위) */
    val targetAssets: Double,
    
    /** 은퇴까지 남은 개월 수 */
    val monthsToRetirement: Int,
    
    /** 현재 진행률 (0~100) */
    val progressPercent: Double,
    
    /** 계산에 사용된 현재 자산 */
    val currentAssets: Double,
    
    /** 이미 은퇴 가능한 경우, 희망 월수입을 만들기 위한 최소 필요 수익률 (%) */
    val requiredReturnRate: Double? = null
) {
    /** 은퇴 가능 여부 */
    val isRetirementReady: Boolean
        get() = monthsToRetirement == 0
    
    /** 은퇴까지 남은 연도 */
    val yearsToRetirement: Int
        get() = monthsToRetirement / 12
    
    /** 은퇴까지 남은 개월 (연도 제외) */
    val remainingMonths: Int
        get() = monthsToRetirement % 12
    
    /** D-DAY 표시용 문자열 */
    val dDayString: String
        get() = formatMonthsToYearsMonths(monthsToRetirement)
    
    private fun formatMonthsToYearsMonths(months: Int): String {
        val years = months / 12
        val remainingMonths = months % 12
        
        return when {
            years > 0 && remainingMonths > 0 -> "${years}년 ${remainingMonths}개월"
            years > 0 -> "${years}년"
            else -> "${remainingMonths}개월"
        }
    }
}

/**
 * 은퇴 계산 유스케이스
 * 목표 자산 계산 및 D-DAY 산출
 * 
 * 4% 룰 기반: 목표 자산 = 연간 지출 / 수익률
 * 사용자가 물가상승률을 반영한 수익률을 직접 입력하도록 함
 */
object RetirementCalculator {
    
    // MARK: - 목표 자산 계산
    
    /**
     * 은퇴 시 필요 자산 계산 (4% 룰)
     * 목표 자산 = (희망 월 수입 × 12) / (은퇴 후 수익률 / 100)
     * 
     * @param desiredMonthlyIncome 은퇴 후 희망 월 수입 (원 단위)
     * @param postRetirementReturnRate 은퇴 후 연 목표 수익률 (%, 예: 4.0)
     *        사용자가 물가상승률을 고려하여 직접 입력
     * @return 필요 자산 (원 단위)
     */
    fun calculateTargetAssets(
        desiredMonthlyIncome: Double,
        postRetirementReturnRate: Double
    ): Double {
        val annualIncome = desiredMonthlyIncome * 12
        val returnRate = postRetirementReturnRate / 100
        
        // 수익률이 0 이하인 경우 방지
        if (returnRate <= 0) {
            return annualIncome * 50  // 50년치 자산
        }
        
        return annualIncome / returnRate
    }
    
    // MARK: - 필요 수익률 역산
    
    /**
     * 현재 자산으로 희망 월수입을 만들기 위한 최소 필요 수익률 계산
     * 필요 수익률 = (희망 월수입 × 12) / 현재 자산 × 100
     * 
     * @param currentAssets 현재 자산 (원 단위)
     * @param desiredMonthlyIncome 희망 월 수입 (원 단위)
     * @return 필요 연 수익률 (%)
     */
    fun calculateRequiredReturnRate(
        currentAssets: Double,
        desiredMonthlyIncome: Double
    ): Double {
        if (currentAssets <= 0) return 0.0
        
        val annualIncome = desiredMonthlyIncome * 12
        return annualIncome / currentAssets * 100
    }
    
    // MARK: - D-DAY 계산
    
    /**
     * 목표 자산 도달까지 필요한 개월 수 계산 (월복리 시뮬레이션)
     * 
     * @param currentAssets 현재 순자산 (원 단위)
     * @param targetAssets 목표 자산 (원 단위)
     * @param monthlyInvestment 월 투자 금액 (원 단위)
     * @param annualReturnRate 은퇴 전 연 목표 수익률 (%, 예: 6.5)
     * @return 필요 개월 수
     */
    fun calculateMonthsToRetirement(
        currentAssets: Double,
        targetAssets: Double,
        monthlyInvestment: Double,
        annualReturnRate: Double
    ): Int {
        // 이미 목표 달성한 경우
        if (currentAssets >= targetAssets) {
            return 0
        }
        
        // 월 수익률 계산
        val monthlyReturnRate = (1 + annualReturnRate / 100).pow(1.0 / 12) - 1
        
        var currentValue = currentAssets
        var months = 0
        val maxMonths = 12 * 100  // 최대 100년
        
        while (currentValue < targetAssets && months < maxMonths) {
            // 월초 투자금 추가
            currentValue += monthlyInvestment
            // 월말 수익 적용
            currentValue *= (1 + monthlyReturnRate)
            months++
        }
        
        return months
    }
    
    // MARK: - 통합 계산
    
    /**
     * UserProfile과 현재 자산 기반 은퇴 계산 실행
     * 
     * @param profile 사용자 프로필
     * @param currentAsset 현재 자산
     * @return 계산 결과
     */
    fun calculate(profile: UserProfile, currentAsset: Double): RetirementCalculationResult {
        val targetAssets = calculateTargetAssets(
            desiredMonthlyIncome = profile.desiredMonthlyIncome,
            postRetirementReturnRate = profile.postRetirementReturnRate
        )
        
        val months = calculateMonthsToRetirement(
            currentAssets = currentAsset,
            targetAssets = targetAssets,
            monthlyInvestment = profile.monthlyInvestment,
            annualReturnRate = profile.preRetirementReturnRate
        )
        
        val progress = min((currentAsset / targetAssets) * 100, 100.0)
        
        // 이미 은퇴 가능한 경우 필요 수익률 계산
        val requiredRate: Double? = if (months == 0) {
            calculateRequiredReturnRate(
                currentAssets = currentAsset,
                desiredMonthlyIncome = profile.desiredMonthlyIncome
            )
        } else null
        
        return RetirementCalculationResult(
            targetAssets = targetAssets,
            monthsToRetirement = months,
            progressPercent = progress,
            currentAssets = currentAsset,
            requiredReturnRate = requiredRate
        )
    }
    
    /**
     * 간단한 값들로 계산 실행
     */
    fun calculate(
        desiredMonthlyIncome: Double,
        currentNetAssets: Double,
        monthlyInvestment: Double,
        preRetirementReturnRate: Double = 6.5,
        postRetirementReturnRate: Double = 4.0
    ): RetirementCalculationResult {
        val targetAssets = calculateTargetAssets(
            desiredMonthlyIncome = desiredMonthlyIncome,
            postRetirementReturnRate = postRetirementReturnRate
        )
        
        val months = calculateMonthsToRetirement(
            currentAssets = currentNetAssets,
            targetAssets = targetAssets,
            monthlyInvestment = monthlyInvestment,
            annualReturnRate = preRetirementReturnRate
        )
        
        val progress = min((currentNetAssets / targetAssets) * 100, 100.0)
        
        // 이미 은퇴 가능한 경우 필요 수익률 계산
        val requiredRate: Double? = if (months == 0) {
            calculateRequiredReturnRate(
                currentAssets = currentNetAssets,
                desiredMonthlyIncome = desiredMonthlyIncome
            )
        } else null
        
        return RetirementCalculationResult(
            targetAssets = targetAssets,
            monthsToRetirement = months,
            progressPercent = progress,
            currentAssets = currentNetAssets,
            requiredReturnRate = requiredRate
        )
    }
}
