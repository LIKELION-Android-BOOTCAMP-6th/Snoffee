package com.snoffee.app.presentation.caffeine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.DrinkItem

// 더미 음료 데이터
private val dummyDrinks = listOf(
    DrinkItem("001", "아메리카노", "커피", "스타벅스", 125.0, 355.0, 125.0, 355.0),
    DrinkItem("002", "라떼", "커피", "스타벅스", 75.0, 355.0, 75.0, 355.0),
    DrinkItem("003", "에스프레소", "커피", "스타벅스", 63.0, 30.0, 63.0, 30.0),
    DrinkItem("004", "콜드브루", "커피", "스타벅스", 155.0, 355.0, 155.0, 355.0),
)

@Composable
fun CaffeineInputScreen(
    viewModel: CaffeineInputViewModel = hiltViewModel()
) {
    val saveState by viewModel.saveState.collectAsState()
    var selectedDrink by remember { mutableStateOf<DrinkItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("음료 선택", style = MaterialTheme.typography.titleMedium)

        /// 음료 목록
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(dummyDrinks) { drink ->
                DrinkItemCard(
                    drink = drink,
                    isSelected = selectedDrink == drink,
                    onClick = {
                        selectedDrink = drink
                        viewModel.resetSaveState() // 상태 초기화
                    }
                )
            }
        }

        // 선택된 음료 표시
        selectedDrink?.let {
            Text("선택된 음료: ${it.name} (${it.totalCaffeine}mg)")
        }

        // 저장 상태 메시지
        when (saveState) {
            is SaveState.Success -> Text("저장 성공!")
            is SaveState.Error -> Text((saveState as SaveState.Error).message)
            is SaveState.Loading -> CircularProgressIndicator()
            else -> {}
        }

        // 저장 버튼
        Button(
            onClick = {
                selectedDrink?.let { drink ->
                    viewModel.saveCaffeineRecord(
                        CaffeineRecord(
                            drinkId = drink.foodId,
                            drinkName = drink.name,
                            brandName = drink.brand,
                            intakeSize = drink.servingSize,
                            intakeCaffeine = drink.totalCaffeine,
                            consumedAt = System.currentTimeMillis()
                        )
                    )
                }
            },
            enabled = selectedDrink != null && saveState !is SaveState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장")
        }
    }
}

@Composable
fun DrinkItemCard(
    drink: DrinkItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(drink.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${drink.brand} | ${drink.totalCaffeine}mg",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}