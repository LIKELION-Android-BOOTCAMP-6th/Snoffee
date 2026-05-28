package com.snoffee.app.presentation.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.data.datasource.preference.OnboardingPreferenceDataSource
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.UserProfileRepository
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
    val isCompleted: Boolean = false
) {
    val isPersonalInfoValid: Boolean
        get() = height.isNotBlank() && weight.isNotBlank() &&
                height.toDoubleOrNull() != null && weight.toDoubleOrNull() != null
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferenceDataSource: OnboardingPreferenceDataSource,
    private val userProfileRepository: UserProfileRepository
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

    fun completeOnboarding(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val userHeight = currentState.height.toDoubleOrNull() ?: 168.0
                val userWeight = currentState.weight.toDoubleOrNull() ?: 62.0
                val chosenSensitivity = currentState.caffeineSensitivity.domainDomainSensitivity

                val initialProfile = UserProfile(
                    id = 1,
                    height = userHeight,
                    weight = userWeight,
                    dailyCaffeineLimit = 400.0,
                    onboardingCompleted = true,
                    userSleepTime = 2230L,
                    wakeTime = 630L,
                    sensitivity = chosenSensitivity,
                    cutoffTime = 0L
                )

                // DB 저장 및 Preference 기록
                userProfileRepository.saveUserProfile(initialProfile)
                onboardingPreferenceDataSource.setOnboardingCompleted(true)

                _uiState.update { it.copy(isCompleted = true) }
                onResult(true)

            } catch (e: Exception) {
                Log.e("Onboarding", "DB 저장 실패", e)
                onResult(false)
            }
        }
    }
}