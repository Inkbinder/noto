package uk.co.inkbinder.noto.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.inkbinder.noto.data.local.db.dao.TagDao
import uk.co.inkbinder.noto.data.local.db.entity.TagEntity
import uk.co.inkbinder.noto.domain.model.Tag

class TagRepository(private val tagDao: TagDao) {
    fun observeActiveTags(): Flow<List<Tag>> = tagDao.observeActive().map { entities ->
        entities.map { entity -> entity.toModel() }
    }

    fun observeAllTags(): Flow<List<Tag>> = tagDao.observeAll().map { entities ->
        entities.map { entity -> entity.toModel() }
    }

    suspend fun upsertDefaults(tags: List<Tag>) {
        tagDao.upsertAll(tags.map { tag -> tag.toEntity() })
    }

    private fun TagEntity.toModel(): Tag = Tag(
        id = id,
        label = label,
        colorHex = colorHex,
        isPeriodTag = isPeriodTag,
        isArchived = isArchived,
        sortOrder = sortOrder,
    )

    private fun Tag.toEntity(): TagEntity = TagEntity(
        id = id,
        label = label,
        colorHex = colorHex,
        isPeriodTag = isPeriodTag,
        isArchived = isArchived,
        sortOrder = sortOrder,
    )
}
