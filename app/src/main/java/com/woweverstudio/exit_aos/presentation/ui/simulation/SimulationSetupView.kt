package com.woweverstudio.exit_aos.presentation.ui.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculator
import com.woweverstudio.exit_aos.presentation.ui.components.AmountEditSheet
import com.woweverstudio.exit_aos.presentation.ui.components.AmountEditType
import com.woweverstudio.exit_aos.presentation.ui.components.ExitSlider
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.presentation.viewmodel.SimulationViewModel
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import kotlin.math.abs

/**
 * 시뮬레이션 설정 화면
 * iOS의 SimulationSetupView.swift와 동일
 */
@Composable
fun SimulationSetupView(
    viewModel: SimulationViewModel,
    userProfile: UserProfile?,
    currentAssetAmount: Double,
    onBack: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Editing States
    var editingCurrentAsset by remember { mutableDoubleStateOf(currentAssetAmount) }
    var editingMonthlyInvestment by remember { mutableDoubleStateOf(userProfile?.monthlyInvestment ?: 500_000.0) }
    var editingMonthlyIncome by remember { mutableDoubleStateOf(userProfile?.desiredMonthlyIncome ?: 3_000_000.0) }
    var editingPreReturnRate by remember { mutableFloatStateOf(userProfile?.preRetirementReturnRate?.toFloat() ?: 6.5f) }
    var editingPostReturnRate by remember { mutableFloatStateOf(userProfile?.postRetirementReturnRate?.toFloat() ?: 4.0f) }
    var spendingRatio by remember { mutableDoubleStateOf(1.0) }
    var failureThreshold by remember { mutableDoubleStateOf(1.1) }
    
    // Amount Edit Sheet State
    var showAmountEditSheet by remember { mutableStateOf<AmountEditType?>(null) }
    
    // Sync with actual values when profile changes
    LaunchedEffect(userProfile, currentAssetAmount) {
        editingCurrentAsset = currentAssetAmount
        userProfile?.let {
            editingMonthlyInvestment = it.monthlyInvestment
            editingMonthlyIncome = it.desiredMonthlyIncome
            editingPreReturnRate = it.preRetirementReturnRate.toFloat()
            editingPostReturnRate = it.postRetirementReturnRate.toFloat()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ExitColors.Background)
    ) {
        // 헤더
        SetupHeader(onBack = onBack)
        
        // 스크롤 컨텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = ExitSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            // 기본 설정 섹션
            BasicSettingsSection(
                currentAsset = editingCurrentAsset,
                monthlyInvestment = editingMonthlyInvestment,
                monthlyIncome = editingMonthlyIncome,
                onAmountEditRequest = { showAmountEditSheet = it }
            )
            
            // 수익률 설정 섹션
            ReturnRateSection(
                preReturnRate = editingPreReturnRate,
                onPreReturnRateChange = { editingPreReturnRate = it },
                postReturnRate = editingPostReturnRate,
                onPostReturnRateChange = { editingPostReturnRate = it }
            )
            
            // 생활비 사용 비율 섹션
            SpendingRatioSection(
                spendingRatio = spendingRatio,
                onSpendingRatioChange = { spendingRatio = it },
                monthlyIncome = editingMonthlyIncome
            )
            
            // 실패 조건 섹션
            FailureThresholdSection(
                failureThreshold = failureThreshold,
                onFailureThresholdChange = { failureThreshold = it },
                currentAsset = editingCurrentAsset,
                monthlyInvestment = editingMonthlyInvestment,
                monthlyIncome = editingMonthlyIncome,
                preReturnRate = editingPreReturnRate.toDouble(),
                postReturnRate = editingPostReturnRate.toDouble()
            )
            
            // 시뮬레이션 요약
            SimulationSummarySection(
                monthlyIncome = editingMonthlyIncome,
                postReturnRate = editingPostReturnRate.toDouble(),
                preReturnRate = editingPreReturnRate.toDouble()
            )
        }
        
        // 시작 버튼
        StartButton(
            onClick = {
                // 1. 설정값 DB에 저장 (DashboardScreen과 동기화 - 비동기)
                viewModel.updateSettings(
                    currentAsset = editingCurrentAsset,
                    desiredMonthlyIncome = editingMonthlyIncome,
                    monthlyInvestment = editingMonthlyInvestment,
                    preRetirementReturnRate = editingPreReturnRate.toDouble(),
                    postRetirementReturnRate = editingPostReturnRate.toDouble()
                )
                
                // 2. 시뮬레이션 파라미터 설정
                viewModel.updateFailureThreshold(failureThreshold)
                viewModel.updateSpendingRatio(spendingRatio)
                val autoVolatility = SimulationViewModel.calculateVolatility(editingPreReturnRate.toDouble())
                viewModel.updateVolatility(autoVolatility)
                
                // 3. 시뮬레이션 시작 (편집한 값을 직접 전달하여 DB 업데이트를 기다리지 않음)
                viewModel.runAllSimulations(
                    overrideCurrentAsset = editingCurrentAsset,
                    overrideMonthlyInvestment = editingMonthlyInvestment,
                    overrideDesiredMonthlyIncome = editingMonthlyIncome,
                    overridePreReturnRate = editingPreReturnRate.toDouble(),
                    overridePostReturnRate = editingPostReturnRate.toDouble(),
                    overrideSpendingRatio = spendingRatio
                )
                onStart()
            }
        )
    }
    
    // Amount Edit Sheet
    showAmountEditSheet?.let { type ->
        AmountEditSheet(
            type = type,
            initialValue = when (type) {
                AmountEditType.CURRENT_ASSET -> editingCurrentAsset
                AmountEditType.MONTHLY_INVESTMENT -> editingMonthlyInvestment
                AmountEditType.DESIRED_MONTHLY_INCOME -> editingMonthlyIncome
            },
            onConfirm = { newValue ->
                when (type) {
                    AmountEditType.CURRENT_ASSET -> editingCurrentAsset = newValue
                    AmountEditType.MONTHLY_INVESTMENT -> editingMonthlyInvestment = newValue
                    AmountEditType.DESIRED_MONTHLY_INCOME -> editingMonthlyIncome = newValue
                }
                showAmountEditSheet = null
            },
            onDismiss = { showAmountEditSheet = null }
        )
    }
}

