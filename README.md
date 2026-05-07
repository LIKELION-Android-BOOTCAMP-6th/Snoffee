# ☕ Snoffee

> “카페인을 관리하면 수면이 달라진다.”

Snoffee는 사용자의 카페인 섭취 패턴과 수면 데이터를 분석하여  
더 건강한 수면 습관을 만들 수 있도록 도와주는 AI 기반 Android 애플리케이션입니다.

단순한 카페인 기록 앱이 아니라,  
Galaxy Watch · Firebase · Gemini AI를 활용하여  
개인 맞춤형 카페인 컷오프 시간과 수면 인사이트를 제공합니다.

---

# 🚀 Key Features

## ☕ Smart Caffeine Tracking
- 음료 기반 카페인 기록
- 카페인 섭취 시간 자동 저장
- 커스텀 음료 직접 입력 지원
- 오늘 섭취량 및 기록 조회

---

## 🧠 Residual Caffeine Analysis
- 카페인 반감기 기반 잔류량 계산
- 실시간 잔류 카페인 게이지 제공
- 누적 섭취량 자동 계산
## 🤖 AI Personalized Cutoff Time
- Gemini AI 기반 개인화 분석
- 수면 패턴 + 카페인 섭취 데이터 분석
- “오늘 몇 시 이후 카페인을 피해야 하는지” 추천

---

## ⌚ Galaxy Watch Integration
- 워치에서 빠른 카페인 입력
- 컷오프 시간 진동 알림
- 취침 전 잔류 카페인 알림
- WearOS 연동 지원

---

## 🌙 Sleep Data Integration
- Samsung Health / Health Connect 연동
- 수면 시작 · 종료 시간 자동 수집
- 깊은 수면 비율 분석
- 수면 데이터 기반 리포트 제공

---

## 📊 Weekly Insight Report
- 주간 카페인 패턴 분석
- 카페인 vs 수면 상관관계 시각화
- AI 기반 자연어 리포트 생성

---

# 🏗️ Architecture & Tech Stack

## Architecture

### MVVM + Clean Architecture Lite
- UI / Domain / Data Layer 분리
- Repository Pattern 적용
- StateFlow 기반 단방향 상태 관리
- 유지보수성과 테스트 용이성 강화

---

## Tech Stack

| Category | Stack |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| Async | Coroutine + Flow |
| DI | Hilt |
| Local DB | Room |
| Remote DB | Firebase Firestore |
| Backend | Firebase Functions |
| AI | Gemini API |
| Watch | WearOS |
| Health Data | Samsung Health SDK / Health Connect |
| Notification | FCM |

---
