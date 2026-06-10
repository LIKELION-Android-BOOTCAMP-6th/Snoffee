package com.snoffee.wear.presentation.home

// 민감도에 따른 반감기 변환 로직
fun String?.toHalfLife(): Double {
    return when (this) {
        "LOW" -> 4.0
        "SENSITIVE" -> 6.0
        else -> 5.0
    }
}