package com.snoffee.app.presentation.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material3.Icon // ◀ 중복 한정자 제거를 위해 단독 임포트 추가
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeSurfaceOverlay
import com.snoffee.app.core.ui.theme.SnoffeeTextDisabled
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain

@Composable
fun ReportEmptyView(
    onRecordClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(SnoffeeSurfaceOverlay, shape = RoundedCornerShape(90.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assessment,
                    contentDescription = "리포트 플레이스홀더",
                    modifier = Modifier.size(48.dp),
                    tint = SnoffeeTextDisabled
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "데이터가 아직 없습니다",
                color = SnoffeeTextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 가이드 서브 텍스트
            Text(
                text = "수면을 기록해 보세요.",
                color = SnoffeeTextHint,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 수면 기록하기 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SnoffeePrimaryDark)
                    .clickable {
                        onRecordClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "수면 기록하기",
                    color = SnoffeeSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}