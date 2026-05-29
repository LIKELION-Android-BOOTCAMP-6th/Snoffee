package com.snoffee.app.domain.usecase.onboarding

import javax.inject.Inject

class ValidateDecimalInputUseCase @Inject constructor() {

    private val decimalRegex = Regex("^\\d*\\.?\\d{0,1}$")

    operator fun invoke(value: String): Boolean {
        return value.isEmpty() || value.matches(decimalRegex)
    }
}