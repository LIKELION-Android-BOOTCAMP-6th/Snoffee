package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.UserProfileLocalDataSource
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.UserProfileRepository
import javax.inject.Inject

// UserProfileRepository 인터페이스 구현체
// UserProfileLocalDataSource를 통해 Room DB에 접근
class UserProfileRepositoryImpl @Inject constructor(
    private val localDataSource: UserProfileLocalDataSource // Hilt가 자동 주입
) : UserProfileRepository {

    override suspend fun saveUserProfile(userProfile: UserProfile) {
        // TODO: Domain Model → DTO 변환 후 저장
    }

    override suspend fun getUserProfile(): UserProfile? {
        // TODO: DTO 조회 후 Domain Model로 변환해서 반환
        return null
    }
}