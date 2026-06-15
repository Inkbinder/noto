package uk.co.inkbinder.noto.domain.prediction

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import uk.co.inkbinder.noto.domain.model.UserPreferences

data class PeriodPrediction(
    val predictedStart: LocalDate,
    val daysUntil: Int,
)

class PeriodPredictionEngine {
    fun predictNextPeriod(
        periodDateStrings: List<String>,
        preferences: UserPreferences,
        today: LocalDate,
    ): PeriodPrediction? {
        val periodDates = periodDateStrings
            .asSequence()
            .map(LocalDate::parse)
            .distinct()
            .sorted()
            .toList()

        if (periodDates.isEmpty()) {
            return null
        }

        val periodStarts = collapseToPeriodStarts(periodDates)
        val lastStart = periodStarts.lastOrNull() ?: return null

        val predictedStart = when {
            periodStarts.size == 1 -> lastStart.plusDays(preferences.defaultCycleLengthDays.toLong())
            else -> {
                val intervals = periodStarts
                    .zipWithNext { current, next -> ChronoUnit.DAYS.between(current, next).toInt() }
                    .takeLast(6)

                val averageInterval = intervals.average().roundToInt().coerceAtLeast(1)
                lastStart.plusDays(averageInterval.toLong())
            }
        }

        return PeriodPrediction(
            predictedStart = predictedStart,
            daysUntil = ChronoUnit.DAYS.between(today, predictedStart).toInt(),
        )
    }

    fun buildBanner(
        periodDateStrings: List<String>,
        preferences: UserPreferences,
        today: LocalDate,
    ): String {
        if (!preferences.periodPredictionEnabled) {
            return "Prediction off"
        }

        val prediction = predictNextPeriod(
            periodDateStrings = periodDateStrings,
            preferences = preferences,
            today = today,
        ) ?: return "No prediction yet"

        return when {
            prediction.daysUntil > 0 -> "Period due in ${prediction.daysUntil} days"
            prediction.daysUntil == 0 -> "Period due today"
            else -> "Delayed by ${-prediction.daysUntil} days"
        }
    }

    private fun collapseToPeriodStarts(periodDates: List<LocalDate>): List<LocalDate> {
        if (periodDates.isEmpty()) return emptyList()

        val starts = mutableListOf(periodDates.first())
        for (index in 1 until periodDates.size) {
            val previous = periodDates[index - 1]
            val current = periodDates[index]
            if (previous.plusDays(1) != current) {
                starts += current
            }
        }
        return starts
    }
}
