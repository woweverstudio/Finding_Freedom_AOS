package com.woweverstudio.exit_aos.domain.usecase

import com.woweverstudio.exit_aos.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 단일 시뮬레이션 경로 (경로 + 소진 연도)
 */
data class RetirementPath(
    /** 연도별 자산 */
    val yearlyAssets: List<Double>,
    
    /** 소진 연도 (없으면 null) */
    val depletionYear: Int?
) {
    val finalAsset: Double
        get() = yearlyAssets.lastOrNull() ?: 0.0
    
    /** 10년 후 자산 (단기 분석용) */
    val assetAt10Years: Double
        get() = if (yearlyAssets.size > 10) yearlyAssets[10] else (yearlyAssets.lastOrNull() ?: 0.0)
}

/**
 * 은퇴 후 시뮬레이션 결과
 */
data class RetirementSimulationResult(
    // MARK: - 장기 (40년 기준) 경로
    
    /** 매우 행운 경로 - 40년 기준 (상위 10%) */
    val veryBestPath: RetirementPath,
    
    /** 행운 경로 - 40년 기준 (상위 30%) */
    val luckyPath: RetirementPath,
    
    /** 평균 경로 - 40년 기준 (중앙값 50%) */
    val medianPath: RetirementPath,
    
    /** 불행 경로 - 40년 기준 (하위 30%) */
    val unluckyPath: RetirementPath,
    
    /** 매우 불행 경로 - 40년 기준 (하위 10%) */
    val veryWorstPath: RetirementPath,
    
    // MARK: - 단기 (10년 기준) 경로
    
    /** 매우 행운 경로 - 10년 기준 (상위 10%) */
    val shortTermVeryBestPath: RetirementPath,
    
    /** 행운 경로 - 10년 기준 (상위 30%) */
    val shortTermLuckyPath: RetirementPath,
    
    /** 평균 경로 - 10년 기준 (중앙값 50%) */
    val shortTermMedianPath: RetirementPath,
    
    /** 불행 경로 - 10년 기준 (하위 30%) */
    val shortTermUnluckyPath: RetirementPath,
    
    /** 매우 불행 경로 - 10년 기준 (하위 10%) */
    val shortTermVeryWorstPath: RetirementPath,
    
    // MARK: - 공통
    
    /** 기존 예측 경로 (변동성 없음) */
    val deterministicPath: RetirementPath,
    
    /** 시뮬레이션 횟수 */
    val totalSimulations: Int
) {
    // 기존 호환성을 위한 별칭
    val bestPath: RetirementPath get() = veryBestPath
    val worstPath: RetirementPath get() = veryWorstPath
    
    // 편의 프로퍼티
    val medianDepletionYear: Int? get() = medianPath.depletionYear
    val worstDepletionYear: Int? get() = veryWorstPath.depletionYear
}

/**
 * 은퇴 후 시뮬레이션 진행률 콜백 타입
 */
typealias RetirementProgressCallback = (completed: Int) -> Unit

/**
 * 은퇴 후 자산 변화 시뮬레이터
 * iOS와 동일한 로직 + 병렬 처리로 성능 최적화
 */
object RetirementSimulator {
    
    // 병렬 처리를 위한 코어 수
    private val parallelism = Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
    
