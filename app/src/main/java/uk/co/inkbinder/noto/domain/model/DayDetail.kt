package uk.co.inkbinder.noto.domain.model

import java.time.LocalDate

data class DayDetail(
    val date: LocalDate,
    val availableTags: List<Tag>,
    val selectedTagIds: Set<String>,
)

