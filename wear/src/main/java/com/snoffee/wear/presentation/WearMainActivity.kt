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
import androidx.wear.compose.material.Text
import com.snoffee.wear.data.WearDataClient
import com.snoffee.wear.presentation.caffeine.CaffeineInputScreen
import com.snoffee.wear.presentation.caffeine.CaffeineViewModel
import com.snoffee.wear.presentation.theme.WearBackground
import com.snoffee.wear.presentation.theme.WearSnoffeeTheme
import com.snoffee.wear.presentation.theme.WearTextMain
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
    viewModel: CaffeineViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var currentScreenIndex by remember { mutableIntStateOf(1) }

    BackHandler(enabled = currentScreenIndex != 1) {
        currentScreenIndex = 1
    }
    val recentDrinks by viewModel.drinkList.collectAsState()
    val isPhoneConnected by wearDataClient.isPhoneConnected.collectAsState()
    val connectionError by wearDataClient.connectionError.collectAsState()
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
                .background(WearBackground)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -50f && currentScreenIndex == 1) {
                            currentScreenIndex = 2
                        } else if (dragAmount > 50f && currentScreenIndex != 1) {
                            currentScreenIndex = 1
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when (targetIndex) {
                0 -> CaffeineInputScreen(
                    drinkList = recentDrinks,
                    onDrinkSelected = { name, amount ->
                        println("음료: $name, 함량: $amount mg 추가")
                        currentScreenIndex = 1
                    },
                    onBack = {
                        currentScreenIndex = 1
                    },
                    modifier = Modifier.fillMaxSize()
                )

                1 -> com.snoffee.wear.presentation.home.HomeScreen(
                    uiState = com.snoffee.wear.presentation.home.WatchHomeUiState(
                        residualCaffeineMg = 55.0,
                        riskLevel = com.snoffee.wear.presentation.home.CaffeineRiskLevel.CAUTION
                    ),
                    isPhoneConnected = if (isEmulatorTestMode) true else isPhoneConnected,
                    connectionError = if (isEmulatorTestMode) false else !connectionError.isNullOrEmpty(),
                    onAddCaffeineClick = { currentScreenIndex = 0 },
                    onRetryClick = { if (!isEmulatorTestMode) wearDataClient.checkPhoneCapability() },
                    modifier = Modifier.fillMaxSize()
                )

                2 -> DummyScreen(title = "설정창 화면")
            }
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = WearTextMain)
    }
}