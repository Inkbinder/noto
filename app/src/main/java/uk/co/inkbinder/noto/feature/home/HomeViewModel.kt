package uk.co.inkbinder.noto.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import uk.co.inkbinder.noto.data.repository.CalendarRepository
import uk.co.inkbinder.noto.domain.model.MonthOverview

data class HomeUiState(
    val monthOverview: MonthOverview? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val calendarRepository: CalendarRepository,
) : ViewModel() {
    private val currentMonth = MutableStateFlow(YearMonth.now())

    val uiState = currentMonth
        .flatMapLatest { month ->
            calendarRepository.observeMonthOverview(month).map { overview ->
                HomeUiState(monthOverview = overview)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun showPreviousMonth() {
        currentMonth.update { it.minusMonths(1) }
    }

    fun showNextMonth() {
        currentMonth.update { it.plusMonths(1) }
    }
}
