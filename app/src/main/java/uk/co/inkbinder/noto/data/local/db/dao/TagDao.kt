package uk.co.inkbinder.noto.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uk.co.inkbinder.noto.data.local.db.entity.TagEntity

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY sortOrder ASC, label ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE isArchived = 0 ORDER BY sortOrder ASC, label ASC")
    fun observeActive(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY sortOrder ASC, label ASC")
    suspend fun getAll(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE isArchived = 0 ORDER BY sortOrder ASC, label ASC")
    suspend fun getActive(): List<TagEntity>

    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Upsert
    suspend fun upsertAll(tags: List<TagEntity>)

    @Query("UPDATE tags SET isArchived = :archived, isPeriodTag = CASE WHEN :archived = 1 THEN 0 ELSE isPeriodTag END WHERE id = :tagId")
    suspend fun setArchived(tagId: String, archived: Boolean)

    @Query("UPDATE tags SET isPeriodTag = 0")
    suspend fun clearPeriodTags()
}
