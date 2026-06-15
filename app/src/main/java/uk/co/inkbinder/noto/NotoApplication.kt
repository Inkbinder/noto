package uk.co.inkbinder.noto

import android.app.Application
import androidx.work.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import uk.co.inkbinder.noto.background.PeriodReminderWorker
import uk.co.inkbinder.noto.data.bootstrap.SeedDataInitializer
import uk.co.inkbinder.noto.di.AppContainer

class NotoApplication : Application(), Configuration.Provider {
    val appContainer: AppContainer by lazy { AppContainer(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()

        PeriodReminderWorker.createNotificationChannel(this)

        applicationScope.launch {
            SeedDataInitializer(
                tagRepository = appContainer.tagRepository,
                userPreferencesRepository = appContainer.userPreferencesRepository,
            ).seedDefaultsIfNeeded()
        }

        applicationScope.launch {
            appContainer.userPreferencesRepository.userPreferences
                .map { preferences ->
                    (
                        preferences.periodPredictionEnabled && preferences.periodReminderEnabled
                    ) to preferences.periodReminderMinutesAfterMidnight
                }
                .distinctUntilChanged()
                .collect { (remindersEnabled, reminderMinutesAfterMidnight) ->
                    appContainer.periodReminderScheduler.sync(
                        remindersEnabled = remindersEnabled,
                        reminderMinutesAfterMidnight = reminderMinutesAfterMidnight,
                    )
                }
        }
    }
}
