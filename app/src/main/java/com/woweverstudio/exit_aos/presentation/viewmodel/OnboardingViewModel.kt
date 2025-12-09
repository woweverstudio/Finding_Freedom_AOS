package com.woweverstudio.exit_aos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woweverstudio.exit_aos.data.repository.ExitRepository
import com.woweverstudio.exit_aos.domain.model.Asset
import com.woweverstudio.exit_aos.domain.model.AssetSnapshot
import com.woweverstudio.exit_aos.domain.model.MonthlyUpdate
import com.woweverstudio.exit_aos.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

/**
 * 온보딩 단계
 */
enum class OnboardingStep(val index: Int, val title: String, val subtitle: String, val defaultValue: Double) {
    DESIRED_INCOME(0, "은퇴 후 희망 월 현금흐름", "매달 얼마가 있으면 일 안 해도 될까요?", 3_000_000.0),
    CURRENT_ASSETS(1, "현재 순자산", "투자 가능한 자산만 입력해주세요", 0.0),
    MONTHLY_INVESTMENT(2, "월 평균 저축·투자 금액", "월급 등 근로 소득만 포함 (배당/이자 재투자 제외)", 500_000.0);
    
    companion object {
        fun fromIndex(index: Int): OnboardingStep? = entries.find { it.index == index }
    }
}

/**
 * 온보딩 ViewModel
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: ExitRepository
) : ViewModel() {
    
    // MARK: - State
    
    /** 환영 화면 표시 여부 */
    private val _showWelcome = MutableStateFlow(true)
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()
    
    /** 현재 온보딩 단계 */
    private val _currentStep = MutableStateFlow(OnboardingStep.DESIRED_INCOME)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()
    
    /** 희망 월 수입 (원 단위) */
    private val _desiredMonthlyIncome = MutableStateFlow(3_000_000.0)
    val desiredMonthlyIncome: StateFlow<Double> = _desiredMonthlyIncome.asStateFlow()
    
    /** 현재 순자산 (원 단위) */
    private val _currentNetAssets = MutableStateFlow(0.0)
    val currentNetAssets: StateFlow<Double> = _currentNetAssets.asStateFlow()
    
    /** 월 투자 금액 (원 단위) */
    private val _monthlyInvestment = MutableStateFlow(500_000.0)
    val monthlyInvestment: StateFlow<Double> = _monthlyInvestment.asStateFlow()
    
    /** 온보딩 완료 여부 */
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()
    
    // MARK: - Computed Properties
    
    val currentInputValue: Double
        get() = when (_currentStep.value) {
            OnboardingStep.DESIRED_INCOME -> _desiredMonthlyIncome.value
            OnboardingStep.CURRENT_ASSETS -> _currentNetAssets.value
            OnboardingStep.MONTHLY_INVESTMENT -> _monthlyInvestment.value
        }
    
    val showsNegativeToggle: Boolean
        get() = _currentStep.value == OnboardingStep.CURRENT_ASSETS
    
    val canProceed: Boolean
        get() = when (_currentStep.value) {
            OnboardingStep.DESIRED_INCOME -> _desiredMonthlyIncome.value > 0
            OnboardingStep.CURRENT_ASSETS -> true
            OnboardingStep.MONTHLY_INVESTMENT -> _monthlyInvestment.value >= 0
        }
    
    val isLastStep: Boolean
        get() = _currentStep.value == OnboardingStep.MONTHLY_INVESTMENT
    
    val progress: Float
        get() = (_currentStep.value.index + 1).toFloat() / OnboardingStep.entries.size
    
    // MARK: - Actions
    
    /** 온보딩 상태 초기화 (데이터 삭제 후 다시 온보딩할 때 사용) */
    fun reset() {
        _showWelcome.value = true
        _currentStep.value = OnboardingStep.DESIRED_INCOME
        _desiredMonthlyIncome.value = 3_000_000.0
        _currentNetAssets.value = 0.0
        _monthlyInvestment.value = 500_000.0
        _isCompleted.value = false
    }
    
    fun dismissWelcome() {
        _showWelcome.value = false
    }
    
    fun goToNextStep() {
        if (!canProceed) return
        
        OnboardingStep.fromIndex(_currentStep.value.index + 1)?.let {
            _currentStep.value = it
        }
    }
    
    fun goToPreviousStep() {
        OnboardingStep.fromIndex(_currentStep.value.index - 1)?.let {
            _currentStep.value = it
        }
    }
    
    fun setCurrentInputValue(value: Double) {
        when (_currentStep.value) {
            OnboardingStep.DESIRED_INCOME -> _desiredMonthlyIncome.value = value
            OnboardingStep.CURRENT_ASSETS -> _currentNetAssets.value = value
            OnboardingStep.MONTHLY_INVESTMENT -> _monthlyInvestment.value = value
        }
    }
    
    fun appendDigit(digit: String) {
        val currentString = abs(currentInputValue).toLong().toString()
        val newString = if (currentString == "0") digit else currentString + digit
        newString.toLongOrNull()?.let { value ->
            if (value <= 100_000_000_000) { // 1000억 제한
                val newValue = if (currentInputValue < 0) -value.toDouble() else value.toDouble()
                setCurrentInputValue(newValue)
            }
        }
    }
    
    fun deleteLastDigit() {
        val currentString = abs(currentInputValue).toLong().toString()
        val newString = if (currentString.length > 1) {
            currentString.dropLast(1)
        } else {
            "0"
        }
        val value = newString.toDoubleOrNull() ?: 0.0
        val newValue = if (currentInputValue < 0) -value else value
        setCurrentInputValue(newValue)
    }
    
    fun addQuickAmount(amount: Double) {
        val newValue = currentInputValue + amount
        if (newValue >= -100_000_000_000 && newValue <= 100_000_000_000) {
            setCurrentInputValue(newValue)
        }
    }
    
    fun toggleSign() {
        setCurrentInputValue(-currentInputValue)
    }
    
    fun resetCurrentValue() {
        setCurrentInputValue(0.0)
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            // UserProfile 저장
            val profile = UserProfile(
                desiredMonthlyIncome = _desiredMonthlyIncome.value,
                currentNetAssets = _currentNetAssets.value,
                monthlyInvestment = _monthlyInvestment.value,
                hasCompletedOnboarding = true
            )
            repository.saveUserProfile(profile)
            
            // Asset 생성
            val asset = Asset(amount = _currentNetAssets.value)
            repository.saveAsset(asset)
            
            // AssetSnapshot 생성
            val snapshot = AssetSnapshot(
                yearMonth = AssetSnapshot.currentYearMonth(),
                amount = _currentNetAssets.value
            )
            repository.saveSnapshot(snapshot)
            
            // 초기 MonthlyUpdate 생성
            val initialUpdate = MonthlyUpdate(
                yearMonth = MonthlyUpdate.currentYearMonth(),
                totalAssets = _currentNetAssets.value
            )
            repository.saveMonthlyUpdate(initialUpdate)
            
            _isCompleted.value = true
        }
    }
}

