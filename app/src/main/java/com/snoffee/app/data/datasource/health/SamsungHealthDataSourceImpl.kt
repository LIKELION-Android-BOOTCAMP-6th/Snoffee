package com.snoffee.app.data.datasource.health

import javax.inject.Inject

// SamsungHealthDataSource 구현체
// 삼성헬스 SDK 실제 연동 구현
// 수면/심박 데이터 수집 로직 포함
class SamsungHealthDataSourceImpl @Inject constructor(
    // TODO: 삼성헬스 SDK 주입 필요 (권한 요청은 OnboardingPermissionScreen에서 처리)
) : SamsungHealthDataSource