package uk.co.inkbinder.noto.background

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class PeriodReminderScheduler(
    private val workManager: WorkManager,
) {
    fun sync(remindersEnabled: Boolean, reminderMinutesAfterMidnight: Int) {
        if (remindersEnabled) {
            scheduleDailyCheck(reminderMinutesAfterMidnight)
        } else {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }

    private fun scheduleDailyCheck(reminderMinutesAfterMidnight: Int) {
        val request = PeriodicWorkRequestBuilder<PeriodReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(nextReminderDelay(reminderMinutesAfterMidnight))
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "period-reminder-daily"
    }
}

internal fun nextReminderDelay(
    reminderMinutesAfterMidnight: Int,
    now: ZonedDateTime = ZonedDateTime.now(),
): Duration {
    val reminderTime = LocalTime.of(
        reminderMinutesAfterMidnight / 60,
        reminderMinutesAfterMidnight % 60,
    )
    val nextRun = now
        .withHour(reminderTime.hour)
        .withMinute(reminderTime.minute)
        .withSecond(0)
        .withNano(0)
        .let { candidate ->
            if (now.isBefore(candidate)) candidate else candidate.plusDays(1)
        }

    return Duration.between(now, nextRun)
}
