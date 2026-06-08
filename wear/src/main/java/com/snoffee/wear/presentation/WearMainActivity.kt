package com.snoffee.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.snoffee.wear.data.WearDataClient
import com.snoffee.wear.presentation.caffeine.CaffeineInputScreen
import com.snoffee.wear.presentation.caffeine.CaffeineViewModel
import com.snoffee.wear.presentation.home.HomeScreen
import com.snoffee.wear.presentation.setting.SettingScreen
import com.snoffee.wear.presentation.setting.SettingViewModel
import com.snoffee.wear.presentation.theme.WearSnoffeeTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class WearMainActivity : ComponentActivity() {
    @Inject
    lateinit var wearDataClient: WearDataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearSnoffeeTheme {
                MainPagerScreen(wearDataClient)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainPagerScreen(
    wearDataClient: WearDataClient,
    homeViewModel: com.snoffee.wear.presentation.home.HomeViewModel = hiltViewModel(),
    caffeineViewModel: CaffeineViewModel = hiltViewModel(),
    settingViewModel: SettingViewModel = hiltViewModel()
) {
    var currentScreenIndex by remember { mutableIntStateOf(1) }

    val colors = MaterialTheme.colors

    BackHandler(enabled = currentScreenIndex != 1) {
        currentScreenIndex = 1
    }
    val homeUiState by homeViewModel.uiState.collectAsState()
    val isPhoneConnected by homeViewModel.isPhoneConnected.collectAsState()
    val connectionError by wearDataClient.connectionError.collectAsState()
    val recentDrinks by caffeineViewModel.drinkList.collectAsState()
    val drinkListForUI = recentDrinks.map { it.name to it.amount }
    val isEmulatorTestMode = true

    //전환 모션
    AnimatedContent(
        targetState = currentScreenIndex,
        transitionSpec = {
            // 화면 인덱스가 커지면 오른쪽에서 왼쪽으로, 작아지면 왼쪽에서 오른쪽으로 슬라이딩
            if (targetState > initialState) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeOut()
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeOut()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { targetIndex ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -50f) { // 왼쪽으로 드래그
                            if (currentScreenIndex == 1) currentScreenIndex = 2
                            else if (currentScreenIndex == 0) currentScreenIndex = 1
                        } else if (dragAmount > 50f) { // 오른쪽으로 드래그
                            if (currentScreenIndex == 1) currentScreenIndex = 0
                            else if (currentScreenIndex == 2) currentScreenIndex = 1
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when (targetIndex) {
                0 -> CaffeineInputScreen(
                    drinkList = drinkListForUI,
                    onDrinkSelected = { name, amount ->
                        caffeineViewModel.addCaffeineRecord(name, amount)
                        currentScreenIndex = 1
                    },
                    onBack = { currentScreenIndex = 1 }
                )

                1 -> HomeScreen(
                    uiState = homeUiState,
                    isPhoneConnected = if (isEmulatorTestMode) true else isPhoneConnected,
                    connectionError = if (isEmulatorTestMode) false else !connectionError.isNullOrEmpty(),
                    onAddCaffeineClick = { currentScreenIndex = 0 },
                    onRetryClick = { if (!isEmulatorTestMode) wearDataClient.checkPhoneCapability() }
                )

                2 -> SettingScreen(viewModel = settingViewModel) // 설정 화면 연결
            }
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = MaterialTheme.colors.onBackground)
    }
}