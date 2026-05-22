package com.snoffee.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.data.datasource.preference.OnboardingPreferenceDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    INTRO,
    PERMISSION,
    PERSONAL_INFO,
    COMPLETE
}

enum class CaffeineSensitivityOption(
    val title: String
) {
    SENSITIVE("민감함"),
    NORMAL("보통"),
    LOW("민감하지 않음")
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.INTRO,
    val height: String = "",
    val weight: String = "",
    val caffeineSensitivity: CaffeineSensitivityOption = CaffeineSensitivityOption.NORMAL,
    val isCompleted: Boolean = false
) {
    val isPersonalInfoValid: Boolean
        get() = height.isNotBlank() && weight.isNotBlank()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferenceDataSource: OnboardingPreferenceDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun moveToNextStep() {
        _uiState.update {
            it.copy(
                currentStep = when (it.currentStep) {
                    OnboardingStep.INTRO -> OnboardingStep.PERMISSION
                    OnboardingStep.PERMISSION -> OnboardingStep.PERSONAL_INFO
                    OnboardingStep.PERSONAL_INFO -> OnboardingStep.COMPLETE
                    OnboardingStep.COMPLETE -> OnboardingStep.COMPLETE
                }
            )
        }
    }

    fun moveToPreviousStep() {
        _uiState.update {
            it.copy(
                currentStep = when (it.currentStep) {
                    OnboardingStep.INTRO -> OnboardingStep.INTRO
                    OnboardingStep.PERMISSION -> OnboardingStep.INTRO
                    OnboardingStep.PERSONAL_INFO -> OnboardingStep.PERMISSION
                    OnboardingStep.COMPLETE -> OnboardingStep.PERSONAL_INFO
                }
            )
        }
    }

    fun updateHeight(value: String) {
        _uiState.update { it.copy(height = value) }
    }

    fun updateWeight(value: String) {
        _uiState.update { it.copy(weight = value) }
    }

    fun updateCaffeineSensitivity(value: CaffeineSensitivityOption) {
        _uiState.update { it.copy(caffeineSensitivity = value) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferenceDataSource.setOnboardingCompleted(true)

            _uiState.update {
                it.copy(isCompleted = true)
            }
        }
    }
}