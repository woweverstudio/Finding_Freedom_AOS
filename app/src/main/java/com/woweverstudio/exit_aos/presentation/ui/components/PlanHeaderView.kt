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
            onToggle = { onExpandedChange(!isExpanded) },
            onDragDown = { onExpandedChange(true) }
        )
        
        // 펼쳐지는 편집 패널
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
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
                onApply = {
                    onApplyChanges(
                        editingCurrentAsset.toDouble(),
                        editingMonthlyIncome.toDouble(),
                        editingMonthlyInvestment.toDouble(),
                        editingPreReturnRate.toDouble(),
                        editingPostReturnRate.toDouble()
                    )
                    onExpandedChange(false)
                }
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
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (!isExpanded && dragAmount > 20) {
                        onDragDown()
                    }
                }
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
        horizontalArrangement = Arrangement.SpaceEvenly
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
        Text(
            text = label,
            style = ExitTypography.Caption2,
            color = ExitColors.TertiaryText
        )
        Text(
            text = value,
            style = ExitTypography.Caption3,
            color = color,
            maxLines = 1
        )
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
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD)
            .padding(bottom = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        // 현재 자산 + 조정 버튼
        AssetSliderWithButtons(
            value = editingCurrentAsset,
            onValueChange = onCurrentAssetChange
        )
        
        // 매월 투자금액 (10만원씩 증감)
        ExitSlider(
            value = editingMonthlyInvestment,
            onValueChange = onMonthlyInvestmentChange,
            valueRange = 0f..10_000_000f,
            label = "매월 투자금액",
            valueFormatter = { ExitNumberFormatter.formatToManWon(it.toDouble()) },
            accentColor = ExitColors.Positive,
            step = 100_000f
        )
        
        // 은퇴 전 수익률 (0.5씩 증감)
        ExitSlider(
            value = editingPreReturnRate,
            onValueChange = onPreReturnRateChange,
            valueRange = 0.5f..50f,
            label = "은퇴 전 수익률",
            valueFormatter = { String.format("%.1f%%", it) },
            accentColor = ExitColors.Accent,
            step = 0.5f
        )
        
        // 은퇴 후 희망 월수입 (10만원씩 증감)
        ExitSlider(
            value = editingMonthlyIncome,
            onValueChange = onMonthlyIncomeChange,
            valueRange = 500_000f..20_000_000f,
            label = "은퇴 후 희망 월수입",
            valueFormatter = { ExitNumberFormatter.formatToManWon(it.toDouble()) },
            accentColor = ExitColors.Accent,
            step = 100_000f
        )
        
        // 은퇴 후 수익률 (0.5씩 증감)
        ExitSlider(
            value = editingPostReturnRate,
            onValueChange = onPostReturnRateChange,
            valueRange = 0.5f..50f,
            label = "은퇴 후 수익률",
            valueFormatter = { String.format("%.1f%%", it) },
            accentColor = ExitColors.Caution,
            step = 0.5f
        )
        
        // 적용 버튼
        ExitSecondaryButton(
            text = "적용",
            onClick = onApply,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AssetSliderWithButtons(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
    ) {
        // 커스텀 슬라이더
        ExitSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10_000_000_000f,
            label = "현재 자산",
            valueFormatter = { ExitNumberFormatter.formatToEokManWon(it.toDouble()) },
            accentColor = ExitColors.PrimaryText
        )
        
        // 조정 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            AssetAdjustButton(
                title = "+10만",
                onClick = { onValueChange(minOf(value + 100_000f, 10_000_000_000f)) },
                modifier = Modifier.weight(1f)
            )
            AssetAdjustButton(
                title = "+100만",
                onClick = { onValueChange(minOf(value + 1_000_000f, 10_000_000_000f)) },
                modifier = Modifier.weight(1f)
            )
            AssetAdjustButton(
                title = "+1000만",
                onClick = { onValueChange(minOf(value + 10_000_000f, 10_000_000_000f)) },
                modifier = Modifier.weight(1f)
            )
            AssetAdjustButton(
                title = "+1억",
                onClick = { onValueChange(minOf(value + 100_000_000f, 10_000_000_000f)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AssetAdjustButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(ExitColors.Accent.copy(alpha = 0.1f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ExitColors.Accent)
            ) { onClick() }
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.SM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = ExitTypography.Caption,
            color = ExitColors.Accent,
            textAlign = TextAlign.Center
        )
    }
}
