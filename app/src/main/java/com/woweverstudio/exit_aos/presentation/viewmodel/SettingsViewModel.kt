package com.woweverstudio.exit_aos.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woweverstudio.exit_aos.data.repository.ExitRepository
import com.woweverstudio.exit_aos.domain.model.Announcement
import com.woweverstudio.exit_aos.domain.model.AnnouncementsData
import com.woweverstudio.exit_aos.domain.model.DepositReminder
import com.woweverstudio.exit_aos.domain.model.RepeatType
import com.woweverstudio.exit_aos.domain.model.Weekday
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * 설정 화면 UI 상태
 */
data class SettingsUiState(
    /** 알람 목록 */
    val depositReminders: List<DepositReminder> = emptyList(),
    
    /** 공지사항 목록 */
    val announcements: List<Announcement> = emptyList(),
    
    /** 읽지 않은 공지사항 수 */
    val unreadCount: Int = 0,
    
    /** 알람 시트 표시 여부 */
    val showReminderSheet: Boolean = false,
    
    /** 수정 중인 알람 (null이면 새로 추가) */
    val editingReminder: DepositReminder? = null,
    
    /** 공지사항 목록 시트 표시 여부 */
    val showAnnouncementList: Boolean = false,
    
    /** 선택된 공지사항 (상세 보기) */
    val selectedAnnouncement: Announcement? = null,
    
    /** 데이터 삭제 확인 다이얼로그 표시 여부 */
    val showDeleteConfirm: Boolean = false,
    
    /** 이메일 복사 토스트 표시 여부 */
    val showCopiedToast: Boolean = false,
    
    // 알람 입력 상태
    val reminderName: String = "",
    val reminderRepeatType: RepeatType = RepeatType.MONTHLY,
    val reminderDayOfMonth: Int = 1,
    val reminderDayOfWeek: Weekday = Weekday.MONDAY,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val reminderIsEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ExitRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // 읽은 공지사항 ID 저장용 SharedPreferences key
    private val readAnnouncementsKey = "read_announcements"
    private val prefs by lazy {
        context.getSharedPreferences("exit_settings", Context.MODE_PRIVATE)
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        loadAnnouncements()
        loadReminders()
    }
    
    // MARK: - Data Loading
    
    private fun loadAnnouncements() {
        viewModelScope.launch {
            try {
                val jsonString = context.assets.open("announcements.json").bufferedReader().use { it.readText() }
                val data = json.decodeFromString<AnnouncementsData>(jsonString)
                val readIds = getReadAnnouncementIds()
                
                val announcements = data.announcements.map { announcement ->
                    announcement.apply { isRead = readIds.contains(id) }
                }
                
                _uiState.update { 
                    it.copy(
                        announcements = announcements,
                        unreadCount = announcements.count { !it.isRead }
                    ) 
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadReminders() {
        viewModelScope.launch {
            repository.getDepositReminders().collect { reminders ->
                _uiState.update { it.copy(depositReminders = reminders) }
            }
        }
    }
    
    // MARK: - Reminder Actions
    
    /** 알람 추가 시트 열기 */
    fun openAddReminderSheet() {
        val calendar = Calendar.getInstance()
        _uiState.update {
            it.copy(
                editingReminder = null,
                reminderName = "",
                reminderRepeatType = RepeatType.MONTHLY,
                reminderDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                reminderDayOfWeek = Weekday.fromValue(calendar.get(Calendar.DAY_OF_WEEK)) ?: Weekday.MONDAY,
                reminderHour = 9,
                reminderMinute = 0,
                reminderIsEnabled = true,
                showReminderSheet = true
            )
        }
    }
    
    /** 알람 수정 시트 열기 */
    fun openEditReminderSheet(reminder: DepositReminder) {
        val calendar = Calendar.getInstance().apply { time = reminder.time }
        _uiState.update {
            it.copy(
                editingReminder = reminder,
                reminderName = reminder.name,
                reminderRepeatType = reminder.repeatType,
                reminderDayOfMonth = reminder.dayOfMonth ?: 1,
                reminderDayOfWeek = reminder.dayOfWeek ?: Weekday.MONDAY,
                reminderHour = calendar.get(Calendar.HOUR_OF_DAY),
                reminderMinute = calendar.get(Calendar.MINUTE),
                reminderIsEnabled = reminder.isEnabled,
                showReminderSheet = true
            )
        }
    }
    
    /** 알람 시트 닫기 */
    fun dismissReminderSheet() {
        _uiState.update { it.copy(showReminderSheet = false) }
    }
    
    /** 알람 이름 변경 */
    fun updateReminderName(name: String) {
        _uiState.update { it.copy(reminderName = name) }
    }
    
    /** 반복 유형 변경 */
    fun updateRepeatType(type: RepeatType) {
        _uiState.update { it.copy(reminderRepeatType = type) }
    }
    
    /** 날짜 변경 (월간) */
    fun updateDayOfMonth(day: Int) {
        _uiState.update { it.copy(reminderDayOfMonth = day) }
    }
    
    /** 요일 변경 (주간) */
    fun updateDayOfWeek(day: Weekday) {
        _uiState.update { it.copy(reminderDayOfWeek = day) }
    }
    
    /** 시간 변경 */
    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute) }
    }
    
    /** 알람 저장 */
    fun saveReminder() {
        val state = _uiState.value
        if (state.reminderName.isBlank()) return
        
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, state.reminderHour)
                set(Calendar.MINUTE, state.reminderMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val existing = state.editingReminder
            if (existing != null) {
                // 기존 알람 수정
                val updated = existing.copy(
                    name = state.reminderName,
                    repeatType = state.reminderRepeatType,
                    dayOfMonth = if (state.reminderRepeatType == RepeatType.MONTHLY) state.reminderDayOfMonth else null,
                    dayOfWeek = if (state.reminderRepeatType == RepeatType.WEEKLY) state.reminderDayOfWeek else null,
                    time = calendar.time,
                    isEnabled = state.reminderIsEnabled,
                    updatedAt = Date()
                )
                repository.updateReminder(updated)
            } else {
                // 새 알람 생성
                val newReminder = DepositReminder(
                    name = state.reminderName,
                    repeatType = state.reminderRepeatType,
                    dayOfMonth = if (state.reminderRepeatType == RepeatType.MONTHLY) state.reminderDayOfMonth else null,
                    dayOfWeek = if (state.reminderRepeatType == RepeatType.WEEKLY) state.reminderDayOfWeek else null,
                    time = calendar.time,
                    isEnabled = state.reminderIsEnabled
                )
                repository.saveReminder(newReminder)
            }
            
            _uiState.update { it.copy(showReminderSheet = false) }
        }
    }
    
    /** 알람 삭제 */
    fun deleteReminder(reminder: DepositReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
    
    /** 알람 활성화 토글 */
    fun toggleReminder(reminder: DepositReminder) {
        viewModelScope.launch {
            val updated = reminder.copy(
                isEnabled = !reminder.isEnabled,
                updatedAt = Date()
            )
            repository.updateReminder(updated)
        }
    }
    
    // MARK: - Announcement Actions
    
    /** 공지사항 목록 열기 */
    fun showAnnouncementList() {
        _uiState.update { it.copy(showAnnouncementList = true) }
    }
    
    /** 공지사항 목록 닫기 */
    fun dismissAnnouncementList() {
        _uiState.update { it.copy(showAnnouncementList = false, selectedAnnouncement = null) }
    }
    
    /** 공지사항 선택 (상세보기) */
    fun selectAnnouncement(announcement: Announcement) {
        markAsRead(announcement)
        _uiState.update { it.copy(selectedAnnouncement = announcement) }
    }
    
    /** 공지사항 상세 닫기 */
    fun dismissAnnouncementDetail() {
        _uiState.update { it.copy(selectedAnnouncement = null) }
    }
    
    /** 공지사항 읽음 처리 */
    fun markAsRead(announcement: Announcement) {
        val readIds = getReadAnnouncementIds().toMutableSet()
        readIds.add(announcement.id)
        saveReadAnnouncementIds(readIds)
        
        _uiState.update { state ->
            val updatedAnnouncements = state.announcements.map {
                if (it.id == announcement.id) it.apply { isRead = true } else it
            }
            state.copy(
                announcements = updatedAnnouncements,
                unreadCount = updatedAnnouncements.count { !it.isRead }
            )
        }
    }
    
    private fun getReadAnnouncementIds(): Set<String> {
        return prefs.getStringSet(readAnnouncementsKey, emptySet()) ?: emptySet()
    }
    
    private fun saveReadAnnouncementIds(ids: Set<String>) {
        prefs.edit().putStringSet(readAnnouncementsKey, ids).apply()
    }
    
    // MARK: - Data Management
    
    /** 삭제 확인 다이얼로그 표시 */
    fun showDeleteConfirmDialog() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }
    
    /** 삭제 확인 다이얼로그 닫기 */
    fun dismissDeleteConfirmDialog() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }
    
    /** 모든 데이터 삭제 */
    fun deleteAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllData()
            // 공지사항 읽음 상태 초기화
            prefs.edit().remove(readAnnouncementsKey).apply()
            _uiState.update { it.copy(showDeleteConfirm = false) }
            onComplete()
        }
    }
    
    // MARK: - Toast
    
    /** 복사 토스트 표시 */
    fun showCopiedToast() {
        _uiState.update { it.copy(showCopiedToast = true) }
    }
    
    /** 복사 토스트 숨기기 */
    fun hideCopiedToast() {
        _uiState.update { it.copy(showCopiedToast = false) }
    }
}

