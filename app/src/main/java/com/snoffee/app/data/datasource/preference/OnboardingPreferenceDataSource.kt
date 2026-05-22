package com.snoffee.app.data.datasource.preference

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferenceDataSource {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
}