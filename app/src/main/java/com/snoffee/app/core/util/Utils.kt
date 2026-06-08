package com.snoffee.app.core.util

import android.util.Log
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Utils {
    // Long(Epoch Milli) -> LocalDateTime 변환 함수
    fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    // LocalDateTime -> Long(Epoch Milli) 변환 함수
    fun LocalDateTime.toEpochMilli(): Long {
        Log.d("LocalDateTime", "LocalDateTime $this")
        return this.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // LocalDate와 시간을 결합하여 타임스탬프로 변환
    fun combineToEpochMilli(date: LocalDate, time: LocalTime): Long {
        return date.atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // Long(EpochMilli) -> LocalTime 변환
    fun Long.toLocalTime(): LocalTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
    }

    // 날짜 포맷
    // "yyyy년 M월" (예: 2026년 6월)
    private val MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월")

    // "M월 d일" (예: 6월 8일)
    private val DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("M월 d일")

    // YearMonth -> "yyyy년 M월" 형식의 라벨 변환
    fun YearMonth.toMonthLabel(): String = this.format(MONTH_LABEL_FORMATTER)

    // LocalDate -> "M월 d일" 형식의 라벨 변환
    fun LocalDate.toDayLabel(): String = this.format(DAY_LABEL_FORMATTER)
}