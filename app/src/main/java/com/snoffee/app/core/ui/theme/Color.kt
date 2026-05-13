package com.snoffee.app.core.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand — Brown ─────────────────────────────────────────────────────
val SnoffeePrimary        = Color(0xFFA0784A)  // 바 차트, 버튼, 주요 강조
val SnoffeePrimaryLight   = Color(0xFFB8926A)  // hover, 아이콘 강조
val SnoffeePrimaryDark    = Color(0xFF7A5830)  // pressed, 딥 강조
val SnoffeePrimaryTint    = Color(0xFFD4B896)  // 칩 배경, 선택 하이라이트
val SnoffeePrimarySubtle  = Color(0xFFEFE0CC)  // 점수 카드 배경, 뱃지

// ── Background ────────────────────────────────────────────────────────
val SnoffeeBgBase         = Color(0xFFF4F3F1)  // 앱 전체 배경 (중성 그레이-베이지)
val SnoffeeBgWarm         = Color(0xFFECEAE7)  // 섹션 구분 배경
val SnoffeeBgMuted        = Color(0xFFE2E0DC)  // 비활성 영역
val SnoffeeBgDim          = Color(0xFFD2D0CB)  // 구분 테두리 안쪽

// ── Surface ───────────────────────────────────────────────────────────
val SnoffeeSurface        = Color(0xFFFFFFFF)  // 카드
val SnoffeeAppbar         = Color(0xFFF2EBE3)
val SnoffeeNavBar         = Color(0xFFEDE4DB)
val SnoffeeSurfaceElevated= Color(0xFFFAFAF9)  // 떠 있는 카드, 다이얼로그
val SnoffeeSurfaceOverlay = Color(0xFFF3F2F0)  // 입력창, 리스트 아이템
val SnoffeeSurfacePressed = Color(0xFFE8E7E4)  // 탭·버튼 눌린 상태

// ── Shadow (Neumorphism) ───────────────────────────────────────────────
val SnoffeeShadowLight    = Color(0xE6FFFFFF)  // 돌출 하이라이트  (alpha 0.9)
val SnoffeeShadowDark     = Color(0x14000000)  // 베이스 깊이 그림자 (alpha 0.08)

// ── Text ──────────────────────────────────────────────────────────────
val SnoffeeTextMain       = Color(0xFF2C1E14)  // 제목, 주요 텍스트
val SnoffeeTextMuted      = Color(0xFF5C4A3A)  // 부제목, 설명
val SnoffeeTextHint       = Color(0xFF8C7A6A)  // 힌트, 플레이스홀더
val SnoffeeTextDisabled   = Color(0xFFB8AA9E)  // 비활성 텍스트

// ── Divider & Border ──────────────────────────────────────────────────
val SnoffeeDivider        = Color(0xFFD2D0CB)  // 섹션 구분선
val SnoffeeBorder         = Color(0xFFC4C2BD)  // 카드·입력창 외곽선

// ── Semantic ──────────────────────────────────────────────────────────
val SnoffeeSuccess        = Color(0xFF6A9E78)  // 완료, 안전 상태
val SnoffeeWarning        = Color(0xFFC4845A)  // 카페인 주의
val SnoffeeError          = Color(0xFFA85454)  // 카페인 초과, 오류
val SnoffeeInfo           = Color(0xFF5480A8)  // 리포트 정보, 안내

// ── Alias ─────────────────────────────────────────────────────────────
val ButtonEnabled        = SnoffeePrimary
val ButtonDisabledBg     = SnoffeeSurfacePressed
val ButtonDisabledText   = SnoffeeTextDisabled
val ButtonPressed        = SnoffeePrimaryDark

val ChipSelected         = SnoffeePrimaryTint
val ChipSelectedText     = SnoffeePrimaryDark

val ScoreCardBg          = SnoffeePrimarySubtle  // "82/100" 점수 카드
val BarChartPrimary      = SnoffeePrimary        // 카페인 타임라인 바 (어제)
val BarChartSecondary    = SnoffeePrimaryTint    // 카페인 타임라인 바 (오늘)
val SleepStageDeep       = SnoffeePrimary        // 깊은 수면 바
val SleepStageLight      = SnoffeePrimaryLight   // 얕은 수면 바
val SleepStageRem        = SnoffeePrimaryTint    // REM 수면 바

val CaffeineSafe         = SnoffeeSuccess
val CaffeineAlert        = SnoffeeWarning
val CaffeineOver         = SnoffeeError