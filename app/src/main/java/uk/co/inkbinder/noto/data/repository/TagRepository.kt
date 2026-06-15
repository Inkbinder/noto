package uk.co.inkbinder.noto.data.repository

import java.util.UUID
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

    suspend fun renameTagLabel(tagId: String, label: String) {
        val existing = tagDao.getAll().firstOrNull { entity -> entity.id == tagId } ?: return
        val normalizedLabel = label.trim()
        if (normalizedLabel.isEmpty() || existing.label == normalizedLabel) return

        tagDao.upsert(existing.copy(label = normalizedLabel))
    }

    suspend fun saveTag(
        tagId: String?,
        label: String,
        colorHex: String,
        isPeriodTag: Boolean,
    ) {
        val trimmedLabel = label.trim()
        require(trimmedLabel.isNotEmpty()) { "Tag label cannot be blank." }

        val existingTags = tagDao.getAll()
        val existing = tagId?.let { id -> existingTags.firstOrNull { entity -> entity.id == id } }
        val normalizedTag = TagEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            label = trimmedLabel,
            colorHex = colorHex,
            isPeriodTag = isPeriodTag && (existing?.isArchived != true),
            isArchived = existing?.isArchived ?: false,
            sortOrder = existing?.sortOrder ?: ((existingTags.maxOfOrNull(TagEntity::sortOrder) ?: -1) + 1),
        )

        if (normalizedTag.isPeriodTag) {
            tagDao.clearPeriodTags()
        }

        tagDao.upsert(normalizedTag)
    }

    suspend fun archiveTag(tagId: String) {
        tagDao.setArchived(tagId, true)
    }

    suspend fun restoreTag(tagId: String) {
        val restoredSortOrder = ((tagDao.getActive().maxOfOrNull(TagEntity::sortOrder) ?: -1) + 1)
        val tag = tagDao.getAll().firstOrNull { entity -> entity.id == tagId } ?: return
        tagDao.upsert(
            tag.copy(
                isArchived = false,
                isPeriodTag = false,
                sortOrder = restoredSortOrder,
            ),
        )
    }

    suspend fun moveTagEarlier(tagId: String) {
        moveTag(tagId = tagId, direction = -1)
    }

    suspend fun moveTagLater(tagId: String) {
        moveTag(tagId = tagId, direction = 1)
    }

    private suspend fun moveTag(tagId: String, direction: Int) {
        val activeTags = tagDao.getActive().sortedBy(TagEntity::sortOrder)
        val currentIndex = activeTags.indexOfFirst { tag -> tag.id == tagId }
        if (currentIndex == -1) return

        val targetIndex = currentIndex + direction
        if (targetIndex !in activeTags.indices) return

        val current = activeTags[currentIndex]
        val target = activeTags[targetIndex]
        tagDao.upsert(current.copy(sortOrder = target.sortOrder))
        tagDao.upsert(target.copy(sortOrder = current.sortOrder))
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
