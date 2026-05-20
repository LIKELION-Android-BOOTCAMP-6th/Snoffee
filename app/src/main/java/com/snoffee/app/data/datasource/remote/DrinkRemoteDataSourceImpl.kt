package com.snoffee.app.data.datasource.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.snoffee.app.data.model.DrinkDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// DrinkRemoteDataSource 구현체
// Firestore drinks 컬렉션 전체 다운로드 구현
// 앱 첫 실행 시 1회만 호출 → Room 캐싱 후 이후 검색은 Room에서 처리
// FirebaseDatabase 사용하던거를 Firestore로 변경
class DrinkRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DrinkRemoteDataSource {

    companion object {
        private const val TAG = "DrinkRemoteDataSource"
        private const val COLLECTION = "drinks"         // Firestore 컬렉션 이름
    }

    override suspend fun fetchDrinkList(): List<DrinkDto> {
        return try {
            // Firestore drinks 컬렉션 전체 문서 가져오기
            val snapshot = firestore.collection(COLLECTION).get().await()

            // 각 문서를 DrinkDto로 변환
            val resultList: List<DrinkDto> = snapshot.documents.mapNotNull { doc ->
                val dtoWithoutId = doc.toObject(DrinkDto::class.java)
                dtoWithoutId?.also { it.foodId = doc.id }
            }

            Log.d(TAG, "Firestore 데이터 개수: ${resultList.size}")
            resultList

        } catch (e: Exception) {
            // 에러 발생 시 빈 리스트 반환
            Log.e(TAG, "Firestore Fetch Error: ${e.message}", e)
            emptyList()
        }
    }
}