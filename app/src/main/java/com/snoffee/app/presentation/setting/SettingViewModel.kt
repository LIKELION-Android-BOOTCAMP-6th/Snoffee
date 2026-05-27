package com.snoffee.app.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfile.value = userProfileRepository.getUserProfile()
        }
    }

    // 신장 업데이트
    fun updateHeight(newHeight: Double) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val updated = current.copy(height = newHeight)
            userProfileRepository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    // 체중 업데이트
    fun updateWeight(newWeight: Double) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val updated = current.copy(weight = newWeight)
            userProfileRepository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    // 카페인 민감도 저장 (1~3단계)
    fun updateSensitivity(sensitivity: CaffeineSensitivity) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val updated = current.copy(sensitivity = sensitivity)
            userProfileRepository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    // 목표 수면 시간 업데이트
    fun updateSleepTime(newSleepTimeStr: String) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val timeLong = newSleepTimeStr.replace(":", "").toLongOrNull() ?: 0L
            val updated = current.copy(userSleepTime = timeLong)
            userProfileRepository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    // 목표 기상 시간 업데이트
    fun updateWakeTime(newWakeTimeStr: String) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val timeLong = newWakeTimeStr.replace(":", "").toLongOrNull() ?: 0L
            val updated = current.copy(wakeTime = timeLong)
            userProfileRepository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    private fun createDefaultProfile(): UserProfile {
        return UserProfile(
            id = 1,
            height = 168.0,
            weight = 62.0,
            dailyCaffeineLimit = 400.0,
            onboardingCompleted = true,
            userSleepTime = 0L,
            wakeTime = 0L,
            sensitivity = CaffeineSensitivity.NORMAL,
            cutoffTime = 0L
        )
    }
}