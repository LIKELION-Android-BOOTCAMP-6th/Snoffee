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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeBgWarm
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning
import kotlin.math.roundToInt

data class BarChartItem(
    val label: String,
    val value: Double
)

@Composable
fun InteractiveBarChart(
    data: List<BarChartItem>,
    modifier: Modifier = Modifier,
    valueSuffix: String = "",
    barWidth: Dp = 24.dp,
    barColor: Color = SnoffeeWarning
) {
    var selectedItem by remember {
        mutableStateOf<BarChartItem?>(null)
    }

    var touchOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    val maxValue =
        data.maxOfOrNull { it.value }
            ?.coerceAtLeast(1.0)
            ?: 1.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val ratio =
                    if (item.value <= 0.0) {
                        0f
                    } else {
                        (item.value / maxValue)
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
                        if (ratio > 0f) {
                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .fillMaxHeight(ratio)
                                    .background(
                                        barColor,
                                        RoundedCornerShape(
                                            topStart = 6.dp,
                                            topEnd = 6.dp
                                        )
                                    )
                            )
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
                            x = touchOffset.x.roundToInt() - 40,
                            y = 0
                        )
                    }
                    .background(
                        SnoffeeTextMain,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "${item.label} ${item.value.toInt()}$valueSuffix",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}