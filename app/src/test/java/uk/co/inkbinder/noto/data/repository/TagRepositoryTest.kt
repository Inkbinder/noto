package uk.co.inkbinder.noto.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.co.inkbinder.noto.data.local.db.NotoDatabase
import uk.co.inkbinder.noto.data.local.db.entity.TagEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TagRepositoryTest {
    private lateinit var database: NotoDatabase
    private lateinit var repository: TagRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TagRepository(database.tagDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveTag_createsAndUpdatesATag() = runBlocking {
        repository.saveTag(
            tagId = null,
            label = " Migraine ",
            colorHex = "#445566",
            isPeriodTag = false,
        )

        val created = repository.observeAllTags().first().single()
        assertEquals("Migraine", created.label)
        assertEquals("#445566", created.colorHex)
        assertEquals(0, created.sortOrder)
        assertFalse(created.isPeriodTag)

        repository.saveTag(
            tagId = created.id,
            label = "Headache",
            colorHex = "#112233",
            isPeriodTag = true,
        )

        val updated = repository.observeAllTags().first().single()
        assertEquals("Headache", updated.label)
        assertEquals("#112233", updated.colorHex)
        assertTrue(updated.isPeriodTag)
        assertFalse(updated.isArchived)
    }

    @Test
    fun saveTag_selectingPeriodTagClearsThePreviousOne() = runBlocking {
        database.tagDao().upsertAll(
            listOf(
                TagEntity("bleeding", "Period", "#D86A6A", isPeriodTag = true, isArchived = false, sortOrder = 0),
                TagEntity("spotting", "Spotting", "#F28A8A", isPeriodTag = false, isArchived = false, sortOrder = 1),
            ),
        )

        repository.saveTag(
            tagId = "spotting",
            label = "Spotting",
            colorHex = "#F28A8A",
            isPeriodTag = true,
        )

        val tagsById = repository.observeAllTags().first().associateBy { tag -> tag.id }
        assertFalse(tagsById.getValue("bleeding").isPeriodTag)
        assertTrue(tagsById.getValue("spotting").isPeriodTag)
    }

    @Test
    fun archiveAndRestoreTag_movesItBetweenSections() = runBlocking {
        database.tagDao().upsert(
            TagEntity("lightheaded", "Lightheaded", "#6FA8A4", isPeriodTag = true, isArchived = false, sortOrder = 4),
        )

        repository.archiveTag("lightheaded")

        val archived = repository.observeAllTags().first().single()
        assertTrue(archived.isArchived)
        assertFalse(archived.isPeriodTag)
        assertTrue(repository.observeActiveTags().first().isEmpty())

        repository.restoreTag("lightheaded")

        val restored = repository.observeAllTags().first().single()
        assertFalse(restored.isArchived)
        assertFalse(restored.isPeriodTag)
        assertEquals(listOf("lightheaded"), repository.observeActiveTags().first().map { tag -> tag.id })
    }

    @Test
    fun moveTagEarlierAndLater_swapsSortOrderWithinActiveTags() = runBlocking {
        database.tagDao().upsertAll(
            listOf(
                TagEntity("first", "First", "#111111", isPeriodTag = false, isArchived = false, sortOrder = 0),
                TagEntity("second", "Second", "#222222", isPeriodTag = false, isArchived = false, sortOrder = 1),
                TagEntity("third", "Third", "#333333", isPeriodTag = false, isArchived = false, sortOrder = 2),
            ),
        )

        repository.moveTagLater("first")
        assertEquals(
            listOf("second", "first", "third"),
            repository.observeActiveTags().first().map { tag -> tag.id },
        )

        repository.moveTagEarlier("third")
        assertEquals(
            listOf("second", "third", "first"),
            repository.observeActiveTags().first().map { tag -> tag.id },
        )
    }
}
