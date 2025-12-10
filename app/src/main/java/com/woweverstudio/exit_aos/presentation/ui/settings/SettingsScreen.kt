package com.woweverstudio.exit_aos.presentation.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.woweverstudio.exit_aos.domain.model.Announcement
import com.woweverstudio.exit_aos.domain.model.DepositReminder
import com.woweverstudio.exit_aos.domain.model.RepeatType
import com.woweverstudio.exit_aos.domain.model.Weekday
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

private const val CONTACT_EMAIL = "woweverstudio@gmail.com"
private const val INSTAGRAM_URL = "https://www.instagram.com/woweverstudio/"
private const val INSTAGRAM_APP_URL = "instagram://user?username=woweverstudio"

/**
 * 설정 화면
 */
@Composable
fun SettingsScreen(
    onDeleteAllData: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 공지사항 목록 시트
    if (uiState.showAnnouncementList) {
        AnnouncementListSheet(
            announcements = uiState.announcements,
            selectedAnnouncement = uiState.selectedAnnouncement,
            onAnnouncementClick = { viewModel.selectAnnouncement(it) },
            onBackFromDetail = { viewModel.dismissAnnouncementDetail() },
            onDismiss = { viewModel.dismissAnnouncementList() }
        )
    }
    
    // 알람 추가/수정 시트
    if (uiState.showReminderSheet) {
        ReminderEditSheet(
            isEditing = uiState.editingReminder != null,
            reminderName = uiState.reminderName,
            repeatType = uiState.reminderRepeatType,
            dayOfMonth = uiState.reminderDayOfMonth,
            dayOfWeek = uiState.reminderDayOfWeek,
            hour = uiState.reminderHour,
            minute = uiState.reminderMinute,
            onNameChange = { viewModel.updateReminderName(it) },
            onRepeatTypeChange = { viewModel.updateRepeatType(it) },
            onDayOfMonthChange = { viewModel.updateDayOfMonth(it) },
            onDayOfWeekChange = { viewModel.updateDayOfWeek(it) },
            onTimeChange = { hour, minute -> viewModel.updateTime(hour, minute) },
            onSave = { viewModel.saveReminder() },
            onDelete = { uiState.editingReminder?.let { viewModel.deleteReminder(it) } },
            onDismiss = { viewModel.dismissReminderSheet() }
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ExitColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XL)
    ) {
        // 공지사항 섹션
        AnnouncementSection(
            latestAnnouncement = uiState.announcements.firstOrNull(),
            onClick = { viewModel.showAnnouncementList() }
        )
        
        // 알람 섹션
        ReminderSection(
            reminders = uiState.depositReminders,
            onAddClick = { viewModel.openAddReminderSheet() },
            onReminderClick = { viewModel.openEditReminderSheet(it) },
            onToggle = { viewModel.toggleReminder(it) }
        )
        
        // 문의하기 섹션
        ContactSection(
            showCopiedToast = uiState.showCopiedToast,
            onEmailCopy = {
                copyToClipboard(context, CONTACT_EMAIL)
                viewModel.showCopiedToast()
            },
            onHideToast = { viewModel.hideCopiedToast() },
            onInstagramClick = { openInstagram(context) }
        )
        
        // 앱 정보 섹션
        AppInfoSection()
        
        // 데이터 관리 섹션
        SettingsItem(
            title = "모든 데이터 삭제",
            titleColor = ExitColors.Warning.copy(alpha = 0.8f),
            onClick = { viewModel.showDeleteConfirmDialog() }
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.XL))
    }
    
    // 삭제 확인 다이얼로그
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmDialog() },
            title = {
                Text(
                    text = "데이터 삭제",
                    style = ExitTypography.Title3,
                    color = ExitColors.PrimaryText
                )
            },
            text = {
                Text(
                    text = "모든 입금 기록, 자산 정보, 시나리오가 삭제됩니다.\n이 작업은 되돌릴 수 없습니다.",
                    style = ExitTypography.Body,
                    color = ExitColors.SecondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData(onComplete = onDeleteAllData)
                    }
                ) {
                    Text(text = "삭제", color = ExitColors.Warning)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmDialog() }) {
                    Text(text = "취소", color = ExitColors.SecondaryText)
                }
            },
            containerColor = ExitColors.CardBackground
        )
    }
}

