package com.snoffee.app.data.datasource.preference

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.onboardingDataStore by preferencesDataStore(
    name = "onboarding_preferences"
)

class OnboardingPreferenceDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OnboardingPreferenceDataSource {

    override val isOnboardingCompleted: Flow<Boolean> =
        context.onboardingDataStore.data.map { preferences ->
            preferences[IS_ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED] = completed
        }
    }

    companion object {
        private val IS_ONBOARDING_COMPLETED =
            booleanPreferencesKey("is_onboarding_completed")
    }
}