package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
        // 빠른 금액 버튼 (가로 스크롤)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = ExitSpacing.XS),
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            // 음수/양수 토글
            if (showNegativeToggle) {
                QuickButton(
                    text = if (isNegative) "양수" else "음수",
                    isDestructive = false,
                    onClick = onToggleSign
                )
            }
            
            // 빠른 금액 버튼들
            QuickButton(text = "+10만", onClick = { onQuickAmountClick(100_000.0) })
            QuickButton(text = "+100만", onClick = { onQuickAmountClick(1_000_000.0) })
            QuickButton(text = "+1000만", onClick = { onQuickAmountClick(10_000_000.0) })
            QuickButton(text = "+1억", onClick = { onQuickAmountClick(100_000_000.0) })
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
            
            // Row 4: 초기화, 0, ←
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                ResetKey(onResetClick, Modifier.weight(1f))
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
        Text(
            text = "←",
            style = ExitTypography.Keypad,
            color = ExitColors.Accent
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
 * 빠른 금액/기능 버튼
 */
@Composable
private fun QuickButton(
    text: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val buttonColor = if (isDestructive) ExitColors.Warning else ExitColors.Accent
    
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(buttonColor.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = buttonColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(ExitRadius.SM)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = buttonColor)
            ) { onClick() }
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.SM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ExitTypography.Caption,
            fontWeight = FontWeight.Medium,
            color = buttonColor,
            textAlign = TextAlign.Center
        )
    }
}
