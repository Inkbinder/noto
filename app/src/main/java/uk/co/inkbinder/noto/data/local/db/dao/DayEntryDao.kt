package uk.co.inkbinder.noto.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uk.co.inkbinder.noto.data.local.db.entity.DayEntryEntity
import uk.co.inkbinder.noto.data.local.db.entity.DayTagCrossRef

@Dao
interface DayEntryDao {
    @Query("SELECT * FROM day_tag_cross_refs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeRefsBetween(startDate: String, endDate: String): Flow<List<DayTagCrossRef>>

    @Query("SELECT * FROM day_tag_cross_refs WHERE date = :date ORDER BY tagId ASC")
    fun observeRefsForDate(date: String): Flow<List<DayTagCrossRef>>

    @Upsert
    suspend fun upsertEntry(entity: DayEntryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRef(ref: DayTagCrossRef)

    @Query("DELETE FROM day_tag_cross_refs WHERE date = :date AND tagId = :tagId")
    suspend fun deleteRef(date: String, tagId: String)

    @Query("SELECT COUNT(*) FROM day_tag_cross_refs WHERE date = :date AND tagId = :tagId")
    suspend fun countRef(date: String, tagId: String): Int

    @Query("DELETE FROM day_entries WHERE date = :date AND NOT EXISTS (SELECT 1 FROM day_tag_cross_refs WHERE date = :date)")
    suspend fun deleteEntryIfEmpty(date: String)

    @Query("SELECT DISTINCT date FROM day_tag_cross_refs WHERE tagId = :tagId ORDER BY date ASC")
    suspend fun getDatesForTag(tagId: String): List<String>

    @Query("DELETE FROM day_tag_cross_refs WHERE tagId = :tagId")
    suspend fun deleteRefsForTag(tagId: String)

    @Query("DELETE FROM day_entries WHERE NOT EXISTS (SELECT 1 FROM day_tag_cross_refs WHERE day_tag_cross_refs.date = day_entries.date)")
    suspend fun deleteEntriesWithoutRefs()

    @Query(
        """
        SELECT refs.date
        FROM day_tag_cross_refs AS refs
        INNER JOIN tags AS tags ON tags.id = refs.tagId
        WHERE tags.isPeriodTag = 1
        ORDER BY refs.date ASC
        """,
    )
    fun observePeriodDates(): Flow<List<String>>

    @Query(
        """
        SELECT refs.date
        FROM day_tag_cross_refs AS refs
        INNER JOIN tags AS tags ON tags.id = refs.tagId
        WHERE tags.isPeriodTag = 1
        ORDER BY refs.date ASC
        """,
    )
    suspend fun getPeriodDates(): List<String>
}
