package com.woweverstudio.exit_aos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.woweverstudio.exit_aos.domain.model.DepositReminder
import com.woweverstudio.exit_aos.domain.model.RepeatType
import com.woweverstudio.exit_aos.domain.model.Weekday
import java.util.Date
import java.util.UUID

@Entity(tableName = "deposit_reminder")
data class DepositReminderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val repeatType: String = RepeatType.MONTHLY.name,
    val dayOfMonth: Int? = null,
    val dayOfWeek: Int? = null,
    val time: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): DepositReminder = DepositReminder(
        id = id,
        name = name,
        repeatType = RepeatType.valueOf(repeatType),
        dayOfMonth = dayOfMonth,
        dayOfWeek = dayOfWeek?.let { Weekday.fromValue(it) },
        time = Date(time),
        isEnabled = isEnabled,
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt)
    )
    
    companion object {
        fun fromDomainModel(reminder: DepositReminder): DepositReminderEntity = DepositReminderEntity(
            id = reminder.id,
            name = reminder.name,
            repeatType = reminder.repeatType.name,
            dayOfMonth = reminder.dayOfMonth,
            dayOfWeek = reminder.dayOfWeek?.value,
            time = reminder.time.time,
            isEnabled = reminder.isEnabled,
            createdAt = reminder.createdAt.time,
            updatedAt = reminder.updatedAt.time
        )
    }
}

