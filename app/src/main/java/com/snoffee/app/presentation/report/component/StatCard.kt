package com.snoffee.app.presentation.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.util.SleepTimeParts

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SnoffeeSurface)
            .padding(16.dp)
    ) {
        Text(label, color = SnoffeeTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom, content = content)
    }
}

@Composable
fun StatValue(value: String, unit: String, numberSize: TextUnit = 24.sp) {
    Text(value, color = SnoffeeTextMain, fontSize = numberSize, fontWeight = FontWeight.Bold)
    Text(unit, color = SnoffeeTextMuted, fontSize = 14.sp)
}

@Composable
fun SleepValue(parts: SleepTimeParts, numberSize: TextUnit = 24.sp) {
    Text(
        parts.hours.toString(),
        color = SnoffeeTextMain,
        fontSize = numberSize,
        fontWeight = FontWeight.Bold
    )
    Text("시간 ", color = SnoffeeTextMuted, fontSize = 14.sp)
    Text(
        "%02d".format(parts.minutes),
        color = SnoffeeTextMain,
        fontSize = numberSize,
        fontWeight = FontWeight.Bold
    )
    Text("분", color = SnoffeeTextMuted, fontSize = 14.sp)
}