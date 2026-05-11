package com.snoffee.app.domain.model

data class CaffeineRecord(
    val id: String = "",
    val uid: String,
    val drinkId: String,
    val drinkName: String,
    val brandName: String,
    val intakeSize: Double,
    val intakeCaffeine: Double,
    val consumedAt: Long
)