/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.steps.ui.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.annotation.Replacement;
import org.vividus.http.client.IHttpClient;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.configuration.AuthenticationMode;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.listener.WebApplicationListener;
import org.vividus.util.UriUtils;
import org.vividus.util.UriUtils.UserInfo;

@TakeScreenshotOnFailure
public class PageSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSteps.class);
    private static final String FORWARD_SLASH = "/";
    private static final String PAGE_TITLE = "Page title";

    @Inject private IUiContext uiContext;
    @Inject private SetContextSteps setContextSteps;
    @Inject private INavigateActions navigateActions;
    @Inject private WebApplicationConfiguration webApplicationConfiguration;
    @Inject private IWebWaitActions waitActions;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;
    @Inject private IWebDriverManager webDriverManager;
    private final List<WebApplicationListener> webApplicationListeners;
    private IHttpClient httpClient;

    private boolean keepUserInfoForProtocolRedirects;

    public PageSteps(List<WebApplicationListener> webApplicationListeners)
    {
        this.webApplicationListeners = webApplicationListeners;
    }

    /**
     * Loading the page which was set as a main application page.
     * <br>
     * One can set an URL for the main page in the properties file
     * by the <b>web-application.main-page-url</b> property
     */
    @Given("I am on main application page")
    public void openMainApplicationPage()
    {
        URI finalUri = updateUrlWithUserInfoForRedirects(webApplicationConfiguration.getMainApplicationPageUrl());
        navigateTo(finalUri);
        boolean refreshPageNeeded = webApplicationListeners.stream()
                .map(WebApplicationListener::onLoad)
                .reduce(false, (a, b) -> a || b);
        if (refreshPageNeeded)
        {
            navigateActions.refresh();
        }
    }

    /**
     * Loads a <b>page</b> with the given <b>URL</b>
     * <p>
     * Requires an <b>absolute</b> URL (like https://example.com/).
     * </p>
     * @param pageURL An <b>absolute</b> URL of the page
     */
    @Given("I am on page with URL `$pageURL`")
    public void openPage(String pageURL)
    {
        uiContext.reset();
        navigateActions.navigateTo(pageURL);
    }

    /**
     * Checks, that the current page has a correct relative URL <br>
     * A <b>relative URL</b> - points to a file within a web site (like <i>'about.html'</i> or <i>'/products'</i>)<br>
     * Actions performed at this step:
     * <ul>
     * <li>Gets the absolute URL of the current page;
     * <li>Gets relative URL from it;
     * <li>Compares it with the specified relative URL.
     * </ul>
     *
     * @param relativeURL A string value of the relative URL
     * @deprecated Use combination of step and expression:
     * "Then `#{extractPathFromUrl(${current-page-url})}` is equal to `$variable2`"
     */
    @Deprecated(since = "0.5.9", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
                 replacementFormatPattern = "Then `#{extractPathFromUrl(${current-page-url})}` is equal to `%1$s`")
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
     * Checks, that the current page has a correct host.<br/>
     * Actions performed at this step:
     * <ul>
     * <li>Gets the absolute URL of the current page;
     * <li>Gets page host from it;
     * <li>Compares it with the specified page host.
     * </ul>

     * @param host A string value of the page host
     * @deprecated Use combination of step and expression:
     * "Then `#{extractHostFromUrl(${current-page-url})}` is equal to `$variable2`"
     */
    @Deprecated(since = "0.5.9", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
                 replacementFormatPattern = "Then `#{extractHostFromUrl(${current-page-url})}` is equal to `%1$s`")
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
    @When("I refresh page")
    public void refreshPage()
    {
        uiContext.reset();
        navigateActions.refresh();
    }

    /**
     * Opens page with the given <b>pageUrl</b> in a new window object(tab)
     * @param pageUrl An absolute URL of the page
     * @deprecated Use step: "When I open URL `$pageUrl` in new tab" instead
     */
    @When("I open URL `$pageUrl` in new window")
    @Deprecated(since = "0.5.11", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern = "When I open URL `%1$s` in new tab")
    public void openPageUrlInNewWindow(String pageUrl)
    {
        javascriptActions.openNewTab();
        setContextSteps.switchingToWindow();
        openPage(pageUrl);
    }

    /**
     * Opens page with the given <b>pageUrl</b> in a new tab
     * @param pageUrl An absolute URL of the page
     */
    @When("I open URL `$pageUrl` in new tab")
    public void openPageUrlInNewTab(String pageUrl)
    {
        javascriptActions.openNewTab();
        setContextSteps.switchToTab();
        openPage(pageUrl);
    }

    /**
     * Checks, that the <b><i>page</i></b> with certain <b>URL</b> is loaded <br>
     * <b>URL</b> is the internet address of the current page which is located in the address bar
     *
     * @param url String value of URL
     * @deprecated Use combination of step and dynamic variable:
     * "Then `${current-page-url}` is equal to `$variable2`"
     */
    @Deprecated(since = "0.5.10", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
                 replacementFormatPattern = "Then `${current-page-url}` is equal to `%1$s`")
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
     * @deprecated Use combination of step and expression:
     * "Then `${current-page-url}` matches `$regex`"
     */
    @Deprecated(since = "0.5.9", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
                 replacementFormatPattern = "Then `${current-page-url}` matches `.*%1$s.*`")
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
     * Navigates the browser to the specific path in the current host
     * defined in the <i>relative URL</i> step variable
     * <br>
     * A <b>relative URL</b> - points to a file within a web site
     * (like <i>'about.html'</i> or <i>'/products'</i>)<br>
     * <br>
     * <table border="1">
     * <caption>Examples:</caption>
     * <tr>
     * <td>Current page</td><td>Relative URL parameter</td><td>Opened page</td>
     * </tr>
     * <tr>
     * <td>https://mysite.com/path/foo</td><td>stats</td><td>https://mysite.com/path/stats</td>
     * </tr>
     * <tr>
     * <td>https://mysite.com/path/foo</td><td>/documents</td><td>https://mysite.com/documents</td>
     * </tr>
     * </table>
     * Actions performed at this step:
     * <ul>
     * <li>Builds the absolute URL by concatenating the base URL and the relative URL;
     * <li>Loads the page with the absolute URL;
     * </ul>
     *
     * @param relativeURL A string value of the relative URL
     */
    @When("I go to relative URL `$relativeURL`")
    public void openRelativeUrl(String relativeURL)
    {
        setContextSteps.switchingToDefault();
        URI currentURI = UriUtils.createUri(getWebDriver().getCurrentUrl());
        URI newURI = UriUtils.buildNewRelativeUrl(currentURI, relativeURL);
        // Workaround: window content is not loaded if basic authentification is used
        newURI = UriUtils.removeUserInfo(newURI);
        navigateTo(newURI);
    }

    /**
     * Checks that the page's <b>title</b> matches to <b>text</b> according to the provided string validation rule
     * <p>
     * A <b>title</b> is a text within a {@literal <title>} tag.
     * @param comparisonRule String validation rule: "is equal to", "contains", "does not contain"
     * @param text The text of the title to compare (ex. {@code <title>}<b>'text'</b>{@code </title>})
     */
    @Then("page title $comparisonRule `$text`")
    public void assertPageTitle(StringComparisonRule comparisonRule, String text)
    {
        descriptiveSoftAssert.assertThat(PAGE_TITLE, getWebDriver().getTitle(), comparisonRule.createMatcher(text));
    }

    /**
     * Stop the loading of page using javascript method:
     * <code>window.stop();</code>
     */
    @When("I stop page loading")
    public void stopPageLoading()
    {
        try
        {
            Map<String, String> pageStates = javascriptActions.stopPageLoading();
            LOGGER.atInfo()
                  .addArgument(() -> pageStates.get("before"))
                  .addArgument(() -> pageStates.get("after"))
                  .log("Page ready state before stop: {}, after stop:{}");
        }
        catch (JavascriptException javascriptException)
        {
            descriptiveSoftAssert.recordFailedAssertion("Unable to stop page loading", javascriptException);
        }
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
                URI pageUriToCheck = pageUrl;
                UserInfo userInfo = UriUtils.getUserInfo(pageUriToCheck);
                ContextBuilder contextBuilder = ContextBuilder.create();
                if (userInfo != null)
                {
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userInfo.getUser(),
                            userInfo.getPassword().toCharArray());
                    HttpHost host = HttpHost.create(pageUriToCheck);
                    contextBuilder = contextBuilder.preemptiveBasicAuth(host, credentials);
                    pageUriToCheck = UriUtils.removeUserInfo(pageUriToCheck);
                }
                HttpClientContext context = contextBuilder.build();
                httpClient.doHttpHead(pageUriToCheck, context);
                List<URI> redirectLocations = context.getRedirectLocations().getAll();
                if (!redirectLocations.isEmpty())
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
                LOGGER.atError()
                        .addArgument(pageUrl)
                        .addArgument(e::getMessage)
                        .log("HTTP request for '{}' failed with the exception: {}");
            }
        }
        return pageUrl;
    }

    /**
     * Navigates to the specified URL and waits for page load if current platform is iOS
     * @param uri URI to open
     * @deprecated Wait for iOS should be removed, it looks outdated and redundant
     */
    @Deprecated(since = "0.5.4", forRemoval = true)
    private void navigateTo(URI uri)
    {
        navigateActions.navigateTo(uri);
        if (webDriverManager.isIOS())
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
