/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.steps.ui.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.http.client.IHttpClient;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.configuration.AuthenticationMode;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.listener.IWebApplicationListener;
import org.vividus.util.UriUtils;

@SuppressWarnings("MethodCount")
@TakeScreenshotOnFailure
public class PageSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSteps.class);
    private static final String FORWARD_SLASH = "/";
    private static final String PAGE_TITLE = "Page title";

    @Inject private IUiContext uiContext;
    @Inject private SetContextSteps setContextSteps;
    @Inject private IWebElementActions webElementActions;
    @Inject private INavigateActions navigateActions;
    @Inject private IBaseValidations baseValidations;
    @Inject private WebApplicationConfiguration webApplicationConfiguration;
    @Inject private IWebApplicationListener webApplicationListener;
    @Inject private IWebWaitActions waitActions;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;
    @Inject private IWebDriverManager webDriverManager;
    private IHttpClient httpClient;

    private boolean keepUserInfoForProtocolRedirects;

    /**
     * Loading the page which was set as a main application page.
     * <br>
     * One can set an URL for the main page in the properties file.
     */
    @Given("I am on the main application page")
    public void iAmOnTheMainApplicationPage()
    {
        loadApplicationPage(webApplicationConfiguration.getMainApplicationPageUrl());
    }

    /**
     * Loads a <b>page</b> with the given <b>URL</b>
     * <p>
     * Requires an <b>absolute</b> URL (like https://example.com/).
     * <p>
     * @param pageURL An <b>absolute</b> URL of the page
     */
    @Given("I am on a page with the URL '$pageURL'")
    public void iAmOnPage(String pageURL)
    {
        uiContext.reset();
        navigateActions.loadPage(pageURL);
    }

    /**
     * Checks, that the current page has a correct relative URL <br>
     * A <b>relative URL</b> - points to a file within a web site (like <i>'about.html'</i> or <i>'/products'</i>)<br>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Gets the absolute URL of the current page;
     * <li>Gets relative URL from it;
     * <li>Compares it with the specified relative URL.
     * </ul>
     * <p>
     * @param relativeURL A string value of the relative URL
     */
    @Then("the page has the relative URL '$relativeURL'")
    public void checkPageRelativeURL(String relativeURL)
    {
        URI url = UriUtils.createUri(getWebDriver().getCurrentUrl());
        // If web application under test is unavailable (no page is opened), an empty URL will be returned
        if (url.getPath() != null)
        {
            String expectedRelativeUrl = relativeURL.isEmpty() ? FORWARD_SLASH : relativeURL;
            descriptiveSoftAssert.assertEquals("Page has correct relative URL",
                    UriUtils.buildNewUrl(getWebDriver().getCurrentUrl(), expectedRelativeUrl), url);
            return;
        }
        descriptiveSoftAssert.recordFailedAssertion("URL path component is null");
    }

    /**
     * Checks, that the current page has a correct host
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Gets the absolute URL of the current page;
     * <li>Gets page host from it;
     * <li>Compares it with the specified page host.
     * </ul>
     * <p>
     * @param host A string value of the page host
     */
    @Then("the host of the page URL is '$host'")
    public void checkPageHost(String host)
    {
        URI url = UriUtils.createUri(getWebDriver().getCurrentUrl());
        descriptiveSoftAssert.assertEquals("Page has correct host", host, url.getHost());
    }

    /**
     * Refreshes the page
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Browser reloads current page which is the same action as one caused by pressing F5 on the keyboard.
     * </ul>
     */
    @When("I refresh the page")
    public void refreshPage()
    {
        uiContext.reset();
        navigateActions.refresh();
    }

    /**
     * Opens page with the given <b>pageUrl</b> in a new window object(tab)
     * @param pageUrl An absolute URL of the page
     */
    @When("I open URL `$pageUrl` in new window")
    public void openPageUrlInNewWindow(String pageUrl)
    {
        javascriptActions.openNewWindow();
        setContextSteps.switchingToWindow();
        iAmOnPage(pageUrl);
    }

    /**
     * Checks that the <b>page</b> was loaded less than in 'pageLoadTimeThreshold' <b>milliseconds</b>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Gets the <b>page's load time</b>
     * <li>Compares it with a 'pageLoadTimeThreshold'
     * </ul>
     * <p>
     * @param pageLoadTimeThreshold The time in <b>milliseconds</b> bigger than an expected <b>page's load time</b><br>
     */
    @Then("the page load time should be less than '$pageLoadTimeThreshold' milliseconds")
    public void thenTheLoadTimeShouldBeLessThan(long pageLoadTimeThreshold)
    {
        descriptiveSoftAssert.assertThat("The page load time is less than load time threshold.",
                String.format("The page load time is less than '%s'", pageLoadTimeThreshold),
                navigateActions.getActualPageLoadTimeInMs(), lessThan(pageLoadTimeThreshold));
    }

    /**
     * Checks, that the <b><i>page</i></b> with certain <b>URL</b> is loaded <br>
     * <b>URL</b> is the internet address of the current page which is located in the address bar
     * <p>
     * @param url String value of URL
     */
    @Then("the page with the URL '$URL' is loaded")
    public void checkUriIsLoaded(String url)
    {
        String actualUrl = getWebDriver().getCurrentUrl();
        descriptiveSoftAssert.assertEquals("Page has correct URL", decodeUrl(url), decodeUrl(actualUrl));
    }

    private String decodeUrl(String url)
    {
        return StringEscapeUtils.unescapeHtml4(URLDecoder.decode(url, StandardCharsets.UTF_8));
    }

    /**
     * Checks if page's url contains part of url and this page is loaded
     * @param urlPart Expected URL part
     */
    @Then("the page with the URL containing '$URLpart' is loaded")
    public void checkUrlPartIsLoaded(String urlPart)
    {
        URI actualUrl = UriUtils.createUri(getWebDriver().getCurrentUrl());
        String actualDecodedUrl = actualUrl.toString();
        descriptiveSoftAssert.assertThat(String.format("Page with the URLpart '%s' is loaded", urlPart),
                String.format("Page url '%1$s' contains part '%2$s'", actualDecodedUrl, urlPart), actualDecodedUrl,
                Matchers.containsString(urlPart));
    }

    /**
     * Checks if the page is scrolled to the specific element located by locator
     * <br>Example: &lt;a id="information_collection" name="information_collection_name"&gt; -
     * Then page is scrolled to element located `id(information_collection)`
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Assert that element with specified locator exists
     * <li>Checks whether page is scrolled to the very bottom
     * <li>If yes --&gt; verify that element's Y coordinate is positive which means that element is visible if no --&gt;
     * get element's Y coordinate and verify that it's close to 0 which means that element is an the very top
     * </ul>
     * @param locator A locator to locate element
     */
    @Then("page is scrolled to element located `$locator`")
    public void isPageScrolledToAnElement(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists("Element to verify position", locator);
        if (element != null)
        {
            boolean pageVisibleAreaScrolledToElement = webElementActions.isPageVisibleAreaScrolledToElement(element);
            descriptiveSoftAssert.assertTrue(String.format("The page is scrolled to an element with located by %s",
                locator), pageVisibleAreaScrolledToElement);
        }
    }

    /**
     * Goes to the relative URL
     * <br>
     * A <b>relative URL</b> - points to a file within a web site (like <i>'about.html'</i> or <i>'/products'</i>)<br>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Builds the absolute URL by concatenating the base URL and the relative URL;
     * <li>Loads the page with the absolute URL;
     * </ul>
     * <p>
     * @param relativeURL A string value of the relative URL
     */
    @When("I go to the relative URL '$relativeURL'")
    public void iGoTo(String relativeURL)
    {
        setContextSteps.switchingToDefault();
        URI currentURI = UriUtils.createUri(getWebDriver().getCurrentUrl());
        URI newURI = UriUtils.buildNewRelativeUrl(currentURI, relativeURL);
        // Workaround: window content is not loaded if basic authentification is used
        newURI = UriUtils.removeUserInfo(newURI);
        navigateActions.loadPage(newURI.toString());
        waitForPageLoad(webDriverManager.isIOS());
    }

    /**
     * Closes <b>current window</b> and switches to the window from which rederection to current window was performed
     * <p>
     * Each browser <b>window</b> or <b>tab</b> is considered to be a separate <b>window object</b>. This object holds
     * corresponding <b>Document</b> object, which itself is a html page. So this method applies to both windows and
     * tabs.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Receives all opened browser windows
     * <li>Identifies current window and closes it
     * <li>Switches back to the window from which rederection to current window was performed
     * </ul>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I close the current window")
    public void closeCurrentWindow()
    {
        WebDriver driver = getWebDriver();
        String currentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles())
        {
            if (!window.equals(currentWindow))
            {
                driver.close();
                driver.switchTo().window(window);
                break;
            }
        }
        descriptiveSoftAssert.assertThat("Current window has been closed",
                String.format("Current window '%s' has been closed", currentWindow), driver.getWindowHandles(),
                not(contains(currentWindow)));
    }

    /**
     * Checks that the page's <b>title</b> matches to <b>text</b> according to the provided string validation rule
     * <p>
     * A <b>title</b> is a text within a {@literal <title>} tag.
     * @param comparisonRule String validation rule: "is equal to", "contains", "does not contain"
     * @param text The text of the title to compare (ex. {@code <title>}<b>'text'</b>{@code </title>})
     */
    @Then("the page title $comparisonRule '$text'")
    public void assertPageTitle(StringComparisonRule comparisonRule, String text)
    {
        descriptiveSoftAssert.assertThat(PAGE_TITLE, getWebDriver().getTitle(), comparisonRule.createMatcher(text));
    }

    /**
     * Method loads given <b>mainApplicationPageUrl</b> page
     * @param pageUrl any valid page URI
     */
    public void loadApplicationPage(URI pageUrl)
    {
        URI finalUri = updateUrlWithUserInfoForRedirects(pageUrl);
        navigateActions.loadPage(finalUri.toString());
        waitForPageLoad(webDriverManager.isIOS());
        webApplicationListener.onLoad();
    }

    private URI updateUrlWithUserInfoForRedirects(URI pageUrl)
    {
        // Workaround for cases when redirects with changed protocols (http -> https)
        // do not keep user information from main application page URL
        if (keepUserInfoForProtocolRedirects
                && AuthenticationMode.URL == webApplicationConfiguration.getAuthenticationMode())
        {
            try
            {
                HttpClientContext context = HttpClientContext.create();
                httpClient.doHttpHead(pageUrl, context);
                List<URI> redirectLocations = context.getRedirectLocations();
                if (null != redirectLocations)
                {
                    URI uri = redirectLocations.get(redirectLocations.size() - 1);
                    if (pageUrl.getHost().equals(uri.getHost()))
                    {
                        return UriUtils.addUserInfoIfNotSet(uri, webApplicationConfiguration.getBasicAuthUser());
                    }
                }
            }
            catch (IOException e)
            {
                LOGGER.error("HTTP request for '{}' failed with the exception: {}", pageUrl, e.getMessage());
            }
        }
        return pageUrl;
    }

    private void waitForPageLoad(boolean waitForPageLoad)
    {
        if (waitForPageLoad)
        {
            waitActions.waitForPageLoad();
        }
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    public void setKeepUserInfoForProtocolRedirects(boolean keepUserInfoForProtocolRedirects)
    {
        this.keepUserInfoForProtocolRedirects = keepUserInfoForProtocolRedirects;
    }
}
