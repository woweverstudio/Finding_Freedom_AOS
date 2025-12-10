package com.woweverstudio.exit_aos.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 앱 리뷰 요청 서비스
 * Google Play In-App Review API를 래핑하여 적절한 시점에 리뷰 요청 팝업을 표시
 * iOS의 ReviewService.swift와 동일한 로직
 */
object ReviewService {
    
    private const val TAG = "ReviewService"
    
    // SharedPreferences Keys
    private const val PREFS_NAME = "ReviewServicePrefs"
    private const val KEY_APP_LAUNCH_COUNT = "appLaunchCount"
    private const val KEY_SIMULATION_RUN_COUNT = "simulationRunCount"
    private const val KEY_HAS_SHOWN_REVIEW = "hasShownReview"
    
    private var prefs: SharedPreferences? = null
    
    // 이번 세션에서 리뷰 요청 여부 (메모리에만 저장)
    private var hasRequestedReviewThisSession = false
    
    /**
     * 초기화 (Application에서 호출)
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 앱 실행 시 호출
     * 3번째 실행 시 리뷰 요청
     */
    fun recordAppLaunch(activity: Activity) {
        // 세션 플래그 초기화
        hasRequestedReviewThisSession = false
        
        val prefs = prefs ?: return
        
        // 이미 리뷰를 표시한 적 있으면 스킵
        if (prefs.getBoolean(KEY_HAS_SHOWN_REVIEW, false)) {
            return
        }
        
        // 실행 횟수 증가
        val launchCount = prefs.getInt(KEY_APP_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_APP_LAUNCH_COUNT, launchCount).apply()
        
        Log.d(TAG, "📝 앱 실행 횟수 = $launchCount")
        
        // 3번째 실행 시 리뷰 요청
        if (launchCount == 3) {
            requestReview(activity, "앱 3회 실행")
        }
    }
    
    /**
     * 시뮬레이션 완료 시 호출
     * 구매 후 2번째 시뮬레이션 완료 시 리뷰 요청
     */
    fun recordSimulationCompleted(activity: Activity) {
        val prefs = prefs ?: return
        
        // 이미 리뷰를 표시한 적 있으면 스킵
        if (prefs.getBoolean(KEY_HAS_SHOWN_REVIEW, false)) {
            return
        }
        
        // 시뮬레이션 실행 횟수 증가
        val runCount = prefs.getInt(KEY_SIMULATION_RUN_COUNT, 0) + 1
        prefs.edit().putInt(KEY_SIMULATION_RUN_COUNT, runCount).apply()
        
        Log.d(TAG, "📝 시뮬레이션 실행 횟수 = $runCount")
        
        // 2번째 시뮬레이션 완료 시 리뷰 요청
        if (runCount == 2) {
            requestReview(activity, "시뮬레이션 2회 실행")
        }
    }
    
    /**
     * 리뷰 요청 실행
     */
    private fun requestReview(activity: Activity, reason: String) {
        // 이번 세션에서 이미 요청했으면 스킵
        if (hasRequestedReviewThisSession) return
        
        Log.d(TAG, "📝 리뷰 요청 (사유: $reason)")
        
        hasRequestedReviewThisSession = true
        prefs?.edit()?.putBoolean(KEY_HAS_SHOWN_REVIEW, true)?.apply()
        
        // 약간의 딜레이 후 리뷰 요청
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // 0.5초
            
            try {
                val reviewManager = ReviewManagerFactory.create(activity)
                val request = reviewManager.requestReviewFlow()
                
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                        
                        flow.addOnCompleteListener {
                            Log.d(TAG, "📝 리뷰 플로우 완료")
                        }
                    } else {
                        Log.e(TAG, "📝 리뷰 요청 실패: ${task.exception?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "📝 리뷰 요청 중 오류: ${e.message}")
            }
        }
    }
}