@Composable
private fun SetupHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.SM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "뒤로",
                tint = ExitColors.SecondaryText
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "시뮬레이션 설정",
            style = ExitTypography.Body,
            fontWeight = FontWeight.SemiBold,
            color = ExitColors.PrimaryText
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 균형용 빈 공간
        Spacer(modifier = Modifier.size(48.dp))
    }
}

// MARK: - Basic Settings Section

@Composable
private fun BasicSettingsSection(
    currentAsset: Double,
    monthlyInvestment: Double,
    monthlyIncome: Double,
    onAmountEditRequest: (AmountEditType) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        SectionHeader("기본 설정")
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            // 현재 자산
            AmountEditRow(
                label = "현재 자산",
                value = currentAsset,
                valueFormatter = { ExitNumberFormatter.formatToEokManWon(it) },
                accentColor = ExitColors.PrimaryText,
                onEditClick = { onAmountEditRequest(AmountEditType.CURRENT_ASSET) }
            )
            
            // 매월 투자금액
            AmountEditRow(
                label = "매월 투자금액",
                value = monthlyInvestment,
                valueFormatter = { ExitNumberFormatter.formatToManWon(it) },
                accentColor = ExitColors.Positive,
                onEditClick = { onAmountEditRequest(AmountEditType.MONTHLY_INVESTMENT) }
            )
            
            // 은퇴 후 희망 월수입
            AmountEditRow(
                label = "은퇴 후 월수입",
                value = monthlyIncome,
                valueFormatter = { ExitNumberFormatter.formatToManWon(it) },
                accentColor = ExitColors.Accent,
                onEditClick = { onAmountEditRequest(AmountEditType.DESIRED_MONTHLY_INCOME) }
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
                    .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.XS),
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

// MARK: - Return Rate Section

@Composable
private fun ReturnRateSection(
    preReturnRate: Float,
    onPreReturnRateChange: (Float) -> Unit,
    postReturnRate: Float,
    onPostReturnRateChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        SectionHeader("수익률 설정")
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            // 은퇴 전 수익률
            RateSliderWithButtons(
                label = "은퇴 전 수익률",
                value = preReturnRate,
                onValueChange = onPreReturnRateChange,
                minValue = 0.5f,
                maxValue = 50f,
                step = 0.5f,
                accentColor = ExitColors.Accent
            )
            
            // 은퇴 후 수익률
            RateSliderWithButtons(
                label = "은퇴 후 수익률",
                value = postReturnRate,
                onValueChange = onPostReturnRateChange,
                minValue = 0.5f,
                maxValue = 50f,
                step = 0.5f,
                accentColor = ExitColors.Caution
            )
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
        
        // 슬라이더
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
    val buttonColor = if (enabled) accentColor else ExitColors.TertiaryText
    val backgroundColor = if (enabled) accentColor.copy(alpha = 0.15f) else ExitColors.Divider.copy(alpha = 0.5f)
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ExitRadius.SM))
            .background(backgroundColor)
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
            color = buttonColor
        )
    }
}

