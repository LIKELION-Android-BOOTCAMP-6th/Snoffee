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
import com.snoffee.app.core.ui.component.LoadingSpinner
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.presentation.sleep.SleepDialog

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    onAddCaffeineClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isSavingError by viewModel.isSavingError.collectAsStateWithLifecycle()
    val isSaveSuccess by viewModel.isSaveSuccess.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("기간") }
    val tabs = listOf("기간", "일간", "주간", "월간", "추이")

    var showSleepDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadReportData()
    }
    LaunchedEffect(isSaveSuccess) {
        if (isSaveSuccess) {
            showSleepDialog = false
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SnoffeeBgBase)
        ) {
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
                            .clickable {
                                if (selectedTab != tab) {
                                    selectedTab = tab
                                    viewModel.onTabChanged()
                                    viewModel.loadReportData()
                                }
                            }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isLoading) {
                    //스피너 노출 (연속 클릭 깜빡임 방지)
                    LoadingSpinner()
                } else if (uiState.isError) {
                    // 에러 발생 시 에러 화면 표출 및 재시도 로직 바인딩
                    ReportErrorView(
                        message = uiState.errorMessage ?: "일시적인 오류가 발생했습니다.",
                        onRetryClick = { viewModel.loadReportData() }
                    )
                } else if (uiState.isDbEmpty) {
                    //데이터 없음
                    if (selectedTab in listOf("기간", "일간")) {
                        ReportEmptyView(
                            title = "데이터가 아직 없습니다",
                            subtitle = "카페인 섭취와 수면을 기록해 보세요.",
                            onRecordClick = { showSleepDialog = true },
                            onCaffeineClick = onAddCaffeineClick
                        )
                    } else {
                        // 주간/월간/추이 탭인데 데이터가 아예 없는 경우 (기존 유지)
                        ReportEmptyView(
                            onRecordClick = { showSleepDialog = true }
                        )
                    }
                } else {
                    //데이터 존재 & 탭별 조건 확인
                    val isInsufficientData =
                        selectedTab in listOf("주간", "월간", "추이") && uiState.totalSleepDaysCount < 3

                    if (isInsufficientData) {
                        // 주간/월간/추이 수면 데이터가 3일 미만인 경우 엠티 뷰 가이드 노출
                        ReportEmptyView(
                            title = "$selectedTab 분석 정보가 부족합니다",
                            subtitle = "정확한 통계를 위해 수면을 3일 이상 기록해 주세요.",
                            onRecordClick = { showSleepDialog = true }
                        )
                    } else {
                        when (selectedTab) {
                            "기간" -> PeriodReportView(
                                uiState = uiState,
                                onDateRangeChanged = { start, end ->
                                    viewModel.updatePeriodRange(start, end)
                                }
                            )

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

    if (showSleepDialog || isSavingError) {
        SleepDialog(
            onDismiss = {
                showSleepDialog = false
                viewModel.resetErrorState()
            },
            onSave = { record ->
                viewModel.saveSleepRecord(record)
            },
            isSavingError = isSavingError,
            onRetry = {
                viewModel.retrySave()
            },
            initialData = null
        )
    }
}