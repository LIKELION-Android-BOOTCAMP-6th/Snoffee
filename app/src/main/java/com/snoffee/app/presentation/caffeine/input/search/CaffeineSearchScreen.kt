package com.snoffee.app.presentation.caffeine.input.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeTheme
import com.snoffee.app.core.util.Utils.toTodayEpochMilli
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.DrinkItem
import com.snoffee.app.presentation.caffeine.component.DrinkListItem
import com.snoffee.app.presentation.caffeine.component.SearchBar
import com.snoffee.app.presentation.caffeine.component.TimePickerBox
import com.snoffee.app.presentation.caffeine.input.dialog.CaffeineInputDialog
import java.time.LocalTime

// Preview용 더미 데이터 (로직 미연결)
val previewDrinkList = listOf(
    DrinkItem(
        foodId = "starbucks_001",
        name = "아이스 카페 아메리카노",
        category = "커피",
        brand = "스타벅스",
        caffeineMg = 150.0,
        servingSize = 355.0,
        totalCaffeine = 150.0,
        totalSize = 355.0
    ),
    DrinkItem(
        foodId = "twosome_002",
        name = "아이스 로얄 밀크티",
        category = "티 라떼",
        brand = "투썸플레이스",
        caffeineMg = 95.0,
        servingSize = 414.0,
        totalCaffeine = 95.0,
        totalSize = 414.0
    ),
    DrinkItem(
        foodId = "mega_003",
        name = "메가리카노",
        category = "커피",
        brand = "메가커피",
        caffeineMg = 290.0,
        servingSize = 500.0,
        totalCaffeine = 580.0,
        totalSize = 1000.0
    ),
    DrinkItem(
        foodId = "gongcha_004",
        name = "블랙 밀크티 + 펄",
        category = "밀크티",
        brand = "공차",
        caffeineMg = 80.0,
        servingSize = 473.0,
        totalCaffeine = 80.0,
        totalSize = 473.0
    ),
    DrinkItem(
        foodId = "paulbassett_005",
        name = "콜드브루",
        category = "커피",
        brand = "폴 바셋",
        caffeineMg = 160.0,
        servingSize = 360.0,
        totalCaffeine = 160.0,
        totalSize = 360.0
    )
)

// 카페인 추가 검색 화면
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaffeineSearchScreen(
    onBack: () -> Unit,
    onConfirmSuccess: () -> Unit,
    onNavigateToDirectInput: () -> Unit,
    viewModel: CaffeineSearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val hasQuery = query.isNotBlank()
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onConfirmSuccess()
    }

    val colorScheme = MaterialTheme.colorScheme
    val extColors = SnoffeeTheme.colors

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.caffeine_drink_save),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.caffeine_drink_back),
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = colorScheme.background,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                    TimePickerBox(
                        selectedTime = selectedTime,
                        onTimeChange = { selectedTime = it },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            uiState.selectedDrink?.let { drink ->
                                viewModel.saveCaffeineRecord(
                                    CaffeineRecord(
                                        id = 0,
                                        drinkId = drink.foodId,
                                        drinkName = drink.name,
                                        brandName = drink.brand,
                                        intakeSize = drink.totalSize,
                                        intakeCaffeine = drink.totalCaffeine,
                                        consumedAt = selectedTime.toTodayEpochMilli()
                                    )
                                )
                            }
                        },
                        enabled = uiState.isRecordEnabled,    // 쿼리가 있을 때만 활성화 등 조건 추가
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            disabledContainerColor = extColors.surfacePressed
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.caffeine_record_button),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(40),
                color = colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { /* 검색 */ },
                    onClear = { query = "" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!hasQuery) {
                EmptySearchState(viewModel, selectedTime, onConfirmSuccess)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.caffeine_drink_search_result_count,
                            previewDrinkList.size
                        ),
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 직접 등록하기 배너
                DirectRegisterBanner(
                    viewModel = viewModel,
                    selectedTime = selectedTime,
                    onConfirmSuccess = onConfirmSuccess,
                    onClick = onNavigateToDirectInput
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 음료 리스트 영역
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(previewDrinkList) { drink ->
                        DrinkListItem(
                            drink = drink,
                            isSelected = uiState.selectedDrink?.foodId == drink.foodId,
                            onClick = { viewModel.onDrinkSelected(drink) }
                        )
                    }
                }
            }
        }
    }
}

// Empty 상태 UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptySearchState(
    viewModel: CaffeineSearchViewModel,
    selectedTime: LocalTime,
    onConfirmSuccess: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extColors = SnoffeeTheme.colors
    var showDialog by remember { mutableStateOf(false) }                    // 다이얼로그 표시 여부

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // 조금 더 둥글게
        color = colorScheme.surface,
        shadowElevation = 3.dp,
        border = BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.caffeine_drink_input_info),
                fontSize = 17.sp,
                color = extColors.textHint
            )

            Text(
                text = stringResource(R.string.caffeine_drink_input_self),
                fontSize = 17.sp,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
            Surface(
                shape = CircleShape,
                color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(60.dp),
                onClick = { showDialog = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "추가하기",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }

            // 상태가 true일 때만 다이얼로그를 화면에 배치
            if (showDialog) {
                Dialog(
                    onDismissRequest = { showDialog = false },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SnoffeeTheme.colors.surfaceElevated)
                    ) {
                        CaffeineInputDialog(
                            onDismiss = { showDialog = false },
                            onConfirm = { record ->
                                // 다이얼로그 직접 입력
                                viewModel.saveCaffeineRecord(
                                    CaffeineRecord(
                                        id = 0,
                                        drinkId = "DIRECT_${System.currentTimeMillis()}",
                                        drinkName = record.drinkName,
                                        brandName = "직접 입력",
                                        intakeSize = 0.0,
                                        intakeCaffeine = record.intakeSize,
                                        consumedAt = selectedTime.toTodayEpochMilli()
                                    )
                                )
                                showDialog = false
                                onConfirmSuccess()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.caffeine_search_drink_info),
                fontSize = 17.sp,
                color = extColors.textDisabled
            )
        }
    }
}

// 직접 등록하기 배너 (검색 결과 상단)
@Composable
private fun DirectRegisterBanner(
    viewModel: CaffeineSearchViewModel,
    selectedTime: LocalTime,
    onConfirmSuccess: () -> Unit,
    onClick: () -> Unit
) { // onClick은 기존 유지
    val colorScheme = MaterialTheme.colorScheme
    val extColors = SnoffeeTheme.colors

    // 다이얼로그 노출 상태 변수 추가
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                showDialog = true
            },
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "원하는 음료가 없으신가요?",
                fontSize = 17.sp,
                color = extColors.textHint
            )
            Text(
                text = "직접 등록하기",
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                style = TextStyle(
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }

    // 상태가 true일 때 다이얼로그 표시
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SnoffeeTheme.colors.surfaceElevated
                )
            ) {
                CaffeineInputDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { record ->
                        viewModel.saveCaffeineRecord(
                            CaffeineRecord(
                                id = 0,
                                drinkId = "DIRECT_${System.currentTimeMillis()}",
                                drinkName = record.drinkName,
                                brandName = "직접 입력",
                                intakeSize = 0.0,
                                intakeCaffeine = record.intakeSize,
                                consumedAt = selectedTime.toTodayEpochMilli()
                            )
                        )
                        showDialog = false
                        onConfirmSuccess()  // 추가
                    }
                )
            }
        }
    }
}
