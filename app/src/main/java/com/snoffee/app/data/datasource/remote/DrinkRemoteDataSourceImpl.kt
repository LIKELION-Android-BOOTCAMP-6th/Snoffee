package com.snoffee.app.data.datasource.remote

import com.google.firebase.database.FirebaseDatabase
import com.snoffee.app.data.model.DrinkDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// DrinkRemoteDataSource 구현체
// Firebase에서 음료 목록 GET 구현
class DrinkRemoteDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : DrinkRemoteDataSource{
    override suspend fun fetchDrinkList(): List<DrinkDto> {
        return try {
            // Firebase의 "Drink" 노드 참조
            val snapshot = database.getReference("Drink").get().await()

            // 데이터 스냅샷을 DrinkDto 리스트로 변환
            snapshot.children.mapNotNull { child ->
                //Firebase 데이터 필드값들만 채워진 Dto 생성
                val dtoWithoutId = child.getValue(DrinkDto::class.java)

                //copy 기능을 사용해 foodId가 고정된 '새로운' 객체를 반환
                // 이렇게 하면 생성된 이후에는 foodId를 수정 불가
                dtoWithoutId?.copy(foodId = child.key ?: "")
            }
        } catch (e: Exception) {
            // 에러 발생 시 빈 리스트 반환 (로그를 찍어두면 디버깅에 좋습니다)
            emptyLog("Firebase Fetch Error: ${e.message}")
            emptyList()
        }
    }

    private fun emptyLog(message: String) {
        android.util.Log.e("DrinkRemoteDataSource", message)
    }
}