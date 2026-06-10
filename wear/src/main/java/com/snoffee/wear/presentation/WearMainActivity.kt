package com.snoffee.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.MaterialTheme
import com.snoffee.wear.data.WearDataClient
import com.snoffee.wear.presentation.caffeine.CaffeineInputScreen
import com.snoffee.wear.presentation.caffeine.CaffeineViewModel
import com.snoffee.wear.presentation.home.HomeScreen
import com.snoffee.wear.presentation.home.HomeViewModel
import com.snoffee.wear.presentation.setting.SettingScreen
import com.snoffee.wear.presentation.setting.SettingViewModel
import com.snoffee.wear.presentation.theme.WearSnoffeeTheme
import com.snoffee.wear.service.WearNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class WearMainActivity : ComponentActivity() {
    @Inject
    lateinit var wearDataClient: WearDataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WearNotificationHelper.createNotificationChannels(applicationContext)
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
    homeViewModel: HomeViewModel = hiltViewModel(),
    caffeineViewModel: CaffeineViewModel = hiltViewModel(),
    settingViewModel: SettingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var currentScreenIndex by remember { mutableIntStateOf(1) }

    val colors = MaterialTheme.colors

    var isNotificationPermissionGranted by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
    }
    // 화면이 처음 켜질 때 권한이 없다면 팝업
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val checkResult = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            isNotificationPermissionGranted = checkResult

            if (!checkResult) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // API 33 미만 기기는 설치 시 자동 허용이므로 항상 true
            isNotificationPermissionGranted = true
        }
    }
    BackHandler(enabled = currentScreenIndex != 1) {
        currentScreenIndex = 1
    }
    val homeUiState by homeViewModel.uiState.collectAsState()
    val isPhoneConnected by wearDataClient.isPhoneConnected.collectAsState()
    val connectionError by wearDataClient.connectionError.collectAsState()
    val recentDrinks by caffeineViewModel.drinkList.collectAsState()
    val drinkListForUI = recentDrinks.map { it.name to it.amount }
    val isEmulatorTestMode = true

    LaunchedEffect(isPhoneConnected) {
        if (!isPhoneConnected) {
            android.widget.Toast.makeText(context, "연결이 끊어졌습니다.", android.widget.Toast.LENGTH_LONG)
                .show()
        }
    }
    //전환 모션
    AnimatedContent(
        targetState = currentScreenIndex,
        transitionSpec = {
            // 홈 ->  설정 : 오른쪽에서 왼쪽으로 슬라이드
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
                // 설정 -> 홈 : 왼쪽에서 오른쪽으로 슬라이드
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
                        if (dragAmount < -50f) {
                            if (currentScreenIndex == 1) currentScreenIndex = 2
                        } else if (dragAmount > 50f) {
                            if (currentScreenIndex == 2) currentScreenIndex = 1
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when (targetIndex) {
                0 -> CaffeineInputScreen(
                    drinkList = drinkListForUI,
                    isPhoneConnected = isPhoneConnected,
                    onDrinkSelected = { name, amount, consumedAt ->
                        caffeineViewModel.addCaffeineRecord(name, amount, consumedAt)
                        currentScreenIndex = 1
                    },
                    onBack = { currentScreenIndex = 1 }
                )
                1 -> {
                    HomeScreen(
                        uiState = homeUiState,
                        isPhoneConnected = if (isEmulatorTestMode) true else isPhoneConnected,
                        connectionError = if (isEmulatorTestMode) false else !connectionError.isNullOrEmpty(),
                        onAddCaffeineClick = { currentScreenIndex = 0 },
                        onRetryClick = { if (!isEmulatorTestMode) wearDataClient.checkPhoneCapability() }
                    )
                }

                2 -> SettingScreen(viewModel = settingViewModel) // 설정 화면 연결
            }
        }
    }
}