// MARK: - Announcement Section

@Composable
private fun AnnouncementSection(
    latestAnnouncement: Announcement?,
    onClick: () -> Unit
) {
    SettingsSection(title = "공지사항") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.CardBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = ExitColors.Accent)
                ) { onClick() }
                .padding(ExitSpacing.MD),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (latestAnnouncement != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = latestAnnouncement.title,
                        style = ExitTypography.Body,
                        fontWeight = if (latestAnnouncement.isRead) FontWeight.Normal else FontWeight.Medium,
                        color = ExitColors.PrimaryText,
                        maxLines = 1
                    )
                    Text(
                        text = latestAnnouncement.relativeTimeText,
                        style = ExitTypography.Caption,
                        color = ExitColors.TertiaryText
                    )
                }
                
                if (!latestAnnouncement.isRead) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(ExitColors.Accent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(ExitSpacing.SM))
                }
            } else {
                Text(
                    text = "공지사항이 없습니다",
                    style = ExitTypography.Body,
                    color = ExitColors.TertiaryText,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ExitColors.TertiaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// MARK: - Reminder Section

@Composable
private fun ReminderSection(
    reminders: List<DepositReminder>,
    onAddClick: () -> Unit,
    onReminderClick: (DepositReminder) -> Unit,
    onToggle: (DepositReminder) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "알람")
            
            TextButton(onClick = onAddClick) {
                Text(
                    text = "추가",
                    style = ExitTypography.Caption,
                    color = ExitColors.Accent
                )
            }
        }
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.CardBackground)
        ) {
            if (reminders.isEmpty()) {
                Text(
                    text = "등록된 알람이 없습니다",
                    style = ExitTypography.Body,
                    color = ExitColors.TertiaryText,
                    modifier = Modifier.padding(ExitSpacing.MD)
                )
            } else {
                reminders.forEachIndexed { index, reminder ->
                    ReminderRow(
                        reminder = reminder,
                        onClick = { onReminderClick(reminder) },
                        onToggle = { onToggle(reminder) }
                    )
                    
                    if (index < reminders.lastIndex) {
                        HorizontalDivider(
                            color = ExitColors.Divider,
                            modifier = Modifier.padding(start = ExitSpacing.MD)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: DepositReminder,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() }
            .padding(ExitSpacing.MD),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.name,
                style = ExitTypography.Body,
                color = if (reminder.isEnabled) ExitColors.PrimaryText else ExitColors.TertiaryText
            )
            Text(
                text = reminder.descriptionText,
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
        }
        
        Switch(
            checked = reminder.isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ExitColors.Accent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ExitColors.TertiaryText.copy(alpha = 0.3f)
            )
        )
    }
}

// MARK: - Contact Section

@Composable
private fun ContactSection(
    showCopiedToast: Boolean,
    onEmailCopy: () -> Unit,
    onHideToast: () -> Unit,
    onInstagramClick: () -> Unit
) {
    // 토스트 자동 숨김
    LaunchedEffect(showCopiedToast) {
        if (showCopiedToast) {
            delay(2000)
            onHideToast()
        }
    }
    
    SettingsSection(title = "문의하기") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.CardBackground)
        ) {
            // 이메일
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = ExitColors.Accent)
                    ) { onEmailCopy() }
                    .padding(ExitSpacing.MD),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "이메일",
                        style = ExitTypography.Body,
                        color = ExitColors.PrimaryText
                    )
                    Text(
                        text = CONTACT_EMAIL,
                        style = ExitTypography.Caption,
                        color = ExitColors.TertiaryText
                    )
                }
                
                Text(
                    text = if (showCopiedToast) "복사됨" else "복사",
                    style = ExitTypography.Caption,
                    color = if (showCopiedToast) ExitColors.Accent else ExitColors.TertiaryText
                )
            }
            
            HorizontalDivider(
                color = ExitColors.Divider,
                modifier = Modifier.padding(start = ExitSpacing.MD)
            )
            
            // 인스타그램
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = ExitColors.Accent)
                    ) { onInstagramClick() }
                    .padding(ExitSpacing.MD),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "인스타그램",
                        style = ExitTypography.Body,
                        color = ExitColors.PrimaryText
                    )
                    Text(
                        text = "@woweverstudio",
                        style = ExitTypography.Caption,
                        color = ExitColors.TertiaryText
                    )
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ExitColors.TertiaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// MARK: - App Info Section

