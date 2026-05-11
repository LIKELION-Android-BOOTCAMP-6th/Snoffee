package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.UserProfile

// 사용자 프로필 저장/조회 계약
// 실제 구현은 UserProfileRepositoryImpl
interface UserProfileRepository {
    suspend fun saveUserProfile(userProfile: UserProfile)   // 사용자 프로필 저장
    suspend fun getUserProfile(): UserProfile?              // 사용자 프로필 조회 (없을 수 있음)
}