package com.snoffee.app.core.config

// 온보딩 작업이나 이외의 디버그로써만 보고싶은 로직은 필요하시다면, 해당 파일에 정의해주고 분기처리 하시면 좋을 것 같습니다.
// true : 디버그용, false : 배포용
// 배포할 때는 해당 부분이 모두 false로 해서 잘 동작되어있는지 확인하기
object DebugConfig {
    // 온보딩 화면 테스트
    const val SHOW_ONBOARDING_ENTRY = false
}