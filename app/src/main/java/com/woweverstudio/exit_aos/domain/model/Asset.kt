package com.woweverstudio.exit_aos.domain.model

import java.util.Date
import java.util.UUID

/**
 * 앱 전체에서 단일로 관리되는 현재 자산
 * 모든 시나리오가 이 값을 참조합니다.
 */
data class Asset(
    val id: String = UUID.randomUUID().toString(),
    
    /** 현재 총 순자산 (원 단위) */
    val amount: Double = 0.0,
    
    /** 생성일 */
    val createdAt: Date = Date(),
    
    /** 마지막 업데이트일 */
    val updatedAt: Date = Date()
) {
    /**
     * 자산 업데이트
     */
    fun update(amount: Double): Asset = copy(
        amount = amount,
        updatedAt = Date()
    )
}

