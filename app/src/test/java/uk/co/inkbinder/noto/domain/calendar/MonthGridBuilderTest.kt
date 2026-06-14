package uk.co.inkbinder.noto.domain.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthGridBuilderTest {
    @Test
    fun build_returnsSixWeeksAndStartsOnConfiguredWeekday() {
        val weeks = MonthGridBuilder.build(
            month = YearMonth.of(2026, 6),
            weekStartsOn = DayOfWeek.SUNDAY,
            visibleTagColorsByDate = emptyMap(),
            today = LocalDate.of(2026, 6, 14),
            maxVisibleSlices = 6,
        )

        assertEquals(6, weeks.size)
        assertTrue(weeks.all { week -> week.size == 7 })
        assertEquals(LocalDate.of(2026, 5, 31), weeks.first().first().date)
        assertEquals(LocalDate.of(2026, 7, 11), weeks.last().last().date)
        assertFalse(weeks.first().first().inCurrentMonth)
        assertFalse(weeks.last().last().inCurrentMonth)
    }

    @Test
    fun build_marksTodayAndCapsVisibleSlices() {
        val targetDate = LocalDate.of(2026, 6, 14)
        val colors = listOf(
            "#110000",
            "#220000",
            "#330000",
            "#440000",
            "#550000",
            "#660000",
            "#770000",
        )

        val weeks = MonthGridBuilder.build(
            month = YearMonth.of(2026, 6),
            weekStartsOn = DayOfWeek.MONDAY,
            visibleTagColorsByDate = mapOf(targetDate.toString() to colors),
            today = targetDate,
            maxVisibleSlices = 6,
        )

        val day = weeks.flatten().first { summary -> summary.date == targetDate }

        assertTrue(day.inCurrentMonth)
        assertTrue(day.isToday)
        assertEquals(colors.take(6), day.visibleTagColors)
        assertEquals(1, day.overflowCount)
    }
}
