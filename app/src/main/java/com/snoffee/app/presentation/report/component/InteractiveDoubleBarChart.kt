package com.snoffee.app.presentation.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeBgWarm
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning
import kotlin.math.roundToInt

data class DoubleBarChartItem(
    val label: String,
    val caffeineValue: Double,
    val sleepValue: Double
)

@Composable
fun InteractiveDoubleBarChart(
    data: List<DoubleBarChartItem>,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember {
        mutableStateOf<DoubleBarChartItem?>(null)
    }

    var touchOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    val maxCaffeine =
        data.maxOfOrNull { it.caffeineValue }
            ?.coerceAtLeast(1.0)
            ?: 1.0

    val maxSleep =
        data.maxOfOrNull { it.sleepValue }
            ?.coerceAtLeast(1.0)
            ?: 1.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val caffeineRatio =
                    if (item.caffeineValue <= 0.0) {
                        0f
                    } else {
                        (item.caffeineValue / maxCaffeine)
                            .toFloat()
                            .coerceIn(0.08f, 1f)
                    }

                val sleepRatio =
                    if (item.sleepValue <= 0.0) {
                        0f
                    } else {
                        (item.sleepValue / maxSleep)
                            .toFloat()
                            .coerceIn(0.08f, 1f)
                    }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(item) {
                            detectTapGestures(
                                onPress = { offset ->
                                    selectedItem = item
                                    touchOffset = offset

                                    tryAwaitRelease()

                                    selectedItem = null
                                }
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (caffeineRatio > 0f) {
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .fillMaxHeight(caffeineRatio)
                                        .background(
                                            SnoffeeWarning,
                                            RoundedCornerShape(
                                                topStart = 4.dp,
                                                topEnd = 4.dp
                                            )
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            if (sleepRatio > 0f) {
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .fillMaxHeight(sleepRatio)
                                        .background(
                                            SnoffeePrimaryDark,
                                            RoundedCornerShape(
                                                topStart = 4.dp,
                                                topEnd = 4.dp
                                            )
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SnoffeeBgWarm)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.label,
                        color = SnoffeeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        selectedItem?.let { item ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = touchOffset.x.roundToInt() - 56,
                            y = 0
                        )
                    }
                    .background(
                        SnoffeeTextMain,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = item.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "카페인 ${item.caffeineValue.roundToInt()}mg",
                        color = Color.White,
                        fontSize = 11.sp
                    )

                    Text(
                        text = "수면 ${formatSleepHour(item.sleepValue)}",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun formatSleepHour(value: Double): String {
    val hours = value.toInt()
    val minutes = ((value - hours) * 60).roundToInt()

    return "${hours}h ${minutes.toString().padStart(2, '0')}m"
}