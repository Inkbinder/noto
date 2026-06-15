package uk.co.inkbinder.noto.domain.prediction

import java.time.LocalDate

data class PeriodDueReminder(
    val title: String,
    val message: String,
    val reminderKey: String,
)

class PeriodReminderPlanner {
    fun plan(
        prediction: PeriodPrediction,
        today: LocalDate,
        lastReminderKey: String?,
    ): PeriodDueReminder? {
        if (prediction.daysUntil !in 0..3) {
            return null
        }

        val reminderKey = "$today|${prediction.predictedStart}"
        if (lastReminderKey == reminderKey) {
            return null
        }

        val title = if (prediction.daysUntil == 0) {
            "Period due today"
        } else {
            "Period due in ${prediction.daysUntil} days"
        }

        return PeriodDueReminder(
            title = title,
            message = "Based on your recent logged period dates.",
            reminderKey = reminderKey,
        )
    }
}
