package com.woweverstudio.exit_aos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        
        // Edge-to-edge 설정 (네비게이션 바 다크 모드 고정)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.parseColor("#0A0A0A"))
        )
        
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ExitColors.CardBackground)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val animatedWeight by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tabWeight"
                )
                
                Box(
                    modifier = Modifier
                        .weight(animatedWeight)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) ExitColors.Accent.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon(),
                            contentDescription = tab.displayName,
                            tint = if (isSelected) ExitColors.Accent else ExitColors.TertiaryText,
                            modifier = Modifier.size(22.dp)
                        )
                        
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Text(
                                text = tab.displayName,
                                style = ExitTypography.Caption,
                                fontWeight = FontWeight.SemiBold,
                                color = ExitColors.Accent,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
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
