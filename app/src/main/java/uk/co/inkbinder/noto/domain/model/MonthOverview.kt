package uk.co.inkbinder.noto.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class MonthOverview(
    val month: YearMonth,
    val bannerText: String,
    val weeks: List<List<CalendarDaySummary>>,
    val weekStartsOn: DayOfWeek,
)

data class CalendarDaySummary(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
    val isToday: Boolean,
    val visibleTagColors: List<String>,
    val overflowCount: Int,
)

