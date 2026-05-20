package com.snoffee.app.presentation.caffeine.input.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.DrinkItem
import com.snoffee.app.domain.usecase.caffeine.SaveCaffeineUseCase
import com.snoffee.app.domain.usecase.drink.SearchDrinkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CaffeineSearchViewModel @Inject constructor(
    private val searchDrinkUseCase: SearchDrinkUseCase,
    private val saveCaffeineUseCase: SaveCaffeineUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaffeineSearchUiState())
    val uiState: StateFlow<CaffeineSearchUiState> = _uiState.asStateFlow()

    // 음료 검색에 관한 페이징 처리
    private var currentPage = 0
    private var isLastPage = false
    private val pageSize = 10

    init {
        // ViewModel 생성 시 데이터가 있는지 확인
        ensureDataLoaded()
    }

    private fun ensureDataLoaded() {
        viewModelScope.launch {
            // repository 내부 로직을 통해 데이터가 없으면 자동 동기화됨
            // 호출만으로도 초기화 로직이 실행되도록 설계되어 있음
            searchDrinkUseCase(query = "", page = 0, pageSize = 1)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),    // 이전 결과 삭제
                isLastPage = false
            )
        }
    }

    // 스크롤 끝 도달 시 호출 → 다음 페이지 로드
    fun loadNextPage() {
        if (isLastPage || _uiState.value.isPagingLoading) return
        searchDrinks(query = _uiState.value.searchQuery, isNewSearch = false)
    }

    fun onSearchClick(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(showErrorToast = true) }
            // TODO: Toast "검색어를 입력해주세요"
            Log.d("DEBUG", "검색어를 입력해주세요")
            return
        }
        searchDrinks(query = query, isNewSearch = true)
    }

    fun onToastShown() {
        _uiState.update { it.copy(showErrorToast = false) } // 상태 초기화
    }

    // 검색한 음료 선택
    fun onDrinkSelected(drink: DrinkItem) {
        _uiState.update { it.copy(selectedDrink = drink) }
    }

    // 음료 검색 searching
    private fun searchDrinks(query: String, isNewSearch: Boolean) {
        viewModelScope.launch {
            if (isNewSearch) {
                currentPage = 0
                isLastPage = false
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        isPagingLoading = false,
                        searchResults = emptyList()
                    )
                }
            } else {
                _uiState.update { it.copy(isPagingLoading = true) }
            }

            try {
                Log.d("SearchDebug", "Query: $query, Page: $currentPage")
                val result = searchDrinkUseCase(
                    query = query,
                    page = currentPage,
                    pageSize = pageSize,
                )
                Log.d("SearchDebug", "Result size: ${result.size}")
                // 결과가 pageSize보다 적으면 마지막 페이지
                if (result.size < pageSize) isLastPage = true

                val updatedList = if (isNewSearch) result
                else _uiState.value.searchResults + result

                currentPage++

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPagingLoading = false,
                        searchResults = updatedList,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPagingLoading = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    // 사용자가 직접 입력한 음료 or Firestore 검색으로 선택한 음료 저장
    fun saveCaffeineRecord(record: CaffeineRecord) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                saveCaffeineUseCase(record)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
