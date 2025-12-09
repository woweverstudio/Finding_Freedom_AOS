package com.woweverstudio.exit_aos.presentation.ui.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woweverstudio.exit_aos.domain.model.UserProfile
import com.woweverstudio.exit_aos.presentation.ui.theme.*
import com.woweverstudio.exit_aos.util.ExitNumberFormatter
import androidx.compose.foundation.Canvas as ComposeCanvas

/**
 * 몬테카를로 시뮬레이션 소개 및 구매 유도 화면
 * - iOS의 SimulationEmptyView.swift와 99% 일치
 */
@Composable
fun SimulationEmptyView(
    userProfile: UserProfile?,
    currentAssetAmount: Double,
    onStart: () -> Unit,
    isPurchased: Boolean = false,
    displayPrice: String = "₩4,900",
    errorMessage: String? = null,
    isPurchasing: Boolean = false,
    onPurchase: () -> Unit = {},
    onRestore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(ExitColors.Background)
            .padding(top = ExitSpacing.LG)
    ) {
        // Hero 섹션
        HeroSection()
        
        Spacer(modifier = Modifier.height(ExitSpacing.XXL))
        
        // 왜 필요한가? 섹션
        WhyNeedSection()
        
        Spacer(modifier = Modifier.height(ExitSpacing.XXL))
        
        // 어떻게 작동하나? 섹션
        HowItWorksSection()
        
        Spacer(modifier = Modifier.height(ExitSpacing.XXL))
        
        // 무엇을 알 수 있는가? 섹션
        WhatYouGetSection()
        
        Spacer(modifier = Modifier.height(ExitSpacing.XXL))
        
        // 데모 카드들 (별도 파일로 분리)
        SimulationDemoCards()
        
        Spacer(modifier = Modifier.height(ExitSpacing.XXL))
        
        // 가격 및 가치 제안
        ValuePropositionSection(
            isPurchased = isPurchased,
            displayPrice = displayPrice,
            errorMessage = errorMessage,
            isPurchasing = isPurchasing,
            onStart = onStart,
            onPurchase = onPurchase,
            onRestore = onRestore
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.XXL))
    }
}

// MARK: - Hero Section

@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ExitSpacing.MD),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프리미엄 아이콘
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radial gradient background
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ExitColors.Accent.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            radius = 240f
                        )
                    )
            )
            
            // Center circle with icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ExitColors.CardBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = ExitColors.Accent
                )
            }
        }
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        // Premium badge
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.width(ExitSpacing.XS))
            
            Text(
                text = "프리미엄 기능",
                style = ExitTypography.Caption,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.width(ExitSpacing.XS))
            
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFFFFD700)
            )
        }
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        Text(
            text = "몬테카를로 시뮬레이션",
            style = ExitTypography.Title.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = ExitColors.PrimaryText
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.SM))
        
        Text(
            text = "30,000가지 미래를 만들어\n당신의 은퇴 계획을 분석해드려요.",
            style = ExitTypography.Body,
            color = ExitColors.SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// MARK: - Why Need Section

@Composable
private fun WhyNeedSection() {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD)
    ) {
        SectionHeader(
            icon = Icons.Default.Lightbulb,
            title = "왜 이 시뮬레이션이 필요할까요?"
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.LG),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            ProblemCard(
                emoji = "🤔",
                title = "단순 계산의 함정",
                description = "\"매년 7% 수익이면 10년 후 2억!\" 이런 계산 많이 보셨죠? 하지만 현실은 달라요."
            )
            
            // 시각적 비교
            ComparisonView()
            
            ProblemCard(
                emoji = "📉",
                title = "실제 주식 시장은?",
                description = "어떤 해는 +30%, 어떤 해는 -20%... 들쭉날쭉해요. 평균 7%라도 매년 7%가 아니에요!"
            )
            
            ProblemCard(
                emoji = "🎯",
                title = "그래서 확률이 중요해요",
                description = "\"10년 후에 정확히 2억\"이 아니라 \"10년 후에 2억 달성할 확률 87%\"처럼 현실적으로 알려드려요."
            )
        }
    }
}

