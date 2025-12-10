package com.woweverstudio.exit_aos

import android.app.Application
import com.woweverstudio.exit_aos.util.HapticService
import com.woweverstudio.exit_aos.util.ReviewService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExitApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // HapticService 초기화
        HapticService.init(this)
        
        // ReviewService 초기화
        ReviewService.init(this)
    }
}

