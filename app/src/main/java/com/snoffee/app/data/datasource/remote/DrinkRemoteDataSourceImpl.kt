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
            val resultList: List<DrinkDto> = snapshot.children.mapNotNull { child ->
                val dtoWithoutId = child.getValue(DrinkDto::class.java)

                // 블록의 마지막은 반드시 '반환할 객체'여야 합니다.
                dtoWithoutId?.copy(foodId = child.key ?: "")
            }
            android.util.Log.d("Snoffee_Debug", "Firebase 데이터 개수: ${resultList.size}")

            // 3. 최종적으로 리스트를 반환합니다.
            resultList

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