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
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.presentation.sleep.SleepDialog

@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
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

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SnoffeeBgBase)
                .padding(innerPadding)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isDbEmpty) {
                    ReportEmptyView(
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