@Composable
private fun AppInfoSection() {
    SettingsSection(title = "앱 정보") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.MD),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "버전",
                style = ExitTypography.Body,
                color = ExitColors.PrimaryText,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "1.0.0",
                style = ExitTypography.Body,
                color = ExitColors.TertiaryText
            )
        }
    }
}

// MARK: - Common Components

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        SectionHeader(title = title)
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        content()
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = ExitTypography.Caption,
        fontWeight = FontWeight.Medium,
        color = ExitColors.SecondaryText,
        modifier = Modifier.padding(start = ExitSpacing.XS)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    titleColor: Color = ExitColors.PrimaryText,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.CardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() }
            .padding(ExitSpacing.MD),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ExitTypography.Body,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = ExitTypography.Caption,
                    color = ExitColors.TertiaryText
                )
            }
        }
        
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ExitColors.TertiaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// MARK: - Helper Functions

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("email", text)
    clipboard.setPrimaryClip(clip)
}

private fun openInstagram(context: Context) {
    try {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_APP_URL))
        appIntent.setPackage("com.instagram.android")
        context.startActivity(appIntent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL))
        context.startActivity(webIntent)
    }
}

// MARK: - Announcement List Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementListSheet(
    announcements: List<Announcement>,
    selectedAnnouncement: Announcement?,
    onAnnouncementClick: (Announcement) -> Unit,
    onBackFromDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ExitColors.Background,
        dragHandle = null
    ) {
        if (selectedAnnouncement != null) {
            // 상세 화면
            AnnouncementDetailContent(
                announcement = selectedAnnouncement,
                onBack = onBackFromDetail
            )
        } else {
            // 목록 화면
            AnnouncementListContent(
                announcements = announcements,
                onAnnouncementClick = onAnnouncementClick,
                onDismiss = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementListContent(
    announcements: List<Announcement>,
    onAnnouncementClick: (Announcement) -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        containerColor = ExitColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "공지사항",
                        style = ExitTypography.Title3,
                        color = ExitColors.PrimaryText
                    )
                },
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "닫기",
                            style = ExitTypography.Body,
                            color = ExitColors.SecondaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ExitColors.Background
                )
            )
        }
    ) { paddingValues ->
        if (announcements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "공지사항이 없습니다",
                    style = ExitTypography.Body,
                    color = ExitColors.TertiaryText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = ExitSpacing.MD)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ExitRadius.MD))
                            .background(ExitColors.CardBackground)
                    ) {
                        announcements.forEachIndexed { index, announcement ->
                            AnnouncementRow(
                                announcement = announcement,
                                onClick = { onAnnouncementClick(announcement) }
                            )
                            
                            if (index < announcements.lastIndex) {
                                HorizontalDivider(
                                    color = ExitColors.Divider,
                                    modifier = Modifier.padding(start = ExitSpacing.MD)
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(ExitSpacing.XL)) }
            }
        }
    }
}

