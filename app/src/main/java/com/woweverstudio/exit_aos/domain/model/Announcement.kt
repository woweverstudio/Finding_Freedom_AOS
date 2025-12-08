package com.woweverstudio.exit_aos.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 공지사항 타입
 */
@Serializable
enum class AnnouncementType(val displayName: String, val icon: String, val colorHex: String) {
    @SerialName("공지")
    NOTICE("공지", "campaign", "00D4AA"),
    
    @SerialName("업데이트")
    UPDATE("업데이트", "auto_awesome", "00B894"),
    
    @SerialName("이벤트")
    EVENT("이벤트", "card_giftcard", "FF9500"),
    
    @SerialName("꿀팁")
    TIP("꿀팁", "lightbulb", "34C759")
}

/**
 * 공지사항 모델 (JSON에서 로드)
 */
@Serializable
data class Announcement(
    /** 고유 식별자 */
    val id: String,
    
    /** 공지사항 제목 */
    val title: String,
    
    /** 공지사항 내용 */
    val content: String,
    
    /** 공지사항 타입 */
    val type: AnnouncementType,
    
    /** 중요 공지 여부 */
    val isImportant: Boolean,
    
    /** 게시일 (ISO8601 형식) */
    val publishedAt: String,
    
    /** 버전 (선택적 - 업데이트 공지용) */
    val version: String? = null,
    
    /** 링크 URL (선택적) */
    val linkURL: String? = null
) {
    /** 읽음 여부 (런타임에서 관리) */
    @kotlinx.serialization.Transient
    var isRead: Boolean = false
    
    /** 게시일 Date 객체 */
    val publishedDate: Date?
        get() {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                format.parse(publishedAt)
            } catch (e: Exception) {
                null
            }
        }
    
    /** 게시일 표시 텍스트 */
    val publishedDateText: String
        get() {
            val date = publishedDate ?: return ""
            val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            return formatter.format(date)
        }
    
    /** 상대 시간 텍스트 (예: "3일 전") */
    val relativeTimeText: String
        get() {
            val date = publishedDate ?: return ""
            val now = Date()
            val diff = now.time - date.time
            
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            
            return when {
                days > 30 -> {
                    val months = days / 30
                    "${months}개월 전"
                }
                days > 0 -> "${days}일 전"
                hours > 0 -> "${hours}시간 전"
                minutes > 0 -> "${minutes}분 전"
                else -> "방금 전"
            }
        }
}

/**
 * 공지사항 JSON 루트 구조
 */
@Serializable
data class AnnouncementsData(
    val announcements: List<Announcement>
)

