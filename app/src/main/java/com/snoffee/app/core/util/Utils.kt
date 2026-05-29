package com.snoffee.app.core.util

import android.util.Log
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

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
}