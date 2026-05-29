package com.snoffee.app.domain.usecase.onboarding

import javax.inject.Inject

class ValidateOnboardingInputUseCase @Inject constructor() {

    operator fun invoke(
        height: String,
        weight: String
    ): Result {
        val heightValue = height.toDoubleOrNull()
        val weightValue = weight.toDoubleOrNull()

        val isHeightValid =
            heightValue != null && heightValue in MIN_HEIGHT_CM..MAX_HEIGHT_CM

        val isWeightValid =
            weightValue != null && weightValue in MIN_WEIGHT_KG..MAX_WEIGHT_KG

        return Result(
            isHeightValid = isHeightValid,
            isWeightValid = isWeightValid,
            isPersonalInfoValid = isHeightValid && isWeightValid
        )
    }

    data class Result(
        val isHeightValid: Boolean,
        val isWeightValid: Boolean,
        val isPersonalInfoValid: Boolean
    )

    companion object {
        private const val MIN_HEIGHT_CM = 100.0
        private const val MAX_HEIGHT_CM = 300.0
        private const val MIN_WEIGHT_KG = 1.0
        private const val MAX_WEIGHT_KG = 400.0
    }
}