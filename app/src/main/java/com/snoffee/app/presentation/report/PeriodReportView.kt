package com.snoffee.app.presentation.report

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PeriodReportView(
    uiState: ReportUiState,
    onDateRangeChanged: (LocalDate, LocalDate) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val today = LocalDate.now()

    fun showDatePicker(
        currentDate: LocalDate,
        maxDate: LocalDate? = null,
        onDateSelected: (LocalDate) -> Unit
    ) {
        Locale.setDefault(Locale.KOREAN)
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )

        maxDate?.let {
            val zoneId = ZoneId.systemDefault()
            val maxCaliMillis = it.atStartOfDay(zoneId).toInstant().toEpochMilli()
            dialog.datePicker.maxDate = maxCaliMillis
        }
        dialog.show()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 기간 선택
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SnoffeeSurface)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.startDate.format(dateFormatter),
                    color = SnoffeeTextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        showDatePicker(
                            uiState.startDate,
                            maxDate = uiState.endDate
                        ) { newStart ->
                            if (!newStart.isAfter(uiState.endDate) && !newStart.isAfter(today)) {
                                onDateRangeChanged(newStart, uiState.endDate)
                            }
                        }
                    }
                )
                Text(text = "  ~  ", color = SnoffeeTextMuted, fontSize = 15.sp)
                Text(
                    text = uiState.endDate.format(dateFormatter),
                    color = SnoffeeTextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        showDatePicker(
                            uiState.endDate,
                            maxDate = today
                        ) { newEnd ->
                            if (!newEnd.isBefore(uiState.startDate) && !newEnd.isAfter(today)) {
                                onDateRangeChanged(uiState.startDate, newEnd)
                            }
                        }
                    }
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "기간 총 카페인",
                        value = uiState.periodTotalCaffeine.toString(),
                        unit = " mg"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "기간 총 수면",
                        value = uiState.periodTotalSleepTime.split(" ").firstOrNull() ?: "0",
                        unit = " " + (uiState.periodTotalSleepTime.split(" ").getOrNull(1) ?: "")
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "일평균 카페인",
                        value = uiState.periodAvgCaffeine.toString(),
                        unit = " mg"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "일평균 수면시간",
                        value = uiState.periodAvgSleepTime,
                        unit = ""
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SnoffeeSurface)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            color = SnoffeeTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = SnoffeeTextMain,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = unit, color = SnoffeeTextMuted, fontSize = 13.sp)
        }
    }
}