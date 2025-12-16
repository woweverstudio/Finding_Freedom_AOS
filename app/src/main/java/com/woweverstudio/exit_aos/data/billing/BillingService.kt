package com.woweverstudio.exit_aos.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing 상수
 */
object BillingConstants {
    /** 몬테카를로 시뮬레이션 상품 ID (Google Play Console에서 설정) */
    const val MONTECARLO_PRODUCT_ID = "montecarlo_simulation"
    
    /** 표시 가격 (상품 정보 로드 실패 시 사용) */
    const val DEFAULT_DISPLAY_PRICE = "₩4,900"
}

/**
 * 결제 상태
 */
sealed class BillingState {
    object Loading : BillingState()
    object Ready : BillingState()
    object Purchased : BillingState()
    object Purchasing : BillingState()
    data class Error(val message: String) : BillingState()
}

/**
 * Google Play Billing Service
 * iOS의 StoreKitService에 대응
 */
@Singleton
class BillingService @Inject constructor(
    private val context: Context
) : PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    
    // MARK: - State
    
    private val _billingState = MutableStateFlow<BillingState>(BillingState.Loading)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()
    
    private val _isMontecarloUnlocked = MutableStateFlow(false)
    val isMontecarloUnlocked: StateFlow<Boolean> = _isMontecarloUnlocked.asStateFlow()
    
    private val _montecarloProduct = MutableStateFlow<ProductDetails?>(null)
    val montecarloProduct: StateFlow<ProductDetails?> = _montecarloProduct.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /** 표시 가격 */
    val displayPrice: String
        get() = _montecarloProduct.value
            ?.oneTimePurchaseOfferDetails
            ?.formattedPrice
            ?: BillingConstants.DEFAULT_DISPLAY_PRICE
    
    // MARK: - Initialization
    
    /**
     * Billing Client 초기화
     */
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        startConnection()
    }
    
    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 연결 성공
                    queryProductDetails()
                    queryPurchases()
                    _billingState.value = BillingState.Ready
                } else {
                    _billingState.value = BillingState.Error("결제 서비스 연결 실패")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // 연결 끊김 - 재연결 시도
                startConnection()
            }
        })
    }
    
    // MARK: - Query Products
    
    /**
     * 상품 정보 조회
     */
    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingConstants.MONTECARLO_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.firstOrNull()?.let { product ->
                    _montecarloProduct.value = product
                }
            }
        }
    }
    
    // MARK: - Query Purchases
    
    /**
     * 구매 내역 조회 (이미 구매했는지 확인)
     */
    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        
        billingClient?.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPurchased = purchasesList.any { purchase ->
                    purchase.products.contains(BillingConstants.MONTECARLO_PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                
                _isMontecarloUnlocked.value = hasPurchased
                
                if (hasPurchased) {
                    _billingState.value = BillingState.Purchased
                }
                
                // Acknowledge unacknowledged purchases
                purchasesList.forEach { purchase ->
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }
    
    // MARK: - Purchase
    
    /**
     * 몬테카를로 시뮬레이션 구매
     * @param activity 현재 Activity
     * @return 구매 성공 여부
     */
    suspend fun purchaseMontecarloSimulation(activity: Activity): Boolean {
        val product = _montecarloProduct.value ?: run {
            _errorMessage.value = "상품 정보를 불러올 수 없습니다"
            return false
        }
        
        _billingState.value = BillingState.Purchasing
        _errorMessage.value = null
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        
        return billingResult?.responseCode == BillingClient.BillingResponseCode.OK
    }
    
    // MARK: - Restore Purchases
    
    /**
     * 구매 복원
     */
    suspend fun restorePurchases() {
        _errorMessage.value = null
        queryPurchases()
        
        if (!_isMontecarloUnlocked.value) {
            _errorMessage.value = "복원할 구매 내역이 없습니다"
        }
    }
    
    // MARK: - PurchasesUpdatedListener
    
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Ready
                _errorMessage.value = null
            }
            else -> {
                _billingState.value = BillingState.Error("구매 중 오류가 발생했습니다")
                _errorMessage.value = "구매 중 오류가 발생했습니다"
            }
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(BillingConstants.MONTECARLO_PRODUCT_ID)) {
                _isMontecarloUnlocked.value = true
                _billingState.value = BillingState.Purchased
            }
            
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgeParams) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                // Acknowledge 실패 처리
            }
        }
    }
    
    // MARK: - Cleanup
    
    /**
     * 리소스 해제
     */
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
}

