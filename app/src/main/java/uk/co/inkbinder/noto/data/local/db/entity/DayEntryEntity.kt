package uk.co.inkbinder.noto.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_entries")
data class DayEntryEntity(
    @PrimaryKey val date: String,
    val note: String? = null,
    val updatedAt: String,
)

