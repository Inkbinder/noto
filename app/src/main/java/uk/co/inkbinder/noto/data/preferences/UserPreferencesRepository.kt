package uk.co.inkbinder.noto.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.inkbinder.noto.domain.model.UserPreferences
import uk.co.inkbinder.noto.domain.model.WeekStart

private val Context.userPreferencesStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository internal constructor(
    private val userPreferencesStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.userPreferencesStore)

    val userPreferences: Flow<UserPreferences> = userPreferencesStore.data.map { preferences ->
        UserPreferences(
            periodPredictionEnabled = preferences[periodPredictionEnabledKey] ?: true,
            defaultCycleLengthDays = preferences[defaultCycleLengthDaysKey] ?: 28,
            weekStartsOn = preferences[weekStartsOnKey]
                ?.let(WeekStart::valueOf)
                ?: WeekStart.MONDAY,
            seededDefaultsVersion = preferences[seededDefaultsVersionKey] ?: 0,
        )
    }

    suspend fun markDefaultsSeeded(version: Int) {
        userPreferencesStore.edit { preferences ->
            preferences[seededDefaultsVersionKey] = version
        }
    }

    suspend fun setPredictionEnabled(enabled: Boolean) {
        userPreferencesStore.edit { preferences ->
            preferences[periodPredictionEnabledKey] = enabled
        }
    }

    suspend fun setDefaultCycleLengthDays(days: Int) {
        userPreferencesStore.edit { preferences ->
            preferences[defaultCycleLengthDaysKey] = days.coerceIn(14, 60)
        }
    }

    suspend fun setWeekStartsOn(weekStart: WeekStart) {
        userPreferencesStore.edit { preferences ->
            preferences[weekStartsOnKey] = weekStart.name
        }
    }

    private companion object {
        val periodPredictionEnabledKey = booleanPreferencesKey("period_prediction_enabled")
        val defaultCycleLengthDaysKey = intPreferencesKey("default_cycle_length_days")
        val weekStartsOnKey = stringPreferencesKey("week_starts_on")
        val seededDefaultsVersionKey = intPreferencesKey("seeded_defaults_version")
    }
}
