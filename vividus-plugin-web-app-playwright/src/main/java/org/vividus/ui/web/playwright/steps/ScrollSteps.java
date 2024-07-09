/*
 * Copyright 2019-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.ui.web.playwright.steps;

import com.microsoft.playwright.Locator;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.web.ScrollDirection;
import org.vividus.ui.web.action.DirectionScroller;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.PlaywrightUiContextScroller;
import org.vividus.ui.web.playwright.action.ScrollActions;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class ScrollSteps
{
    private final ISoftAssert softAssert;
    private final UiContext uiContext;
    private final ScrollActions scrollActions;

    public ScrollSteps(ISoftAssert softAssert, UiContext uiContext, PlaywrightSoftAssert playwrightSoftAssert,
            ScrollActions scrollActions)
    {
        this.softAssert = softAssert;
        this.uiContext = uiContext;
        this.scrollActions = scrollActions;
    }

    /**
     * Scrolls current context to the picked direction;
     * If context is not set scroll will be applied to a page;
     * @param scrollDirection to apply scroll
     * <br>Possible directions:
     * <br><b>LEFT</b> - start of a page/element horizontally,
     * <br><b>RIGHT</b> - end of a page/element horizontally,
     * <br><b>TOP</b> - start of a page/element vertically,
     * <br><b>BOTTOM</b> - end of a page/element vertically
     */
    @When("I scroll context to $scrollDirection edge")
    public void scrollContextIn(ScrollDirection scrollDirection)
    {
        DirectionScroller scroller = new PlaywrightUiContextScroller(uiContext, scrollActions);
        scrollDirection.scroll(scroller);
    }

    /**
     * Finds an element and scrolls it into view with centred positioning.
     * <br>
     * Please note that if the element to scroll is located inside an
     * <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/overflow">overflow</a> container then native JS
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView">scrollIntoView</a> method
     * with top alignment is used.
     *
     * @param locator The locator of the element to scroll into view
     */
    @When("I scroll element located by `$locator` into view")
    public void scrollIntoView(PlaywrightLocator locator)
    {
        Locator element = uiContext.locateElement(locator);
        scrollActions.scrollElementIntoViewportCenter(element);
    }

    /**
     * Checks if the page is scrolled to the specific element located by locator.
     * <br>Example: &lt;a id="information_collection" name="information_collection_name"&gt; -
     * Then page is scrolled to element located by `id(information_collection)`
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Checks whether page is scrolled to the very bottom
     * <li>If yes --&gt; verify that element's Y coordinate is positive which means that element is visible if no --&gt;
     * get element's Y coordinate and verify that it's close to 0 which means that element is an the very top
     * </ul>
     * @param locator A locator to locate element
     */
    @Then("page is scrolled to element located by `$locator`")
    public void isPageScrolledToElement(PlaywrightLocator locator)
    {
        Locator element = uiContext.locateElement(locator);
        softAssert.assertTrue(String.format("The page is scrolled to an element with located by %s", locator),
                scrollActions.isScrolledToElement(element));
    }
}
