package com.snoffee.app.presentation.setting

import android.util.Log
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
    fun updateHeight(newHeight: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val updated = current.copy(height = newHeight)
            try {
                userProfileRepository.saveUserProfile(updated)
                _userProfile.value = updated
                onResult(true) // 성공
            } catch (e: Exception) {
                Log.e("SettingViewModel", "신장 저장 실패", e)
                onResult(false) // 실패
            }
        }
    }

    // 체중 업데이트
    fun updateWeight(newWeight: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val updated = current.copy(weight = newWeight)
            try {
                userProfileRepository.saveUserProfile(updated)
                _userProfile.value = updated
                onResult(true) // 성공
            } catch (e: Exception) {
                Log.e("SettingViewModel", "체중 저장 실패", e)
                onResult(false) // 실패
            }
        }
    }

    // 카페인 민감도 저장 (1~3단계)
    fun updateSensitivity(sensitivity: CaffeineSensitivity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val updated = current.copy(sensitivity = sensitivity)
            try {
                userProfileRepository.saveUserProfile(updated)
                _userProfile.value = updated
                onResult(true) // 성공
            } catch (e: Exception) {
                Log.e("SettingViewModel", "민감도 저장 실패", e)
                onResult(false) // 실패
            }
        }
    }

    // 목표 수면 시간 업데이트
    fun updateSleepTime(newSleepTimeStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val timeLong = newSleepTimeStr.replace(":", "").toLongOrNull() ?: 0L
            val updated = current.copy(userSleepTime = timeLong)
            try {
                userProfileRepository.saveUserProfile(updated)
                _userProfile.value = updated
                onResult(true) // 성공
            } catch (e: Exception) {
                Log.e("SettingViewModel", "목표 수면 시간 저장 실패", e)
                onResult(false) // 실패
            }
        }
    }

    // 목표 기상 시간 업데이트
    fun updateWakeTime(newWakeTimeStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val timeLong = newWakeTimeStr.replace(":", "").toLongOrNull() ?: 0L
            val updated = current.copy(wakeTime = timeLong)
            try {
                userProfileRepository.saveUserProfile(updated)
                _userProfile.value = updated
                onResult(true) // 성공
            } catch (e: Exception) {
                Log.e("SettingViewModel", "목표 기상 시간 저장 실패", e)
                onResult(false) // 실패
            }
        }
    }
    fun updateNotificationEnabled(enabled: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = _userProfile.value ?: createDefaultProfile()
            val updated = current.copy(notificationEnabled = enabled)

            try {
                userProfileRepository.saveUserProfile(updated)
                _userProfile.value = updated
                onResult(true)
            } catch (e: Exception) {
                Log.e("SettingViewModel", "알림 설정 저장 실패", e)
                onResult(false)
            }
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
            cutoffTime = 0L,
            notificationEnabled = true
        )
    }
}