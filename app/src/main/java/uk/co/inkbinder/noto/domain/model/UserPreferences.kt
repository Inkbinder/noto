package uk.co.inkbinder.noto.domain.model

import java.time.DayOfWeek

enum class WeekStart(val dayOfWeek: DayOfWeek) {
    MONDAY(DayOfWeek.MONDAY),
    SUNDAY(DayOfWeek.SUNDAY),
}

data class UserPreferences(
    val periodPredictionEnabled: Boolean = true,
    val periodPredictionUsesHistory: Boolean = true,
    val periodReminderEnabled: Boolean = false,
    val periodReminderMinutesAfterMidnight: Int = 9 * 60,
    val defaultCycleLengthDays: Int = 28,
    val defaultPeriodLengthDays: Int = 4,
    val weekStartsOn: WeekStart = WeekStart.MONDAY,
    val lastPeriodReminderKey: String? = null,
    val seededDefaultsVersion: Int = 0,
)
