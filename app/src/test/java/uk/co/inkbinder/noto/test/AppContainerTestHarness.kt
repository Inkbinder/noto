package uk.co.inkbinder.noto.test

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.File
import uk.co.inkbinder.noto.data.bootstrap.SeedDataInitializer
import uk.co.inkbinder.noto.data.local.db.NotoDatabase
import uk.co.inkbinder.noto.data.preferences.UserPreferencesRepository
import uk.co.inkbinder.noto.di.AppContainer

internal class AppContainerTestHarness(rootDirectory: File) {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = Room.inMemoryDatabaseBuilder(context, NotoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val preferencesHarness = PreferenceDataStoreTestHarness(rootDirectory)
    private val userPreferencesRepository = UserPreferencesRepository(preferencesHarness.dataStore)

    val appContainer = AppContainer(
        context = context,
        database = database,
        userPreferencesRepository = userPreferencesRepository,
    )

    suspend fun seedDefaults() {
        SeedDataInitializer(
            tagRepository = appContainer.tagRepository,
            userPreferencesRepository = appContainer.userPreferencesRepository,
        ).seedDefaultsIfNeeded()
    }

    fun close() {
        database.close()
        preferencesHarness.close()
    }
}
