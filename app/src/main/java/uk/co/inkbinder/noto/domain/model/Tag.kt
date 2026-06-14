package uk.co.inkbinder.noto.domain.model

data class Tag(
    val id: String,
    val label: String,
    val colorHex: String,
    val isPeriodTag: Boolean,
    val isArchived: Boolean,
    val sortOrder: Int,
)

