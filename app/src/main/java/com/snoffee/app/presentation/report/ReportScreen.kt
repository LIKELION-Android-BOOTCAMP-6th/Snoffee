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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.core.ui.component.SnoffeeAppBar
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted

@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //기본 탭 = 기간
    var selectedTab by remember { mutableStateOf("기간") }
    val tabs = listOf("기간", "일간", "주간", "월간", "추이")

    LaunchedEffect(Unit) {
        viewModel.loadReportData()
    }

    Scaffold(
        topBar = {
            SnoffeeAppBar(title = "리포트")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SnoffeeBgBase)
                .padding(innerPadding)
        ) {
            // ── 상단 필터 ──
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

            // UI State 바인딩 연동
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isDbEmpty) {
                    ReportEmptyView()
                } else {
                    // 기록이 하나라도 존재할 때만 사용자가 선택한 탭별 분석 화면을 정상 노출합니다.
                    when (selectedTab) {
                        "기간" -> DailyReportView(uiState = uiState) // 기간 탭 기본 화면으로 오늘 요약 대용 매핑
                        "일간" -> DailyReportView(uiState = uiState)
                        "주간" -> WeeklyReportView(uiState = uiState)
                        "월간" -> MonthlyReportView(uiState = uiState)
                        "추이" -> TrendReportView(uiState = uiState)
                    }
                }
            }
        }
    }
}