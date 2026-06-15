package uk.co.inkbinder.noto.domain.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import uk.co.inkbinder.noto.domain.model.CalendarDaySummary

object MonthGridBuilder {
    fun build(
        month: YearMonth,
        weekStartsOn: DayOfWeek,
        visibleTagColorsByDate: Map<String, List<String>>,
        predictedPeriodDates: Set<String>,
        predictedPeriodColorHex: String?,
        today: LocalDate,
        maxVisibleSlices: Int,
    ): List<List<CalendarDaySummary>> {
        val firstOfMonth = month.atDay(1)
        val firstOffset = ((firstOfMonth.dayOfWeek.value - weekStartsOn.value) + 7) % 7
        val gridStart = firstOfMonth.minusDays(firstOffset.toLong())

        return (0 until 42)
            .map { dayIndex ->
                val date = gridStart.plusDays(dayIndex.toLong())
                val colors = visibleTagColorsByDate[date.toString()].orEmpty()
                CalendarDaySummary(
                    date = date,
                    inCurrentMonth = date.month == month.month,
                    isToday = date == today,
                    visibleTagColors = colors.take(maxVisibleSlices),
                    overflowCount = (colors.size - maxVisibleSlices).coerceAtLeast(0),
                    predictedPeriodColorHex = if (date.toString() in predictedPeriodDates) {
                        predictedPeriodColorHex
                    } else {
                        null
                    },
                )
            }
            .chunked(7)
    }
}
