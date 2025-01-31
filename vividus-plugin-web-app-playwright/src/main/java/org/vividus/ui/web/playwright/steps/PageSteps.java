/*
 * Copyright 2019-2025 the original author or authors.
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

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.microsoft.playwright.Page;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.action.NavigateActions;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.util.UriUtils;

public class PageSteps
{
    private final BrowserContextProvider browserContextProvider;
    private final UiContext uiContext;
    private final NavigateActions navigateActions;
    private final WebApplicationConfiguration webApplicationConfiguration;
    private final ISoftAssert softAssert;

    public PageSteps(BrowserContextProvider browserContextProvider, UiContext uiContext,
            NavigateActions navigateActions,
            WebApplicationConfiguration webApplicationConfiguration, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.browserContextProvider = browserContextProvider;
        this.navigateActions = navigateActions;
        this.webApplicationConfiguration = webApplicationConfiguration;
        this.softAssert = softAssert;
    }

    /**
     * Navigates to the page which was configured as the main application page in the property with name
     * <code>web-application.main-page-url</code>.
     */
    @Given("I am on main application page")
    public void openMainApplicationPage()
    {
        openPage(webApplicationConfiguration.getMainApplicationPageUrl().toString());
    }

    /**
     * Navigates to the page with the given absolute URL, e.g. {@code https://docs.vividus.dev/}
     *
     * @param pageUrl An absolute URL of the page to navigate to.
     */
    @Given("I am on page with URL `$pageUrl`")
    public void openPage(String pageUrl)
    {
        Optional.ofNullable(uiContext.getCurrentPage()).orElseGet(this::openNewPage).navigate(pageUrl);
    }

    /**
     * Opens a new browser tab and switches the focus for future commands to this tab.
     */
    @When("I open new tab")
    public void openNewTab()
    {
        openNewPage();
    }

    /**
     * Closes <b>current tab</b> and switches to the tab from which redirection to current tab was performed
     * Actions performed at this step:
     * <ul>
     * <li>Receives all opened browser tabs
     * <li>Closes current tab
     * <li>Switches back to the last browser tab
     * </ul>
     */
    @When("I close current tab")
    public void closeCurrentTab()
    {
        Page currentPage = uiContext.getCurrentPage();
        List<Page> pages = browserContextProvider.get().pages();
        for (int i = pages.size() - 1; i >= 0; i--)
        {
            Page page = pages.get(i);
            if (!page.equals(currentPage))
            {
                currentPage.close();
                uiContext.reset();
                uiContext.setCurrentPage(page);
                break;
            }
        }
        softAssert.recordAssertion(!browserContextProvider.get().pages().contains(currentPage),
                "Current tab has been closed");
    }

    /**
     * Reloads the current page: does the same as the reload button in the browser.
     */
    @When("I refresh page")
    public void refreshPage()
    {
        navigateActions.refresh();
    }

    /**
     * Navigates back to the previous page.
     */
    @When("I navigate back")
    public void navigateBack()
    {
        uiContext.getCurrentPage().goBack();
    }

    /**
     * Navigates to the page with the given relative URL.
     *
     * @param relativeUrl A relative URL pointing to a resource within a website (e.g. <i>'about.html'</i> or
     *                    <i>'/products'</i>). If the relative URL starts with '/' char, the navigation will be
     *                    performed from the root.
     *                    Otherwise, the navigation will be performed from the current URL path.
     *                    <table border="1">
     *                    <caption>Examples:</caption>
     *                    <tr>
     *                    <td>Current page</td><td>Relative URL parameter</td><td>Resulting page URL</td>
     *                    </tr>
     *                    <tr>
     *                    <td>https://mysite.com</td><td>about.html</td><td>https://mysite.com/about.html</td>
     *                    </tr>
     *                    <tr>
     *                    <td>https://mysite.com</td><td>/products/new</td><td>https://mysite.com/products/new</td>
     *                    </tr>
     *                    <tr>
     *                    <td>https://mysite.com/path/foo</td><td>stats</td><td>https://mysite.com/path/foo/stats</td>
     *                    </tr>
     *                    <tr>
     *                    <td>https://mysite.com/path/foo</td><td>./docs/info.html</td><td>https://mysite
     *                    .com/path/foo/docs/info.html</td>
     *                    </tr>
     *                    <tr>
     *                    <td>https://mysite.com/path/foo</td><td>/documents</td><td>https://mysite.com/documents</td>
     *                    </tr>
     *                    </table>
     */
    @When("I go to relative URL `$relativeUrl`")
    public void openRelativeUrl(String relativeUrl)
    {
        URI currentURI = UriUtils.createUri(uiContext.getCurrentPage().url());
        URI newURI = UriUtils.buildNewRelativeUrl(currentURI, relativeUrl);
        openPage(newURI.toString());
    }

    /**
     * Checks the page title matches the text according to the provided validation rule.
     *
     * @param comparisonRule The page title validation rule. One of the following options:
     *                       <ul>
     *                       <li><code>is equal to</code> - validate the page title is equal to <code>$text</code>
     *                       parameter,</li>
     *                       <li><code>contains</code> - validate the page title title contains the string from
     *                       <code>$text</code> parameter,</li>
     *                       <li><code>does not contain</code> - validate the page title title does not contain the
     *                       value from <code>$text</code> parameter.</li>
     *                       </ul>
     * @param text           The text to match according to the rule.
     */
    @Then("page title $comparisonRule `$text`")
    public void assertPageTitle(StringComparisonRule comparisonRule, String text)
    {
        softAssert.assertThat("Page title", uiContext.getCurrentPage().title(), comparisonRule.createMatcher(text));
    }

    private Page openNewPage()
    {
        uiContext.reset();
        Page page = browserContextProvider.get().newPage();
        uiContext.setCurrentPage(page);
        page.onFrameNavigated(frame -> {
            if (frame.parentFrame() == null)
            {
                uiContext.reset();
            }
        });
        return page;
    }
}