// MARK: - Spending Ratio Section

@Composable
private fun SpendingRatioSection(
    spendingRatio: Double,
    onSpendingRatioChange: (Double) -> Unit,
    monthlyIncome: Double
) {
    val actualSpending = monthlyIncome * spendingRatio
    
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            SectionHeader("생활비 사용 비율")
            
            Text(
                text = "은퇴 후 희망 월수입 중 실제로 사용할 비율을 설정합니다",
                style = ExitTypography.Caption2,
                color = ExitColors.TertiaryText,
                modifier = Modifier.padding(horizontal = ExitSpacing.MD)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            // 생활비 비율 선택 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                SpendingRatioButton(
                    value = 0.5,
                    label = "50%",
                    isSelected = abs(spendingRatio - 0.5) < 0.01,
                    onClick = { onSpendingRatioChange(0.5) },
                    modifier = Modifier.weight(1f)
                )
                SpendingRatioButton(
                    value = 0.7,
                    label = "70%",
                    isSelected = abs(spendingRatio - 0.7) < 0.01,
                    onClick = { onSpendingRatioChange(0.7) },
                    modifier = Modifier.weight(1f)
                )
                SpendingRatioButton(
                    value = 0.85,
                    label = "85%",
                    isSelected = abs(spendingRatio - 0.85) < 0.01,
                    onClick = { onSpendingRatioChange(0.85) },
                    modifier = Modifier.weight(1f)
                )
                SpendingRatioButton(
                    value = 1.0,
                    label = "100%",
                    isSelected = abs(spendingRatio - 1.0) < 0.01,
                    onClick = { onSpendingRatioChange(1.0) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 현재 설정 예시
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    tint = ExitColors.SecondaryText
                )
                Text(
                    text = "월 ${ExitNumberFormatter.formatToManWon(monthlyIncome)} × ${String.format("%.0f", spendingRatio * 100)}% = ${ExitNumberFormatter.formatToManWon(actualSpending)} 사용",
                    style = ExitTypography.Caption2,
                    color = ExitColors.SecondaryText
                )
            }
        }
    }
}

@Composable
private fun SpendingRatioButton(
    value: Double,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ExitRadius.SM))
            .then(
                if (isSelected) {
                    Modifier.background(ExitColors.Positive)
                } else {
                    Modifier
                        .background(ExitColors.Background)
                        .border(1.dp, ExitColors.Divider, RoundedCornerShape(ExitRadius.SM))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (isSelected) Color.White else ExitColors.Positive)
            ) { onClick() }
            .padding(vertical = ExitSpacing.SM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ExitTypography.Caption,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else ExitColors.SecondaryText
        )
    }
}

// MARK: - Failure Threshold Section

