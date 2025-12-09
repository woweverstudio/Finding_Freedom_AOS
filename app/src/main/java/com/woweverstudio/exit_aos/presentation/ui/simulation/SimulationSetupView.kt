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
    var editingCurrentAsset by remember { mutableFloatStateOf(currentAssetAmount.toFloat()) }
    var editingMonthlyInvestment by remember { mutableFloatStateOf(userProfile?.monthlyInvestment?.toFloat() ?: 500_000f) }
    var editingMonthlyIncome by remember { mutableFloatStateOf(userProfile?.desiredMonthlyIncome?.toFloat() ?: 3_000_000f) }
    var editingPreReturnRate by remember { mutableFloatStateOf(userProfile?.preRetirementReturnRate?.toFloat() ?: 6.5f) }
    var editingPostReturnRate by remember { mutableFloatStateOf(userProfile?.postRetirementReturnRate?.toFloat() ?: 5.0f) }
    var editingInflationRate by remember { mutableFloatStateOf(userProfile?.inflationRate?.toFloat() ?: 2.5f) }
    var failureThreshold by remember { mutableDoubleStateOf(1.1) }
    
    // Sync with actual values when profile changes
    LaunchedEffect(userProfile, currentAssetAmount) {
        editingCurrentAsset = currentAssetAmount.toFloat()
        userProfile?.let {
            editingMonthlyInvestment = it.monthlyInvestment.toFloat()
            editingMonthlyIncome = it.desiredMonthlyIncome.toFloat()
            editingPreReturnRate = it.preRetirementReturnRate.toFloat()
            editingPostReturnRate = it.postRetirementReturnRate.toFloat()
            editingInflationRate = it.inflationRate.toFloat()
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
                onCurrentAssetChange = { editingCurrentAsset = it },
                monthlyInvestment = editingMonthlyInvestment,
                onMonthlyInvestmentChange = { editingMonthlyInvestment = it },
                monthlyIncome = editingMonthlyIncome,
                onMonthlyIncomeChange = { editingMonthlyIncome = it }
            )
            
            // 수익률 설정 섹션
            ReturnRateSection(
                preReturnRate = editingPreReturnRate,
                onPreReturnRateChange = { editingPreReturnRate = it },
                postReturnRate = editingPostReturnRate,
                onPostReturnRateChange = { editingPostReturnRate = it },
                inflationRate = editingInflationRate,
                onInflationRateChange = { editingInflationRate = it }
            )
            
            // 실패 조건 섹션
            FailureThresholdSection(
                failureThreshold = failureThreshold,
                onFailureThresholdChange = { failureThreshold = it },
                currentAsset = editingCurrentAsset.toDouble(),
                monthlyInvestment = editingMonthlyInvestment.toDouble(),
                monthlyIncome = editingMonthlyIncome.toDouble(),
                preReturnRate = editingPreReturnRate.toDouble(),
                postReturnRate = editingPostReturnRate.toDouble(),
                inflationRate = editingInflationRate.toDouble()
            )
            
            // 시뮬레이션 요약
            SimulationSummarySection(
                monthlyIncome = editingMonthlyIncome.toDouble(),
                postReturnRate = editingPostReturnRate.toDouble(),
                inflationRate = editingInflationRate.toDouble(),
                preReturnRate = editingPreReturnRate.toDouble()
            )
        }
        
        // 시작 버튼
        StartButton(
            onClick = {
                // 1. 설정값 DB에 저장 (DashboardScreen과 동기화 - 비동기)
                viewModel.updateSettings(
                    currentAsset = editingCurrentAsset.toDouble(),
                    desiredMonthlyIncome = editingMonthlyIncome.toDouble(),
                    monthlyInvestment = editingMonthlyInvestment.toDouble(),
                    preRetirementReturnRate = editingPreReturnRate.toDouble(),
                    postRetirementReturnRate = editingPostReturnRate.toDouble(),
                    inflationRate = editingInflationRate.toDouble()
                )
                
                // 2. 시뮬레이션 파라미터 설정
                viewModel.updateFailureThreshold(failureThreshold)
                val autoVolatility = SimulationViewModel.calculateVolatility(editingPreReturnRate.toDouble())
                viewModel.updateVolatility(autoVolatility)
                
                // 3. 시뮬레이션 시작 (편집한 값을 직접 전달하여 DB 업데이트를 기다리지 않음)
                viewModel.runAllSimulations(
                    overrideCurrentAsset = editingCurrentAsset.toDouble(),
                    overrideMonthlyInvestment = editingMonthlyInvestment.toDouble(),
                    overrideDesiredMonthlyIncome = editingMonthlyIncome.toDouble(),
                    overridePreReturnRate = editingPreReturnRate.toDouble(),
                    overridePostReturnRate = editingPostReturnRate.toDouble(),
                    overrideInflationRate = editingInflationRate.toDouble()
                )
                onStart()
            }
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
    currentAsset: Float,
    onCurrentAssetChange: (Float) -> Unit,
    monthlyInvestment: Float,
    onMonthlyInvestmentChange: (Float) -> Unit,
    monthlyIncome: Float,
    onMonthlyIncomeChange: (Float) -> Unit
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
            // 현재 자산 + 조정 버튼
            AssetSliderWithButtons(
                value = currentAsset,
                onValueChange = onCurrentAssetChange
            )
            
            // 매월 투자금액
            ExitSlider(
                value = monthlyInvestment,
                onValueChange = onMonthlyInvestmentChange,
                valueRange = 0f..10_000_000f,
                label = "매월 투자금액",
                valueFormatter = { ExitNumberFormatter.formatToManWon(it.toDouble()) },
                accentColor = ExitColors.Positive,
                step = 100_000f
            )
            
            // 은퇴 후 희망 월수입
            ExitSlider(
                value = monthlyIncome,
                onValueChange = onMonthlyIncomeChange,
                valueRange = 500_000f..20_000_000f,
                label = "은퇴 후 월수입",
                valueFormatter = { ExitNumberFormatter.formatToManWon(it.toDouble()) },
                accentColor = ExitColors.Accent,
                step = 100_000f
            )
        }
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
        
        // 조정 버튼들 (텍스트 크기에 맞게 fit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            AssetAdjustButton(
                title = "+10만",
                onClick = { onValueChange(minOf(value + 100_000f, 10_000_000_000f)) }
            )
            AssetAdjustButton(
                title = "+100만",
                onClick = { onValueChange(minOf(value + 1_000_000f, 10_000_000_000f)) }
            )
            AssetAdjustButton(
                title = "+1000만",
                onClick = { onValueChange(minOf(value + 10_000_000f, 10_000_000_000f)) }
            )
            AssetAdjustButton(
                title = "+1억",
                onClick = { onValueChange(minOf(value + 100_000_000f, 10_000_000_000f)) }
            )
        }
    }
}

