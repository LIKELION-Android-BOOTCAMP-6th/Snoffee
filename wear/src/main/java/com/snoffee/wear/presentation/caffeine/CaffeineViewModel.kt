package com.snoffee.wear.presentation.caffeine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.wear.domain.model.CaffeineRecord // 이전 단계에서 생성한 모델 참조
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaffeineViewModel @Inject constructor(
    // private val caffeineRepository: CaffeineRepository (필요시 주입)
) : ViewModel() {

    // 화면에 보여줄 음료 리스트 상태
    private val _drinkList = MutableStateFlow<List<CaffeineRecord>>(emptyList())
    val drinkList: StateFlow<List<CaffeineRecord>> = _drinkList.asStateFlow()

    init {
        loadDrinks()
    }

    private fun loadDrinks() {
        viewModelScope.launch {
            // 레포지토리로부터 데이터를 불러dhha
            // 예시 데이터를 설정합니다.
            _drinkList.value = listOf(
                CaffeineRecord("아메리카노", 150.0, System.currentTimeMillis()),
                CaffeineRecord("라떼", 120.0, System.currentTimeMillis()),
                CaffeineRecord("에너지드링크", 80.0, System.currentTimeMillis())
            )
        }
    }

    fun addCaffeineRecord(name: String, amount: Double) {
        viewModelScope.launch {
            // 음료 추가 로직 (데이터 저장 및 통신)
            println("뷰모델: $name, $amount mg 저장 완료")

            // 필요하다면 리스트 업데이트
            val newList = _drinkList.value.toMutableList()
            newList.add(CaffeineRecord(name, amount, System.currentTimeMillis()))
            _drinkList.value = newList
        }
    }
}