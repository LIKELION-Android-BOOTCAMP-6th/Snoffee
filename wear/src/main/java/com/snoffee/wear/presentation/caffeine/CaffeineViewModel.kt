package com.snoffee.wear.presentation.caffeine

import androidx.lifecycle.ViewModel
import com.snoffee.wear.data.WearDataClient
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class CaffeineViewModel @Inject constructor(
    private val wearDataClient: WearDataClient
) : ViewModel() {
    // 데이터 클라이언트로부터 리스트 전달
    val drinkList = wearDataClient.recentDrinks
}