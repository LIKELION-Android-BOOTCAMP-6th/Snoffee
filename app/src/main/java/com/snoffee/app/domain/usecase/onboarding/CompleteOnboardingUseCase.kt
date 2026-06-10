package com.snoffee.app.domain.usecase.onboarding

import com.snoffee.app.data.datasource.preference.OnboardingPreferenceDataSource
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.UserProfileRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val onboardingPreferenceDataSource: OnboardingPreferenceDataSource
) {
    suspend operator fun invoke(
        height: String,
        weight: String,
        sensitivity: CaffeineSensitivity,
        sleepTime: Long,
        wakeTime: Long
    ): Result<Unit> {
        return runCatching {
            val userHeight = height.toDoubleOrNull() ?: DEFAULT_HEIGHT
            val userWeight = weight.toDoubleOrNull() ?: DEFAULT_WEIGHT

            val initialProfile = UserProfile(
                id = DEFAULT_PROFILE_ID,
                height = userHeight,
                weight = userWeight,
                dailyCaffeineLimit = DEFAULT_DAILY_CAFFEINE_LIMIT,
                onboardingCompleted = true,
                userSleepTime = sleepTime,
                wakeTime = wakeTime,
                sensitivity = sensitivity,
                cutoffTime = DEFAULT_CUTOFF_TIME
            )

            userProfileRepository.saveUserProfile(initialProfile)
            onboardingPreferenceDataSource.setOnboardingCompleted(true)
        }
    }

    companion object {
        private const val DEFAULT_PROFILE_ID = 1
        private const val DEFAULT_HEIGHT = 168.0
        private const val DEFAULT_WEIGHT = 62.0
        private const val DEFAULT_DAILY_CAFFEINE_LIMIT = 400.0
        private const val DEFAULT_SLEEP_TIME = 2230L
        private const val DEFAULT_WAKE_TIME = 630L
        private const val DEFAULT_CUTOFF_TIME = 0L
    }
}