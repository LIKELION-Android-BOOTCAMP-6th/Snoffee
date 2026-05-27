package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.dao.UserProfileDao
import com.snoffee.app.data.local.entity.UserProfileEntity
import javax.inject.Inject

// UserProfileLocalDataSource 구현체
class UserProfileLocalDataSourceImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileLocalDataSource {

    override suspend fun saveUserProfile(entity: UserProfileEntity) {
        userProfileDao.insertUserProfile(entity)
    }

    override suspend fun getUserProfile(): UserProfileEntity? {
        return userProfileDao.getUserProfileOnce()
    }
}