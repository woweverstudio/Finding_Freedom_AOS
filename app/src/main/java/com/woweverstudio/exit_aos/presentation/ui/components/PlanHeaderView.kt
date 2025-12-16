package com.woweverstudio.exit_aos.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import com.woweverstudio.exit_aos.util.rememberHaptic

/**
 * 상단 드롭다운 편집 패널
 * 아래로 드래그하면 편집 패널이 펼쳐짐
 */
@Composable
fun PlanHeaderView(
    userProfile: UserProfile?,
    currentAssetAmount: Double,
    hideAmounts: Boolean,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onApplyChanges: (
        currentAsset: Double,
        desiredMonthlyIncome: Double,
        monthlyInvestment: Double,
        preRetirementReturnRate: Double,
        postRetirementReturnRate: Double
    ) -> Unit,
    onAmountEditRequest: (AmountEditType, Double) -> Unit = { _, _ -> },
    // AmountEditSheet에서 돌아온 결과 (null이면 변경 없음)
    amountEditResult: Pair<AmountEditType, Double>? = null,
    onAmountEditResultConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 편집 중인 임시 값들
    var editingCurrentAsset by remember { mutableFloatStateOf(currentAssetAmount.toFloat()) }
    var editingMonthlyIncome by remember { mutableFloatStateOf(userProfile?.desiredMonthlyIncome?.toFloat() ?: 3_000_000f) }
    var editingMonthlyInvestment by remember { mutableFloatStateOf(userProfile?.monthlyInvestment?.toFloat() ?: 500_000f) }
    var editingPreReturnRate by remember { mutableFloatStateOf(userProfile?.preRetirementReturnRate?.toFloat() ?: 6.5f) }
    var editingPostReturnRate by remember { mutableFloatStateOf(userProfile?.postRetirementReturnRate?.toFloat() ?: 5.0f) }
    
    // 외부 값이 변경되면 동기화
    LaunchedEffect(currentAssetAmount, userProfile) {
        editingCurrentAsset = currentAssetAmount.toFloat()
        userProfile?.let {
            editingMonthlyIncome = it.desiredMonthlyIncome.toFloat()
            editingMonthlyInvestment = it.monthlyInvestment.toFloat()
            editingPreReturnRate = it.preRetirementReturnRate.toFloat()
            editingPostReturnRate = it.postRetirementReturnRate.toFloat()
        }
    }
    
    // AmountEditSheet 결과 반영 (편집 값만 업데이트, 실제 계산은 적용 시)
    LaunchedEffect(amountEditResult) {
        amountEditResult?.let { (type, value) ->
            when (type) {
                AmountEditType.CURRENT_ASSET -> editingCurrentAsset = value.toFloat()
                AmountEditType.MONTHLY_INVESTMENT -> editingMonthlyInvestment = value.toFloat()
                AmountEditType.DESIRED_MONTHLY_INCOME -> editingMonthlyIncome = value.toFloat()
            }
            onAmountEditResultConsumed()
        }
    }
    
    // 햅틱 피드백
    val haptic = rememberHaptic()
    
    // 변경 여부 확인
    val hasChanges = remember(
        editingCurrentAsset,
        editingMonthlyIncome,
        editingMonthlyInvestment,
        editingPreReturnRate,
        editingPostReturnRate,
        currentAssetAmount,
        userProfile
    ) {
        val assetChanged = kotlin.math.abs(editingCurrentAsset - currentAssetAmount.toFloat()) > 1
        val incomeChanged = userProfile?.let { kotlin.math.abs(editingMonthlyIncome - it.desiredMonthlyIncome.toFloat()) > 1 } ?: false
        val investmentChanged = userProfile?.let { kotlin.math.abs(editingMonthlyInvestment - it.monthlyInvestment.toFloat()) > 1 } ?: false
        val preRateChanged = userProfile?.let { kotlin.math.abs(editingPreReturnRate - it.preRetirementReturnRate.toFloat()) > 0.01f } ?: false
        val postRateChanged = userProfile?.let { kotlin.math.abs(editingPostReturnRate - it.postRetirementReturnRate.toFloat()) > 0.01f } ?: false
        
        assetChanged || incomeChanged || investmentChanged || preRateChanged || postRateChanged
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.XS)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(ExitRadius.LG),
                ambientColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .border(
                width = 1.dp,
                color = if (isExpanded) ExitColors.Accent.copy(alpha = 0.4f) else ExitColors.Divider,
                shape = RoundedCornerShape(ExitRadius.LG)
            )
    ) {
        // 메인 헤더 (탭 영역)
        HeaderButton(
            editingCurrentAsset = editingCurrentAsset,
            editingMonthlyIncome = editingMonthlyIncome,
            editingMonthlyInvestment = editingMonthlyInvestment,
            editingPreReturnRate = editingPreReturnRate,
            hideAmounts = hideAmounts,
            isExpanded = isExpanded,
            onToggle = {
                haptic.light()
                onExpandedChange(!isExpanded)
            },
            onDragDown = {
                haptic.light()
                onExpandedChange(true)
            }
        )
        
        // 펼쳐지는 편집 패널
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut()
        ) {
            EditPanel(
                editingCurrentAsset = editingCurrentAsset,
                onCurrentAssetChange = { editingCurrentAsset = it },
                editingMonthlyIncome = editingMonthlyIncome,
                onMonthlyIncomeChange = { editingMonthlyIncome = it },
                editingMonthlyInvestment = editingMonthlyInvestment,
                onMonthlyInvestmentChange = { editingMonthlyInvestment = it },
                editingPreReturnRate = editingPreReturnRate,
                onPreReturnRateChange = { editingPreReturnRate = it },
                editingPostReturnRate = editingPostReturnRate,
                onPostReturnRateChange = { editingPostReturnRate = it },
                onAmountEditClick = { type ->
                    haptic.soft()
                    val value = when (type) {
                        AmountEditType.CURRENT_ASSET -> editingCurrentAsset.toDouble()
                        AmountEditType.MONTHLY_INVESTMENT -> editingMonthlyInvestment.toDouble()
                        AmountEditType.DESIRED_MONTHLY_INCOME -> editingMonthlyIncome.toDouble()
                    }
                    onAmountEditRequest(type, value)
                },
                onApply = {
                    haptic.success()
                    onApplyChanges(
                        editingCurrentAsset.toDouble(),
                        editingMonthlyIncome.toDouble(),
                        editingMonthlyInvestment.toDouble(),
                        editingPreReturnRate.toDouble(),
                        editingPostReturnRate.toDouble()
                    )
                    onExpandedChange(false)
                },
                hasChanges = hasChanges
            )
        }
    }
}

