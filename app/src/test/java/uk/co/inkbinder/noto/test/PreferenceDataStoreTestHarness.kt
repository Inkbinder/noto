package uk.co.inkbinder.noto.test

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class PreferenceDataStoreTestHarness(rootDirectory: File) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope) {
        rootDirectory.resolve("user-preferences-${UUID.randomUUID()}.preferences_pb")
    }

    fun close() {
        scope.cancel()
    }
}
