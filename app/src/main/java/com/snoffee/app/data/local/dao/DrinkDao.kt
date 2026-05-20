package com.snoffee.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snoffee.app.data.local.entity.DrinkEntity

@Dao
interface DrinkDao {
    // Room에 저장된 음료 데이터 개수 확인 (첫 실행 여부 판단용)
    @Query("SELECT COUNT(*) FROM DrinkEntity")
    suspend fun getCount(): Int

    @Query("SELECT * FROM DrinkEntity")
    suspend fun getAllDrinks(): List<DrinkEntity>

    // 음료 데이터 저장 (Firestore에서 가져온 데이터 캐싱)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Query("SELECT * FROM DrinkEntity WHERE name LIKE '%' || :query || '%'")
    suspend fun searchDrinks(query: String): List<DrinkEntity>

    // 음료 검색 + 페이징 (name, brand contains 검색)
    // query가 빈 문자열이면 전체 조회
    // LIMIT/OFFSET으로 20개씩 페이징
    @Query(
        """
        SELECT * FROM DrinkEntity
        WHERE name LIKE '%' || :query || '%'
           OR brand LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun searchDrinks(
        query: String,
        limit: Int,
        offset: Int,
    ): List<DrinkEntity>
}