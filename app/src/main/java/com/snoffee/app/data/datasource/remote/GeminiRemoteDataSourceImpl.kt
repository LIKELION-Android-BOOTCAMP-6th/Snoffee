package com.snoffee.app.data.datasource.remote

import javax.inject.Inject

/**
 * GeminiRemoteDataSource 구현체
 * Retrofit으로 Gemini API 실제 호출 구현
 */
class GeminiRemoteDataSourceImpl @Inject constructor(
    // TODO: Retrofit 주입 필요
) : GeminiRemoteDataSource