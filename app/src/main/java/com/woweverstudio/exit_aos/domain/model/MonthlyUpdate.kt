package com.woweverstudio.exit_aos.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 월별 입금 기록
 */
data class MonthlyUpdate(
    val id: String = UUID.randomUUID().toString(),
    
    /** 기록 연월 (yyyyMM 형식) */
    val yearMonth: String = "",
    
    /** 해당 월 투자·저축 입금액 (원 단위) - 레거시, 마이그레이션용 */
    val depositAmount: Double = 0.0,
    
    /** 해당 월 패시브인컴 총액 (배당+이자+월세 등, 원 단위) - 레거시, 마이그레이션용 */
    val passiveIncome: Double = 0.0,
    
    // MARK: - 카테고리별 금액 (5개 항목)
    
    /** 월급/보너스 (원 단위) */
    val salaryAmount: Double = 0.0,
    
    /** 배당금 (원 단위) */
    val dividendAmount: Double = 0.0,
    
    /** 이자 수입 (원 단위) */
    val interestAmount: Double = 0.0,
    
    /** 월세/임대료 (원 단위) */
    val rentAmount: Double = 0.0,
    
    /** 기타 입금 (원 단위) */
    val otherAmount: Double = 0.0,
    
    /** 해당 월 총 자산 (원 단위) */
    val totalAssets: Double = 0.0,
    
    /** 입금 날짜 (실제 입금한 날짜) */
    val depositDate: Date = Date(),
    
    /** 기록일 (데이터 생성/수정 시간) */
    val recordedAt: Date = Date()
) {
    // MARK: - Computed Properties
    
    /** 총 입금액 (5개 카테고리 합계) */
    val totalDeposit: Double
        get() = salaryAmount + dividendAmount + interestAmount + rentAmount + otherAmount
    
    /** 총 패시브인컴 (배당 + 이자 + 월세) */
    val totalPassiveIncome: Double
        get() = dividendAmount + interestAmount + rentAmount
    
    /** 근로소득 (월급 + 기타) */
    val totalActiveIncome: Double
        get() = salaryAmount + otherAmount
    
    companion object {
        private val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
        
        /** 현재 연월 문자열 생성 */
        fun currentYearMonth(): String = dateFormat.format(Date())
        
        /** 날짜로부터 연월 문자열 생성 */
        fun yearMonth(from: Date): String = dateFormat.format(from)
    }
}

