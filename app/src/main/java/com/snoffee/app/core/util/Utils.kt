package com.snoffee.app.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object Utils {
    // LocalTime -> 밀리초(Long) 타임스탬프 변환 작업
    fun LocalTime.toTodayEpochMilli(): Long {
        return LocalDateTime.now()
            .with(this)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // LocalDate)와 시간을 결합하여 타임스탬프로 변환한다. (확장성 고려)
    fun combineToEpochMilli(date: LocalDate, time: LocalTime): Long {
        return date.atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}