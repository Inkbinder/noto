package uk.co.inkbinder.noto.background

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import uk.co.inkbinder.noto.MainActivity
import uk.co.inkbinder.noto.NotoApplication

internal const val PERIOD_REMINDER_CHANNEL_ID = "period-reminders"

class PeriodReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        if (!canPostNotifications()) {
            return Result.success()
        }

        val appContainer = (applicationContext as NotoApplication).appContainer
        val reminder = appContainer.calendarRepository.getPeriodReminder() ?: return Result.success()

        NotificationManagerCompat.from(applicationContext).notify(
            PERIOD_REMINDER_NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, PERIOD_REMINDER_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(reminder.title)
                .setContentText(reminder.message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createContentIntent())
                .setAutoCancel(true)
                .build(),
        )

        appContainer.userPreferencesRepository.setLastPeriodReminderKey(reminder.reminderKey)
        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return false
        }

        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val PERIOD_REMINDER_NOTIFICATION_ID = 1001

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return
            }

            val channel = NotificationChannel(
                PERIOD_REMINDER_CHANNEL_ID,
                "Period reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Daily reminders when a predicted period is close."
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
