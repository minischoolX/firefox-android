/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui.robots

import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mozilla.fenix.R
import org.mozilla.fenix.helpers.Constants
import org.mozilla.fenix.helpers.MatcherHelper.assertItemContainingTextExists
import org.mozilla.fenix.helpers.MatcherHelper.assertItemWithDescriptionExists
import org.mozilla.fenix.helpers.MatcherHelper.assertItemWithResIdAndTextExists
import org.mozilla.fenix.helpers.MatcherHelper.assertItemWithResIdExists
import org.mozilla.fenix.helpers.MatcherHelper.itemContainingText
import org.mozilla.fenix.helpers.MatcherHelper.itemWithDescription
import org.mozilla.fenix.helpers.MatcherHelper.itemWithResId
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTime
import org.mozilla.fenix.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.fenix.helpers.TestHelper.getStringResource
import org.mozilla.fenix.helpers.TestHelper.mDevice
import org.mozilla.fenix.helpers.TestHelper.packageName
import org.mozilla.fenix.helpers.click
import org.mozilla.fenix.helpers.ext.waitNotNull

/**
 * Implementation of Robot Pattern for the history menu.
 */
class HistoryRobot {

    fun verifyHistoryMenuView() = assertHistoryMenuView()

    fun verifyEmptyHistoryView() {
        mDevice.findObject(
            UiSelector().text("No history here"),
        ).waitForExists(waitingTime)

        assertEmptyHistoryView()
    }

    fun verifyHistoryListExists() = assertHistoryListExists()

    fun verifyVisitedTimeTitle() {
        mDevice.waitNotNull(
            Until.findObject(
                By.text("Today"),
            ),
            waitingTime,
        )
        assertVisitedTimeTitle()
    }

    fun verifyHistoryItemExists(shouldExist: Boolean, item: String) = assertHistoryItemExists(shouldExist, item)

    fun verifyFirstTestPageTitle(title: String) = assertTestPageTitle(title)

    fun verifyTestPageUrl(expectedUrl: Uri) = pageUrl(expectedUrl.toString()).check(matches(isDisplayed()))

    fun verifyCopySnackBarText() = assertCopySnackBarText()

    fun verifyDeleteConfirmationMessage() = assertDeleteConfirmationMessage()

    fun verifyHomeScreen() = HomeScreenRobot().verifyHomeScreen()

    fun clickDeleteHistoryButton(item: String) {
        deleteButton(item).click()
    }

    fun clickDeleteAllHistoryButton() = deleteButton().click()

    fun selectEverythingOption() = deleteHistoryEverythingOption().click()

