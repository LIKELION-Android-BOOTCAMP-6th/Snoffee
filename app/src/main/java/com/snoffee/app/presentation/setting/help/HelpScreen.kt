package com.snoffee.app.presentation.setting.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted

@Composable
fun HelpScreen(
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "뒤로",
                    Modifier.size(28.dp),
                    tint = SnoffeeTextMain
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "건강데이터 및 기기연동",
                color = SnoffeeTextMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp)
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
                        text = "헬스 커넥트로 수면 기록 불러오기",
                        color = SnoffeeTextMain,
                        fontSize = 30.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            lineBreak = LineBreak.Heading
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "삼성 헬스와 연동하는 경우, 다음과 같이 설정해주세요",
                        color = SnoffeeTextMuted,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(28.dp))
                            .background(SnoffeeSurface, RoundedCornerShape(28.dp))
                            .padding(22.dp)
                    ) {
                        Text(
                            text = "삼성 헬스 > 설정 > 헬스 커넥트 순서대로 진입해 주세요.",
                            color = SnoffeeTextMuted,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Image(
                            painter = painterResource(R.drawable.onboarding_health_info),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "건강 앱에서 Snoffee를 선택한 후 권한을 모두 허용으로 전환해주세요.",
                            color = SnoffeeTextMuted,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Image(
                            painter = painterResource(R.drawable.onboarding_health_permission),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Snoffee에서 요청하는 필수 권한에 동의하면 수면 정보를 가져올 수 있습니다.",
                            color = SnoffeeTextMuted,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Image(
                            painter = painterResource(R.drawable.onboarding_health_permission2),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(16.dp))
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
                                text = "헬스 커넥트를 연결한 시점부터 수면 데이터 수집이 시작됩니다. 연결 전 기록은 가져올 수 없습니다.",
                                color = SnoffeeTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}