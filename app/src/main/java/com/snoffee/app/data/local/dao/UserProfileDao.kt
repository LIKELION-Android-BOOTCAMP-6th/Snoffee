package com.snoffee.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snoffee.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)

    // Flow를 반환 -> 데이터 변경 시 presentation 레이어까지 실시간
    @Query("SELECT * FROM user_table WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    // 단발성 조회를 위한 suspend 함수 (테스트 및 UseCase 대응용)
    @Query("SELECT * FROM user_table WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Query(
        """
        UPDATE user_table
        SET notification_enabled = :enabled
        WHERE id = 1
    """
    )
    suspend fun updateNotificationEnabled(enabled: Boolean)
}