package com.snoffee.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object Utils {
    // LocalTime -> 밀리초(Long) 타임스탬프 변환
    fun LocalTime.toTodayEpochMilli(): Long {
        return LocalDateTime.now()
            .with(this)
            .atZone(ZoneId.systemDefault())
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