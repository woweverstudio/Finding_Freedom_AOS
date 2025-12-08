package com.woweverstudio.exit_aos.util

import java.text.DecimalFormat
import kotlin.math.abs

/**
 * Exit 앱 전용 숫자 포맷터
 */
object ExitNumberFormatter {
    
    private val decimalFormat = DecimalFormat("#,###")
    
    // MARK: - 금액 포맷팅 (만원 단위)
    
    /**
     * 원 단위를 "X,XXX만원" 형식으로 변환
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "7,500만원")
     */
    fun formatToManWon(value: Double): String {
        val manWon = value / 10_000
        return "${decimalFormat.format(manWon.toLong())}만원"
    }
    
    /**
     * 원 단위를 "X,XXX만" 형식으로 변환
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "7,500만")
     */
    fun formatToMan(value: Double): String {
        val manWon = value / 10_000
        return "${decimalFormat.format(manWon.toLong())}만"
    }
    
    /**
     * 원 단위를 "X억 X,XXX만원" 형식으로 변환 (억 단위 포함)
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "4억 2,750만원")
     */
    fun formatToEokManWon(value: Double): String {
        val absValue = abs(value)
        val eok = (absValue / 100_000_000).toLong()
        val remainingManWon = ((absValue % 100_000_000) / 10_000).toLong()
        
        val sign = if (value < 0) "-" else ""
        
        return when {
            eok > 0 && remainingManWon > 0 -> "$sign${eok}억 ${decimalFormat.format(remainingManWon)}만원"
            eok > 0 -> "$sign${eok}억원"
            else -> formatToManWon(value)
        }
    }
    
    /**
     * 원 단위를 "X억 X,XXX만" 형식으로 변환 (억 단위 포함)
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "4억 2,750만")
     */
    fun formatToEokMan(value: Double): String {
        val absValue = abs(value)
        val eok = (absValue / 100_000_000).toLong()
        val remainingManWon = ((absValue % 100_000_000) / 10_000).toLong()
        
        val sign = if (value < 0) "-" else ""
        
        return when {
            eok > 0 && remainingManWon > 0 -> "$sign${eok}억 ${decimalFormat.format(remainingManWon)}만"
            eok > 0 -> "$sign${eok}억"
            else -> formatToMan(value)
        }
    }
    
    /**
     * 원 단위를 짧은 형식으로 변환 (차트용)
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "150만")
     */
    fun formatToManWonShort(value: Double): String {
        val manWon = value / 10_000
        return "${decimalFormat.format(manWon.toLong())}만"
    }
    
    /**
     * 원 단위를 차트 Y축용 간결한 형식으로 변환
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "3.5억", "0.7억", "500만")
     */
    fun formatChartAxis(value: Double): String {
        if (value <= 0) return "0"
        
        val eok = value / 100_000_000
        
        return when {
            eok >= 1 -> {
                if (eok == eok.toLong().toDouble()) {
                    "${eok.toLong()}억"
                } else {
                    String.format("%.1f억", eok)
                }
            }
            eok >= 0.1 -> String.format("%.1f억", eok)
            else -> {
                val man = value / 10_000
                "${man.toLong()}만"
            }
        }
    }
    
    /**
     * 축약된 금액 표시 (홈화면용)
     * @param current 현재 금액
     * @param target 목표 금액
     * @return 포맷된 문자열 (예: "7,500만원 / 4억 2,750만원")
     */
    fun formatProgressDisplay(current: Double, target: Double): String {
        return "${formatToEokManWon(current)} / ${formatToEokManWon(target)}"
    }
    
    // MARK: - 퍼센트 포맷팅
    
    /**
     * 퍼센트 포맷 (소수점 1자리)
     * @param value 0~100 사이의 퍼센트 값
     * @return 포맷된 문자열 (예: "28.5%")
     */
    fun formatPercent(value: Double): String {
        return String.format("%.1f%%", value)
    }
    
    /**
     * 퍼센트 포맷 (정수)
     * @param value 0~100 사이의 퍼센트 값
     * @return 포맷된 문자열 (예: "28%")
     */
    fun formatPercentInt(value: Double): String {
        return "${value.toInt()}%"
    }
    
    // MARK: - 기간 포맷팅
    
    /**
     * 개월 수를 "X년 Y개월" 형식으로 변환
     * @param months 총 개월 수
     * @return 포맷된 문자열 (예: "7년 6개월")
     */
    fun formatMonthsToYearsMonths(months: Int): String {
        val years = months / 12
        val remainingMonths = months % 12
        
        return when {
            years > 0 && remainingMonths > 0 -> "${years}년 ${remainingMonths}개월"
            years > 0 -> "${years}년"
            else -> "${remainingMonths}개월"
        }
    }
    
    // MARK: - 점수 포맷팅
    
    /**
     * 점수 포맷 (정수)
     * @param value 점수 값
     * @return 포맷된 문자열 (예: "84점")
     */
    fun formatScore(value: Double): String {
        return "${value.toInt()}점"
    }
    
    /**
     * 점수 변화 포맷
     * @param change 변화량
     * @return 포맷된 문자열 (예: "↑5점" 또는 "↓3점")
     */
    fun formatScoreChange(change: Double): String {
        return if (change >= 0) {
            "↑${change.toInt()}점"
        } else {
            "↓${abs(change).toInt()}점"
        }
    }
    
    // MARK: - 입력용 포맷팅
    
    /**
     * 숫자 문자열을 원 단위로 파싱
     * @param string 입력된 문자열
     * @return 원 단위 금액
     */
    fun parseToWon(string: String): Double {
        val cleanString = string
            .replace(",", "")
            .replace("만원", "")
            .replace("원", "")
            .replace(" ", "")
        return cleanString.toDoubleOrNull() ?: 0.0
    }
    
    /**
     * 입력 중인 금액을 포맷팅 (천 단위 쉼표)
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "75,000,000")
     */
    fun formatInputDisplay(value: Double): String {
        return decimalFormat.format(abs(value).toLong())
    }
    
    /**
     * 원 단위 포맷 (천 단위 쉼표 + "원")
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "75,000,000원")
     */
    fun formatToWon(value: Double): String {
        val sign = if (value < 0) "-" else ""
        return "$sign${decimalFormat.format(abs(value).toLong())}원"
    }
    
    /**
     * 원 단위 포맷 (천 단위 쉼표만, "원" 없음)
     * @param value 원 단위 금액
     * @return 포맷된 문자열 (예: "75,000,000")
     */
    fun formatWithComma(value: Double): String {
        val sign = if (value < 0) "-" else ""
        return "$sign${decimalFormat.format(abs(value).toLong())}"
    }
}

