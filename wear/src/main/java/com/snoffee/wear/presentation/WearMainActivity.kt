package com.snoffee.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.snoffee.wear.data.WearDataClient
import com.snoffee.wear.presentation.caffeine.CaffeineInputScreen
import com.snoffee.wear.presentation.caffeine.CaffeineViewModel
import com.snoffee.wear.presentation.home.HomeScreen
import com.snoffee.wear.presentation.home.HomeViewModel
import com.snoffee.wear.presentation.theme.WearSnoffeeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

@Composable
fun MainPagerScreen(
    wearDataClient: WearDataClient,
    homeViewModel: HomeViewModel = hiltViewModel(),
    caffeineViewModel: CaffeineViewModel = hiltViewModel()
) {
    var currentScreenIndex by remember { mutableIntStateOf(0) }

    val homeUiState by homeViewModel.uiState.collectAsState()
    val isPhoneConnected by wearDataClient.isPhoneConnected.collectAsState()
    val recentDrinks by caffeineViewModel.drinkList.collectAsState()

    // connectionError를 가져옵니다.
    val connectionError by wearDataClient.connectionError.collectAsState()

    AnimatedContent(
        targetState = currentScreenIndex,
        label = "MainPagerAnimation" // label을 추가하여 경고 해결
    ) { targetIndex ->
        when (targetIndex) {
            0 -> HomeScreen(
                uiState = homeUiState,
                isPhoneConnected = isPhoneConnected,
                // connectionError 파라미터 전달 (null이 아니면 true)
                connectionError = connectionError != null,
                onAddCaffeineClick = { currentScreenIndex = 1 },
                onRetryClick = { wearDataClient.checkPhoneCapability() }
            )

            1 -> CaffeineInputScreen(
                drinkList = recentDrinks.map { it.name to it.amount },
                isPhoneConnected = isPhoneConnected,
                onDrinkSelected = { name, amount, consumedAt ->
                    caffeineViewModel.addCaffeineRecord(name, amount, consumedAt)
                    currentScreenIndex = 0
                },
                onBack = { currentScreenIndex = 0 }
            )
        }
    }
}