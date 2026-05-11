package com.snoffee.app.presentation.home

import androidx.lifecycle.ViewModel
import com.snoffee.app.domain.usecase.caffeine.CalculateResidualUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 홈 화면 ViewModel
 * @HiltViewModel → Hilt가 ViewModel 생성 및 의존성 주입 관리
 * @Inject constructor → 필요한 UseCase를 Hilt가 자동으로 주입
 * 5분 주기 잔류량 갱신, CalculateResidualUseCase 호출
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateResidualUseCase: CalculateResidualUseCase  // Hilt가 자동 주입
) : ViewModel() {
    // TODO: 잔류량 상태 관리
}