@Composable
private fun FailureThresholdSection(
    failureThreshold: Double,
    onFailureThresholdChange: (Double) -> Unit,
    currentAsset: Double,
    monthlyInvestment: Double,
    monthlyIncome: Double,
    preReturnRate: Double,
    postReturnRate: Double
) {
    val targetAsset = RetirementCalculator.calculateTargetAssets(
        desiredMonthlyIncome = monthlyIncome,
        postRetirementReturnRate = postReturnRate
    )
    
    val originalMonths = RetirementCalculator.calculateMonthsToRetirement(
        currentAssets = currentAsset,
        targetAssets = targetAsset,
        monthlyInvestment = monthlyInvestment,
        annualReturnRate = preReturnRate
    )
    
    val failureMonths = (originalMonths * failureThreshold).toInt()
    
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            SectionHeader("실패 조건")
            
            Text(
                text = "목표 기간의 몇 %를 초과하면 실패로 판정할지 설정합니다",
                style = ExitTypography.Caption2,
                color = ExitColors.TertiaryText,
                modifier = Modifier.padding(horizontal = ExitSpacing.MD)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            // 실패 조건 선택 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
            ) {
                FailureOptionButton(
                    value = 1.0,
                    label = "100%",
                    isSelected = abs(failureThreshold - 1.0) < 0.01,
                    onClick = { onFailureThresholdChange(1.0) },
                    modifier = Modifier.weight(1f)
                )
                FailureOptionButton(
                    value = 1.1,
                    label = "110%",
                    isSelected = abs(failureThreshold - 1.1) < 0.01,
                    onClick = { onFailureThresholdChange(1.1) },
                    modifier = Modifier.weight(1f)
                )
                FailureOptionButton(
                    value = 1.3,
                    label = "130%",
                    isSelected = abs(failureThreshold - 1.3) < 0.01,
                    onClick = { onFailureThresholdChange(1.3) },
                    modifier = Modifier.weight(1f)
                )
                FailureOptionButton(
                    value = 1.5,
                    label = "150%",
                    isSelected = abs(failureThreshold - 1.5) < 0.01,
                    onClick = { onFailureThresholdChange(1.5) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 현재 설정 예시
            if (originalMonths > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = ExitColors.SecondaryText
                    )
                    Text(
                        text = "목표 ${formatPeriod(originalMonths)} → ${formatPeriod(failureMonths)} 초과 시 실패",
                        style = ExitTypography.Caption2,
                        color = ExitColors.SecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun FailureOptionButton(
    value: Double,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ExitRadius.SM))
            .then(
                if (isSelected) {
                    Modifier.background(ExitColors.Accent)
                } else {
                    Modifier
                        .background(ExitColors.Background)
                        .border(1.dp, ExitColors.Divider, RoundedCornerShape(ExitRadius.SM))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (isSelected) Color.White else ExitColors.Accent)
            ) { onClick() }
            .padding(vertical = ExitSpacing.SM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ExitTypography.Caption,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else ExitColors.SecondaryText
        )
    }
}

// MARK: - Simulation Summary Section

@Composable
private fun SimulationSummarySection(
    monthlyIncome: Double,
    postReturnRate: Double,
    preReturnRate: Double
) {
    val targetAsset = RetirementCalculator.calculateTargetAssets(
        desiredMonthlyIncome = monthlyIncome,
        postRetirementReturnRate = postReturnRate
    )
    val preVolatility = SimulationViewModel.calculateVolatility(preReturnRate)
    val postVolatility = SimulationViewModel.calculateVolatility(postReturnRate)
    
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        SectionHeader("시뮬레이션 정보")
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.MD),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            SummaryRow("목표 자산", ExitNumberFormatter.formatToEokManWon(targetAsset), ExitColors.Accent)
            SummaryRow("은퇴 전 변동성", String.format("%.0f%%", preVolatility))
            SummaryRow("은퇴 후 변동성", String.format("%.0f%%", postVolatility))
            
            HorizontalDivider(color = ExitColors.Divider)
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    tint = ExitColors.TertiaryText
                )
                Text(
                    text = "변동성은 목표 수익률 기반으로 자동 계산됩니다",
                    style = ExitTypography.Caption2,
                    color = ExitColors.TertiaryText
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = ExitColors.PrimaryText
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = ExitTypography.Caption,
            color = ExitColors.TertiaryText
        )
        Text(
            text = value,
            style = ExitTypography.Caption,
            color = valueColor
        )
    }
}

// MARK: - Start Button

@Composable
private fun StartButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD, vertical = ExitSpacing.LG)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(ExitRadius.LG)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF00D4AA), Color(0xFF00B894))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Text(
                    text = "시뮬레이션 시작",
                    style = ExitTypography.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// MARK: - Helper Components

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = ExitTypography.Caption,
        color = ExitColors.SecondaryText,
        modifier = Modifier.padding(horizontal = ExitSpacing.MD)
    )
}

private fun formatPeriod(months: Int): String {
    val years = months / 12
    val remainingMonths = months % 12
    return when {
        remainingMonths == 0 -> "${years}년"
        years == 0 -> "${remainingMonths}개월"
        else -> "${years}년 ${remainingMonths}개월"
    }
}
