package uk.co.inkbinder.noto.data.local.db.entity

import androidx.room.Entity

@Entity(
    tableName = "day_tag_cross_refs",
    primaryKeys = ["date", "tagId"],
)
data class DayTagCrossRef(
    val date: String,
    val tagId: String,
)

