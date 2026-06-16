package com.snoffee.wear.presentation.caffeine

import android.app.Activity
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.input.RemoteInputIntentHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CaffeineInputScreen(
    modifier: Modifier = Modifier,
    drinkList: List<Pair<String, Double>>,
    isPhoneConnected: Boolean,
    onDrinkSelected: (String, Double, Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableIntStateOf(0) }

    var amount by remember { mutableFloatStateOf(100f) }

    var isFromDirectInput by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography

    BackHandler {
        when (mode) {
            2 -> mode = if (isFromDirectInput) 1 else 0
            1 -> mode = 0
            else -> onBack()
        }
    }
    var drinkName by remember { mutableStateOf("") }
    val isNameValid = drinkName.isNotEmpty() && drinkName.length <= 10
    val keyboardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val results: Bundle = RemoteInput.getResultsFromIntent(result.data!!)
                ?: return@rememberLauncherForActivityResult
            val inputCharSequence = results.getCharSequence("extra_drink_name")
            if (inputCharSequence != null) {
                val rawInput = inputCharSequence.toString().trim()
                if (rawInput.length > 10) {
                    Toast.makeText(context, "이름은 최대 10자까지 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    drinkName = rawInput
                }
            }
        }
    }
    Box(modifier = modifier
        .fillMaxSize()
        .background(colors.background)) {

        Text(
            text = when (mode) {
                0 -> "음료 선택"
                1 -> "직접 입력"
                else -> "최종 확인"
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            style = typography.title3.copy(color = colors.primary)
        )

        when (mode) {
            0 -> {
                // 기존 리스트 선택 화면
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp)
                        .padding(bottom = 10.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (drinkList.isEmpty()) {
                        Text("최근에 마신 음료가\n없습니다.", textAlign = TextAlign.Center, fontSize = 15.sp)
                    } else {
                        drinkList.forEach { drink ->
                            Card(
                                onClick = {
                                    // 최종 확인(Mode 2)으로 유도
                                    drinkName = drink.first
                                    amount = drink.second.toFloat()
                                    isFromDirectInput = false
                                    mode = 2
                                },
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
                        onClick = {
                            // 직접 추가 진입 시 버퍼 초기화 및 플래그 갱신
                            drinkName = ""
                            amount = 100f
                            mode = 1
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(40.dp),
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) {
                        Text("직접 추가 +", style = typography.caption1)
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            1 -> {
                // 직접 추가하기 화면
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .padding(top = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(34.dp)
                            .background(colors.surface, shape = RoundedCornerShape(18.dp))
                            .clickable {
                                val remoteInputs = listOf(
                                    RemoteInput.Builder("extra_drink_name")
                                        .setLabel("음료 이름 입력 (최대 10자)")
                                        .build()
                                )
                                val intent: Intent =
                                    RemoteInputIntentHelper.createActionRemoteInputIntent()
                                RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                                keyboardLauncher.launch(intent)
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = drinkName.ifEmpty { "음료 이름 입력" },
                            color = if (drinkName.isEmpty()) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface,
                            style = typography.caption2,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (amount >= 10f) amount -= 10f },
                            enabled = amount > 10f,
                            modifier = Modifier
                                .width(36.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.secondaryButtonColors()
                        ) {
                            Text("-", style = typography.body1)
                        }

                        Text(
                            text = "${amount.roundToInt()} mg",
                            style = typography.body2.copy(color = colors.onBackground),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { if (amount <= 490f) amount += 10f },
                            enabled = amount < 400f,
                            modifier = Modifier
                                .width(36.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.secondaryButtonColors()
                        ) {
                            Text("+", style = typography.body1)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (amount < 1f || amount > 400f) {
                                Toast.makeText(
                                    context,
                                    "카페인은 1mg ~ 400mg까지만 입력 가능합니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                isFromDirectInput = true
                                mode = 2
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(30.dp)
                    ) {
                        Text("저장하기", style = typography.caption2)
                    }
                }
            }

            2 -> {
                // 최종 확인 화면
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 34.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = drinkName.ifEmpty { "직접입력" },
                        style = typography.body2.copy(color = colors.onBackground),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "${amount.roundToInt()} mg",
                        style = typography.title2.copy(color = colors.primary),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "이대로 기록할까요?",
                        style = typography.caption2.copy(color = colors.onSurface.copy(alpha = 0.6f)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 취소 버튼
                        Button(
                            onClick = {
                                mode = if (isFromDirectInput) 1 else 0
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            colors = ButtonDefaults.secondaryButtonColors()
                        ) {
                            Text("취소", style = typography.caption2)
                        }

                        // 최종 저장 버튼
                        Button(
                            onClick = {
                                if (!isPhoneConnected) {
                                    Toast.makeText(
                                        context,
                                        "연결이 되어 있지 않습니다. 연결 후 다시 시도해주세요.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onBack()
                                } else {
                                    scope.launch {
                                        delay(100L)
                                        val finalName = drinkName.ifEmpty { "직접입력" }
                                        val consumedAt = System.currentTimeMillis()
                                        onDrinkSelected(finalName, amount.toDouble(), consumedAt)
                                    }
                                }
                            },
                            enabled = (amount in 1f..400f) && isNameValid,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            colors = ButtonDefaults.primaryButtonColors(backgroundColor = colors.primary)
                        ) {
                            Text("확인", style = typography.caption2.copy(color = colors.onPrimary))
                        }
                    }
                }
            }
        }
    }
}