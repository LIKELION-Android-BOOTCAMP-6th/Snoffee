# ☕ Snoffee

> "카페인을 관리하면 수면이 달라진다."

Snoffee는 사용자의 카페인 섭취 기록과 수면 데이터를 분석하여 더 건강한 수면 습관 형성을 돕는 AI 기반 Android 애플리케이션입니다.

단순히 카페인을 기록하는 것을 넘어, Health Connect, Galaxy Watch, Gemini AI를 활용하여 사용자의 수면 패턴과 카페인 섭취 습관을 분석하고 개인화된 인사이트를 제공합니다.

---

# 🚀 Key Features

## ☕ Smart Caffeine Tracking

* 음료 기반 카페인 섭취 기록
* 커스텀 카페인 직접 입력
* 섭취 시간 선택 및 저장
* 일일 카페인 섭취량 확인
* 카페인 기록 히스토리 제공

---

## 🧠 Residual Caffeine Analysis

* 카페인 반감기 기반 잔류량 계산
* 사용자 민감도(반감기) 적용
* 실시간 잔류 카페인 게이지 제공
* 취침 시 예상 잔류 카페인 분석

---

## 🤖 AI Sleep Insight

* Gemini AI 기반 수면 인사이트 생성
* 카페인 섭취 패턴 분석
* 수면 데이터 분석
* 자연어 형태의 개인 맞춤 리포트 제공

---

## ⌚ Galaxy Watch Integration

* 워치에서 빠른 카페인 입력
* 컷오프 시간 알림
* 취침 전 카페인 알림
* Wear OS 연동 지원

---

## 🌙 Sleep Data Integration

* Health Connect 연동
* 수면 시작 및 종료 시간 수집
* 수면 시간 자동 분석
* 수면 데이터 기반 리포트 제공

---

## 📊 Period Report

### 일간 리포트

* 오늘의 카페인 섭취량
* 오늘의 수면 정보
* 잔류 카페인 확인

### 주간 리포트

* 주간 평균 카페인 섭취량
* 주간 평균 수면 시간
* 요일별 변화 시각화

### 기간 리포트

* 최근 7일 데이터 분석
* 카페인과 수면 패턴 비교
* AI 기반 인사이트 제공

---

# 🏗️ Architecture

## MVVM + Clean Architecture Lite

* UI / Domain / Data Layer 분리
* Repository Pattern 적용
* UseCase 기반 비즈니스 로직 관리
* StateFlow 기반 상태 관리
* 테스트 및 유지보수 용이성 향상

---

# 🛠 Tech Stack

| Category       | Stack                              |
| -------------- | ---------------------------------- |
| Language       | Kotlin                             |
| UI             | Jetpack Compose                    |
| Architecture   | MVVM + Clean Architecture Lite     |
| Async          | Coroutine + Flow                   |
| DI             | Hilt                               |
| Local Storage  | Room                               |
| Cloud Database | Firebase Firestore                 |
| Analytics      | Firebase Analytics                 |
| AI             | Gemini API                         |
| Health Data    | Health Connect                     |
| Wearable       | Wear OS                            |
| Notification   | AlarmManager + NotificationManager |

---

# 📈 What Snoffee Solves

* 카페인을 마셨지만 언제까지 몸에 남아있는지 알기 어렵다.
* 카페인이 수면에 얼마나 영향을 주는지 체감하기 어렵다.
* 자신의 수면 패턴에 맞는 카페인 섭취 시간을 알기 어렵다.

Snoffee는 카페인 섭취 데이터와 수면 데이터를 연결하여 사용자가 자신의 생활 습관을 데이터 기반으로 이해하고 개선할 수 있도록 돕습니다.
