package uk.co.inkbinder.noto.feature.tags

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
class TagsRouteSmokeTest {
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
    fun tagsRoute_canCreateAndEditATag() {
        composeRule.setContent {
            NotoTheme(dynamicColor = false) {
                TagsRoute(appContainer = harness.appContainer)
            }
        }

        waitForText("Period")
        composeRule.onNodeWithContentDescription("Add tag").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Migraine")
        composeRule.onNodeWithText("Create").performClick()

        scrollToText("Migraine")
        waitForText("Migraine")
        composeRule.onNodeWithContentDescription("Edit Migraine").performClick()
        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput("Headache")
        composeRule.onNodeWithText("Save").performClick()

        scrollToText("Headache")
        waitForText("Headache")
    }

    @Test
    fun tagsRoute_showsAnArchivedTagSection() = runBlocking {
        harness.appContainer.tagRepository.archiveTag("no_hangover")

        composeRule.setContent {
            NotoTheme(dynamicColor = false) {
                TagsRoute(appContainer = harness.appContainer)
            }
        }

        scrollToText("Archived tags")
        waitForText("Archived tags")
        scrollToContentDescription("Restore No hangover")
        waitForContentDescription("Restore No hangover")
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = TimeUnit.SECONDS.toMillis(5)) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(text).assertExists()
    }

    private fun waitForContentDescription(contentDescription: String) {
        composeRule.waitUntil(timeoutMillis = TimeUnit.SECONDS.toMillis(5)) {
            composeRule.onAllNodesWithContentDescription(contentDescription).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(contentDescription).assertExists()
    }

    private fun scrollToText(text: String) {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(text))
    }

    private fun scrollToContentDescription(contentDescription: String) {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasContentDescription(contentDescription))
    }
}
