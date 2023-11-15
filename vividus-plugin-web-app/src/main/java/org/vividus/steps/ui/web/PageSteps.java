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
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.configuration.AuthenticationMode;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.listener.WebApplicationListener;
import org.vividus.util.UriUtils;
import org.vividus.util.UriUtils.UserInfo;

import jakarta.inject.Inject;

@TakeScreenshotOnFailure
public class PageSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSteps.class);
    private static final String FORWARD_SLASH = "/";
    private static final String PAGE_TITLE = "Page title";

    @Inject private SetContextSteps setContextSteps;
    @Inject private INavigateActions navigateActions;
    @Inject private WebApplicationConfiguration webApplicationConfiguration;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;
    private final List<WebApplicationListener> webApplicationListeners;
    private IHttpClient httpClient;

    private boolean keepUserInfoForProtocolRedirects;

    public PageSteps(List<WebApplicationListener> webApplicationListeners)
    {
        this.webApplicationListeners = webApplicationListeners;
    }

    /**
     * Navigates to the page which was configured as the main application page in the property with name
     * <code>web-application.main-page-url</code>.
     */
    @Given("I am on main application page")
    public void openMainApplicationPage()
    {
        URI finalUri = updateUrlWithUserInfoForRedirects(webApplicationConfiguration.getMainApplicationPageUrl());
        navigateActions.navigateTo(finalUri.toString());
        boolean refreshPageNeeded = webApplicationListeners.stream()
                .map(WebApplicationListener::onLoad)
                .reduce(false, (a, b) -> a || b);
        if (refreshPageNeeded)
        {
            navigateActions.refresh();
        }
    }

    /**
     * Navigates to the page with the given absolute URL, e.g. {@code https://docs.vividus.dev/}
     *
     * @param pageUrl An absolute URL of the page to navigate to.
     */
    @Given("I am on page with URL `$pageUrl`")
    public void openPage(String pageUrl)
    {
        navigateActions.navigateTo(pageUrl);
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
     * @param relativeUrl A string value of the relative URL
     * @deprecated Use combination of step and expression:
     * "Then `#{extractPathFromUrl(${current-page-url})}` is equal to `$variable2`"
     */
    @Deprecated(since = "0.5.9", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
                 replacementFormatPattern = "Then `#{extractPathFromUrl(${current-page-url})}` is equal to `%1$s`")
    @Then("the page has the relative URL '$relativeUrl'")
    public void checkPageRelativeURL(String relativeUrl)
    {
        URI url = UriUtils.createUri(getWebDriver().getCurrentUrl());
        // If web application under test is unavailable (no page is opened), an empty URL will be returned
        if (url.getPath() != null)
        {
            String expectedRelativeUrl = relativeUrl.isEmpty() ? FORWARD_SLASH : relativeUrl;
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
     * Reloads the current page: does the same as the reload button in the browser.
     */
    @When("I refresh page")
    public void refreshPage()
    {
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
     * Opens new tab and navigates to the page with the given URL.
     *
     * @param pageUrl An absolute URL of the page to open in new tab.
     */
    @When("I open URL `$pageUrl` in new tab")
    public void openPageInNewTab(String pageUrl)
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
        setContextSteps.switchingToDefault();
        URI currentURI = UriUtils.createUri(getWebDriver().getCurrentUrl());
        URI newURI = UriUtils.buildNewRelativeUrl(currentURI, relativeUrl);
        // Workaround: window content is not loaded if basic authentication is used
        newURI = UriUtils.removeUserInfo(newURI);
        navigateActions.navigateTo(newURI.toString());
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
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userInfo.user(),
                            userInfo.password().toCharArray());
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
