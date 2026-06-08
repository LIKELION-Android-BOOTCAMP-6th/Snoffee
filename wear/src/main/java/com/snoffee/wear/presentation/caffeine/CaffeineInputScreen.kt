package com.snoffee.wear.presentation.caffeine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Stepper
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CaffeineInputScreen(
    modifier: Modifier = Modifier,
    drinkList: List<Pair<String, Double>>,
    onDrinkSelected: (String, Double) -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography

    BackHandler { if (mode == 1) mode = 0 else onBack() }

    Box(modifier = modifier
        .fillMaxSize()
        .background(colors.background)) {

        Text(
            text = if (mode == 0) "음료 선택" else "직접 입력",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            style = typography.title3.copy(color = colors.primary)
        )

        if (mode == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 45.dp) // [수정] padding 중첩 방지
                    .padding(bottom = 10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (drinkList.isEmpty()) {
                    Text("최근에 마신 음료가\n없습니다.", textAlign = TextAlign.Center, fontSize = 15.sp)
                } else {
                    drinkList.forEach { drink ->
                        Card(
                            onClick = { onDrinkSelected(drink.first, drink.second) },
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(40.dp)
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "${drink.first} (${drink.second.toInt()}mg)",
                                style = typography.caption3,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { mode = 1 },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(40.dp),
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("직접 추가 +", style = typography.caption1)
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        } else {
            var amount by remember { mutableFloatStateOf(100f) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${amount.toInt()} mg", style = typography.body1)

                // [수정: Stepper 파라미터 및 아이콘 구현]
                Stepper(
                    value = amount,
                    onValueChange = { amount = it },
                    valueRange = 0f..500f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth(),
                    decreaseIcon = { Text("-") },
                    increaseIcon = { Text("+") }
                ) {
                    Text("${amount.toInt()} mg")
                }

                Button(
                    onClick = {
                        scope.launch {
                            delay(200L)
                            onDrinkSelected("직접입력", amount.toDouble())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(32.dp)
                ) {
                    Text("추가 완료", style = typography.caption1)
                }
            }
        }
    }
}