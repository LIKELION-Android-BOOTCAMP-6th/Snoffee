package com.snoffee.app.presentation.onboarding.complete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeSurfaceOverlay
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.presentation.onboarding.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingCompleteScreen(
    onStartClick: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SnoffeePrimarySubtle,
                            SnoffeeBgBase,
                            SnoffeeBgBase
                        )
                    )
                )
        ) {
            Text(
                text = "Snoffee",
                color = SnoffeePrimary.copy(alpha = 0.85f),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(start = 24.dp, top = 48.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 36.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(18.dp, RoundedCornerShape(28.dp))
                        .background(SnoffeeSurface, RoundedCornerShape(28.dp))
                        .padding(22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = SnoffeePrimary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "SETUP COMPLETE",
                            color = SnoffeePrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "설정이 완료되었습니다!",
                        color = SnoffeeTextMain,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "이제 Snoffee와 함께 더 나은 수면을 시작해보세요. 당신의 카페인 섭취를 정밀하게 관리하여 가장 평온한 밤을 선사합니다.",
                        color = SnoffeeTextMuted,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 재시도 이벤트
                    var attemptSave: (() -> Unit)? = null

                    attemptSave = {
                        if (!isSaving) {
                            isSaving = true
                            viewModel.completeOnboarding { success ->
                                isSaving = false
                                if (success) {
                                    onStartClick() // 홈 화면 이동
                                } else {
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "데이터베이스 저장에 실패했습니다.",
                                            actionLabel = "재시도",
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            attemptSave?.invoke() // 재시도
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { attemptSave.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SnoffeePrimary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "시작하기",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompleteMiniCard(
                        icon = Icons.Outlined.Bedtime,
                        title = "수면 분석",
                        modifier = Modifier.weight(1f)
                    )

                    CompleteMiniCard(
                        icon = Icons.Outlined.LocalCafe,
                        title = "카페인 관리",
                        modifier = Modifier.weight(1f)
                    )

                    CompleteMiniCard(
                        icon = Icons.Outlined.Insights,
                        title = "데이터 인사이트",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompleteMiniCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .background(SnoffeeSurfaceOverlay, RoundedCornerShape(18.dp))
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SnoffeeTextMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = SnoffeeTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}