@Composable
private fun AssetAdjustButton(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
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
            maxLines = 1
        )
    }
}

// MARK: - Return Rate Section

@Composable
private fun ReturnRateSection(
    preReturnRate: Float,
    onPreReturnRateChange: (Float) -> Unit,
    postReturnRate: Float,
    onPostReturnRateChange: (Float) -> Unit,
    inflationRate: Float,
    onInflationRateChange: (Float) -> Unit
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
            ExitSlider(
                value = preReturnRate,
                onValueChange = onPreReturnRateChange,
                valueRange = 0.5f..50f,
                label = "은퇴 전 수익률",
                valueFormatter = { String.format("%.1f%%", it) },
                accentColor = ExitColors.Accent,
                step = 0.5f
            )
            
            // 은퇴 후 수익률
            ExitSlider(
                value = postReturnRate,
                onValueChange = onPostReturnRateChange,
                valueRange = 0.5f..50f,
                label = "은퇴 후 수익률",
                valueFormatter = { String.format("%.1f%%", it) },
                accentColor = ExitColors.Caution,
                step = 0.5f
            )
            
            HorizontalDivider(color = ExitColors.Divider)
            
            // 물가 상승률
            ExitSlider(
                value = inflationRate,
                onValueChange = onInflationRateChange,
                valueRange = 0f..10f,
                label = "물가 상승률",
                valueFormatter = { String.format("%.1f%%", it) },
                accentColor = ExitColors.Warning,
                step = 0.1f
            )
        }
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
    postReturnRate: Double,
    inflationRate: Double
) {
    val targetAsset = RetirementCalculator.calculateTargetAssets(
        desiredMonthlyIncome = monthlyIncome,
        postRetirementReturnRate = postReturnRate,
        inflationRate = inflationRate
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
                        tint = ExitColors.TertiaryText
                    )
                    Text(
                        text = "목표 ${formatPeriod(originalMonths)} → ${formatPeriod(failureMonths)} 초과 시 실패",
                        style = ExitTypography.Caption2,
                        color = ExitColors.TertiaryText
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
    inflationRate: Double,
    preReturnRate: Double
) {
    val targetAsset = RetirementCalculator.calculateTargetAssets(
        desiredMonthlyIncome = monthlyIncome,
        postRetirementReturnRate = postReturnRate,
        inflationRate = inflationRate
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
