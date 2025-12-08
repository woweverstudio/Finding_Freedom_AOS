package com.woweverstudio.exit_aos.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.woweverstudio.exit_aos.presentation.ui.components.ExitPrimaryButton
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitColors
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitRadius
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitSpacing
import com.woweverstudio.exit_aos.presentation.ui.theme.ExitTypography

/**
 * 환영 화면
 */
@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ExitColors.Background)
    ) {
        // 배경 장식 효과
        BackgroundEffects()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ExitSpacing.LG),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // 타이틀 섹션
            TitleSection()
            
            Spacer(modifier = Modifier.height(ExitSpacing.XXL))
            
            // 메시지 섹션
            MessagesSection()
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 시작 버튼
            ExitPrimaryButton(
                text = "회사 탈출 계획 시작하기",
                onClick = onStartClick,
                modifier = Modifier.padding(bottom = ExitSpacing.XXL)
            )
        }
    }
}

@Composable
private fun BackgroundEffects() {
    // 상단 그라데이션 원
    Box(
        modifier = Modifier
            .size(400.dp)
            .offset(x = 100.dp, y = (-150).dp)
            .blur(60.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ExitColors.Accent.copy(alpha = 0.15f),
                        ExitColors.Accent.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
    
    // 하단 그라데이션 원
    Box(
        modifier = Modifier
            .size(300.dp)
            .offset(x = (-120).dp, y = 300.dp)
            .blur(40.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ExitColors.Accent.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
private fun TitleSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로고 아이콘 (텍스트로 대체)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(ExitRadius.XL))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ExitColors.Accent, ExitColors.AccentSecondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Exit",
                style = ExitTypography.Title2,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        Text(
            text = "자유를 찾아서",
            style = ExitTypography.Title,
            color = ExitColors.Accent
        )
        
        Text(
            text = "에 오신 것을 환영합니다!",
            style = ExitTypography.Title2,
            color = ExitColors.PrimaryText
        )
    }
}

@Composable
private fun MessagesSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "회사생활 지긋지긋 하시죠?",
            style = ExitTypography.Title3,
            color = ExitColors.PrimaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.XL))
        
        Text(
            text = "꿈만같던 은퇴,",
            style = ExitTypography.Body,
            color = ExitColors.SecondaryText
        )
        
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = ExitColors.Accent, fontWeight = FontWeight.Bold)) {
                    append("자유를 찾아서")
                }
                withStyle(style = SpanStyle(color = ExitColors.SecondaryText)) {
                    append("와 함께라면 가능합니다.")
                }
            },
            style = ExitTypography.Body,
            textAlign = TextAlign.Center
        )
    }
}

