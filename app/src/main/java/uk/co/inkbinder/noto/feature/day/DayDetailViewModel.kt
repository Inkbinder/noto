package uk.co.inkbinder.noto.feature.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.co.inkbinder.noto.data.repository.CalendarRepository
import uk.co.inkbinder.noto.domain.model.Tag

data class DayDetailUiState(
    val date: LocalDate? = null,
    val availableTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<String> = emptySet(),
)

class DayDetailViewModel(
    private val date: LocalDate,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {
    val uiState = calendarRepository.observeDayDetail(date)
        .map { detail ->
            DayDetailUiState(
                date = detail.date,
                availableTags = detail.availableTags,
                selectedTagIds = detail.selectedTagIds,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DayDetailUiState(date = date),
        )

    fun toggleTag(tagId: String) {
        viewModelScope.launch {
            calendarRepository.toggleTag(date, tagId)
        }
    }
}

