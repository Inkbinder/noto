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
            periodPredictionUsesHistory = preferences[periodPredictionUsesHistoryKey] ?: true,
            periodReminderEnabled = preferences[periodReminderEnabledKey] ?: false,
            periodReminderMinutesAfterMidnight = preferences[periodReminderMinutesAfterMidnightKey] ?: 9 * 60,
            defaultCycleLengthDays = preferences[defaultCycleLengthDaysKey] ?: 28,
            defaultPeriodLengthDays = preferences[defaultPeriodLengthDaysKey] ?: 4,
            weekStartsOn = preferences[weekStartsOnKey]
                ?.let(WeekStart::valueOf)
                ?: WeekStart.MONDAY,
            lastPeriodReminderKey = preferences[lastPeriodReminderKey],
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

    suspend fun setPredictionUsesHistory(enabled: Boolean) {
        userPreferencesStore.edit { preferences ->
            preferences[periodPredictionUsesHistoryKey] = enabled
        }
    }

    suspend fun setPeriodReminderEnabled(enabled: Boolean) {
        userPreferencesStore.edit { preferences ->
            preferences[periodReminderEnabledKey] = enabled
        }
    }

    suspend fun setPeriodReminderMinutesAfterMidnight(minutesAfterMidnight: Int) {
        userPreferencesStore.edit { preferences ->
            preferences[periodReminderMinutesAfterMidnightKey] = minutesAfterMidnight.coerceIn(0, 1_439)
        }
    }

    suspend fun setDefaultCycleLengthDays(days: Int) {
        userPreferencesStore.edit { preferences ->
            preferences[defaultCycleLengthDaysKey] = days.coerceIn(14, 60)
        }
    }

    suspend fun setDefaultPeriodLengthDays(days: Int) {
        userPreferencesStore.edit { preferences ->
            preferences[defaultPeriodLengthDaysKey] = days.coerceIn(1, 14)
        }
    }

    suspend fun setWeekStartsOn(weekStart: WeekStart) {
        userPreferencesStore.edit { preferences ->
            preferences[weekStartsOnKey] = weekStart.name
        }
    }

    suspend fun setLastPeriodReminderKey(value: String?) {
        userPreferencesStore.edit { preferences ->
            if (value == null) {
                preferences.remove(lastPeriodReminderKey)
            } else {
                preferences[lastPeriodReminderKey] = value
            }
        }
    }

    private companion object {
        val periodPredictionEnabledKey = booleanPreferencesKey("period_prediction_enabled")
        val periodPredictionUsesHistoryKey = booleanPreferencesKey("period_prediction_uses_history")
        val periodReminderEnabledKey = booleanPreferencesKey("period_reminder_enabled")
        val periodReminderMinutesAfterMidnightKey = intPreferencesKey("period_reminder_minutes_after_midnight")
        val defaultCycleLengthDaysKey = intPreferencesKey("default_cycle_length_days")
        val defaultPeriodLengthDaysKey = intPreferencesKey("default_period_length_days")
        val weekStartsOnKey = stringPreferencesKey("week_starts_on")
        val lastPeriodReminderKey = stringPreferencesKey("last_period_reminder_key")
        val seededDefaultsVersionKey = intPreferencesKey("seeded_defaults_version")
    }
}
