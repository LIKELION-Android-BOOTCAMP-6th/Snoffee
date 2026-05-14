package com.snoffee.app.core.navigation
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Caffeine : Screen("caffeine")
    object Sleep : Screen("sleep")
    object Report : Screen("report")
    object MySetting : Screen("mySetting")
}