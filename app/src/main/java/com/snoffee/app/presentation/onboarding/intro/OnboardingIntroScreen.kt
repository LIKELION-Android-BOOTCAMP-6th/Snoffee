package com.snoffee.app.presentation.onboarding.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeBgWarm
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeTheme

@Composable
fun OnboardingIntroScreen(
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgWarm)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Snoffee",
            color = SnoffeePrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "정밀한 카페인 관리로 시작하는 스마트한 일상",
            color = SnoffeeTextMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(28.dp))
                .background(SnoffeeSurface, RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Text(
                text = "카페인과 수면을 함께 관리해요",
                color = SnoffeeTextMain,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Snoffee는 카페인 섭취 기록과 수면 데이터를 기반으로 나에게 맞는 리포트를 제공합니다.",
                color = SnoffeeTextMuted,
                fontSize = 14.sp
            )

        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = SnoffeePrimary
            )
        ) {
            Text(
                text = "시작하기  →",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingIntroScreenPreview() {
    SnoffeeTheme {
        OnboardingIntroScreen(onNextClick = {})
    }
}