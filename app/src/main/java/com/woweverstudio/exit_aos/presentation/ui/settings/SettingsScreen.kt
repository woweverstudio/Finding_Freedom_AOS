package com.woweverstudio.exit_aos.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography

/**
 * 설정 화면
 */
@Composable
fun SettingsScreen(
    onDeleteAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ExitColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XL)
    ) {
        // 공지사항 섹션
        SettingsSection(title = "공지사항") {
            SettingsItem(
                title = "환영합니다! 🎉",
                subtitle = "방금 전",
                onClick = { }
            )
        }
        
        // 문의하기 섹션
        SettingsSection(title = "문의하기") {
            SettingsItem(
                title = "이메일",
                subtitle = "woweverstudio@gmail.com",
                onClick = { }
            )
            SettingsItem(
                title = "인스타그램",
                subtitle = "@woweverstudio",
                onClick = { }
            )
        }
        
        // 앱 정보 섹션
        SettingsSection(title = "앱 정보") {
            SettingsItem(
                title = "버전",
                subtitle = "1.0.0",
                showArrow = false,
                onClick = { }
            )
        }
        
        // 데이터 관리 섹션
        SettingsItem(
            title = "모든 데이터 삭제",
            titleColor = ExitColors.Warning.copy(alpha = 0.8f),
            onClick = { showDeleteConfirm = true }
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.XL))
    }
    
    // 삭제 확인 다이얼로그
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
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
                        showDeleteConfirm = false
                        onDeleteAllData()
                    }
                ) {
                    Text(
                        text = "삭제",
                        color = ExitColors.Warning
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text(
                        text = "취소",
                        color = ExitColors.SecondaryText
                    )
                }
            },
            containerColor = ExitColors.CardBackground
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = ExitTypography.Caption,
            color = ExitColors.SecondaryText,
            modifier = Modifier.padding(start = ExitSpacing.XS, bottom = ExitSpacing.SM)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.CardBackground)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = ExitColors.PrimaryText,
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

