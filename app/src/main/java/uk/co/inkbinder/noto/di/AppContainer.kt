package uk.co.inkbinder.noto.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import uk.co.inkbinder.noto.background.PeriodReminderScheduler
import uk.co.inkbinder.noto.data.local.db.NotoDatabase
import uk.co.inkbinder.noto.data.preferences.UserPreferencesRepository
import uk.co.inkbinder.noto.data.repository.CalendarRepository
import uk.co.inkbinder.noto.data.repository.TagRepository

class AppContainer private constructor(
    context: Context,
    private val databaseFactory: (Context) -> NotoDatabase,
    private val userPreferencesRepositoryFactory: (Context) -> UserPreferencesRepository,
) {
    constructor(context: Context) : this(
        context = context,
        databaseFactory = { appContext ->
            Room.databaseBuilder(appContext, NotoDatabase::class.java, "noto.db").build()
        },
        userPreferencesRepositoryFactory = { appContext ->
            UserPreferencesRepository(appContext)
        },
    )

    internal constructor(
        context: Context,
        database: NotoDatabase,
        userPreferencesRepository: UserPreferencesRepository,
    ) : this(
        context = context,
        databaseFactory = { database },
        userPreferencesRepositoryFactory = { userPreferencesRepository },
    )

    private val appContext = context.applicationContext

    private val database: NotoDatabase by lazy {
        databaseFactory(appContext)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        userPreferencesRepositoryFactory(appContext)
    }

    val tagRepository: TagRepository by lazy {
        TagRepository(
            tagDao = database.tagDao(),
            dayEntryDao = database.dayEntryDao(),
        )
    }

    val calendarRepository: CalendarRepository by lazy {
        CalendarRepository(
            dayEntryDao = database.dayEntryDao(),
            tagDao = database.tagDao(),
            userPreferencesRepository = userPreferencesRepository,
        )
    }

    val periodReminderScheduler: PeriodReminderScheduler by lazy {
        PeriodReminderScheduler(WorkManager.getInstance(appContext))
    }
}
