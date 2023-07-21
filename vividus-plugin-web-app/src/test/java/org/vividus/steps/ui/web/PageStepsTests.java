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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.HttpHost;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.configuration.AuthenticationMode;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.listener.WebApplicationListener;
import org.vividus.util.UriUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class PageStepsTests
{
    private static final String HTTP_EXAMPLE_COM = "http://example.com";
    private static final String PAGE_HAS_CORRECT_RELATIVE_URL = "Page has correct relative URL";
    private static final String PAGE_HAS_CORRECT_HOST = "Page has correct host";
    private static final String RELATIVE_URL = "/";
    private static final String URL = "http://qa.vividus.org/";
    private static final String HOST = "qa.vividus.org";
    private static final String PAGE_TITLE = "Page title";

    @Mock private WebDriver driver;
    @Mock private IDescriptiveSoftAssert softAssert;
    @Mock private INavigateActions navigateActions;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private IUiContext uiContext;
    @Mock private WebApplicationConfiguration webApplicationConfiguration;
    @Mock private SetContextSteps setContextSteps;
    @Mock private IWebWaitActions waitActions;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private IHttpClient httpClient;
    private final List<WebApplicationListener> webApplicationListeners = new ArrayList<>();
    @InjectMocks private PageSteps pageSteps = new PageSteps(webApplicationListeners);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(PageSteps.class);

    @Test
    void testCheckPageRelativeURL()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(URL);
        pageSteps.checkPageRelativeURL(RELATIVE_URL);
        URI uri = UriUtils.createUri(URL);
        verify(softAssert).assertEquals(PAGE_HAS_CORRECT_RELATIVE_URL, uri, uri);
    }

    @Test
    void testCheckPageRelativeURLEmptyString()
    {
        String relativeURL = "";
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(URL);
        pageSteps.checkPageRelativeURL(relativeURL);
        URI uri = UriUtils.createUri(URL);
        verify(softAssert).assertEquals(PAGE_HAS_CORRECT_RELATIVE_URL, uri, uri);
    }

    @Test
    void testCheckPageRelativeURLNonLatinRelativeUrl()
    {
        String relativeURL = "/search/site/可伶可俐";
        String url = "http://stage-en.vividus.org/"
                + "search/site/%E5%8F%AF%E4%BC%B6%E5%8F%AF%E4%BF%90";
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);
        pageSteps.checkPageRelativeURL(relativeURL);
        URI uri = UriUtils.createUri(url);
        verify(softAssert).assertEquals(PAGE_HAS_CORRECT_RELATIVE_URL, uri, uri);
    }

    @Test
    void testCheckPageRelativeURLEncodedActualDecodedExpected()
    {
        String relativeURL = "/relative-&-encoded";
        String url = "http://www.example.com/relative-%26-encoded";
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);
        pageSteps.checkPageRelativeURL(relativeURL);
        URI uri = UriUtils.createUri(url);
        verify(softAssert).assertEquals(PAGE_HAS_CORRECT_RELATIVE_URL, uri, uri);
    }

    @Test
    void testCheckPageRelativeURLFalseEncoded()
    {
        String relativeURL = "/relative-%2526-encoded";
        String url = "http://www.example.com/relative-%2526-encoded";
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);
        pageSteps.checkPageRelativeURL(relativeURL);
        URI uri = UriUtils.createUri(url);
        verify(softAssert).assertEquals(PAGE_HAS_CORRECT_RELATIVE_URL, uri, uri);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPageWithURLpartIsLoaded()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        String urlValue = "http://example.com/";
        when(driver.getCurrentUrl()).thenReturn(urlValue);
        pageSteps.checkUrlPartIsLoaded(urlValue);
        verify(softAssert).assertThat(eq("Page with the URLpart '" + urlValue + "' is loaded"),
                eq("Page url '" + urlValue + "' contains part '" + urlValue + "'"), eq(urlValue),
                (Matcher<String>) isA(Matcher.class));
    }

    @ParameterizedTest
    @CsvSource({
            // CHECKSTYLE:OFF
            // @formatter:off
            "http://url1.com,                                          relative1,   http://url1.com/relative1",
            "http://url2.com/,                                         relative2,   http://url2.com/relative2",
            "http://url3.com,                                          /relative3,  http://url3.com/relative3",
            "http://url4.com/,                                         /relative4,  http://url4.com/relative4",
            "http://url5.com/path,                                     /relative5,  http://url5.com/relative5",
            "http://user:pass@url6.com/path,                           relative6,   http://url6.com/relative6",
            "http://url7.com/path/index.html,                          relative7,   http://url7.com/path/relative7",
            "http://url8.com/path/index.html,                          /relative8,  http://url8.com/relative8",
            "http://url9.com/path/foo,                                 relative9,   http://url9.com/path/relative9",
            "http://url10.com/path/foo,                                /relative10, http://url10.com/relative10",
            "http://url11.com/path/foo/,                               relative11,  http://url11.com/path/foo/relative11",
            "https://stage-web.vividus/surgeon-locator#!/#%2F,          /qa,        https://stage-web.vividus/qa"
            // @formatter:on
            // CHECKSTYLE:ON
    })
    void testOpenRelativeUrl(String baseUrl, String toGo, String expected)
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(baseUrl);
        pageSteps.openRelativeUrl(toGo);
        verify(setContextSteps).switchingToDefault();
        verify(navigateActions).navigateTo(URI.create(expected));
    }

    @Test
    void testOpenRelativeUrlIOS()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(URL);
        when(webDriverManager.isIOS()).thenReturn(true);
        pageSteps.openRelativeUrl(RELATIVE_URL);
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testOpenRelativeUrlNotIOS()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(URL);
        when(webDriverManager.isIOS()).thenReturn(false);
        pageSteps.openRelativeUrl(RELATIVE_URL);
        verifyNoInteractions(waitActions);
    }

    @Test
    void testOpenPage()
    {
        String pageURL = "pageURL";
        pageSteps.openPage(pageURL);
        verify(navigateActions).navigateTo(pageURL);
    }

    @Test
    void testRefreshPage()
    {
        pageSteps.refreshPage();
        verify(navigateActions).refresh();
        verify(uiContext).reset();
    }

    @Test
    void testOpenPageUrlInNewWindow()
    {
        InOrder ordered = Mockito.inOrder(setContextSteps, navigateActions, javascriptActions, uiContext);
        pageSteps.openPageUrlInNewWindow(URL);
        ordered.verify(javascriptActions).openNewTab();
        ordered.verify(setContextSteps).switchingToWindow();
        ordered.verify(uiContext).reset();
        ordered.verify(navigateActions).navigateTo(URL);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testOpenPageUrlInNewTab()
    {
        InOrder ordered = Mockito.inOrder(setContextSteps, navigateActions, javascriptActions, uiContext);
        pageSteps.openPageUrlInNewTab(URL);
        ordered.verify(javascriptActions).openNewTab();
        ordered.verify(setContextSteps).switchToTab();
        ordered.verify(uiContext).reset();
        ordered.verify(navigateActions).navigateTo(URL);
        ordered.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @CsvSource({
            "true, false",
            "false, true"
    })
    void shouldOpenMainApplicationPage(boolean result1, boolean result2)
    {
        WebApplicationListener webApplicationListener1 = mock();
        when(webApplicationListener1.onLoad()).thenReturn(result1);
        webApplicationListeners.add(webApplicationListener1);
        WebApplicationListener webApplicationListener2 = mock();
        when(webApplicationListener2.onLoad()).thenReturn(result2);
        webApplicationListeners.add(webApplicationListener2);
        pageSteps.setKeepUserInfoForProtocolRedirects(false);
        URI mainPage = URI.create(URL);
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
        pageSteps.openMainApplicationPage();
        verify(navigateActions).navigateTo(mainPage);
        verify(navigateActions).refresh();
    }

    @Test
    void testOpenMainApplicationPageIOS()
    {
        when(webDriverManager.isIOS()).thenReturn(true);
        shouldOpenMainApplicationPage(true, true);
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testOpenMainApplicationPageRedirect() throws IOException
    {
        mockPageRedirect(URL, URL, HTTP_EXAMPLE_COM);
        pageSteps.openMainApplicationPage();
        verify(navigateActions).navigateTo(URI.create(URL));
        verifyNoMoreInteractions(navigateActions);
    }

    @Test
    void testOpenMainApplicationPageRedirectSameHost() throws IOException, URISyntaxException
    {
        String httpExampleComWithUserInfo = "http://user:password@example.com";
        WebApplicationListener webApplicationListener1 = mock();
        when(webApplicationListener1.onLoad()).thenReturn(false);
        webApplicationListeners.add(webApplicationListener1);
        WebApplicationListener webApplicationListener2 = mock();
        when(webApplicationListener2.onLoad()).thenReturn(false);
        webApplicationListeners.add(webApplicationListener2);
        mockPageRedirect(httpExampleComWithUserInfo, HTTP_EXAMPLE_COM, "https://example.com");
        when(webApplicationConfiguration.getBasicAuthUser()).thenReturn("user:password");
        HttpHost expectedHost = HttpHost.create(HTTP_EXAMPLE_COM);
        var contextBuilder = mock(ContextBuilder.class);

        try (MockedStatic<ContextBuilder> contextBuilderStatic = Mockito.mockStatic(ContextBuilder.class))
        {
            contextBuilderStatic.when(ContextBuilder::create).thenReturn(contextBuilder);
            when(contextBuilder.preemptiveBasicAuth(argThat(expectedHost::equals),
                    argThat(credentials -> credentials.getUserPrincipal().getName().equals("user")
                            && Arrays.equals(credentials.getPassword(), "password".toCharArray()))))
                                    .thenReturn(contextBuilder);
            when(contextBuilder.build()).thenReturn(new HttpClientContext());
            pageSteps.openMainApplicationPage();
        }
        verify(navigateActions).navigateTo(URI.create("https://user:password@example.com"));
        verifyNoMoreInteractions(navigateActions);
    }

    private void mockPageRedirect(String mainPageUrl, String expectedHeadUrl, String redirectPageUrl) throws IOException
    {
        pageSteps.setKeepUserInfoForProtocolRedirects(true);
        when(webApplicationConfiguration.getAuthenticationMode()).thenReturn(AuthenticationMode.URL);
        URI mainPage = URI.create(mainPageUrl);
        URI requestUrl = URI.create(expectedHeadUrl);
        URI redirectPage = URI.create(redirectPageUrl);
        when(httpClient.doHttpHead(eq(requestUrl), argThat(context ->
        {
            RedirectLocations redirectLocations = new RedirectLocations();
            redirectLocations.add(redirectPage);
            context.setAttribute(HttpClientContext.REDIRECT_LOCATIONS, redirectLocations);
            return true;
        }))).thenReturn(new HttpResponse());
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
    }

    @Test
    void testOpenMainApplicationPageNoRedirect() throws IOException
    {
        pageSteps.setKeepUserInfoForProtocolRedirects(true);
        when(webApplicationConfiguration.getAuthenticationMode()).thenReturn(AuthenticationMode.URL);
        URI mainPage = URI.create(HTTP_EXAMPLE_COM);
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
        when(httpClient.doHttpHead(eq(mainPage), argThat(context ->
        {
            context.setAttribute(HttpClientContext.REDIRECT_LOCATIONS, new RedirectLocations());
            return true;
        }))).thenReturn(new HttpResponse());
        pageSteps.openMainApplicationPage();
        verify(navigateActions).navigateTo(mainPage);
        verifyNoMoreInteractions(navigateActions);
    }

    @Test
    void testOpenMainApplicationPageIOExeption() throws IOException
    {
        String exceptionMessage = "message";
        pageSteps.setKeepUserInfoForProtocolRedirects(true);
        URI mainPage = URI.create("http://xxx");
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
        when(webApplicationConfiguration.getAuthenticationMode()).thenReturn(AuthenticationMode.URL);
        IOException exception = new IOException(exceptionMessage);
        doThrow(exception).when(httpClient).doHttpHead(eq(mainPage), any(HttpClientContext.class));
        pageSteps.openMainApplicationPage();
        assertThat(logger.getLoggingEvents(),
                is(List.of(error("HTTP request for '{}' failed with the exception: {}", mainPage, exceptionMessage))));
    }

    @Test
    void testCheckPageRelativeURLNull()
    {
        String url = "data:,";
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);
        pageSteps.checkPageRelativeURL(RELATIVE_URL);
        verify(softAssert).recordFailedAssertion("URL path component is null");
    }

    @Test
    void testCheckUriIsLoaded()
    {
        String url = HTTP_EXAMPLE_COM;
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);
        pageSteps.checkUriIsLoaded(url);
        verify(softAssert).assertEquals("Page has correct URL", url, url);
    }

    @Test
    void testCheckPageHost()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(URL);
        pageSteps.checkPageHost(HOST);
        URI uri = UriUtils.createUri(URL);
        String uriHost = uri.getHost();
        verify(softAssert).assertEquals(PAGE_HAS_CORRECT_HOST, uriHost, uriHost);
    }

    @Test
    void testPageTitleContainsText()
    {
        String text = "text";
        WebDriver driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getTitle()).thenReturn(text);
        pageSteps.assertPageTitle(StringComparisonRule.CONTAINS, text);
        verify(softAssert).assertThat(eq(PAGE_TITLE), eq(text),
                argThat(matcher -> "a string containing \"text\"".equals(matcher.toString())));
    }

    @Test
    void shouldStopThePageAndLogStatuses()
    {
        String interactive = "interactive";
        String complete = "complete";
        Map<String, String> result = Map.of("before", interactive, "after", complete);
        when(javascriptActions.stopPageLoading()).thenReturn(result);
        pageSteps.stopPageLoading();
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Page ready state before stop: {}, after stop:{}", interactive, complete))));
        verifyNoMoreInteractions(javascriptActions);
    }

    @Test
    void shouldRecordFailedAssertionInCaseOfJavascriptExceptionDuringPageStoppage()
    {
        JavascriptException javascriptException = new JavascriptException("I can't take this any moooore");
        when(javascriptActions.stopPageLoading()).thenThrow(javascriptException);
        pageSteps.stopPageLoading();
        softAssert.recordFailedAssertion("Unable to stop page loading", javascriptException);
        verifyNoMoreInteractions(javascriptActions);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }
}
