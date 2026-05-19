package com.snoffee.app.presentation.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeBgMuted
import com.snoffee.app.core.ui.theme.SnoffeeError
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextDisabled
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    onNotificationSettingClick: () -> Unit = {},
    onLanguageSettingClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onHeightWeightClick: () -> Unit = {},
    onSleepTimeClick: () -> Unit = {},
    onWakeTimeClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var isDarkMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //이름
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "이름 표출?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SnoffeeTextMain
            )
            Text(
                text = "워치 연동 여부 표시?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SnoffeeTextMain
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 키 / 체중
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onHeightWeightClick() },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(title = "신장", value = "168cm", modifier = Modifier.weight(1f))
            InfoCard(title = "체중", value = "62kg", modifier = Modifier.weight(1f))
        }

        //카페인 민감도 지표
        CaffeineSensitivityCard()

        //목표 수면 / 기상 시간 카드
        TimeSettingCard(
            title = "목표 수면 시간",
            time = "22:30",
            iconRes = R.drawable.ic_main_bottombar_sleep,
            onClick = onSleepTimeClick
        )
        TimeSettingCard(
            title = "목표 기상 시간",
            time = "06:30",
            iconRes = R.drawable.ic_setting_light_mode,
            onClick = onWakeTimeClick
        )

        //통합 내부 앱 설정
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "앱 설정",
                fontSize = 13.sp,
                color = SnoffeeTextMuted,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SnoffeeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // 필요시 디자인에 맞춰 조절
            ) {
                Column {
                    MenuRowItem(
                        title = "알림 설정",
                        iconRes = R.drawable.ic_setting_bell,
                        onClick = onNotificationSettingClick
                    )

                    MenuRowItemWithSwitch(
                        title = "다크 모드",
                        iconRes = R.drawable.ic_main_bottombar_sleep, // 다크모드 대응 달 아이콘
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    )

                    MenuRowItem(
                        title = "언어 설정",
                        value = "한국어",
                        iconRes = R.drawable.ic_setting_language,
                        onClick = onLanguageSettingClick
                    )

                    MenuRowItem(
                        title = "도움말",
                        iconRes = R.drawable.ic_setting_question,
                        onClick = onHelpClick
                    )

                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, fontSize = 13.sp, color = SnoffeeTextHint)
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SnoffeePrimaryDark
            )
        }
    }
}

@Composable
fun CaffeineSensitivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "카페인 민감도",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )

                Surface(
                    color = SnoffeePrimarySubtle,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "보통",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SnoffeePrimaryDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            //민감도
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(SnoffeeBgBase, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .background(SnoffeePrimary, RoundedCornerShape(4.dp))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "둔감함", fontSize = 11.sp, color = SnoffeeTextHint)
                Text(text = "보통", fontSize = 11.sp, color = SnoffeeTextHint)
                Text(text = "민감함", fontSize = 11.sp, color = SnoffeeTextHint)
            }
        }
    }
}

@Composable
fun TimeSettingCard(title: String, time: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SnoffeeBgBase, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = SnoffeePrimary
                    )
                }
                Column {
                    Text(text = title, fontSize = 12.sp, color = SnoffeeTextHint)
                    Text(
                        text = time,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SnoffeeTextMain
                    )
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = SnoffeeTextDisabled,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun MenuRowItem(
    title: String,
    iconRes: Int,
    value: String? = null,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isLogout) SnoffeeError else SnoffeeTextMain
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = if (isLogout) SnoffeeError else SnoffeeTextMain,
                fontWeight = if (isLogout) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = SnoffeeTextMuted,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            if (!isLogout) {
                // 수정한 부분: imageVector 시스템 아이콘을 커스텀 화살표 아이콘(ic_arrow_right)으로 일치화
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = SnoffeeTextDisabled,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MenuRowItemWithSwitch(
    title: String,
    iconRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = SnoffeeTextMain
            )
            Text(text = title, fontSize = 14.sp, color = SnoffeeTextMain)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SnoffeePrimary,
                uncheckedThumbColor = SnoffeeTextDisabled,
                uncheckedTrackColor = SnoffeeBgMuted
            )
        )
    }
}