package com.snoffee.wear.presentation.caffeine

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.wear.data.WearDataClient
import com.snoffee.wear.domain.model.CaffeineRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaffeineViewModel @Inject constructor(
    private val wearDataClient: WearDataClient
) : ViewModel() {

    // 화면에 보여줄 음료 리스트 상태
    private val _drinkList = MutableStateFlow<List<CaffeineRecord>>(emptyList())
    val drinkList: StateFlow<List<CaffeineRecord>> = _drinkList.asStateFlow()

    init {
        loadDrinks()
    }

    private fun loadDrinks() {
        viewModelScope.launch {
            // 레포지토리로부터 데이터를 불러오기 -> 고도화 때 의논 후 추가
            _drinkList.value = listOf(
                CaffeineRecord("아메리카노", 150.0, System.currentTimeMillis()),
                CaffeineRecord("라떼", 120.0, System.currentTimeMillis()),
                CaffeineRecord("에너지드링크", 80.0, System.currentTimeMillis())
            )
        }
    }

    fun addCaffeineRecord(name: String, amount: Double, consumedAt: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wearDataClient.sendCustomCaffeineRecord(name, amount.toInt(), consumedAt)
            } catch (e: Exception) {
                Log.e("CaffeineViewModel", "폰으로 데이터 전송 실패: ${e.message}")
            }
        }
    }
}