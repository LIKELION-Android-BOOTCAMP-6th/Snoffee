package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.entity.UserProfileEntity

// 사용자 프로필 로컬 데이터 접근 계약 (인터페이스)
// 실제 구현은 UserProfileLocalDataSourceImpl
interface UserProfileLocalDataSource {
    suspend fun saveUserProfile(entity: UserProfileEntity)
    suspend fun getUserProfile(): UserProfileEntity?
    suspend fun updateNotificationEnabled(enabled: Boolean)
}