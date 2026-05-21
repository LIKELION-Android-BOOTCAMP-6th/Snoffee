package com.snoffee.app.presentation.home.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snoffee.app.presentation.home.CaffeineRiskLevel
import kotlin.math.roundToInt

@Composable
fun CaffeineGauge(
    residualCaffeineMg: Double,
    riskLevel: CaffeineRiskLevel,
    modifier: Modifier = Modifier
) {
    val maxCaffeineMg = 300f

    val progress by animateFloatAsState(
        targetValue = (residualCaffeineMg.toFloat() / maxCaffeineMg).coerceIn(0f, 1f),
        label = "caffeineGaugeProgress"
    )

    val gaugeColor = when (riskLevel) {
        CaffeineRiskLevel.SAFE -> Color(0xFF5C4B51)
        CaffeineRiskLevel.CAUTION -> Color(0xFFD9A441)
        CaffeineRiskLevel.DANGER -> Color(0xFFD96C6C)
    }

    val riskText = when (riskLevel) {
        CaffeineRiskLevel.SAFE -> "안전"
        CaffeineRiskLevel.CAUTION -> "주의"
        CaffeineRiskLevel.DANGER -> "위험"
    }

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(180.dp),
            strokeWidth = 12.dp,
            color = gaugeColor,
            trackColor = Color(0xFFE8E1DC)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${residualCaffeineMg.roundToInt()}mg",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF2E2A2A)
            )

            Text(
                text = riskText,
                style = MaterialTheme.typography.bodyMedium,
                color = gaugeColor
            )
        }
    }
}