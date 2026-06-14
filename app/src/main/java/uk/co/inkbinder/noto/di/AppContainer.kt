package uk.co.inkbinder.noto.di

import android.content.Context
import androidx.room.Room
import uk.co.inkbinder.noto.data.local.db.NotoDatabase
import uk.co.inkbinder.noto.data.preferences.UserPreferencesRepository
import uk.co.inkbinder.noto.data.repository.CalendarRepository
import uk.co.inkbinder.noto.data.repository.TagRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: NotoDatabase by lazy {
        Room.databaseBuilder(appContext, NotoDatabase::class.java, "noto.db").build()
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    val tagRepository: TagRepository by lazy {
        TagRepository(database.tagDao())
    }

    val calendarRepository: CalendarRepository by lazy {
        CalendarRepository(
            dayEntryDao = database.dayEntryDao(),
            tagDao = database.tagDao(),
            userPreferencesRepository = userPreferencesRepository,
        )
    }
}

