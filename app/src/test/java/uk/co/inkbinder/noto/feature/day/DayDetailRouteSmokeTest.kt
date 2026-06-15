package uk.co.inkbinder.noto.feature.day

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
class DayDetailRouteSmokeTest {
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
    fun dayDetailRoute_rendersSeededTagsForTheSelectedDay() {
        val dayKey = "2026-06-14"
        val title = LocalDate.parse(dayKey).format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))

        composeRule.setContent {
            NotoTheme(dynamicColor = false) {
                DayDetailRoute(
                    appContainer = harness.appContainer,
                    dayKey = dayKey,
                    onBack = {},
                )
            }
        }

        waitForText(title)
        composeRule.onNodeWithText("Select all tags that apply to this day.").assertExists()
        composeRule.onNodeWithText("Period").assertExists()
        composeRule.onNodeWithText("Bad hangover").assertExists()
        composeRule.onNodeWithText("No hangover").assertExists()
        composeRule.onNodeWithText("Dodgy poops").assertExists()
        composeRule.onNodeWithText("Lightheaded").assertExists()
        composeRule.onNodeWithText("Used for period prediction").assertExists()
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = TimeUnit.SECONDS.toMillis(5)) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(text).assertExists()
    }
}
