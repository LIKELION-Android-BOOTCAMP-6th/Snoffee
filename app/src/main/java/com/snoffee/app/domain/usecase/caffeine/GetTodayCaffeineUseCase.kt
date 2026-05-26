package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

// 오늘 카페인 섭취 목록 조회 UseCase
// ViewModel에서 호출, CaffeineRepository를 통해 조회
class GetTodayCaffeineUseCase @Inject constructor(
    private val repository: CaffeineRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<List<CaffeineRecord>> {
        return repository.getCaffeineRecordsByDateRange(
            startTimeMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endTimeMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli() + 24 * 60 * 60 * 1000 - 1
        )
    }

    // 월 범위 조회 추가
    operator fun invoke(yearMonth: YearMonth): Flow<List<CaffeineRecord>> {
        val startOfMonth = yearMonth.atDay(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = yearMonth.atEndOfMonth()
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
            .toEpochMilli() + 24 * 60 * 60 * 1000 - 1
        return repository.getCaffeineRecordsByDateRange(startOfMonth, endOfMonth)
    }
}