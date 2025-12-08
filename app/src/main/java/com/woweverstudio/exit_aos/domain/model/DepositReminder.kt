package com.woweverstudio.exit_aos.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 반복 유형
 */
enum class RepeatType(val displayName: String, val icon: String) {
    ONCE("한 번만", "1"),
    DAILY("매일", "calendar_today"),
    WEEKLY("매주", "date_range"),
    MONTHLY("매월", "calendar_month");
    
    companion object {
        fun fromDisplayName(name: String): RepeatType =
            entries.find { it.displayName == name } ?: MONTHLY
    }
}

/**
 * 요일 (주간 반복용)
 */
enum class Weekday(val value: Int, val shortName: String, val fullName: String) {
    SUNDAY(Calendar.SUNDAY, "일", "일요일"),
    MONDAY(Calendar.MONDAY, "월", "월요일"),
    TUESDAY(Calendar.TUESDAY, "화", "화요일"),
    WEDNESDAY(Calendar.WEDNESDAY, "수", "수요일"),
    THURSDAY(Calendar.THURSDAY, "목", "목요일"),
    FRIDAY(Calendar.FRIDAY, "금", "금요일"),
    SATURDAY(Calendar.SATURDAY, "토", "토요일");
    
    companion object {
        fun fromValue(value: Int): Weekday? =
            entries.find { it.value == value }
    }
}

/**
 * 입금 알람 모델
 */
data class DepositReminder(
    val id: String = UUID.randomUUID().toString(),
    
    /** 알람 이름 (예: "월급", "배당금") */
    val name: String,
    
    /** 반복 유형 */
    val repeatType: RepeatType = RepeatType.MONTHLY,
    
    /** 월간 반복 시 날짜 (1~31) */
    val dayOfMonth: Int? = null,
    
    /** 주간 반복 시 요일 */
    val dayOfWeek: Weekday? = null,
    
    /** 알람 시간 */
    val time: Date = createDefaultTime(),
    
    /** 활성화 여부 */
    val isEnabled: Boolean = true,
    
    /** 생성일 */
    val createdAt: Date = Date(),
    
    /** 수정일 */
    val updatedAt: Date = Date()
) {
    /** 알람 설명 텍스트 */
    val descriptionText: String
        get() {
            val timeFormatter = SimpleDateFormat("a h:mm", Locale.KOREAN)
            val timeString = timeFormatter.format(time)
            
            return when (repeatType) {
                RepeatType.ONCE -> timeString
                RepeatType.DAILY -> "매일 $timeString"
                RepeatType.WEEKLY -> {
                    dayOfWeek?.let { "매주 ${it.fullName} $timeString" } ?: "매주 $timeString"
                }
                RepeatType.MONTHLY -> {
                    dayOfMonth?.let { "매월 ${it}일 $timeString" } ?: "매월 $timeString"
                }
            }
        }
    
    companion object {
        private fun createDefaultTime(): Date {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.time
        }
    }
}

