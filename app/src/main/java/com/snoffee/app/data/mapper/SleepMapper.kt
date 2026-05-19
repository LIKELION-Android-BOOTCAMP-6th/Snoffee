package com.snoffee.app.data.mapper

import com.snoffee.app.data.local.entity.SleepEntity
import com.snoffee.app.data.model.SleepDataDto
import com.snoffee.app.domain.model.SleepData
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject


// SleepDataDto ↔ SleepData 변환
class SleepMapper @Inject constructor() {
    fun toEntity(domain: SleepData): SleepEntity {
        return SleepEntity(
            id = domain.id,
            date = domain.date,
            sleepStart = domain.sleepStart,
            sleepEnd = domain.sleepEnd,
            deepSleepRatio = domain.deepSleepRatio,
            source = domain.source
        )
    }

    fun toDomain(entity: SleepEntity): SleepData {
        return SleepData(
            id = entity.id,
            date = entity.date,
            sleepStart = entity.sleepStart,
            sleepEnd = entity.sleepEnd,
            deepSleepRatio = entity.deepSleepRatio,
            source = entity.source
        )
    }

    fun toDto(domain: SleepData): SleepDataDto {
        return SleepDataDto(
            date = convertMillisToLocalDateTime(domain.date),
            sleepStart = domain.sleepStart,
            sleepEnd = domain.sleepEnd,
            deepSleepRatio = domain.deepSleepRatio
        )
    }

    fun toDomain(dto: SleepDataDto): SleepData {
        return SleepData(
            date = convertLocalDateTimeToMillis(dto.date),
            sleepStart = dto.sleepStart,
            sleepEnd = dto.sleepEnd,
            deepSleepRatio = dto.deepSleepRatio,
            source = "health"
        )
    }

    private fun convertMillisToLocalDateTime(timeMillis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timeMillis),
            ZoneId.systemDefault()
        )
    }

    private fun convertLocalDateTimeToMillis(dateTime: LocalDateTime): Long {
        return dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}