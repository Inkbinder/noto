package uk.co.inkbinder.noto.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val label: String,
    val colorHex: String,
    val isPeriodTag: Boolean,
    val isArchived: Boolean,
    val sortOrder: Int,
)

