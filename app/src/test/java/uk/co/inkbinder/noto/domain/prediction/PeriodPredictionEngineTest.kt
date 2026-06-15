package uk.co.inkbinder.noto.domain.prediction

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.co.inkbinder.noto.domain.model.UserPreferences

class PeriodPredictionEngineTest {
    private val engine = PeriodPredictionEngine()

    @Test
    fun buildBanner_returnsPredictionOffWhenDisabled() {
        val banner = engine.buildBanner(
            periodDateStrings = listOf("2026-06-01"),
            preferences = UserPreferences(periodPredictionEnabled = false),
            today = LocalDate.of(2026, 6, 14),
        )

        assertEquals("Prediction off", banner)
    }

    @Test
    fun buildBanner_showsCurrentPeriodDayWhenTodayIsLogged() {
        val banner = engine.buildBanner(
            periodDateStrings = listOf(
                "2026-06-14",
                "2026-06-13",
                "2026-06-12",
            ),
            preferences = UserPreferences(),
            today = LocalDate.of(2026, 6, 14),
        )

        assertEquals("Period. Day 3", banner)
    }

    @Test
    fun buildBanner_showsCurrentPeriodDayEvenWhenPredictionIsOff() {
        val banner = engine.buildBanner(
            periodDateStrings = listOf("2026-06-14"),
            preferences = UserPreferences(periodPredictionEnabled = false),
            today = LocalDate.of(2026, 6, 14),
        )

        assertEquals("Period. Day 1", banner)
    }

    @Test
    fun buildBanner_returnsNoPredictionYetWithoutHistory() {
        val banner = engine.buildBanner(
            periodDateStrings = emptyList(),
            preferences = UserPreferences(),
            today = LocalDate.of(2026, 6, 14),
        )

        assertEquals("No prediction yet", banner)
    }

    @Test
    fun buildBanner_usesDefaultCycleLengthWhenOnlyOnePeriodStartExists() {
        val banner = engine.buildBanner(
            periodDateStrings = listOf("2026-06-01"),
            preferences = UserPreferences(defaultCycleLengthDays = 30),
            today = LocalDate.of(2026, 6, 20),
        )

        assertEquals("Period due in 11 days", banner)
    }

    @Test
    fun buildBanner_collapsesConsecutiveDaysBeforeCalculatingAverage() {
        val banner = engine.buildBanner(
            periodDateStrings = listOf(
                "2026-05-29",
                "2026-05-30",
                "2026-05-01",
                "2026-05-02",
                "2026-05-02",
            ),
            preferences = UserPreferences(),
            today = LocalDate.of(2026, 6, 26),
        )

        assertEquals("Period due today", banner)
    }

    @Test
    fun buildBanner_reportsDelayAfterPredictedDate() {
        val banner = engine.buildBanner(
            periodDateStrings = listOf(
                "2026-05-01",
                "2026-05-29",
            ),
            preferences = UserPreferences(),
            today = LocalDate.of(2026, 6, 30),
        )

        assertEquals("Delayed by 4 days", banner)
    }
}