    fun confirmDeleteAllHistory() {
        onView(withText("Delete"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .click()
    }

    fun cancelDeleteHistory() =
        mDevice
            .findObject(
                UiSelector()
                    .textContains(getStringResource(R.string.delete_browsing_data_prompt_cancel)),
            ).click()

    fun verifyDeleteSnackbarText(text: String) = assertSnackBarText(text)

    fun verifyUndoDeleteSnackBarButton() = assertUndoDeleteSnackBarButton()

    fun clickUndoDeleteButton() {
        snackBarUndoButton().click()
    }

    fun verifySearchGroupDisplayed(shouldBeDisplayed: Boolean, searchTerm: String, groupSize: Int) {
        // checks if the search group exists in the Recently visited section
        if (shouldBeDisplayed) {
            assertTrue(
                mDevice.findObject(UiSelector().text(searchTerm))
                    .getFromParent(UiSelector().text("$groupSize sites"))
                    .waitForExists(waitingTimeShort),
            )
        } else {
            assertFalse(
                mDevice.findObject(UiSelector().text(searchTerm))
                    .getFromParent(UiSelector().text("$groupSize sites"))
                    .waitForExists(waitingTimeShort),
            )
        }
    }

    fun clickSearchButton() = itemWithResId("$packageName:id/history_search").click()

    fun verifyHistorySearchBar(exists: Boolean) {
        assertItemWithResIdExists(
            itemWithResId("$packageName:id/toolbar"),
            itemWithResId("$packageName:id/mozac_browser_toolbar_edit_icon"),
            exists = exists,
        )
        assertItemWithResIdAndTextExists(
            itemWithResId("$packageName:id/mozac_browser_toolbar_edit_url_view"),
            itemContainingText(getStringResource(R.string.history_search_1)),
            exists = exists,
        )
        assertItemWithDescriptionExists(
            itemWithDescription(getStringResource(R.string.voice_search_content_description)),
            exists = exists,
        )
    }

    fun verifyHistorySearchBarPosition(defaultPosition: Boolean) {
        onView(withId(R.id.toolbar))
            .check(
                if (defaultPosition) {
                    PositionAssertions.isCompletelyBelow(withId(R.id.pill_wrapper_divider))
                } else {
                    PositionAssertions.isCompletelyAbove(withId(R.id.pill_wrapper_divider))
                },
            )
    }

    fun tapOutsideToDismissSearchBar() {
        itemWithResId("$packageName:id/search_wrapper").click()
        itemWithResId("$packageName:id/mozac_browser_toolbar_edit_url_view")
            .waitUntilGone(waitingTime)
    }

    fun dismissHistorySearchBarUsingBackButton() {
        for (i in 1..Constants.RETRY_COUNT) {
            try {
                mDevice.pressBack()
                assertTrue(
                    itemWithResId("$packageName:id/mozac_browser_toolbar_edit_url_view")
                        .waitUntilGone(waitingTime),
                )
                break
            } catch (e: AssertionError) {
                if (i == Constants.RETRY_COUNT) {
                    throw e
                }
            }
        }
    }

    fun searchForHistoryItem(vararg historyItems: String) {
        for (historyItem in historyItems) {
            itemWithResId("$packageName:id/mozac_browser_toolbar_edit_url_view").also {
                it.waitForExists(waitingTime)
                it.setText(historyItem)
            }
            mDevice.waitForWindowUpdate(packageName, waitingTimeShort)
        }
    }

    fun verifySearchedHistoryItemExists(historyItemUrl: String, exists: Boolean = true) =
        assertItemContainingTextExists(itemContainingText(historyItemUrl), exists = exists)

    class Transition {
        fun goBack(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            onView(withContentDescription("Navigate up")).click()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openWebsite(url: Uri, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            assertHistoryListExists()
            onView(withText(url.toString())).click()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openRecentlyClosedTabs(interact: RecentlyClosedTabsRobot.() -> Unit): RecentlyClosedTabsRobot.Transition {
            recentlyClosedTabsListButton.waitForExists(waitingTime)
            recentlyClosedTabsListButton.click()

            RecentlyClosedTabsRobot().interact()
            return RecentlyClosedTabsRobot.Transition()
        }
    }
}

fun historyMenu(interact: HistoryRobot.() -> Unit): HistoryRobot.Transition {
    HistoryRobot().interact()
    return HistoryRobot.Transition()
}

private fun testPageTitle() = onView(allOf(withId(R.id.title), withText("Test_Page_1")))

private fun pageUrl(url: String) = onView(allOf(withId(R.id.url), withText(url)))

private fun deleteButton(title: String) =
    onView(allOf(withContentDescription("Delete"), hasSibling(withText(title))))

private fun deleteButton() = onView(withId(R.id.history_delete))

private fun snackBarText() = onView(withId(R.id.snackbar_text))

private fun assertHistoryMenuView() {
    onView(
        allOf(withText("History"), withParent(withId(R.id.navigationToolbar))),
    )
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}

private fun assertEmptyHistoryView() =
    onView(
        allOf(
            withId(R.id.history_empty_view),
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
        ),
    )
        .check(matches(withText("No history here")))

private fun assertHistoryListExists() =
    mDevice.findObject(UiSelector().resourceId("$packageName:id/history_list")).waitForExists(waitingTime)

private fun assertHistoryItemExists(shouldExist: Boolean, item: String) {
    if (shouldExist) {
        assertTrue(mDevice.findObject(UiSelector().textContains(item)).waitForExists(waitingTime))
    } else {
        assertFalse(mDevice.findObject(UiSelector().textContains(item)).waitForExists(waitingTimeShort))
    }
}

private fun assertVisitedTimeTitle() =
    onView(withId(R.id.header_title)).check(matches(withText("Today")))

private fun assertTestPageTitle(title: String) = testPageTitle()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    .check(matches(withText(title)))

private fun assertDeleteConfirmationMessage() {
    assertTrue(deleteHistoryPromptTitle().waitForExists(waitingTime))
    assertTrue(deleteHistoryPromptSummary().waitForExists(waitingTime))
}

private fun assertCopySnackBarText() = snackBarText().check(matches(withText("URL copied")))

private fun assertSnackBarText(text: String) =
    snackBarText().check(matches(withText(Matchers.containsString(text))))

private fun snackBarUndoButton() = onView(withId(R.id.snackbar_btn))

private fun assertUndoDeleteSnackBarButton() =
    snackBarUndoButton().check(matches(withText("UNDO")))

private fun deleteHistoryPromptTitle() =
    mDevice
        .findObject(
            UiSelector()
                .textContains(getStringResource(R.string.delete_history_prompt_title))
                .resourceId("$packageName:id/title"),
        )

private fun deleteHistoryPromptSummary() =
    mDevice
        .findObject(
            UiSelector()
                .textContains(getStringResource(R.string.delete_history_prompt_body_2))
                .resourceId("$packageName:id/body"),
        )

private fun deleteHistoryEverythingOption() =
    mDevice
        .findObject(
            UiSelector()
                .textContains(getStringResource(R.string.delete_history_prompt_button_everything))
                .resourceId("$packageName:id/everything_button"),
        )

private val recentlyClosedTabsListButton =
    mDevice.findObject(UiSelector().resourceId("$packageName:id/recently_closed_tabs_header"))
