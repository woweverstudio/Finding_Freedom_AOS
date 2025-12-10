package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography

/**
 * 커스텀 숫자 키보드
 */
@Composable
fun CustomNumberKeyboard(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onQuickAmountClick: (Double) -> Unit,
    onResetClick: () -> Unit,
    showNegativeToggle: Boolean = false,
    isNegative: Boolean = false,
    onToggleSign: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ExitColors.Background)
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        // 빠른 금액 버튼 (가로 꽉 채움)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            QuickButton(text = "+1만", onClick = { onQuickAmountClick(10_000.0) }, modifier = Modifier.weight(1f))
            QuickButton(text = "+10만", onClick = { onQuickAmountClick(100_000.0) }, modifier = Modifier.weight(1f))
            QuickButton(text = "+100만", onClick = { onQuickAmountClick(1_000_000.0) }, modifier = Modifier.weight(1f))
            QuickButton(text = "+1000만", onClick = { onQuickAmountClick(10_000_000.0) }, modifier = Modifier.weight(1f))
            QuickButton(text = "+1억", onClick = { onQuickAmountClick(100_000_000.0) }, modifier = Modifier.weight(1f))
        }
        
        // 숫자 키패드
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            // Row 1: 1, 2, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                NumberKey("1", onDigitClick, Modifier.weight(1f))
                NumberKey("2", onDigitClick, Modifier.weight(1f))
                NumberKey("3", onDigitClick, Modifier.weight(1f))
            }
            
            // Row 2: 4, 5, 6
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                NumberKey("4", onDigitClick, Modifier.weight(1f))
                NumberKey("5", onDigitClick, Modifier.weight(1f))
                NumberKey("6", onDigitClick, Modifier.weight(1f))
            }
            
            // Row 3: 7, 8, 9
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                NumberKey("7", onDigitClick, Modifier.weight(1f))
                NumberKey("8", onDigitClick, Modifier.weight(1f))
                NumberKey("9", onDigitClick, Modifier.weight(1f))
            }
            
            // Row 4: C, -, 0, ←
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                // C와 -는 한 칸을 나눠서 사용
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
                ) {
                    ResetKey(onResetClick, Modifier.weight(1f))
                    if (showNegativeToggle) {
                        NegativeKey(onToggleSign, Modifier.weight(1f))
                    }
                }
                NumberKey("0", onDigitClick, Modifier.weight(1f))
                DeleteKey(onDeleteClick, Modifier.weight(1f))
            }
        }
    }
}

/**
 * 숫자 키
 */
@Composable
private fun NumberKey(
    digit: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.SecondaryCardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick(digit) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = ExitTypography.Keypad,
            color = ExitColors.PrimaryText
        )
    }
}

/**
 * 삭제 키
 */
@Composable
private fun DeleteKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.SecondaryCardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "삭제",
            modifier = Modifier.size(28.dp),
            tint = ExitColors.Accent
        )
    }
}

/**
 * 초기화 키
 */
@Composable
private fun ResetKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.Warning.copy(alpha = 0.15f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Warning)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "C",
            style = ExitTypography.Keypad,
            color = ExitColors.Warning
        )
    }
}

/**
 * 음수 토글 키
 */
@Composable
private fun NegativeKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.SecondaryCardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "-",
            style = ExitTypography.Keypad,
            color = ExitColors.Accent
        )
    }
}

/**
 * 빠른 금액/기능 버튼
 */
@Composable
private fun QuickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(ExitColors.Accent.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = ExitColors.Accent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(ExitRadius.SM)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = ExitColors.Accent,
            textAlign = TextAlign.Center
        )
    }
}
