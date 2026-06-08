package com.snoffee.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.data.datasource.preference.OnboardingPreferenceDataSource
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.snoffee.app.domain.usecase.onboarding.ValidateDecimalInputUseCase
import com.snoffee.app.domain.usecase.onboarding.ValidateOnboardingInputUseCase
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
    HEALTH_CONNECT_INFO,
    PERSONAL_INFO,
    COMPLETE
}

enum class CaffeineSensitivityOption(
    val title: String,
    val domainDomainSensitivity: CaffeineSensitivity
) {
    LOW("둔감함", CaffeineSensitivity.LOW),
    NORMAL("보통", CaffeineSensitivity.NORMAL),
    SENSITIVE("민감함", CaffeineSensitivity.SENSITIVE)
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.INTRO,
    val height: String = "",
    val weight: String = "",
    val caffeineSensitivity: CaffeineSensitivityOption = CaffeineSensitivityOption.NORMAL,
    val isHeightValid: Boolean = false,
    val isWeightValid: Boolean = false,
    val isPersonalInfoValid: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferenceDataSource: OnboardingPreferenceDataSource,
    private val userProfileRepository: UserProfileRepository,
    private val validateOnboardingInputUseCase: ValidateOnboardingInputUseCase,
    private val validateDecimalInputUseCase: ValidateDecimalInputUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    fun moveToNextStep() {
        _uiState.update {
            it.copy(
                currentStep = when (it.currentStep) {
                    OnboardingStep.INTRO -> OnboardingStep.PERMISSION
                    OnboardingStep.PERMISSION -> OnboardingStep.HEALTH_CONNECT_INFO
                    OnboardingStep.HEALTH_CONNECT_INFO -> OnboardingStep.PERSONAL_INFO
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
                    OnboardingStep.HEALTH_CONNECT_INFO -> OnboardingStep.PERMISSION
                    OnboardingStep.PERSONAL_INFO -> OnboardingStep.HEALTH_CONNECT_INFO
                    OnboardingStep.COMPLETE -> OnboardingStep.PERSONAL_INFO
                }
            )
        }
    }

    fun updateHeight(value: String) {
        if (!validateDecimalInputUseCase(value)) return
        _uiState.update {
            it.copy(height = value)
        }
        validatePersonalInfo()
    }

    fun updateWeight(value: String) {
        if (!validateDecimalInputUseCase(value)) return
        _uiState.update {
            it.copy(weight = value)
        }
        validatePersonalInfo()
    }

    private fun validatePersonalInfo() {
        val state = _uiState.value
        val result = validateOnboardingInputUseCase(
            height = state.height,
            weight = state.weight
        )
        _uiState.update {
            it.copy(
                isHeightValid = result.isHeightValid,
                isWeightValid = result.isWeightValid,
                isPersonalInfoValid = result.isPersonalInfoValid
            )
        }
    }

    fun updateCaffeineSensitivity(value: CaffeineSensitivityOption) {
        _uiState.update { it.copy(caffeineSensitivity = value) }
    }

    fun completeOnboarding(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value

            val result = completeOnboardingUseCase(
                height = state.height,
                weight = state.weight,
                sensitivity = state.caffeineSensitivity.domainDomainSensitivity
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isCompleted = true)
                }
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }
}