@Composable
private fun HeaderButton(
    editingCurrentAsset: Float,
    editingMonthlyIncome: Float,
    editingMonthlyInvestment: Float,
    editingPreReturnRate: Float,
    hideAmounts: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDragDown: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onToggle() }
            .pointerInput(isExpanded) {
                detectVerticalDragGestures(
                    onDragStart = {
                        if (!isExpanded) {
                            onDragDown()
                        }
                    }
                ) { _, _ -> }
            }
            .padding(horizontal = ExitSpacing.MD)
            .padding(top = if (isExpanded) ExitSpacing.SM else ExitSpacing.MD)
            .padding(bottom = ExitSpacing.XS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        // 핵심 정보 (접힌 상태에서만 표시)
        if (!isExpanded) {
            InfoRow(
                editingCurrentAsset = editingCurrentAsset,
                editingMonthlyIncome = editingMonthlyIncome,
                editingMonthlyInvestment = editingMonthlyInvestment,
                editingPreReturnRate = editingPreReturnRate,
                hideAmounts = hideAmounts
            )
            
            // 풀 인디케이터
            PullIndicator()
        }
    }
}

@Composable
private fun InfoRow(
    editingCurrentAsset: Float,
    editingMonthlyIncome: Float,
    editingMonthlyInvestment: Float,
    editingPreReturnRate: Float,
    hideAmounts: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        // 현재 자산
        InfoItem(
            label = "자산",
            value = if (hideAmounts) "•••" else ExitNumberFormatter.formatToEokMan(editingCurrentAsset.toDouble()),
            color = ExitColors.PrimaryText
        )
        
        // 월 투자
        InfoItem(
            label = "월투자",
            value = ExitNumberFormatter.formatToEokMan(editingMonthlyInvestment.toDouble()),
            color = ExitColors.Positive
        )
        
        // 수익률
        InfoItem(
            label = "수익률",
            value = String.format("%.1f%%", editingPreReturnRate),
            color = ExitColors.Accent
        )
        
        // 목표 월수입
        InfoItem(
            label = "목표",
            value = ExitNumberFormatter.formatToEokMan(editingMonthlyIncome.toDouble()),
            color = ExitColors.Accent
        )
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Box로 감싸서 고정 높이 내에서 중앙 정렬
        Box(
            modifier = Modifier.height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = ExitTypography.Caption2,
                color = ExitColors.TertiaryText
            )
        }
        Box(
            modifier = Modifier.height(18.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = value,
                style = ExitTypography.Caption3,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PullIndicator() {
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(ExitColors.TertiaryText.copy(alpha = 0.5f))
    )
}

@Composable
private fun EditPanel(
    editingCurrentAsset: Float,
    onCurrentAssetChange: (Float) -> Unit,
    editingMonthlyIncome: Float,
    onMonthlyIncomeChange: (Float) -> Unit,
    editingMonthlyInvestment: Float,
    onMonthlyInvestmentChange: (Float) -> Unit,
    editingPreReturnRate: Float,
    onPreReturnRateChange: (Float) -> Unit,
    editingPostReturnRate: Float,
    onPostReturnRateChange: (Float) -> Unit,
    onAmountEditClick: (AmountEditType) -> Unit,
    onApply: () -> Unit,
    hasChanges: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .padding(bottom = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        // 현재 자산 (직접입력)
        AmountEditRow(
            label = "현재 자산",
            value = editingCurrentAsset.toDouble(),
            valueFormatter = { ExitNumberFormatter.formatToEokManWon(it) },
            accentColor = ExitColors.PrimaryText,
            onEditClick = { onAmountEditClick(AmountEditType.CURRENT_ASSET) }
        )
        
        // 매월 투자금액 (직접입력)
        AmountEditRow(
            label = "매월 투자금액",
            value = editingMonthlyInvestment.toDouble(),
            valueFormatter = { ExitNumberFormatter.formatToManWon(it) },
            accentColor = ExitColors.Positive,
            onEditClick = { onAmountEditClick(AmountEditType.MONTHLY_INVESTMENT) }
        )
        
        // 은퇴 후 희망 월수입 (직접입력)
        AmountEditRow(
            label = "은퇴 후 희망 월수입",
            value = editingMonthlyIncome.toDouble(),
            valueFormatter = { ExitNumberFormatter.formatToManWon(it) },
            accentColor = ExitColors.Accent,
            onEditClick = { onAmountEditClick(AmountEditType.DESIRED_MONTHLY_INCOME) }
        )
        
        // 은퇴 전 수익률 (슬라이더 + 버튼)
        RateSliderWithButtons(
            label = "은퇴 전 수익률",
            value = editingPreReturnRate,
            onValueChange = onPreReturnRateChange,
            minValue = 0.5f,
            maxValue = 30f,
            step = 0.5f,
            accentColor = ExitColors.Accent
        )
        
        // 은퇴 후 수익률 (슬라이더 + 버튼)
        RateSliderWithButtons(
            label = "은퇴 후 수익률",
            value = editingPostReturnRate,
            onValueChange = onPostReturnRateChange,
            minValue = 0.5f,
            maxValue = 30f,
            step = 0.5f,
            accentColor = ExitColors.Caution
        )
        
        // 적용 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(ExitColors.CardBackground)
                .border(
                    width = 1.dp,
                    color = if (hasChanges) ExitColors.Accent else ExitColors.Divider,
                    shape = RoundedCornerShape(ExitRadius.MD)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = if (hasChanges) ExitColors.Accent else ExitColors.SecondaryText)
                ) { onApply() }
                .padding(vertical = ExitSpacing.MD),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasChanges) "적용" else "확인",
                style = ExitTypography.Body,
                fontWeight = FontWeight.SemiBold,
                color = if (hasChanges) ExitColors.Accent else ExitColors.SecondaryText
            )
        }
    }
}

