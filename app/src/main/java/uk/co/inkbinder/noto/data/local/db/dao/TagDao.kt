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

    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Upsert
    suspend fun upsertAll(tags: List<TagEntity>)

    @Query("UPDATE tags SET isArchived = 1 WHERE id = :tagId")
    suspend fun archive(tagId: String)
}

