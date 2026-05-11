package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.util.CaffeineCalculator
import javax.inject.Inject

// 현재 체내 카페인 잔류량 계산 UseCase
// CaffeineCalculator를 통해 반감기 기반 잔류량 계산
// HomeViewModel에서 5분 주기로 호출
class CalculateResidualUseCase @Inject constructor(
    private val caffeineRepository: CaffeineRepository,     // Hilt가 자동 주입
    private val userProfileRepository: UserProfileRepository, // Hilt가 자동 주입
    private val calculator: CaffeineCalculator              // Hilt가 자동 주입
) {
    suspend operator fun invoke(): Double {
        // TODO: 오늘 섭취 기록 + 사용자 프로필 기반으로 잔류량 계산
        return 0.0
    }
}