@Composable
private fun AnnouncementRow(
    announcement: Announcement,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() }
            .padding(ExitSpacing.MD),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = announcement.title,
                style = ExitTypography.Body,
                fontWeight = if (announcement.isRead) FontWeight.Normal else FontWeight.Medium,
                color = if (announcement.isRead) ExitColors.SecondaryText else ExitColors.PrimaryText,
                maxLines = 2
            )
            Text(
                text = announcement.relativeTimeText,
                style = ExitTypography.Caption,
                color = ExitColors.TertiaryText
            )
        }
        
        if (!announcement.isRead) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(ExitColors.Accent, CircleShape)
            )
            Spacer(modifier = Modifier.width(ExitSpacing.SM))
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ExitColors.TertiaryText,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementDetailContent(
    announcement: Announcement,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = ExitColors.Background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "뒤로",
                            tint = ExitColors.SecondaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ExitColors.Background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(ExitSpacing.LG)
        ) {
            // 날짜
            Text(
                text = announcement.publishedDateText,
                style = ExitTypography.Caption,
                color = ExitColors.TertiaryText
            )
            
            Spacer(modifier = Modifier.height(ExitSpacing.SM))
            
            // 제목
            Text(
                text = announcement.title,
                style = ExitTypography.Title3,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.PrimaryText
            )
            
            Spacer(modifier = Modifier.height(ExitSpacing.LG))
            
            // 내용
            Text(
                text = announcement.content,
                style = ExitTypography.Subheadline,
                color = ExitColors.SecondaryText,
                lineHeight = ExitTypography.Subheadline.lineHeight * 1.5f
            )
        }
    }
}

// MARK: - Reminder Edit Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderEditSheet(
    isEditing: Boolean,
    reminderName: String,
    repeatType: RepeatType,
    dayOfMonth: Int,
    dayOfWeek: Weekday,
    hour: Int,
    minute: Int,
    onNameChange: (String) -> Unit,
    onRepeatTypeChange: (RepeatType) -> Unit,
    onDayOfMonthChange: (Int) -> Unit,
    onDayOfWeekChange: (Weekday) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = false
    )
    
    // 시간 변경 감지
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(timePickerState.hour, timePickerState.minute)
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ExitColors.Background,
        dragHandle = null
    ) {
        Scaffold(
            containerColor = ExitColors.Background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEditing) "알람 수정" else "알람 추가",
                            style = ExitTypography.Title3,
                            color = ExitColors.PrimaryText
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "취소",
                                style = ExitTypography.Body,
                                color = ExitColors.SecondaryText
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = onSave,
                            enabled = reminderName.isNotBlank()
                        ) {
                            Text(
                                text = "저장",
                                style = ExitTypography.Body,
                                fontWeight = FontWeight.SemiBold,
                                color = if (reminderName.isBlank()) ExitColors.TertiaryText else ExitColors.Accent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ExitColors.Background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(ExitSpacing.LG),
                verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
            ) {
                // 이름 입력
                Column {
                    Text(
                        text = "알람 이름",
                        style = ExitTypography.Caption,
                        color = ExitColors.SecondaryText
                    )
                    Spacer(modifier = Modifier.height(ExitSpacing.SM))
                    TextField(
                        value = reminderName,
                        onValueChange = onNameChange,
                        placeholder = {
                            Text(
                                text = "예: 월급, 배당금",
                                style = ExitTypography.Body,
                                color = ExitColors.TertiaryText
                            )
                        },
                        textStyle = ExitTypography.Body,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ExitColors.CardBackground,
                            unfocusedContainerColor = ExitColors.CardBackground,
                            focusedTextColor = ExitColors.PrimaryText,
                            unfocusedTextColor = ExitColors.PrimaryText,
                            cursorColor = ExitColors.Accent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(ExitRadius.MD),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 반복 유형
                Column {
                    Text(
                        text = "반복",
                        style = ExitTypography.Caption,
                        color = ExitColors.SecondaryText
                    )
                    Spacer(modifier = Modifier.height(ExitSpacing.SM))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RepeatType.entries.forEach { type ->
                            val isSelected = repeatType == type
                            Surface(
                                onClick = { onRepeatTypeChange(type) },
                                shape = RoundedCornerShape(ExitRadius.MD),
                                color = if (isSelected) ExitColors.Accent.copy(alpha = 0.1f) else ExitColors.CardBackground,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = type.displayName,
                                    style = ExitTypography.Caption,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) ExitColors.Accent else ExitColors.SecondaryText,
                                    modifier = Modifier.padding(vertical = ExitSpacing.MD),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // 날짜/요일 선택
                when (repeatType) {
                    RepeatType.MONTHLY -> {
                        Column {
                            Text(
                                text = "날짜",
                                style = ExitTypography.Caption,
                                color = ExitColors.SecondaryText
                            )
                            Spacer(modifier = Modifier.height(ExitSpacing.SM))
                            DayOfMonthGrid(
                                selectedDay = dayOfMonth,
                                onDaySelected = onDayOfMonthChange
                            )
                        }
                    }
                    RepeatType.WEEKLY -> {
                        Column {
                            Text(
                                text = "요일",
                                style = ExitTypography.Caption,
                                color = ExitColors.SecondaryText
                            )
                            Spacer(modifier = Modifier.height(ExitSpacing.SM))
                            WeekdaySelector(
                                selectedDay = dayOfWeek,
                                onDaySelected = onDayOfWeekChange
                            )
                        }
                    }
                    else -> { /* ONCE, DAILY - 날짜 선택 없음 */ }
                }
                
                // 시간 선택
                Column {
                    Text(
                        text = "시간",
                        style = ExitTypography.Caption,
                        color = ExitColors.SecondaryText
                    )
                    Spacer(modifier = Modifier.height(ExitSpacing.SM))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ExitRadius.MD))
                            .background(ExitColors.CardBackground)
                            .padding(ExitSpacing.MD),
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(
                            state = timePickerState,
                            colors = androidx.compose.material3.TimePickerDefaults.colors(
                                clockDialColor = ExitColors.SecondaryCardBackground,
                                selectorColor = ExitColors.Accent,
                                containerColor = ExitColors.CardBackground,
                                periodSelectorSelectedContainerColor = ExitColors.Accent,
                                periodSelectorUnselectedContainerColor = ExitColors.SecondaryCardBackground,
                                periodSelectorSelectedContentColor = Color.White,
                                periodSelectorUnselectedContentColor = ExitColors.SecondaryText,
                                timeSelectorSelectedContainerColor = ExitColors.Accent,
                                timeSelectorUnselectedContainerColor = ExitColors.SecondaryCardBackground,
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContentColor = ExitColors.SecondaryText
                            )
                        )
                    }
                }
                
                // 삭제 버튼 (수정 시에만)
                if (isEditing) {
                    Button(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExitColors.CardBackground,
                            contentColor = ExitColors.Warning.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(ExitRadius.MD),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "알람 삭제",
                            style = ExitTypography.Body,
                            modifier = Modifier.padding(vertical = ExitSpacing.XS)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ExitSpacing.XL))
            }
        }
    }
}

