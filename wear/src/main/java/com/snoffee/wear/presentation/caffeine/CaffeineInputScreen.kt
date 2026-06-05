package com.snoffee.wear.presentation.caffeine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CaffeineInputScreen(
    modifier: Modifier = Modifier,
    drinkList: List<String> = emptyList(),
    onDrinkSelected: (String, Float) -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()

    // 화면 이탈 시 비동기 작업 취소
    DisposableEffect(Unit) {
        onDispose { isProcessing = false }
    }

    BackHandler {
        if (mode == 1) mode = 0 else onBack()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 타이틀 상단 고정
        Text(
            text = if (mode == 0) "음료 선택" else "직접 입력",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colors.primary
        )

        if (mode == 0) {
            if (drinkList.isEmpty()) {
                // 빈 데이터 시 물리적 중앙 정렬
                Text(
                    text = "최근에 마신 음료가\n없습니다.",
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // 리스트 데이터 있을 시 타이틀 아래부터 배치
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 45.dp) // 타이틀 영역 확보
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    drinkList.forEach { drink ->
                        Card(
                            onClick = {
                                val currentTime = System.currentTimeMillis()
                                if (!isProcessing && currentTime - lastClickTime > 3000L) {
                                    lastClickTime = currentTime
                                    isProcessing = true
                                    scope.launch { delay(1000L); onDrinkSelected(drink, 100f) }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(40.dp)
                                .padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    drink,
                                    style = MaterialTheme.typography.caption3,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Card(
                        onClick = { mode = 1 },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(40.dp)
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "직접 추가하기",
                                style = MaterialTheme.typography.caption3,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } else {
            // 입력 모드 (중앙 정렬)
            var amount by remember { mutableFloatStateOf(100f) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("${amount.toInt()} mg", style = MaterialTheme.typography.body1)
                Slider(
                    value = amount,
                    onValueChange = { amount = it },
                    valueRange = 0f..500f,
                    modifier = Modifier.height(30.dp),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colors.primary)
                )
                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            scope.launch { delay(1000L); onDrinkSelected("직접입력", amount) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(32.dp)
                ) {
                    Text("추가 완료", style = MaterialTheme.typography.caption1)
                }
            }
        }

        // 전송 애니메이션 (전체 중앙)
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
            )
        }
    }
}