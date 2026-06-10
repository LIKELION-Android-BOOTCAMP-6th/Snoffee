package com.snoffee.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.ButtonEnabled
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeBgWarm
import com.snoffee.app.core.ui.theme.SnoffeeError
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextDisabled
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.presentation.home.component.CaffeineGauge
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddCaffeineClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadResidualCaffeine()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            val error = uiState.errorMessage
            when {
                uiState.isLoading -> LoadingState()
                error != null -> ErrorState(error) { viewModel.loadResidualCaffeine() }
                else -> SuccessState(
                    uiState = uiState,
                    onAddCaffeineClick = onAddCaffeineClick
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("최근 기록", style = MaterialTheme.typography.titleLarge, color = SnoffeeTextMain)
                TextButton(onClick = onViewAllClick) {
                    Text("전체보기", color = SnoffeeTextMuted)
                }
            }
        }

        if (uiState.recentLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("오늘 마신 음료 기록이 없습니다.", color = SnoffeeTextDisabled)
                    }
                }
            }
        } else {
            items(uiState.recentLogs.size) { index ->
                val log = uiState.recentLogs[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (log.brandName.isNotEmpty()) {
                                Text(
                                    text = log.brandName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SnoffeeTextMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = log.drinkName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = SnoffeeTextMain
                            )
                        }
                        Text(
                            text = "${log.intakeCaffeine.roundToInt()} mg",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SnoffeePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessState(uiState: HomeUiState, onAddCaffeineClick: () -> Unit) {
    var showCaffeineInfoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 30.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "현재 체내 카페인 잔량",
                    style = MaterialTheme.typography.titleLarge,
                    color = SnoffeeTextMain
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = "카페인 잔량 안내",
                    tint = ButtonEnabled,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showCaffeineInfoDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
            CaffeineGauge(uiState.residualCaffeineMg, uiState.riskLevel)
            Spacer(modifier = Modifier.height(48.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                InfoColumn("잔류 농도", uiState.concentrationLevel)
                InfoColumn("대사 예상 시간", uiState.metabolismTime)
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = onAddCaffeineClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "카페인 추가",
                    fontSize = 17.sp
                )
            }

            if (showCaffeineInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showCaffeineInfoDialog = false },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = null,
                            tint = SnoffeePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "카페인 잔량 안내",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = SnoffeeTextMain
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "표시되는 카페인 잔량은 평균적인 반감기를 기반으로 계산한 추정치입니다. 카페인 대사 속도는 체중, 체질, 임신 여부, 복용 약물 등에 따라 개인차가 큽니다.",
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                color = SnoffeeTextMain
                            )

                            // 잔량 수준 안내 (추가된 부분)
                            Text(
                                text = "일반적으로 체내 잔량이 10mg 이하로 떨어지면 몸에 큰 영향을 주지 않는 수준으로 봅니다.",
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                color = SnoffeeTextMain
                            )

                            // 면책 부분
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SnoffeeBgWarm,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "이 수치는 일반적인 참고용 정보이며 의학적 진단이나 조언을 대체하지 않습니다. 건강 관련 판단은 반드시 의사 등 전문가와 상담하세요.",
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    color = SnoffeeTextMuted,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showCaffeineInfoDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SnoffeePrimary)
                        ) {
                            Text(
                                text = "확인했어요",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = SnoffeeSurface,   // 베이지 대신 깔끔한 흰/표면색
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onAddCaffeineClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("아직 기록이 없어요", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = onAddCaffeineClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("카페인 입력하기") }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = SnoffeePrimary)
    }
}

@Composable
private fun ErrorState(message: String, onRetryClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("오류 발생: $message", color = SnoffeeError)
            Button(onClick = onRetryClick) { Text("다시 시도") }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label, style = MaterialTheme.typography.labelSmall,
            fontSize = 14.sp,
            color = SnoffeeTextHint
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SnoffeeTextMain
        )
    }
}