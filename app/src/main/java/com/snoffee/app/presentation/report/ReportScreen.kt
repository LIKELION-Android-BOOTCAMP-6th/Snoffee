package com.snoffee.app.presentation.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.component.SnoffeeAppBar
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted

@Composable
fun ReportScreen() {
    //기본 활성화 탭 = 기간
    var selectedTab by remember { mutableStateOf("기간") }
    val tabs = listOf("기간", "일간", "주간", "월간", "추이")

    Scaffold(
        topBar = {
            // 코어 공통 앱바 컴포넌트 활용
            SnoffeeAppBar(title = "리포트")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SnoffeeBgBase)
                .padding(innerPadding)
        ) {
            // ── 상단 세그먼트 필터 (Filter Chip Row) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SnoffeePrimary else SnoffeeSurface)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) SnoffeeSurface else SnoffeeTextMuted,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // ── 분석 기간 단위별 서브 컨텐츠 렌더링 영역 ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    "기간" -> ReportEmptyView()  // 최초 진입 시 데이터가 없을 때의 Empty 상태 배치
                    "일간" -> DailyReportView()   // 전 단계에서 만든 일간 기록 뷰
                    "주간" -> WeeklyReportView()  // 다음 단계로 구현할 주간 분석 뷰
                    "월간" -> MonthlyReportView()  // 월간 대조 분석 뷰
                    "추이" -> TrendReportView()   // 장기 추이 분석 뷰
                }
            }
        }
    }
}