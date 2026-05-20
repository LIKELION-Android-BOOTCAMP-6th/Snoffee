package com.snoffee.app.data.datasource.remote

import android.util.Log
import com.google.firebase.FirebaseApp
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
        private const val DATABASE = "drink"            //database 이름
        private const val COLLECTION = "drinks"         // Firestore 컬렉션 이름
    }

    override suspend fun fetchDrinkList(): List<DrinkDto> {
        return try {
            // "drink" 데이터베이스 연결
            val db = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), DATABASE)

            // 발견된 경로 'drinks' 사용
            val snapshot = db.collection(COLLECTION).get().await()

            if (snapshot.isEmpty) return emptyList()

            return snapshot.documents.map { doc ->
                doc.toObject(DrinkDto::class.java)
                    ?.apply { foodId = doc.id } ?: DrinkDto()
            }
        } catch (e: Exception) {
            Log.e("DEBUG", "에러: ${e.message}")
            emptyList()
        }
    }
}