@Composable
private fun DayOfMonthGrid(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.SecondaryCardBackground)
            .padding(ExitSpacing.MD)
    ) {
        val rows = (1..31).chunked(7)
        rows.forEach { week ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                modifier = Modifier.fillMaxWidth()
            ) {
                week.forEach { day ->
                    val isSelected = day == selectedDay
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (isSelected) ExitColors.Accent else Color.Transparent)
                            .clickable { onDaySelected(day) }
                            .padding(vertical = ExitSpacing.SM),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            style = ExitTypography.Caption,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else ExitColors.PrimaryText
                        )
                    }
                }
                // 빈 칸 채우기
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(ExitSpacing.XS))
        }
    }
}

@Composable
private fun WeekdaySelector(
    selectedDay: Weekday,
    onDaySelected: (Weekday) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
        modifier = Modifier.fillMaxWidth()
    ) {
        Weekday.entries.forEach { day ->
            val isSelected = day == selectedDay
            Surface(
                onClick = { onDaySelected(day) },
                shape = RoundedCornerShape(ExitRadius.MD),
                color = if (isSelected) ExitColors.Accent else ExitColors.CardBackground,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = day.shortName,
                    style = ExitTypography.Caption,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else ExitColors.PrimaryText,
                    modifier = Modifier.padding(vertical = ExitSpacing.MD),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
