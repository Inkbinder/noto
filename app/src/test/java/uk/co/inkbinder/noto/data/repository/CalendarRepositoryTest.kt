package uk.co.inkbinder.noto.data.repository

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import uk.co.inkbinder.noto.data.local.db.NotoDatabase
import uk.co.inkbinder.noto.data.local.db.entity.DayTagCrossRef
import uk.co.inkbinder.noto.data.local.db.entity.TagEntity
import uk.co.inkbinder.noto.data.preferences.UserPreferencesRepository
import uk.co.inkbinder.noto.domain.model.WeekStart
import uk.co.inkbinder.noto.test.PreferenceDataStoreTestHarness

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CalendarRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var database: NotoDatabase
    private lateinit var dataStoreHarness: PreferenceDataStoreTestHarness
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var repository: CalendarRepository

    private val clock: Clock = Clock.fixed(
        Instant.parse("2026-06-14T09:00:00Z"),
        ZoneOffset.UTC,
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotoDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dataStoreHarness = PreferenceDataStoreTestHarness(newRootDirectory())
        userPreferencesRepository = UserPreferencesRepository(dataStoreHarness.dataStore)
        repository = CalendarRepository(
            dayEntryDao = database.dayEntryDao(),
            tagDao = database.tagDao(),
            userPreferencesRepository = userPreferencesRepository,
            clock = clock,
        )
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreHarness.close()
    }

    @Test
    fun toggleTag_addsAndRemovesRefsAndDeletesEmptyEntry() = runBlocking {
        val date = LocalDate.of(2026, 6, 14)
        database.tagDao().upsert(
            TagEntity(
                id = "hangover",
                label = "Hangover",
                colorHex = "#FF0000",
                isPeriodTag = false,
                isArchived = false,
                sortOrder = 0,
            ),
        )

        repository.toggleTag(date, "hangover")

        assertEquals(1, database.dayEntryDao().countRef(date.toString(), "hangover"))
        assertEquals(1, countDayEntries(date))

        repository.toggleTag(date, "hangover")

        assertEquals(0, database.dayEntryDao().countRef(date.toString(), "hangover"))
        assertEquals(0, countDayEntries(date))
    }

    @Test
    fun observeDayDetail_hidesArchivedTagsFromAvailableChoices() = runBlocking {
        val date = LocalDate.of(2026, 6, 14)
        database.tagDao().upsertAll(
            listOf(
                TagEntity(
                    id = "active",
                    label = "Active",
                    colorHex = "#00AA00",
                    isPeriodTag = false,
                    isArchived = false,
                    sortOrder = 0,
                ),
                TagEntity(
                    id = "archived",
                    label = "Archived",
                    colorHex = "#AA0000",
                    isPeriodTag = false,
                    isArchived = true,
                    sortOrder = 1,
                ),
            ),
        )
        database.dayEntryDao().insertRef(DayTagCrossRef(date = date.toString(), tagId = "active"))
        database.dayEntryDao().insertRef(DayTagCrossRef(date = date.toString(), tagId = "archived"))

        val detail = repository.observeDayDetail(date).first()

        assertEquals(listOf("active"), detail.availableTags.map { tag -> tag.id })
        assertEquals(setOf("active", "archived"), detail.selectedTagIds)
    }

    @Test
    fun observeMonthOverview_sortsVisibleColorsAndReportsOverflow() = runBlocking {
        userPreferencesRepository.setWeekStartsOn(WeekStart.SUNDAY)

        val visibleDate = LocalDate.of(2026, 6, 14)
        val activeTags = listOf(
            TagEntity("tag-1", "Tag 1", "#110000", false, false, 1),
            TagEntity("tag-2", "Tag 2", "#220000", false, false, 2),
            TagEntity("tag-3", "Tag 3", "#330000", false, false, 3),
            TagEntity("tag-4", "Tag 4", "#440000", false, false, 4),
            TagEntity("tag-5", "Tag 5", "#550000", false, false, 5),
            TagEntity("tag-6", "Tag 6", "#660000", false, false, 6),
            TagEntity("tag-7", "Tag 7", "#770000", false, false, 7),
        )
        val archivedTag = TagEntity("archived", "Archived", "#000000", false, true, 0)
        val bleedingTag = TagEntity("bleeding", "Bleeding", "#CC0000", true, false, 100)

        database.tagDao().upsertAll(activeTags + archivedTag + bleedingTag)
        activeTags.forEach { tag ->
            database.dayEntryDao().insertRef(DayTagCrossRef(date = visibleDate.toString(), tagId = tag.id))
        }
        database.dayEntryDao().insertRef(DayTagCrossRef(date = visibleDate.toString(), tagId = archivedTag.id))
        database.dayEntryDao().insertRef(DayTagCrossRef(date = "2026-05-01", tagId = bleedingTag.id))
        database.dayEntryDao().insertRef(DayTagCrossRef(date = "2026-05-29", tagId = bleedingTag.id))

        val overview = repository.observeMonthOverview(YearMonth.of(2026, 6)).first()
        val day = overview.weeks.flatten().first { summary -> summary.date == visibleDate }

        assertEquals("Period due in 12 days", overview.bannerText)
        assertEquals(DayOfWeek.SUNDAY, overview.weekStartsOn)
        assertEquals(LocalDate.of(2026, 5, 31), overview.weeks.first().first().date)
        assertTrue(day.isToday)
        assertTrue(day.inCurrentMonth)
        assertEquals(activeTags.take(6).map { tag -> tag.colorHex }, day.visibleTagColors)
        assertEquals(1, day.overflowCount)
        assertFalse(day.visibleTagColors.contains(archivedTag.colorHex))
    }

    private fun countDayEntries(date: LocalDate): Int = database
        .query(
            SimpleSQLiteQuery(
                "SELECT COUNT(*) FROM day_entries WHERE date = ?",
                arrayOf(date.toString()),
            ),
        )
        .use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun newRootDirectory(): File = temporaryFolder.newFolder()
}
