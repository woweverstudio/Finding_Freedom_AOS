package com.woweverstudio.exit_aos.presentation.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.woweverstudio.exit_aos.presentation.ui.components.CustomNumberKeyboard
import com.woweverstudio.exit_aos.presentation.ui.components.ExitPrimaryButton
import com.woweverstudio.exit_aos.presentation.ui.components.ExitSecondaryButton
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.presentation.viewmodel.OnboardingStep
import com.woweverstudio.exit_aos.presentation.viewmodel.OnboardingViewModel
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import kotlin.math.abs

/**
 * 온보딩 화면
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val showWelcome by viewModel.showWelcome.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            onComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExitColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = showWelcome,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
                } else {
                    slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                }
            },
            label = "onboarding_content"
        ) { isWelcome ->
            if (isWelcome) {
                WelcomeScreen(
                    onStartClick = { viewModel.dismissWelcome() }
                )
            } else {
                OnboardingStepContent(
                    viewModel = viewModel,
                    currentStep = currentStep
                )
            }
        }
    }
}

@Composable
private fun OnboardingStepContent(
    viewModel: OnboardingViewModel,
    currentStep: OnboardingStep
) {
    val desiredMonthlyIncome by viewModel.desiredMonthlyIncome.collectAsState()
    val currentNetAssets by viewModel.currentNetAssets.collectAsState()
    val monthlyInvestment by viewModel.monthlyInvestment.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = ExitSpacing.LG)
    ) {
        // 진행률 표시
        ProgressIndicator(
            progress = viewModel.progress,
            modifier = Modifier.padding(horizontal = ExitSpacing.LG)
        )
        
        // 메인 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // 제목
            Text(
                text = currentStep.title,
                style = ExitTypography.Title,
                color = ExitColors.PrimaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = ExitSpacing.LG)
            )
            
            Spacer(modifier = Modifier.height(ExitSpacing.SM))
            
            Text(
                text = currentStep.subtitle,
                style = ExitTypography.Body,
                color = ExitColors.SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = ExitSpacing.LG)
            )
            
            Spacer(modifier = Modifier.height(ExitSpacing.XL))
            
            // 금액 표시
            val value = when (currentStep) {
                OnboardingStep.DESIRED_INCOME -> desiredMonthlyIncome
                OnboardingStep.CURRENT_ASSETS -> currentNetAssets
                OnboardingStep.MONTHLY_INVESTMENT -> monthlyInvestment
            }
            
            AmountDisplay(value = value)
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 키보드
            CustomNumberKeyboard(
                onDigitClick = { viewModel.appendDigit(it) },
                onDeleteClick = { viewModel.deleteLastDigit() },
                onQuickAmountClick = { viewModel.addQuickAmount(it) },
                onResetClick = { viewModel.resetCurrentValue() },
                showNegativeToggle = viewModel.showsNegativeToggle,
                isNegative = value < 0,
                onToggleSign = { viewModel.toggleSign() }
            )
        }
        
        // 하단 버튼
        BottomButtons(
            viewModel = viewModel,
            currentStep = currentStep
        )
    }
}

@Composable
private fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        OnboardingStep.entries.forEach { step ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (step.index <= (progress * OnboardingStep.entries.size).toInt() - 1) {
                            ExitColors.Accent
                        } else {
                            ExitColors.Divider
                        }
                    )
            )
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

@Composable
private fun BottomButtons(
    viewModel: OnboardingViewModel,
    currentStep: OnboardingStep
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ExitSpacing.LG,
                vertical = ExitSpacing.XL
            ),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        // 이전 버튼
        if (currentStep.index > 0) {
            ExitSecondaryButton(
                text = "이전",
                onClick = { viewModel.goToPreviousStep() },
                modifier = Modifier.weight(1f)
            )
        }
        
        // 다음/완료 버튼
        ExitPrimaryButton(
            text = if (viewModel.isLastStep) "완료하고 시작하기" else "다음",
            onClick = {
                if (viewModel.isLastStep) {
                    viewModel.completeOnboarding()
                } else {
                    viewModel.goToNextStep()
                }
            },
            enabled = viewModel.canProceed,
            modifier = Modifier.weight(if (currentStep.index > 0) 1f else 2f)
        )
    }
}