@Composable
private fun AmountEditRow(
    label: String,
    value: Double,
    valueFormatter: (Double) -> String,
    accentColor: Color,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ExitSpacing.XS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 라벨
        Text(
            text = label,
            style = ExitTypography.Caption,
            color = ExitColors.SecondaryText
        )
        
        // 값 + 편집 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 현재 값
            Text(
                text = valueFormatter(value),
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            
            // 편집 버튼 (수익률 +/- 버튼과 동일한 스타일)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(ExitRadius.SM))
                    .background(accentColor.copy(alpha = 0.15f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = accentColor)
                    ) { onEditClick() }
                    .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.SM),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "편집",
                    style = ExitTypography.Caption,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun RateSliderWithButtons(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    minValue: Float,
    maxValue: Float,
    step: Float,
    accentColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        // 라벨 + 값 + 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 라벨
            Text(
                text = label,
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
            
            // +/- 버튼과 값
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // - 버튼
                RateButton(
                    text = "−",
                    enabled = value > minValue,
                    accentColor = accentColor,
                    onClick = { onValueChange(maxOf(value - step, minValue)) }
                )
                
                // 현재 값
                Text(
                    text = String.format("%.1f%%", value),
                    style = ExitTypography.Caption,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.width(52.dp),
                    textAlign = TextAlign.Center
                )
                
                // + 버튼
                RateButton(
                    text = "+",
                    enabled = value < maxValue,
                    accentColor = accentColor,
                    onClick = { onValueChange(minOf(value + step, maxValue)) }
                )
            }
        }
        
        // 슬라이더 (라벨 없이)
        ExitSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = minValue..maxValue,
            accentColor = accentColor,
            step = step
        )
    }
}

@Composable
private fun RateButton(
    text: String,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(
                if (enabled) accentColor.copy(alpha = 0.15f)
                else ExitColors.Divider.copy(alpha = 0.5f)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor)
            ) { onClick() }
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.XS),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ExitTypography.Body,
            fontWeight = FontWeight.Bold,
            color = if (enabled) accentColor else ExitColors.TertiaryText
        )
    }
}

