package uk.co.inkbinder.noto.data.preferences

import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uk.co.inkbinder.noto.domain.model.UserPreferences
import uk.co.inkbinder.noto.domain.model.WeekStart
import uk.co.inkbinder.noto.test.PreferenceDataStoreTestHarness

class UserPreferencesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStoreHarness: PreferenceDataStoreTestHarness
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        dataStoreHarness = PreferenceDataStoreTestHarness(newRootDirectory())
        repository = UserPreferencesRepository(dataStoreHarness.dataStore)
    }

    @After
    fun tearDown() {
        dataStoreHarness.close()
    }

    @Test
    fun userPreferences_returnsDefaultValues() = runBlocking {
        assertEquals(UserPreferences(), repository.userPreferences.first())
    }

    @Test
    fun setPredictionEnabled_persistsUpdatedValue() = runBlocking {
        repository.setPredictionEnabled(false)

        assertEquals(false, repository.userPreferences.first().periodPredictionEnabled)
    }

    @Test
    fun setPeriodReminderEnabled_time_andLastReminderKey_persistUpdatedValues() = runBlocking {
        repository.setPeriodReminderEnabled(true)
        repository.setPeriodReminderMinutesAfterMidnight(22 * 60 + 15)
        repository.setLastPeriodReminderKey("2026-06-14|2026-06-17")

        val preferences = repository.userPreferences.first()

        assertEquals(true, preferences.periodReminderEnabled)
        assertEquals(22 * 60 + 15, preferences.periodReminderMinutesAfterMidnight)
        assertEquals("2026-06-14|2026-06-17", preferences.lastPeriodReminderKey)
    }

    @Test
    fun setDefaultCycleLengthDays_clampsToSupportedRange() = runBlocking {
        repository.setDefaultCycleLengthDays(8)
        assertEquals(14, repository.userPreferences.first().defaultCycleLengthDays)

        repository.setDefaultCycleLengthDays(70)
        assertEquals(60, repository.userPreferences.first().defaultCycleLengthDays)
    }

    @Test
    fun setDefaultPeriodLengthDays_clampsToSupportedRange() = runBlocking {
        repository.setDefaultPeriodLengthDays(0)
        assertEquals(1, repository.userPreferences.first().defaultPeriodLengthDays)

        repository.setDefaultPeriodLengthDays(20)
        assertEquals(14, repository.userPreferences.first().defaultPeriodLengthDays)
    }

    @Test
    fun setWeekStartsOn_andMarkDefaultsSeeded_persistValues() = runBlocking {
        repository.setWeekStartsOn(WeekStart.SUNDAY)
        repository.markDefaultsSeeded(3)

        assertEquals(
            UserPreferences(
                weekStartsOn = WeekStart.SUNDAY,
                seededDefaultsVersion = 3,
            ),
            repository.userPreferences.first(),
        )
    }

    private fun newRootDirectory(): File = temporaryFolder.newFolder()
}
