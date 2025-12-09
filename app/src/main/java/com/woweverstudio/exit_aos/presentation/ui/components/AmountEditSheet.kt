package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import kotlin.math.abs

/**
 * 금액 편집 타입
 */
enum class AmountEditType(
    val title: String,
    val subtitle: String,
    val showNegativeToggle: Boolean = false
) {
    CURRENT_ASSET(
        title = "현재 자산",
        subtitle = "현재 보유하고 있는 순자산을 입력해주세요",
        showNegativeToggle = true
    ),
    MONTHLY_INVESTMENT(
        title = "매월 투자금액",
        subtitle = "매월 투자할 금액을 입력해주세요"
    ),
    DESIRED_MONTHLY_INCOME(
        title = "은퇴 후 희망 월수입",
        subtitle = "은퇴 후 매월 필요한 금액을 입력해주세요"
    )
}

/**
 * 금액 편집 ModalBottomSheet
 * 전체 화면을 덮는 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountEditSheet(
    type: AmountEditType,
    initialValue: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var editingValue by remember(initialValue) { mutableDoubleStateOf(initialValue) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ExitColors.Background,
        dragHandle = null // 드래그 핸들 숨김
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 컨텐츠 (유동적 - 중앙 정렬)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 제목
                Text(
                    text = type.title,
                    style = ExitTypography.Title,
                    color = ExitColors.PrimaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = ExitSpacing.LG)
                )
                
                Spacer(modifier = Modifier.height(ExitSpacing.SM))
                
                Text(
                    text = type.subtitle,
                    style = ExitTypography.Body,
                    color = ExitColors.SecondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = ExitSpacing.LG)
                )
                
                Spacer(modifier = Modifier.height(ExitSpacing.XL))
                
                // 금액 표시
                AmountDisplay(value = editingValue)
            }
            
            // 키보드 (하단 고정)
            CustomNumberKeyboard(
                onDigitClick = { digit ->
                    val isNegative = editingValue < 0
                    val absValue = abs(editingValue)
                    var currentString = absValue.toLong().toString()
                    
                    currentString = if (currentString == "0") {
                        digit
                    } else {
                        currentString + digit
                    }
                    
                    currentString.toDoubleOrNull()?.let { newValue ->
                        if (newValue <= 100_000_000_000) {
                            editingValue = if (isNegative) -newValue else newValue
                        }
                    }
                },
                onDeleteClick = {
                    val isNegative = editingValue < 0
                    var currentString = abs(editingValue).toLong().toString()
                    
                    if (currentString.length > 1) {
                        currentString = currentString.dropLast(1)
                        val newValue = currentString.toDoubleOrNull() ?: 0.0
                        editingValue = if (isNegative) -newValue else newValue
                    } else {
                        editingValue = 0.0
                    }
                },
                onQuickAmountClick = { amount ->
                    val newValue = editingValue + amount
                    if (newValue <= 100_000_000_000) {
                        editingValue = newValue
                    }
                },
                onResetClick = {
                    editingValue = 0.0
                },
                showNegativeToggle = type.showNegativeToggle,
                isNegative = editingValue < 0,
                onToggleSign = {
                    editingValue = -editingValue
                }
            )
            
            // 하단 버튼 (하단 고정)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ExitSpacing.LG,
                        vertical = ExitSpacing.MD
                    ),
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
            ) {
                // 취소 버튼
                ExitSecondaryButton(
                    text = "취소",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                
                // 확인 버튼
                ExitPrimaryButton(
                    text = "확인",
                    onClick = { onConfirm(editingValue) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AmountDisplay(value: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = ExitNumberFormatter.formatInputDisplay(abs(value)),
            style = ExitTypography.NumberDisplay,
            color = if (value < 0) ExitColors.Warning else ExitColors.PrimaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        Text(
            text = ExitNumberFormatter.formatToEokManWon(value),
            style = ExitTypography.Title3,
            color = ExitColors.Accent
        )
    }
}

