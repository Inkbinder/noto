package uk.co.inkbinder.noto.background

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class PeriodReminderSchedulerTest {
    @Test
    fun nextReminderDelay_targetsNineAmLaterTheSameDay() {
        val delay = nextReminderDelay(
            reminderMinutesAfterMidnight = 9 * 60,
            now = ZonedDateTime.of(2026, 6, 15, 8, 30, 0, 0, ZoneId.of("Europe/London")),
        )

        assertEquals(30, delay.toMinutes())
    }

    @Test
    fun nextReminderDelay_rollsToTheNextDayAfterNineAm() {
        val delay = nextReminderDelay(
            reminderMinutesAfterMidnight = 9 * 60,
            now = ZonedDateTime.of(2026, 6, 15, 9, 30, 0, 0, ZoneId.of("Europe/London")),
        )

        assertEquals(23 * 60 + 30, delay.toMinutes())
    }

    @Test
    fun nextReminderDelay_usesTheConfiguredReminderTime() {
        val delay = nextReminderDelay(
            reminderMinutesAfterMidnight = 18 * 60 + 45,
            now = ZonedDateTime.of(2026, 6, 15, 18, 15, 0, 0, ZoneId.of("Europe/London")),
        )

        assertEquals(30, delay.toMinutes())
    }
}
