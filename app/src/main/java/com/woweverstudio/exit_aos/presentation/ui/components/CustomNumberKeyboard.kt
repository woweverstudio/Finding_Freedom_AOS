package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
    showNegativeToggle: Boolean = false,
    onToggleSign: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ExitColors.Background)
            .padding(horizontal = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        // 빠른 금액 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            QuickAmountButton("+100만", 1_000_000.0, onQuickAmountClick, Modifier.weight(1f))
            QuickAmountButton("+500만", 5_000_000.0, onQuickAmountClick, Modifier.weight(1f))
            QuickAmountButton("+1000만", 10_000_000.0, onQuickAmountClick, Modifier.weight(1f))
            QuickAmountButton("+1억", 100_000_000.0, onQuickAmountClick, Modifier.weight(1f))
        }
        
        // 숫자 키패드
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            // 1, 2, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                NumberKey("1", onDigitClick, Modifier.weight(1f))
                NumberKey("2", onDigitClick, Modifier.weight(1f))
                NumberKey("3", onDigitClick, Modifier.weight(1f))
            }
            
            // 4, 5, 6
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                NumberKey("4", onDigitClick, Modifier.weight(1f))
                NumberKey("5", onDigitClick, Modifier.weight(1f))
                NumberKey("6", onDigitClick, Modifier.weight(1f))
            }
            
            // 7, 8, 9
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                NumberKey("7", onDigitClick, Modifier.weight(1f))
                NumberKey("8", onDigitClick, Modifier.weight(1f))
                NumberKey("9", onDigitClick, Modifier.weight(1f))
            }
            
            // +/-, 0, 삭제
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                if (showNegativeToggle) {
                    ToggleSignKey(onToggleSign, Modifier.weight(1f))
                } else {
                    Box(Modifier.weight(1f))
                }
                NumberKey("0", onDigitClick, Modifier.weight(1f))
                DeleteKey(onDeleteClick, Modifier.weight(1f))
            }
        }
    }
}

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
            .background(ExitColors.CardBackground)
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

@Composable
private fun DeleteKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.CardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "삭제",
            tint = ExitColors.SecondaryText,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ToggleSignKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(ExitColors.CardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+/-",
            style = ExitTypography.Body,
            color = ExitColors.SecondaryText
        )
    }
}

@Composable
private fun QuickAmountButton(
    text: String,
    amount: Double,
    onClick: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(ExitColors.SecondaryCardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick(amount) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ExitTypography.Caption,
            color = ExitColors.Accent,
            textAlign = TextAlign.Center
        )
    }
}

