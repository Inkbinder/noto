package uk.co.inkbinder.noto

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import uk.co.inkbinder.noto.data.bootstrap.SeedDataInitializer
import uk.co.inkbinder.noto.di.AppContainer

class NotoApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            SeedDataInitializer(
                tagRepository = appContainer.tagRepository,
                userPreferencesRepository = appContainer.userPreferencesRepository,
            ).seedDefaultsIfNeeded()
        }
    }
}

