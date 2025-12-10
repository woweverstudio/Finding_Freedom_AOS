package com.woweverstudio.exit_aos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.woweverstudio.exit_aos.presentation.ui.dashboard.DashboardScreen
import com.woweverstudio.exit_aos.presentation.ui.onboarding.OnboardingScreen
import com.woweverstudio.exit_aos.presentation.ui.settings.SettingsScreen
import com.woweverstudio.exit_aos.presentation.ui.simulation.SimulationScreen
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTheme
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography
import com.woweverstudio.exit_aos.data.billing.BillingService
import com.woweverstudio.exit_aos.presentation.viewmodel.AppStateViewModel
import com.woweverstudio.exit_aos.presentation.viewmodel.MainTab
import com.woweverstudio.exit_aos.util.ReviewService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var billingService: BillingService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 앱 실행 기록 (3번째 실행 시 리뷰 요청)
        ReviewService.recordAppLaunch(this)
        
        setContent {
            ExitTheme {
                ExitApp(billingService = billingService)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        billingService.endConnection()
    }
}

@Composable
fun ExitApp(
    billingService: BillingService
) {
    val appStateViewModel: AppStateViewModel = hiltViewModel()
    val userProfile by appStateViewModel.userProfile.collectAsState()
    
    var isLoading by remember { mutableStateOf(true) }
    
    // 초기 로딩 (한 번만 실행)
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500) // 초기 로딩 대기
        isLoading = false
    }
    
    // userProfile의 hasCompletedOnboarding 값으로 직접 판단 (iOS와 동일한 방식)
    val hasCompletedOnboarding = userProfile?.hasCompletedOnboarding == true
    
    when {
        isLoading -> {
            // 로딩 화면
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ExitColors.Background)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ExitColors.Accent)
            }
        }
        !hasCompletedOnboarding -> {
            // 온보딩 화면 (userProfile이 없거나 온보딩 미완료 시)
            OnboardingScreen(
                onComplete = {
                    appStateViewModel.resetToHomeTab() // 탭을 홈으로 초기화
                    // userProfile이 생성되면 hasCompletedOnboarding이 true가 되어
                    // 자동으로 메인 화면으로 전환됨
                }
            )
        }
        else -> {
            // 메인 화면
            MainScreen(
                appStateViewModel = appStateViewModel,
                billingService = billingService,
                onNavigateToOnboarding = {
                    appStateViewModel.resetToHomeTab() // 탭을 홈으로 미리 초기화
                    // 데이터 삭제 후 userProfile이 null이 되면
                    // 자동으로 온보딩 화면으로 전환됨
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    appStateViewModel: AppStateViewModel,
    billingService: BillingService,
    onNavigateToOnboarding: () -> Unit
) {
    val selectedTab by appStateViewModel.selectedTab.collectAsState()
    
    Scaffold(
        containerColor = ExitColors.Background,
        bottomBar = {
            ExitBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { appStateViewModel.selectTab(it) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                MainTab.DASHBOARD -> DashboardScreen(viewModel = appStateViewModel)
                MainTab.SIMULATION -> SimulationScreen(billingService = billingService)
                MainTab.MENU -> SettingsScreen(
                    onDeleteAllData = onNavigateToOnboarding
                )
            }
        }
    }
}

@Composable
fun ExitBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = ExitColors.CardBackground,
        contentColor = ExitColors.Accent
    ) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = tab.icon(),
                        contentDescription = tab.displayName
                    )
                },
                label = {
                    Text(
                        text = tab.displayName,
                        style = ExitTypography.Caption2
                    )
                },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ExitColors.Accent,
                    selectedTextColor = ExitColors.Accent,
                    unselectedIconColor = ExitColors.TertiaryText,
                    unselectedTextColor = ExitColors.TertiaryText,
                    indicatorColor = ExitColors.Accent.copy(alpha = 0.1f)
                )
            )
        }
    }
}

private fun MainTab.icon(): ImageVector {
    return when (this) {
        MainTab.DASHBOARD -> Icons.Default.Home
        MainTab.SIMULATION -> Icons.Default.Analytics
        MainTab.MENU -> Icons.Default.Menu
    }
}
