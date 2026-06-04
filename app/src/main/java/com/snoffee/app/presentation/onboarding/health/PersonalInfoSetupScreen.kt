package com.snoffee.app.presentation.onboarding.health

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeError
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeSurfaceOverlay
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.presentation.onboarding.CaffeineSensitivityOption

@Composable
fun PersonalInfoSetupScreen(
    height: String,
    weight: String,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    selectedSensitivity: CaffeineSensitivityOption,
    onSensitivityClick: (CaffeineSensitivityOption) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    isHeightValid: Boolean,
    isWeightValid: Boolean,
    isNextEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        ProgressSection()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "당신에 대해 알려주세요",
            color = SnoffeeTextMain,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "정확한 카페인 분해 속도 계산을 위해 필요합니다.",
            color = SnoffeeTextMuted,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            val isHeightError = height.isNotBlank() && !isHeightValid
            InfoInputCard(
                title = "신장",
                value = height,
                unit = "cm",
                onValueChange = onHeightChange,
                isError = isHeightError,
                errorMessage = "100~300cm 입력 가능",
                modifier = Modifier.weight(1f)
            )

            val isWeightError = weight.isNotBlank() && !isWeightValid
            InfoInputCard(
                title = "체중",
                value = weight,
                unit = "kg",
                onValueChange = onWeightChange,
                isError = isWeightError,
                errorMessage = "1~400kg 입력 가능",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        CaffeineSensitivityCard(
            selectedSensitivity = selectedSensitivity,
            onSensitivityClick = onSensitivityClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNextClick,
            enabled = isNextEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SnoffeePrimary
            )
        ) {
            Text(
                text = "다음 단계로 이동  →",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProgressSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "단계 2 / 3",
                color = SnoffeePrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "거의 다 왔어요!",
                color = SnoffeeTextMuted,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(SnoffeeSurfaceOverlay, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.66f)
                    .fillMaxHeight()
                    .background(SnoffeePrimary, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun InfoInputCard(
    title: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .background(SnoffeeSurface, RoundedCornerShape(16.dp))
            .border(
                width = if (isError) 1.5.dp else 0.dp,
                color = if (isError) SnoffeeError else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = if (isError) SnoffeeError else SnoffeePrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("0", color = SnoffeeTextMuted)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                isError = isError
            )

            Text(
                text = unit,
                color = if (isError) SnoffeeError else SnoffeeTextMuted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 11.dp)
            )
        }
        if (isError) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = SnoffeeError,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp),
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun CaffeineSensitivityCard(
    selectedSensitivity: CaffeineSensitivityOption,
    onSensitivityClick: (CaffeineSensitivityOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .background(SnoffeeSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Bolt,
                contentDescription = null,
                tint = SnoffeePrimary
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "카페인 민감도",
                color = SnoffeeTextMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "커피를 마셨을 때 몸이 어떻게 반응하나요?",
            color = SnoffeeTextMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SnoffeeSurfaceOverlay, RoundedCornerShape(16.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SensitivityChip(
                text = "둔감함",
                option = CaffeineSensitivityOption.LOW,
                selected = selectedSensitivity == CaffeineSensitivityOption.LOW,
                onClick = onSensitivityClick,
                modifier = Modifier.weight(1f)
            )

            SensitivityChip(
                text = "보통",
                option = CaffeineSensitivityOption.NORMAL,
                selected = selectedSensitivity == CaffeineSensitivityOption.NORMAL,
                onClick = onSensitivityClick,
                modifier = Modifier.weight(1f)
            )

            SensitivityChip(
                text = "민감함",
                option = CaffeineSensitivityOption.SENSITIVE,
                selected = selectedSensitivity == CaffeineSensitivityOption.SENSITIVE,
                onClick = onSensitivityClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SnoffeePrimarySubtle.copy(alpha = 0.45f),
                    RoundedCornerShape(10.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ⓘ",
                color = SnoffeePrimary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "민감도는 수면 패턴과 심박수 데이터를 통해 나중에 더 정확하게 조정됩니다.",
                color = SnoffeeTextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SensitivityChip(
    text: String,
    option: CaffeineSensitivityOption,
    selected: Boolean,
    onClick: (CaffeineSensitivityOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                if (selected) SnoffeePrimary else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable {
                onClick(option)
            }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else SnoffeeTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}