@Composable
private fun ProblemCard(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        Text(
            text = emoji,
            fontSize = 32.sp
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            Text(
                text = title,
                style = ExitTypography.Subheadline,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.PrimaryText
            )
            
            Text(
                text = description,
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
        }
    }
}

@Composable
private fun ComparisonView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ExitSpacing.SM),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 단순 계산
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Text(
                text = "단순 계산",
                style = ExitTypography.Caption2,
                color = ExitColors.SecondaryText
            )
            
            // 직선 그래프
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 60.dp)
                    .clip(RoundedCornerShape(ExitRadius.SM))
                    .background(ExitColors.SecondaryCardBackground)
            ) {
                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    val padding = 12f
                    val startX = padding
                    val endX = size.width - padding
                    val startY = size.height - padding
                    val endY = padding
                    
                    val path = Path().apply {
                        moveTo(startX, startY)
                        lineTo(endX, endY)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFA0A0A0),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }
            
            Text(
                text = "매년 똑같이 오름",
                style = ExitTypography.Caption2,
                color = ExitColors.SecondaryText
            )
        }
        
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ExitColors.Accent
        )
        
        // 시뮬레이션
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
        ) {
            Text(
                text = "실제 시장",
                style = ExitTypography.Caption2,
                color = ExitColors.Accent
            )
            
            // 변동성 그래프
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 60.dp)
                    .clip(RoundedCornerShape(ExitRadius.SM))
                    .background(ExitColors.Accent.copy(alpha = 0.1f))
            ) {
                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    val padding = 12f
                    val w = size.width
                    val h = size.height
                    
                    // 시작점: 왼쪽 아래에서 시작해서 오르락내리락하며 오른쪽 위로
                    val path = Path().apply {
                        moveTo(padding, h * 0.7f)
                        cubicTo(
                            w * 0.3f, h * 0.9f,   // 첫번째 제어점 (아래로)
                            w * 0.5f, h * 0.1f,   // 두번째 제어점 (위로)
                            w - padding, h * 0.25f // 끝점 (오른쪽 위)
                        )
                    }
                    drawPath(
                        path = path,
                        color = ExitColors.Accent,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }
            
            Text(
                text = "오르락내리락",
                style = ExitTypography.Caption2,
                color = ExitColors.Accent
            )
        }
    }
}

// MARK: - How It Works Section

@Composable
private fun HowItWorksSection() {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD)
    ) {
        SectionHeader(
            icon = Icons.Default.Settings,
            title = "어떻게 작동하나요?"
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.LG),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XL)
        ) {
            // 1. 난수 생성 원리
            Column(verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)) {
                StepHeader(number = "1", title = "컴퓨터가 무작위 숫자를 만들어요")
                
                Text(
                    text = "주식 시장의 수익률은 예측할 수 없어요. 올해 +20%일 수도 있고, 내년에 -15%일 수도 있죠.",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                Text(
                    text = "그래서 컴퓨터가 \"난수(무작위 숫자)\"를 이용해서 매년 수익률을 무작위로 정해요. 마치 주사위를 굴리는 것처럼요!",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                RandomNumberVisualization()
            }
            
            // 2. 30,000번 반복
            Column(verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)) {
                StepHeader(number = "2", title = "이걸 30,000번 반복해요")
                
                Text(
                    text = "한 번만 시뮬레이션하면 우연히 좋은 결과나 나쁜 결과가 나올 수 있어요.",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                Text(
                    text = "그래서 30,000번이나 반복해요! 그러면 \"대부분의 경우\"와 \"특별히 운이 좋거나 나쁜 경우\"를 모두 볼 수 있어요.",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                RepetitionVisualization()
            }
            
            // 3. 결과 정렬
            Column(verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)) {
                StepHeader(number = "3", title = "결과를 순서대로 줄 세워요")
                
                Text(
                    text = "30,000개의 결과를 \"목표 달성이 빠른 순서\"로 정렬해요.",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                Text(
                    text = "학교에서 시험 점수로 등수를 매기는 것처럼, 30,000개 결과에 1등부터 30,000등까지 순위를 매겨요.",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                SortingVisualization()
            }
            
            // 4. 대표 시나리오 선택
            Column(verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)) {
                StepHeader(number = "4", title = "대표 결과 3개를 보여드려요")
                
                Text(
                    text = "30,000개 전부 보여드리면 너무 많으니까, 대표적인 3개만 골라서 보여드려요:",
                    style = ExitTypography.Caption,
                    color = ExitColors.SecondaryText
                )
                
                PercentileExplanation()
            }
            
            // 5. 결론
            Column(verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)) {
                StepHeader(number = "5", title = "이렇게 하면 뭐가 좋아요?")
                
                BulletPoint(text = "\"딱 10년 후에 2억!\" 같은 확정적인 예측은 거의 틀려요")
                BulletPoint(text = "대신 \"빠르면 10년, 보통 12년, 늦으면 14년\"처럼 범위로 알려드려요")
                BulletPoint(text = "운이 좋을 때와 나쁠 때 모두 대비할 수 있어요!")
            }
        }
    }
}

