package uk.co.inkbinder.noto.domain.model

import java.time.DayOfWeek

enum class WeekStart(val dayOfWeek: DayOfWeek) {
    MONDAY(DayOfWeek.MONDAY),
    SUNDAY(DayOfWeek.SUNDAY),
}

data class UserPreferences(
    val periodPredictionEnabled: Boolean = true,
    val defaultCycleLengthDays: Int = 28,
    val weekStartsOn: WeekStart = WeekStart.MONDAY,
    val seededDefaultsVersion: Int = 0,
)

