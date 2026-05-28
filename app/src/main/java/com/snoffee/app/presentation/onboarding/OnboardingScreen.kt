package com.snoffee.app.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.snoffee.app.presentation.onboarding.complete.OnboardingCompleteScreen
import com.snoffee.app.presentation.onboarding.health.PersonalInfoSetupScreen
import com.snoffee.app.presentation.onboarding.intro.OnboardingIntroScreen
import com.snoffee.app.presentation.onboarding.permission.OnboardingPermissionScreen

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onFinishOnboarding()
        }
    }

    when (uiState.currentStep) {
        OnboardingStep.INTRO -> {
            OnboardingIntroScreen(
                onNextClick = viewModel::moveToNextStep
            )
        }

        OnboardingStep.PERMISSION -> {
            OnboardingPermissionScreen(
                onNextClick = viewModel::moveToNextStep,
                onBackClick = viewModel::moveToPreviousStep
            )
        }

        OnboardingStep.PERSONAL_INFO -> {
            PersonalInfoSetupScreen(
                height = uiState.height,
                weight = uiState.weight,
                onHeightChange = viewModel::updateHeight,
                onWeightChange = viewModel::updateWeight,
                selectedSensitivity = uiState.caffeineSensitivity,
                onSensitivityClick = viewModel::updateCaffeineSensitivity,
                onNextClick = viewModel::moveToNextStep,
                onBackClick = viewModel::moveToPreviousStep,
                isHeightValid = uiState.isHeightValid,
                isWeightValid = uiState.isWeightValid,
                isNextEnabled = uiState.isPersonalInfoValid
            )
        }

        OnboardingStep.COMPLETE -> {
            OnboardingCompleteScreen(
                onStartClick = { /* LauchedEffect가 이벤트를 catch */ },
                viewModel = viewModel
            )
        }
    }
}