    /**
     * 은퇴 후 시뮬레이션 실행 (병렬 처리)
     */
    suspend fun simulate(
        initialAsset: Double,
        monthlySpending: Double,
        annualReturn: Double,
        volatility: Double,
        years: Int = 40,
        simulationCount: Int = 30_000,
        progressCallback: RetirementProgressCallback? = null
    ): RetirementSimulationResult = coroutineScope {
        // 월 수익률 파라미터 미리 계산
        val monthlyMean = ln(1 + annualReturn / 100) / 12.0
        val monthlyVolatility = (volatility / 100) / sqrt(12.0)
        
        // 청크 크기 계산
        val chunkSize = simulationCount / parallelism
        val completed = java.util.concurrent.atomic.AtomicInteger(0)
        
        // 병렬로 시뮬레이션 실행
        val allPaths = (0 until parallelism).map { chunkIndex ->
            async(Dispatchers.Default) {
                val random = Random(System.nanoTime() + chunkIndex)
                val startIndex = chunkIndex * chunkSize
                val endIndex = if (chunkIndex == parallelism - 1) simulationCount else startIndex + chunkSize
                val chunkCount = endIndex - startIndex
                
                val paths = ArrayList<RetirementPath>(chunkCount)
                
                for (i in 0 until chunkCount) {
                    val path = runSingleSimulation(
                        initialAsset = initialAsset,
                        monthlySpending = monthlySpending,
                        monthlyMean = monthlyMean,
                        monthlyVolatility = monthlyVolatility,
                        years = years,
                        random = random
                    )
                    paths.add(path)
                    
                    // 진행률 콜백 (300번마다)
                    val totalCompleted = completed.incrementAndGet()
                    if (totalCompleted % 300 == 0) {
                        progressCallback?.invoke(totalCompleted)
                    }
                }
                paths
            }
        }.awaitAll().flatten()
        
        // 최종 콜백
        progressCallback?.invoke(simulationCount)
        
        // 기존 예측 (변동성 없음)
        val deterministicPath = calculateDeterministicPath(
            initialAsset = initialAsset,
            monthlySpending = monthlySpending,
            annualReturn = annualReturn,
            years = years
        )
        
        // === 장기 (40년) 기준 정렬 ===
        val sortedByFinal = allPaths.sortedWith(compareBy<RetirementPath> { it.finalAsset }
            .thenBy { it.depletionYear ?: Int.MAX_VALUE })
        
        val veryWorstPath = sortedByFinal[simulationCount * 10 / 100]
        val unluckyPath = sortedByFinal[simulationCount * 30 / 100]
        val medianPath = sortedByFinal[simulationCount * 50 / 100]
        val luckyPath = sortedByFinal[simulationCount * 70 / 100]
        val veryBestPath = sortedByFinal[simulationCount * 90 / 100]
        
        // === 단기 (10년) 기준 정렬 ===
        val sortedBy10Years = allPaths.sortedWith(compareBy<RetirementPath> { it.assetAt10Years }
            .thenBy { it.depletionYear ?: Int.MAX_VALUE })
        
        val shortTermVeryWorstPath = sortedBy10Years[simulationCount * 10 / 100]
        val shortTermUnluckyPath = sortedBy10Years[simulationCount * 30 / 100]
        val shortTermMedianPath = sortedBy10Years[simulationCount * 50 / 100]
        val shortTermLuckyPath = sortedBy10Years[simulationCount * 70 / 100]
        val shortTermVeryBestPath = sortedBy10Years[simulationCount * 90 / 100]
        
        RetirementSimulationResult(
            veryBestPath = veryBestPath,
            luckyPath = luckyPath,
            medianPath = medianPath,
            unluckyPath = unluckyPath,
            veryWorstPath = veryWorstPath,
            shortTermVeryBestPath = shortTermVeryBestPath,
            shortTermLuckyPath = shortTermLuckyPath,
            shortTermMedianPath = shortTermMedianPath,
            shortTermUnluckyPath = shortTermUnluckyPath,
            shortTermVeryWorstPath = shortTermVeryWorstPath,
            deterministicPath = deterministicPath,
            totalSimulations = simulationCount
        )
    }
    
    // MARK: - Single Simulation
    
    /**
     * 단일 시뮬레이션 실행
     */
    private fun runSingleSimulation(
        initialAsset: Double,
        monthlySpending: Double,
        monthlyMean: Double,
        monthlyVolatility: Double,
        years: Int,
        random: Random
    ): RetirementPath {
        val yearlyAssets = DoubleArray(years + 1)
        yearlyAssets[0] = initialAsset
        
        var currentAsset = initialAsset
        var depletionYear: Int? = null
        
        for (year in 1..years) {
            // 12개월 시뮬레이션 (인라인 최적화)
            repeat(12) {
                // Box-Muller 변환
                val u1 = random.nextDouble()
                val u2 = random.nextDouble()
                val z0 = sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
                val monthlyReturn = monthlyMean + z0 * monthlyVolatility
                
                currentAsset = currentAsset * exp(monthlyReturn) - monthlySpending
            }
            
            yearlyAssets[year] = max(0.0, currentAsset)
            
            if (currentAsset <= 0 && depletionYear == null) {
                depletionYear = year
            }
        }
        
        return RetirementPath(yearlyAssets = yearlyAssets.toList(), depletionYear = depletionYear)
    }
    
    // MARK: - Deterministic Calculation
    
    private fun calculateDeterministicPath(
        initialAsset: Double,
        monthlySpending: Double,
        annualReturn: Double,
        years: Int
    ): RetirementPath {
        val yearlyAssets = DoubleArray(years + 1)
        yearlyAssets[0] = initialAsset
        
        var currentAsset = initialAsset
        val monthlyReturnFactor = 1 + annualReturn / 100 / 12
        var depletionYear: Int? = null
        
        for (year in 1..years) {
            repeat(12) {
                currentAsset = currentAsset * monthlyReturnFactor - monthlySpending
            }
            yearlyAssets[year] = max(0.0, currentAsset)
            
            if (currentAsset <= 0 && depletionYear == null) {
                depletionYear = year
            }
        }
        
        return RetirementPath(yearlyAssets = yearlyAssets.toList(), depletionYear = depletionYear)
    }
    
    // MARK: - UserProfile-based Simulation
    
    suspend fun simulate(
        profile: UserProfile,
        volatility: Double = 15.0,
        simulationCount: Int = 30_000,
        progressCallback: RetirementProgressCallback? = null
    ): RetirementSimulationResult {
        val targetAsset = RetirementCalculator.calculateTargetAssets(
            desiredMonthlyIncome = profile.desiredMonthlyIncome,
            postRetirementReturnRate = profile.postRetirementReturnRate,
            inflationRate = profile.inflationRate
        )
        
        return simulate(
            initialAsset = targetAsset,
            monthlySpending = profile.desiredMonthlyIncome,
            annualReturn = profile.postRetirementReturnRate,
            volatility = volatility,
            simulationCount = simulationCount,
            progressCallback = progressCallback
        )
    }
}
