package com.woweverstudio.exit_aos.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 월별 자산 스냅샷 (히스토리용)
 * 안전 점수 중 "자산 성장성" 계산 및 그래프용 데이터로 활용됩니다.
 */
data class AssetSnapshot(
    val id: String = UUID.randomUUID().toString(),
    
    /** 기록 연월 (yyyyMM 형식) */
    val yearMonth: String = "",
    
    /** 해당 시점 자산 (원 단위) */
    val amount: Double = 0.0,
    
    /** 스냅샷 생성일 */
    val snapshotDate: Date = Date()
) {
    companion object {
        private val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
        
        /** 현재 연월 문자열 생성 */
        fun currentYearMonth(): String = dateFormat.format(Date())
        
        /** 이전 달 연월 문자열 생성 */
        fun previousYearMonth(): String? {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            return dateFormat.format(calendar.time)
        }
        
        /** 날짜로부터 연월 문자열 생성 */
        fun yearMonth(from: Date): String = dateFormat.format(from)
    }
}