@Composable
private fun StepHeader(number: String, title: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(ExitColors.Accent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Text(
            text = title,
            style = ExitTypography.Subheadline,
            fontWeight = FontWeight.SemiBold,
            color = ExitColors.PrimaryText
        )
    }
}

@Composable
private fun RandomNumberVisualization() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 주사위
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ExitColors.SecondaryCardBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = ExitColors.Accent
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = ExitColors.TertiaryText
            )
            
            // 난수
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ExitColors.SecondaryCardBackground)
                    .padding(horizontal = ExitSpacing.SM, vertical = ExitSpacing.XS),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "난수",
                    style = ExitTypography.Caption2,
                    color = ExitColors.TertiaryText
                )
                Text(
                    text = "0.7234",
                    style = ExitTypography.Caption.copy(fontWeight = FontWeight.Bold),
                    color = ExitColors.Accent
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = ExitColors.TertiaryText
            )
            
            // 수익률
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ExitColors.Positive.copy(alpha = 0.1f))
                    .padding(horizontal = ExitSpacing.SM, vertical = ExitSpacing.XS),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "수익률",
                    style = ExitTypography.Caption2,
                    color = ExitColors.TertiaryText
                )
                Text(
                    text = "+12.3%",
                    style = ExitTypography.Caption.copy(fontWeight = FontWeight.Bold),
                    color = ExitColors.Positive
                )
            }
        }
        
        Text(
            text = "이렇게 매년 수익률을 무작위로 정해서 10년, 20년 후 자산을 예측해요.",
            style = ExitTypography.Caption2,
            color = ExitColors.TertiaryText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RepetitionVisualization() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { index ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "#$index",
                        style = ExitTypography.Caption2,
                        color = ExitColors.TertiaryText
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ExitColors.Accent.copy(alpha = 0.3f + index * 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = ExitColors.Accent
                        )
                    }
                }
            }
            
            Text(
                text = "...",
                style = ExitTypography.Body,
                color = ExitColors.TertiaryText
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "#30000",
                    style = ExitTypography.Caption2,
                    color = ExitColors.TertiaryText
                )
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ExitColors.Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }
        
        Text(
            text = "각각의 시뮬레이션이 \"만약 이렇게 되면?\"이라는 하나의 미래예요",
            style = ExitTypography.Caption2,
            color = ExitColors.TertiaryText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SortingVisualization() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ExitSpacing.XS),
            verticalAlignment = Alignment.Bottom
        ) {
            (0..9).forEach { index ->
                val height = (40 - index * 3).dp
                val color = when {
                    index < 3 -> ExitColors.Positive
                    index < 7 -> ExitColors.Accent
                    else -> ExitColors.Caution
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = when (index) {
                            0 -> "1등"
                            4 -> "중간"
                            9 -> "꼴등"
                            else -> ""
                        },
                        style = ExitTypography.Caption2.copy(fontSize = 8.sp),
                        color = when (index) {
                            0 -> ExitColors.Positive
                            4 -> ExitColors.Accent
                            9 -> ExitColors.Caution
                            else -> Color.Transparent
                        }
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(height)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
            
            Text(
                text = "...",
                style = ExitTypography.Caption,
                color = ExitColors.TertiaryText,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🏆 빨리 달성",
                style = ExitTypography.Caption2,
                color = ExitColors.Positive
            )
            
            Text(
                text = "⏰ 늦게 달성",
                style = ExitTypography.Caption2,
                color = ExitColors.Caution
            )
        }
    }
}

