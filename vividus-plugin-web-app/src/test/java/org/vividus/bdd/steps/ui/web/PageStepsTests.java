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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IHighlightingSoftAssert;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.ui.web.action.IWaitActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.configuration.AuthenticationMode;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.listener.IWebApplicationListener;
import org.vividus.ui.web.util.LocatorUtil;
import org.vividus.util.UriUtils;

@SuppressWarnings("MethodCount")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class PageStepsTests
{
    private static final String HTTP_EXAMPLE_COM = "http://example.com";
    private static final String AN_ELEMENT_WITH_THE_ATTRIBUTE_ID_VALUE = "An element with the attribute 'id'='value'";
    private static final String CURRENT_WINDOW_GUID = "{770e3411-5e19-4831-8f36-fc76e46a2807}";
    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String PAGE_HAS_CORRECT_RELATIVE_URL = "Page has correct relative URL";
    private static final String PAGE_HAS_CORRECT_HOST = "Page has correct host";
    private static final String RELATIVE_URL = "/";
    private static final String URL = "http://qa.vividus.org/";
    private static final String HOST = "qa.vividus.org";
    private static final long PAGE_LOAD_TIME_THRESHOLD = 1000L;
    private static final String PAGE_TITLE = "Page title";

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver driver;

    @Mock
    private IHighlightingSoftAssert softAssert;

    @Mock
    private IBaseValidations mockedBaseValidations;

    @Mock
    private INavigateActions navigateActions;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private IWebElementActions webElementActions;

    @Mock
    private WebApplicationConfiguration webApplicationConfiguration;

    @Mock
    private IWebApplicationListener webApplicationListener;

    @Mock
    private SetContextSteps setContextSteps;

    @Mock
    private IWaitActions waitActions;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private IHttpClient httpClient;

    @InjectMocks
    private PageSteps pageSteps;

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

    @Test
    void testPageWithURLpartIsLoaded()
    {
        String urlPart = "//somePartURL";
        pageSteps.checkUrlPartIsLoaded(urlPart);
        verify(mockedBaseValidations).assertPageWithURLPartIsLoaded(urlPart);
    }

    @Test
    void testIsElementAtTheTop()
    {
        WebElement webElement = mock(WebElement.class);
        when(mockedBaseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTE_ID_VALUE,
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPathByAttribute(ID, VALUE), Visibility.ALL))))
                            .thenReturn(webElement);
        when(webElementActions.isPageVisibleAreaScrolledToElement(webElement)).thenReturn(true);
        pageSteps.isPageScrolledToAnElement(ID, VALUE);
        verify(softAssert)
                .assertTrue("The page is scrolled to an element with the attribute id and value value", true);
    }

    @Test
    void testIsElementAtTheTopElementNull()
    {
        when(mockedBaseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTE_ID_VALUE,
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPathByAttribute(ID, VALUE), Visibility.ALL))))
                            .thenReturn(null);
        pageSteps.isPageScrolledToAnElement(ID, VALUE);
        verify(softAssert, never()).assertTrue("The page is scrolled to an element with the attribute",
                false);
    }

    @Test
    void testCloseCurrentWindow()
    {
        String currentWindow = CURRENT_WINDOW_GUID;
        String windowToSwitchTo = "{248427e8-e67d-47ba-923f-4051f349f813}";
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getWindowHandle()).thenReturn(currentWindow);
        Set<String> windowHandles = new LinkedHashSet<>(List.of(currentWindow, windowToSwitchTo));
        when(driver.getWindowHandles()).thenReturn(windowHandles);
        TargetLocator mockedTargetLocator = mock(TargetLocator.class);
        when(driver.switchTo()).thenReturn(mockedTargetLocator);
        pageSteps.closeCurrentWindow();
        verify(driver).close();
        verify(mockedTargetLocator).window(windowToSwitchTo);
    }

    @Test
    void testCloseCurrentWindowNoAdditionalWindowIsOpened()
    {
        String currentWindow = CURRENT_WINDOW_GUID;
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getWindowHandle()).thenReturn(currentWindow);
        when(driver.getWindowHandles()).thenReturn(Set.of(currentWindow));
        pageSteps.closeCurrentWindow();
        verify(softAssert, times(0)).assertThat("New window or browser tab is found",
                driver.getWindowHandles(), contains(currentWindow));
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
    void testIGoTo(String baseUrl, String toGo, String expected)
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(baseUrl);
        pageSteps.iGoTo(toGo);
        verify(setContextSteps).switchingToDefault();
        verify(navigateActions).loadPage(expected);
    }

    @Test
    void testIGoToIOS()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(URL);
        when(webDriverManager.isIOS()).thenReturn(true);
        pageSteps.iGoTo(RELATIVE_URL);
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testIGoToNotIOS()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(URL);
        when(webDriverManager.isIOS()).thenReturn(false);
        pageSteps.iGoTo(RELATIVE_URL);
        verifyNoInteractions(waitActions);
    }

    @Test
    void testIAmOnPage()
    {
        String pageURL = "pageURL";
        pageSteps.iAmOnPage(pageURL);
        verify(navigateActions).loadPage(pageURL);
    }

    @Test
    void testRefreshPage()
    {
        pageSteps.refreshPage();
        verify(navigateActions).refresh();
        verify(webUiContext).reset();
    }

    @Test
    void testScrollToTheEndOfThePage()
    {
        pageSteps.scrollToTheEndOfThePage();
        verify(javascriptActions).scrollToEndOfPage();
    }

    @Test
    void testScrollToTheStartOfThePage()
    {
        pageSteps.scrollToTheStartOfThePage();
        verify(javascriptActions).scrollToStartOfPage();
    }

    @Test
    void testScrollToEndOfContextWhenContextIsPage()
    {
        when(webUiContext.getSearchContext()).thenReturn(driver);
        pageSteps.scrollToEndOfContext();
        verify(javascriptActions).scrollToEndOfPage();
    }

    @Test
    void testScrollToEndOfContextWhenContextIsElement()
    {
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        pageSteps.scrollToEndOfContext();
        verify(javascriptActions).scrollToEndOf(webElement);
    }

    @Test
    void testScrollToStartOfContextWhenContextIsPage()
    {
        when(webUiContext.getSearchContext()).thenReturn(driver);
        pageSteps.scrollToStartOfContext();
        verify(javascriptActions).scrollToStartOfPage();
    }

    @Test
    void testScrollToStartOfContextWhenContextIsElement()
    {
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        pageSteps.scrollToStartOfContext();
        verify(javascriptActions).scrollToStartOf(webElement);
    }

    @Test
    void testOpenPageUrlInNewWindow()
    {
        pageSteps.openPageUrlInNewWindow(URL);
        verify(javascriptActions).openPageUrlInNewWindow(URL);
    }

    @Test
    void testIAmOnTheMainApplicationPage()
    {
        pageSteps.setKeepUserInfoForProtocolRedirects(false);
        URI mainPage = URI.create(URL);
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
        pageSteps.iAmOnTheMainApplicationPage();
        verify(navigateActions).loadPage(URL);
        verify(webApplicationListener).onLoad();
    }

    @Test
    void testIAmOnTheMainApplicationPageIOS()
    {
        when(webDriverManager.isIOS()).thenReturn(true);
        testIAmOnTheMainApplicationPage();
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testIAmOnTheMainApplicationPageRedirect() throws IOException
    {
        mockPageRedirect(URL, HTTP_EXAMPLE_COM);
        pageSteps.iAmOnTheMainApplicationPage();
        verify(navigateActions).loadPage(URL);
        verify(webApplicationListener).onLoad();
    }

    @Test
    void testIAmOnTheMainApplicationPageRedirectSameHost() throws IOException
    {
        mockPageRedirect(HTTP_EXAMPLE_COM, "https://example.com");
        when(webApplicationConfiguration.getBasicAuthUser()).thenReturn("user:password");
        pageSteps.iAmOnTheMainApplicationPage();
        verify(navigateActions).loadPage("https://user:password@example.com");
        verify(webApplicationListener).onLoad();
    }

    private void mockPageRedirect(String mainPageUrl, String redirectPageUrl) throws IOException
    {
        pageSteps.setKeepUserInfoForProtocolRedirects(true);
        when(webApplicationConfiguration.getAuthenticationMode()).thenReturn(AuthenticationMode.URL);
        URI mainPage = URI.create(mainPageUrl);
        URI redirectPage = URI.create(redirectPageUrl);
        when(httpClient.doHttpHead(eq(mainPage), argThat(context ->
        {
            context.setAttribute(HttpClientContext.REDIRECT_LOCATIONS, List.of(redirectPage));
            return true;
        }))).thenReturn(new HttpResponse());
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
    }

    @Test
    void testIAmOnTheMainApplicationPageNoRedirect()
    {
        pageSteps.setKeepUserInfoForProtocolRedirects(true);
        when(webApplicationConfiguration.getAuthenticationMode()).thenReturn(AuthenticationMode.URL);
        URI mainPage = URI.create(HTTP_EXAMPLE_COM);
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
        pageSteps.iAmOnTheMainApplicationPage();
        verify(navigateActions).loadPage(HTTP_EXAMPLE_COM);
        verify(webApplicationListener).onLoad();
    }

    @Test
    void testIAmOnTheMainApplicationPageIOExeption() throws IOException
    {
        String exceptionMessage = "message";
        pageSteps.setKeepUserInfoForProtocolRedirects(true);
        URI mainPage = URI.create("http://xxx");
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPage);
        when(webApplicationConfiguration.getAuthenticationMode()).thenReturn(AuthenticationMode.URL);
        IOException exception = new IOException(exceptionMessage);
        doThrow(exception).when(httpClient).doHttpHead(eq(mainPage), any(HttpClientContext.class));
        pageSteps.iAmOnTheMainApplicationPage();
        assertThat(logger.getLoggingEvents(),
                is(List.of(error("HTTP request for '{}' failed with the exception: {}", mainPage, exceptionMessage))));
    }

    @Test
    void testIAmOnTheMainApplicationPageNull()
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> pageSteps.iAmOnTheMainApplicationPage());
        assertEquals("URL of the main application page should be non-blank", exception.getMessage());
    }

    @Test
    void testThenTheLoadTimeShouldBeLessThan()
    {
        when(navigateActions.getActualPageLoadTimeInMs()).thenReturn(PAGE_LOAD_TIME_THRESHOLD);
        pageSteps.thenTheLoadTimeShouldBeLessThan(PAGE_LOAD_TIME_THRESHOLD);
        verify(softAssert).assertThat(eq("The page load time is less than load time threshold."),
                eq("The page load time is less than '1000'"), eq(PAGE_LOAD_TIME_THRESHOLD), any());
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
    void testNavigateBack()
    {
        WebDriver driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        WebDriver.Navigation navigation = mock(WebDriver.Navigation.class);
        when(driver.navigate()).thenReturn(navigation);
        pageSteps.navigateBack();
        verify(navigation).back();
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
}
