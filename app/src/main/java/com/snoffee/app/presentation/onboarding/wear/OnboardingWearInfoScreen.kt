package com.snoffee.app.presentation.onboarding.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.presentation.onboarding.component.ProgressSection

@Composable
fun OnboardingWearInfoScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left),
                        contentDescription = "뒤로가기",
                        tint = SnoffeeTextMain
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ProgressSection("단계 4 / 5", 0.77f)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "워치와 함께 사용하기",
                            color = SnoffeeTextMain,
                            fontSize = 25.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                lineBreak = LineBreak.Heading
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "워치에서 카페인 잔량을 확인하고, 카페인을 추가하거나 알림을 받을 수 있습니다.\n" +
                                    "이 기능을 사용하려면 폰 앱과 워치가 연결되어 있어야 합니다.",
                            color = SnoffeeTextMuted,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(18.dp, RoundedCornerShape(28.dp))
                                .background(SnoffeeSurface, RoundedCornerShape(28.dp))
                                .padding(22.dp)
                        ) {
                            Text(
                                text = "연결 조건",
                                color = SnoffeeTextMain,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            WearConditionItem(
                                index = "1",
                                text = "폰과 워치의 블루투스가 모두 켜져 있어야 합니다."
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            WearConditionItem(
                                index = "2",
                                text = "폰과 워치가 같은 Wi-Fi에 접속되어 있어야 합니다."
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            WearConditionItem(
                                index = "3",
                                text = "워치 앱을 새로 설치하거나 워치를 재부팅한 뒤에는 폰 앱과 다시 연결해야 합니다."
                            )

                            Spacer(modifier = Modifier.height(20.dp))

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
                                    text = "워치가 없어도 괜찮습니다. 폰만으로도 모든 기능을 사용할 수 있습니다.",
                                    color = SnoffeeTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onNextClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SnoffeePrimary
            )
        ) {
            Text(
                text = "다음으로",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun WearConditionItem(
    index: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(SnoffeePrimarySubtle, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index,
                color = SnoffeePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = SnoffeeTextMuted,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingWearInfoScreenPreview() {
    OnboardingWearInfoScreen(
        onNextClick = {},
        onBackClick = {}
    )
}