@Composable
private fun PercentileExplanation() {
    Column(
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        PercentileRow(
            emoji = "🍀",
            title = "행운 (상위 10%)",
            subtitle = "3,000등",
            description = "30,000개 결과 중 3,000등의 결과예요.\n\"운이 좋은 케이스에요.\"",
            color = ExitColors.Positive
        )
        
        PercentileRow(
            emoji = "📊",
            title = "평균 (50%)",
            subtitle = "15,000등",
            description = "정확히 중간인 15,000등의 결과예요.\n\"가장 가능성 높은, 평범한 경우예요.\"",
            color = ExitColors.Accent
        )
        
        PercentileRow(
            emoji = "🌧️",
            title = "불행 (하위 10%)",
            subtitle = "27,000등",
            description = "30,000개 결과 중 27,000등의 결과예요.\n\"운이 정말 나쁜 케이스예요.\"",
            color = ExitColors.Caution
        )
    }
}

@Composable
private fun PercentileRow(
    emoji: String,
    title: String,
    subtitle: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.MD))
            .background(color.copy(alpha = 0.1f))
            .padding(ExitSpacing.SM),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            Text(
                text = title,
                style = ExitTypography.Caption,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = description,
                style = ExitTypography.Caption2,
                color = ExitColors.SecondaryText
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = ExitColors.Accent
        )
        
        Text(
            text = text,
            style = ExitTypography.Caption,
            color = ExitColors.SecondaryText
        )
    }
}

// MARK: - What You Get Section

@Composable
private fun WhatYouGetSection() {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD)
    ) {
        SectionHeader(
            icon = Icons.Default.CardGiftcard,
            title = "무엇을 알 수 있나요?"
        )
        
        Spacer(modifier = Modifier.height(ExitSpacing.LG))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            FeatureCard(
                icon = Icons.Default.Percent,
                iconColor = ExitColors.Accent,
                title = "은퇴 계획 성공 확률",
                description = "\"78% 확률로 목표 달성!\" 처럼 정확한 확률을 알려드려요."
            )
            
            FeatureCard(
                icon = Icons.Default.ShowChart,
                iconColor = ExitColors.Positive,
                title = "자산 변화 예측",
                description = "행운/평균/불행 3가지 시나리오로 시각화해요."
            )
            
            FeatureCard(
                icon = Icons.Default.GpsFixed,
                iconColor = Color(0xFFFF9500),
                title = "목표 달성 시점 분포",
                description = "가장 가능성 높은 달성 시점을 알려드려요."
            )
            
            FeatureCard(
                icon = Icons.Default.Event,
                iconColor = Color(0xFFFF6B6B),
                title = "은퇴 초반 10년 분석",
                description = "가장 중요한 처음 10년의 시장 리스크를 분석해요."
            )
            
            FeatureCard(
                icon = Icons.Default.HourglassEmpty,
                iconColor = Color(0xFFFFD700),
                title = "은퇴 후 40년 예측",
                description = "장기적인 자산 변화와 소진 가능성을 예측해요."
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExitRadius.LG))
            .background(ExitColors.CardBackground)
            .padding(ExitSpacing.MD),
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(ExitRadius.MD))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconColor
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.XS)
        ) {
            Text(
                text = title,
                style = ExitTypography.Subheadline,
                fontWeight = FontWeight.SemiBold,
                color = ExitColors.PrimaryText
            )
            
            Text(
                text = description,
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText
            )
        }
    }
}

