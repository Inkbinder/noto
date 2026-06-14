package uk.co.inkbinder.noto.feature.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import java.util.concurrent.TimeUnit
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
class SettingsRouteSmokeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var harness: AppContainerTestHarness

    @Before
    fun setUp() {
        harness = AppContainerTestHarness(temporaryFolder.newFolder())
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun settingsRoute_updatesCycleLengthFromTheUi() {
        composeRule.setContent {
            NotoTheme(dynamicColor = false) {
                SettingsRoute(appContainer = harness.appContainer)
            }
        }

        waitForText("28 days")
        composeRule.onNodeWithText("Period prediction").assertExists()
        composeRule.onNodeWithText("+").performClick()
        waitForText("29 days")
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = TimeUnit.SECONDS.toMillis(5)) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(text).assertExists()
    }
}
