package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.UserProfileLocalDataSource
import com.snoffee.app.data.local.entity.UserProfileEntity
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.UserProfileRepository
import javax.inject.Inject

// UserProfileRepository 인터페이스 구현체
// UserProfileLocalDataSource를 통해 Room DB에 접근
class UserProfileRepositoryImpl @Inject constructor(
    private val localDataSource: UserProfileLocalDataSource // Hilt가 자동 주입
) : UserProfileRepository {

    override suspend fun saveUserProfile(userProfile: UserProfile) {
        val entity = UserProfileEntity(
            id = userProfile.id,
            height = userProfile.height,
            weight = userProfile.weight,
            sensitivity = when (userProfile.sensitivity) {
                1 -> "LOW"
                3 -> "HIGH"
                else -> "NORMAL"
            },
            targetSleepTime = userProfile.userSleepTime.toString(),
            targetWakeTime = userProfile.wakeTime.toString()
        )
        localDataSource.saveUserProfile(entity)
    }

    override suspend fun getUserProfile(): UserProfile? {
        val entity = localDataSource.getUserProfile() ?: return null

        val sensitivityInt = when (entity.sensitivity) {
            "LOW" -> 1
            "HIGH" -> 3
            else -> 2
        }

        return UserProfile(
            id = entity.id,
            height = entity.height,
            weight = entity.weight,
            dailyCaffeineLimit = 400.0,
            onboardingCompleted = true,
            userSleepTime = entity.targetSleepTime.toLongOrNull() ?: 0L,
            wakeTime = entity.targetWakeTime.toLongOrNull() ?: 0L,
            sensitivity = sensitivityInt,
            cutoffTime = 0L // 초기
        )
    }
}