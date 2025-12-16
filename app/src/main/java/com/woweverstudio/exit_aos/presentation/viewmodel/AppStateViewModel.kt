package com.woweverstudio.exit_aos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woweverstudio.exit_aos.data.repository.ExitRepository
import com.woweverstudio.exit_aos.domain.model.Asset
import com.woweverstudio.exit_aos.domain.model.AssetSnapshot
import com.woweverstudio.exit_aos.domain.model.MonthlyUpdate
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculationResult
import com.woweverstudio.exit_aos.domain.usecase.RetirementCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * 앱 전역 상태 관리 ViewModel
 */
@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val repository: ExitRepository
) : ViewModel() {
    
    // MARK: - UI State
    
    /** 현재 선택된 탭 */
    private val _selectedTab = MutableStateFlow(MainTab.DASHBOARD)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()
    
    /** 금액 숨김 여부 */
    private val _hideAmounts = MutableStateFlow(false)
    val hideAmounts: StateFlow<Boolean> = _hideAmounts.asStateFlow()
    
    /** D-Day 애니메이션 트리거 */
    private val _dDayAnimationTrigger = MutableStateFlow(UUID.randomUUID().toString())
    val dDayAnimationTrigger: StateFlow<String> = _dDayAnimationTrigger.asStateFlow()
    
    // MARK: - Data State
    
    /** 현재 자산 */
    private val _currentAsset = MutableStateFlow<Asset?>(null)
    val currentAsset: StateFlow<Asset?> = _currentAsset.asStateFlow()
    
    /** 사용자 프로필 */
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    /** 월별 업데이트 기록 */
    private val _monthlyUpdates = MutableStateFlow<List<MonthlyUpdate>>(emptyList())
    val monthlyUpdates: StateFlow<List<MonthlyUpdate>> = _monthlyUpdates.asStateFlow()
    
    /** 자산 스냅샷 히스토리 */
    private val _assetSnapshots = MutableStateFlow<List<AssetSnapshot>>(emptyList())
    val assetSnapshots: StateFlow<List<AssetSnapshot>> = _assetSnapshots.asStateFlow()
    
    /** 은퇴 계산 결과 */
    private val _retirementResult = MutableStateFlow<RetirementCalculationResult?>(null)
    val retirementResult: StateFlow<RetirementCalculationResult?> = _retirementResult.asStateFlow()
    
    // MARK: - Sheet States
    
    /** 입금 시트 표시 */
    private val _showDepositSheet = MutableStateFlow(false)
    val showDepositSheet: StateFlow<Boolean> = _showDepositSheet.asStateFlow()
    
    /** 자산 업데이트 시트 표시 */
    private val _showAssetUpdateSheet = MutableStateFlow(false)
    val showAssetUpdateSheet: StateFlow<Boolean> = _showAssetUpdateSheet.asStateFlow()
    
    /** 자산 업데이트 확인 표시 */
    private val _showAssetUpdateConfirm = MutableStateFlow(false)
    val showAssetUpdateConfirm: StateFlow<Boolean> = _showAssetUpdateConfirm.asStateFlow()
    
    /** 수정할 월 */
    private val _editingYearMonth = MutableStateFlow<String?>(null)
    val editingYearMonth: StateFlow<String?> = _editingYearMonth.asStateFlow()
    
    // MARK: - Computed Properties
    
    val currentAssetAmount: Double
        get() = _currentAsset.value?.amount ?: 0.0
    
    val progressValue: Double
        get() = (_retirementResult.value?.progressPercent ?: 0.0) / 100
    
    val totalDepositAmount: Double
        get() = _monthlyUpdates.value.sumOf { update ->
            val categoryTotal = update.salaryAmount + update.dividendAmount + 
                update.interestAmount + update.rentAmount + update.otherAmount
            if (categoryTotal > 0) categoryTotal else update.depositAmount + update.passiveIncome
        }
    
    val totalPassiveIncome: Double
        get() = _monthlyUpdates.value.sumOf { update ->
            val newPassive = update.dividendAmount + update.interestAmount + update.rentAmount
            if (newPassive > 0) newPassive else update.passiveIncome
        }
    
    // MARK: - Initialization
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            // 병렬로 데이터 로드
            launch {
                repository.getUserProfile().collect { profile ->
                    _userProfile.value = profile
                    calculateResults()
                }
            }
            
            launch {
                repository.getAsset().collect { asset ->
                    _currentAsset.value = asset
                    calculateResults()
                }
            }
            
            launch {
                repository.getMonthlyUpdates().collect { updates ->
                    _monthlyUpdates.value = updates
                }
            }
            
            launch {
                repository.getAssetSnapshots().collect { snapshots ->
                    _assetSnapshots.value = snapshots
                }
            }
        }
    }
    
    // MARK: - Calculations
    
    private fun calculateResults() {
        val profile = _userProfile.value ?: return
        val assetAmount = _currentAsset.value?.amount ?: 0.0
        
        _retirementResult.value = RetirementCalculator.calculate(profile, assetAmount)
    }
    
    // MARK: - UI Actions
    
    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }
    
    /** 탭을 홈으로 초기화 */
    fun resetToHomeTab() {
        _selectedTab.value = MainTab.DASHBOARD
    }
    
    fun toggleHideAmounts() {
        _hideAmounts.value = !_hideAmounts.value
    }
    
    fun setShowDepositSheet(show: Boolean) {
        _showDepositSheet.value = show
    }
    
    fun setShowAssetUpdateSheet(show: Boolean) {
        _showAssetUpdateSheet.value = show
    }
    
    fun setShowAssetUpdateConfirm(show: Boolean) {
        _showAssetUpdateConfirm.value = show
    }
    
    fun setEditingYearMonth(yearMonth: String?) {
        _editingYearMonth.value = yearMonth
    }
    
    // MARK: - Settings Actions
    
    fun updateSettings(
        desiredMonthlyIncome: Double? = null,
        monthlyInvestment: Double? = null,
        preRetirementReturnRate: Double? = null,
        postRetirementReturnRate: Double? = null
    ) {
        viewModelScope.launch {
            val profile = _userProfile.value ?: return@launch
            
            val updatedProfile = profile.updateSettings(
                desiredMonthlyIncome = desiredMonthlyIncome,
                monthlyInvestment = monthlyInvestment,
                preRetirementReturnRate = preRetirementReturnRate,
                postRetirementReturnRate = postRetirementReturnRate
            )
            
            repository.updateUserProfile(updatedProfile)
            
            // D-Day 애니메이션 트리거
            _dDayAnimationTrigger.value = UUID.randomUUID().toString()
            
            // Note: 시뮬레이션 결과 초기화는 SimulationViewModel에서 Flow 관찰로 자동 처리됨
        }
    }
    
    // MARK: - Asset Actions
    
    fun updateCurrentAsset(amount: Double) {
        viewModelScope.launch {
            val existingAsset = _currentAsset.value
            
            if (existingAsset != null) {
                val updatedAsset = existingAsset.update(amount)
                repository.updateAsset(updatedAsset)
            } else {
                val newAsset = Asset(amount = amount)
                repository.saveAsset(newAsset)
            }
            
            // Note: 시뮬레이션 결과 초기화는 SimulationViewModel에서 Flow 관찰로 자동 처리됨
        }
    }
    
    fun submitAssetUpdate(totalAssetsInput: Double) {
        viewModelScope.launch {
            val yearMonth = AssetSnapshot.currentYearMonth()
            
            // 자산 업데이트
            updateCurrentAsset(totalAssetsInput)
            
            // 스냅샷 업데이트/생성
            val existingSnapshot = repository.getSnapshotByYearMonth(yearMonth)
            if (existingSnapshot != null) {
                repository.updateSnapshot(
                    existingSnapshot.copy(amount = totalAssetsInput, snapshotDate = Date())
                )
            } else {
                repository.saveSnapshot(
                    AssetSnapshot(yearMonth = yearMonth, amount = totalAssetsInput)
                )
            }
            
            // 월별 업데이트에도 자산 기록
            val existingUpdate = repository.getUpdateByYearMonth(yearMonth)
            if (existingUpdate != null) {
                repository.updateMonthlyUpdate(
                    existingUpdate.copy(totalAssets = totalAssetsInput, recordedAt = Date())
                )
            }
            
            _showAssetUpdateSheet.value = false
            _showAssetUpdateConfirm.value = false
        }
    }
    
    // MARK: - Deposit Actions
    
    fun submitCategoryDeposit(
        yearMonth: String,
        salaryAmount: Double,
        dividendAmount: Double,
        interestAmount: Double,
        rentAmount: Double,
        otherAmount: Double
    ) {
        viewModelScope.launch {
            val existingUpdate = repository.getUpdateByYearMonth(yearMonth)
            
            if (existingUpdate != null) {
                repository.updateMonthlyUpdate(
                    existingUpdate.copy(
                        salaryAmount = salaryAmount,
                        dividendAmount = dividendAmount,
                        interestAmount = interestAmount,
                        rentAmount = rentAmount,
                        otherAmount = otherAmount,
                        depositAmount = salaryAmount + otherAmount,
                        passiveIncome = dividendAmount + interestAmount + rentAmount,
                        totalAssets = currentAssetAmount,
                        recordedAt = Date()
                    )
                )
            } else {
                repository.saveMonthlyUpdate(
                    MonthlyUpdate(
                        yearMonth = yearMonth,
                        salaryAmount = salaryAmount,
                        dividendAmount = dividendAmount,
                        interestAmount = interestAmount,
                        rentAmount = rentAmount,
                        otherAmount = otherAmount,
                        depositAmount = salaryAmount + otherAmount,
                        passiveIncome = dividendAmount + interestAmount + rentAmount,
                        totalAssets = currentAssetAmount
                    )
                )
            }
            
            _showDepositSheet.value = false
            
            // 자산 업데이트 확인 표시
            kotlinx.coroutines.delay(300)
            _showAssetUpdateConfirm.value = true
        }
    }
    
    fun deleteMonthlyUpdate(update: MonthlyUpdate) {
        viewModelScope.launch {
            repository.deleteMonthlyUpdate(update)
        }
    }
}

/**
 * 메인 탭 종류
 */
enum class MainTab(val displayName: String, val icon: String) {
    DASHBOARD("홈", "home"),
    SIMULATION("시뮬레이션", "analytics"),
    MENU("메뉴", "menu")
}

