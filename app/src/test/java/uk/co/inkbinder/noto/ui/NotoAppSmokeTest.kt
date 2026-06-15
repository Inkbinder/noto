package uk.co.inkbinder.noto.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.co.inkbinder.noto.test.AppContainerTestHarness
import uk.co.inkbinder.noto.ui.theme.NotoTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NotoAppSmokeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var harness: AppContainerTestHarness

    @Before
    fun setUp() = runBlocking {
        harness = AppContainerTestHarness(temporaryFolder.newFolder())
        harness.seedDefaults()
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun notoApp_showsHomeAndNavigatesAcrossTopLevelScreens() {
        composeRule.setContent {
            NotoTheme(dynamicColor = false) {
                NotoApp(appContainer = harness.appContainer)
            }
        }

        waitForText("No prediction yet")
        composeRule.onNodeWithText("Calendar").assertExists()

        composeRule.onNodeWithText("Tags").performClick()
        waitForText("Period")
        composeRule.onNodeWithText("Period").assertExists()

        composeRule.onNodeWithText("Settings").performClick()
        waitForText("Period prediction")
        composeRule.onNodeWithText("Period reminders").assertExists()
    }

    @Test
    fun notoApp_monthHeaderOpensPickerAndAppliesSelection() {
        val currentMonth = YearMonth.now()
        val targetMonth = if (currentMonth.monthValue == 1) {
            YearMonth.of(currentMonth.year, 2)
        } else {
            YearMonth.of(currentMonth.year, 1)
        }
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

        composeRule.setContent {
            NotoTheme(dynamicColor = false) {
                NotoApp(appContainer = harness.appContainer)
            }
        }

        waitForText(currentMonth.format(monthFormatter))
        composeRule.onNodeWithContentDescription("Choose month").performClick()
        waitForText("Choose month")
        composeRule.onNodeWithText(monthPickerLabel(targetMonth.month)).performClick()
        composeRule.onNodeWithText("Apply").performClick()

        waitForText(targetMonth.format(monthFormatter))
    }

    @Test
    fun notoApp_calendarSwipesBetweenMonths() {
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)
        val nextMonth = currentMonth.plusMonths(1)
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

        composeRule.setContent {
            NotoTheme(dynamicColor = false) {
                NotoApp(appContainer = harness.appContainer)
            }
        }

        waitForText(currentMonth.format(monthFormatter))
        composeRule.onNodeWithContentDescription("Calendar grid").performTouchInput { swipeLeft() }
        waitForText(nextMonth.format(monthFormatter))
        composeRule.onNodeWithContentDescription("Calendar grid").performTouchInput { swipeRight() }
        waitForText(currentMonth.format(monthFormatter))
        composeRule.onNodeWithContentDescription("Calendar grid").performTouchInput { swipeRight() }
        waitForText(previousMonth.format(monthFormatter))
    }

    private fun monthPickerLabel(month: Month): String =
        month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).replace(".", "").take(3)

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = TimeUnit.SECONDS.toMillis(5)) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(text).assertExists()
    }
}
