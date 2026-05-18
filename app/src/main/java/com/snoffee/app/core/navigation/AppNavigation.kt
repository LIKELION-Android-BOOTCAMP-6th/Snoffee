package com.snoffee.app.core.navigation
sealed class Screen(val route: String) {
    // 온보딩
    object Onboarding : Screen("onboarding")

    // 홈
    object Home : Screen("home")

    // 카페인
    object Caffeine : Screen("caffeine")
    object CaffeineInput : Screen("caffeine_input")

    // 수면
    object Sleep : Screen("sleep")

    // 리포트
    object Report : Screen("report")

    // 마이 설정
    object MySetting : Screen("mySetting")
}