// MARK: - Value Proposition Section

@Composable
private fun ValuePropositionSection(
    isPurchased: Boolean,
    displayPrice: String,
    errorMessage: String?,
    isPurchasing: Boolean,
    onStart: () -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = ExitSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.LG)
    ) {
        // 신뢰도 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ExitRadius.LG))
                .background(ExitColors.CardBackground)
                .padding(ExitSpacing.LG),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ExitSpacing.MD)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ExitColors.Accent
                )
                
                Text(
                    text = "금융공학에서 검증된 방법론",
                    style = ExitTypography.Subheadline,
                    fontWeight = FontWeight.SemiBold,
                    color = ExitColors.PrimaryText
                )
            }
            
            Text(
                text = "몬테카를로 시뮬레이션은 월스트리트 투자은행, 연기금 등에서 실제로 사용하는 분석 기법이에요. 복잡한 금융공학을 누구나 쉽게 사용할 수 있도록 만들었어요.",
                style = ExitTypography.Caption,
                color = ExitColors.SecondaryText,
                textAlign = TextAlign.Start
            )
        }
        
        // 플로팅 구매 버튼
        FloatingPurchaseButton(
            isPurchased = isPurchased,
            displayPrice = displayPrice,
            errorMessage = errorMessage,
            isPurchasing = isPurchasing,
            onStart = onStart,
            onPurchase = onPurchase,
            onRestore = onRestore
        )
    }
}

@Composable
private fun FloatingPurchaseButton(
    isPurchased: Boolean,
    displayPrice: String,
    errorMessage: String?,
    isPurchasing: Boolean,
    onStart: () -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ExitSpacing.SM)
    ) {
        Button(
            onClick = {
                if (isPurchased) {
                    onStart()
                } else {
                    onPurchase()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isPurchasing,
            colors = ButtonDefaults.buttonColors(
                containerColor = ExitColors.Accent,
                contentColor = Color.White,
                disabledContainerColor = ExitColors.DisabledBackground,
                disabledContentColor = ExitColors.TertiaryText
            ),
            shape = RoundedCornerShape(ExitRadius.MD)
        ) {
            if (isPurchasing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(ExitSpacing.SM))
                Text(
                    text = "구매 중...",
                    style = ExitTypography.Body,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (isPurchased) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(ExitSpacing.SM))
                Text(
                    text = "시뮬레이션 시작",
                    style = ExitTypography.Body,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(ExitSpacing.XS))
                Text(
                    text = "프리미엄 구매 • $displayPrice",
                    style = ExitTypography.Body,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // 복원 버튼 또는 안내 텍스트
        if (!isPurchased) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ExitSpacing.MD),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "한 번 구매로 평생 & 무한 사용",
                    style = ExitTypography.Caption2,
                    color = ExitColors.PrimaryText
                )
                
                TextButton(onClick = onRestore) {
                    Text(
                        text = "이전 구매 복원",
                        style = ExitTypography.Caption2,
                        color = ExitColors.Accent
                    )
                }
            }
        } else {
            Text(
                text = "약 3~10초 소요됩니다",
                style = ExitTypography.Caption2,
                color = ExitColors.SecondaryText
            )
        }
        
        // 에러 메시지
        errorMessage?.let { error ->
            Text(
                text = error,
                style = ExitTypography.Caption2,
                color = ExitColors.Warning,
                textAlign = TextAlign.Center
            )
        }
    }
}

// MARK: - Helper Components

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ExitSpacing.SM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = ExitColors.Accent
        )
        
        Text(
            text = title,
            style = ExitTypography.Title3,
            fontWeight = FontWeight.Bold,
            color = ExitColors.PrimaryText
        )
    }
}

@Composable
internal fun DemoBadge() {
    Text(
        text = "예시",
        style = ExitTypography.Caption2,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFFFFD700),
        modifier = Modifier
            .clip(RoundedCornerShape(ExitRadius.Full))
            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
            .padding(horizontal = ExitSpacing.SM, vertical = ExitSpacing.XS)
    )
}

