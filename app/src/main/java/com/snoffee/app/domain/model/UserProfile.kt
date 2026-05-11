package com.snoffee.app.domain.model



enum class CaffeineSensitivity(
    val halfLifeHours: Double
) {
    SENSITIVE(6.0), // 민감
    NORMAL(5.0), // 적당
    LOW(4.0) // 낮음
}