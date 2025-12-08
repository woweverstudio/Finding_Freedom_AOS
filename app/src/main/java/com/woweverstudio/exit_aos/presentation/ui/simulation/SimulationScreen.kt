package com.woweverstudio.exit_aos.presentation.ui.simulation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.woweverstudio.exit_aos.presentation.ui.components.ExitPrimaryButton
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.presentation.viewmodel.SimulationViewModel
import com.woweverstudio.exit_aos.util.ExitNumberFormatter

/**
 * 시뮬레이션 화면
 */
@Composable
fun SimulationScreen(
    viewModel: SimulationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val monteCarloResult by viewModel.monteCarloResult.collectAsState()
    val retirementResult by viewModel.retirementResult.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val simulationProgress by viewModel.simulationProgress.collectAsState()
    val simulationPhase by viewModel.simulationPhase.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ExitColors.Background)
    ) {
        if (userProfile == null) {
            // 데이터 로딩 중
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ExitColors.Accent)
            }
        } else if (monteCarloResult == null && !isSimulating) {
            // 시뮬레이션 시작 전
            SimulationEmptyView(
                onStartSimulation = { viewModel.runAllSimulations() }
            )
        } else if (isSimulating) {
            // 시뮬레이션 진행 중
            SimulationProgressView(
                progress = simulationProgress,
                phaseDescription = simulationPhase.description
            )
        } else {
            // 결과 표시
            SimulationResultView(
                viewModel = viewModel,
                monteCarloResult = monteCarloResult!!,
                retirementResult = retirementResult
            )
        }
    }
}

@Composable
private fun SimulationEmptyView(
    onStartSimulation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ExitSpacing.LG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎲",
            style = ExitTypography.LargeTitle
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        Text(
            text = "몬테카를로 시뮬레이션",
            style = ExitTypography.Title2,
            color = ExitColors.PrimaryText,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        Text(
            text = "30,000가지 미래 시나리오를 분석하여\n은퇴 성공 확률을 계산합니다",
            style = ExitTypography.Body,
            color = ExitColors.SecondaryText,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.XL))
        
        ExitPrimaryButton(
            text = "시뮬레이션 시작",
            onClick = onStartSimulation,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}

@Composable
private fun SimulationProgressView(
    progress: Float,
    phaseDescription: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ExitSpacing.LG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            color = ExitColors.Accent,
            trackColor = ExitColors.Divider,
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = ExitTypography.Title2,
            color = ExitColors.Accent
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        Text(
            text = phaseDescription,
            style = ExitTypography.Body,
            color = ExitColors.SecondaryText
        )
    }
}

@Composable
private fun SimulationResultView(
    viewModel: SimulationViewModel,
    monteCarloResult: com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult,
    retirementResult: com.woweverstudio.exit_aos.domain.usecase.RetirementSimulationResult?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ExitSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        // 성공률 카드
        SuccessRateCard(
            successRate = monteCarloResult.successRate,
            confidenceLevel = monteCarloResult.confidenceLevel
        )
        
        // 통계 카드
        StatisticsCard(
            monteCarloResult = monteCarloResult
        )
        
        // 퍼센타일 카드
        PercentileCard(
            percentileData = viewModel.percentileData
        )
        
        // 새로고침 버튼
        ExitPrimaryButton(
            text = "다시 시뮬레이션",
            onClick = { viewModel.refreshSimulation() }
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
    }
}

@Composable
private fun SuccessRateCard(
    successRate: Double,
    confidenceLevel: com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult.ConfidenceLevel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "은퇴 성공 확률",
            style = ExitTypography.Title3,
            color = ExitColors.SecondaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.MD))
        
        Text(
            text = "${(successRate * 100).toInt()}%",
            style = ExitTypography.ScoreDisplay,
            color = ExitColors.Accent
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        // 진행률 바
        LinearProgressIndicator(
            progress = { successRate.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = ExitColors.Accent,
            trackColor = ExitColors.Divider
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.MD))
        
        Text(
            text = "신뢰도: ${confidenceLevel.displayName}",
            style = ExitTypography.Caption,
            color = ExitColors.SecondaryText
        )
    }
}

@Composable
private fun StatisticsCard(
    monteCarloResult: com.woweverstudio.exit_aos.domain.usecase.MonteCarloResult
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG)
    ) {
        Text(
            text = "시뮬레이션 통계",
            style = ExitTypography.Title3,
            color = ExitColors.PrimaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.MD))
        
        StatRow("총 시뮬레이션", "${monteCarloResult.totalSimulations}회")
        StatRow("성공", "${monteCarloResult.successCount}회")
        StatRow("실패", "${monteCarloResult.failureCount}회")
        StatRow("평균 도달 기간", ExitNumberFormatter.formatMonthsToYearsMonths(monteCarloResult.averageMonthsToSuccess.toInt()))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ExitSpacing.XS),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = ExitTypography.Body,
            color = ExitColors.SecondaryText
        )
        Text(
            text = value,
            style = ExitTypography.Body,
            fontWeight = FontWeight.SemiBold,
            color = ExitColors.PrimaryText
        )
    }
}

@Composable
private fun PercentileCard(
    percentileData: List<com.woweverstudio.exit_aos.presentation.viewmodel.PercentilePoint>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.LG)
    ) {
        Text(
            text = "도달 기간 분포",
            style = ExitTypography.Title3,
            color = ExitColors.PrimaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.MD))
        
        percentileData.forEach { point ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ExitSpacing.XS),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = point.label,
                    style = ExitTypography.Body,
                    color = ExitColors.SecondaryText
                )
                Text(
                    text = point.displayText,
                    style = ExitTypography.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = when (point.percentile) {
                        10 -> ExitColors.Positive
                        90 -> ExitColors.Caution
                        else -> ExitColors.Accent
                    }
                )
            }
        }
    }
}

