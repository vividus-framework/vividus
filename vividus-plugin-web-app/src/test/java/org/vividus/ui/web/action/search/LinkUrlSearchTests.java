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

package org.vividus.ui.web.action.search;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.AbstractElementAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.WebWaitActions;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class LinkUrlSearchTests
{
    private static final String HREF = "href";
    private static final String URL_PATH = "/urlPath";
    private static final String SIMPLE_URL = "http://example.com";
    private static final String URL = SIMPLE_URL + URL_PATH;
    private static final By LOCATOR = By.xpath(".//a[normalize-space(@href)=\"" + URL + "\"]");
    private static final String URL_WITH_SLASH = SIMPLE_URL + "/";
    private static final String URL_WITH_QUERY = SIMPLE_URL + "/?q=uri";
    private static final String URL_OPAQUE = "tel:1234567";
    private static final String SOME_URL = "/someUrl";
    private static final String PORT = ":8080";
    private static final String SIMPLE_URL_WITH_PATH = SIMPLE_URL + SOME_URL;
    private static final String PART = "#part";
    private static final By LINK_URL_LOCATOR_CASE_INSENSITIVE = By.xpath(".//a[normalize-space(translate(@href,"
            + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))=\"" + URL.toLowerCase() + "\"" + "]");
    private static final Duration TIMEOUT = Duration.ofSeconds(0);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementAction.class);

    @Mock private WebDriver webDriver;
    @Mock private WebElement webElement;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private SearchContext searchContext;
    @Mock private WebWaitActions waitActions;
    @Mock private ElementActions elementActions;
    @Mock private IExpectedConditions<By> expectedSearchContextConditions;

    @InjectMocks private LinkUrlSearch search;

    static Stream<Arguments> hrefProvider()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                Arguments.of(SOME_URL,             SIMPLE_URL_WITH_PATH,      SIMPLE_URL            ),
                Arguments.of(SIMPLE_URL_WITH_PATH, SIMPLE_URL_WITH_PATH,      SIMPLE_URL + PORT     ),
                Arguments.of(PART,                 "http://example.com#part", SIMPLE_URL            ),
                Arguments.of(SIMPLE_URL_WITH_PATH, SIMPLE_URL_WITH_PATH,      SIMPLE_URL            ),
                Arguments.of("someUrl",            SIMPLE_URL_WITH_PATH,      "http://example.com/" ),
                Arguments.of(SIMPLE_URL_WITH_PATH, SIMPLE_URL_WITH_PATH,      "https://example.com/")
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("hrefProvider")
    void testGetCurrentHrefDifferentScheme(String expected, String href, String currentUrl)
    {
        when(webElement.getAttribute(HREF)).thenReturn(href);
        search.setCaseSensitiveSearch(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(currentUrl);
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, expected);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByTextByLinkTextHeightWidthPositive()
    {
        testJavascriptActionsWasCalled();
        verifyLogging();
    }

    @Test
    void testFindLinksByTextByLinkTextHeightWidthNegative()
    {
        testJavascriptActionsWasCalled();
        verifyLogging();
    }

    private void verifyLogging()
    {
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info("Total number of elements found {} is {}", LOCATOR, 1),
            info("Number of {} elements is {}", Visibility.VISIBLE.getDescription(), 1)
        )));
    }

    @Test
    void testFindLinksByTextByLinkText()
    {
        search.setCaseSensitiveSearch(true);
        LinkUrlSearch spy = Mockito.spy(search);
        SearchParameters parameters = new SearchParameters(URL, Visibility.ALL, false);
        List<WebElement> webElements = List.of(webElement);
        when(searchContext.findElements(LOCATOR)).thenReturn(webElements);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByCaseInsensitiveUrl()
    {
        search.setCaseSensitiveSearch(false);
        LinkUrlSearch spy = Mockito.spy(search);
        SearchParameters parameters = new SearchParameters(URL, Visibility.ALL, false);
        List<WebElement> webElements = List.of(webElement);
        when(searchContext.findElements(LINK_URL_LOCATOR_CASE_INSENSITIVE)).thenReturn(webElements);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByTextNullSearchContext()
    {
        List<WebElement> foundElements = search.search(null, new SearchParameters(URL));
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testFilterLinksNullHrefAttribute()
    {
        when(webElement.getAttribute(HREF)).thenReturn(null);
        mockGetCurrentUrl();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, URL_PATH);
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testFilterLinksByHttpsUrl()
    {
        when(webElement.getAttribute(HREF)).thenReturn(SIMPLE_URL_WITH_PATH);
        search.setCaseSensitiveSearch(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(SIMPLE_URL);
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, "https://example.com/someUrl");
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testGetCurrentHrefMalformedUrl()
    {
        when(webElement.getAttribute(HREF)).thenReturn(URL);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn("data,;");
        List<WebElement> webElements = List.of(webElement);
        assertThrows(IllegalStateException.class, () -> search.filter(webElements, URL_PATH));
    }

    @Test
    void testSearchLinksByUrlPartNotMatchCaseInsensitive()
    {
        search.setCaseSensitiveSearch(false);
        when(webElement.getAttribute(HREF)).thenReturn(SIMPLE_URL);
        mockGetCurrentUrl();
        List<WebElement> foundElements = search.filter(List.of(webElement), URL_PATH);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchLinksByUrlPartNotMatch()
    {
        search.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(SIMPLE_URL);
        mockGetCurrentUrl();
        List<WebElement> foundElements = search.filter(List.of(webElement), URL_PATH);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchLinksByUrlNoLinkUrl()
    {
        List<WebElement> foundElements = search.filter(List.of(webElement), null);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrl()
    {
        search.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL);
        mockGetCurrentUrl();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, URL);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlCaseInsensitive()
    {
        search.setCaseSensitiveSearch(false);
        when(webElement.getAttribute(HREF)).thenReturn(URL);
        mockGetCurrentUrl();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, URL);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlAbsolute()
    {
        search.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL_WITH_SLASH);
        mockGetCurrentUrl();
        List<WebElement> foundElements = search.filter(List.of(webElement), SIMPLE_URL);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlWithQuery()
    {
        search.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL_WITH_QUERY);
        mockGetCurrentUrl();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, URL_WITH_QUERY);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlOpaque()
    {
        search.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL_OPAQUE);
        mockGetCurrentUrl();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, URL_OPAQUE);
        assertEquals(webElements, foundElements);
    }

    @SuppressWarnings("unchecked")
    private void testJavascriptActionsWasCalled()
    {
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        search.setWaitForElementTimeout(TIMEOUT);
        when(expectedSearchContextConditions.presenceOfAllElementsLocatedBy(LOCATOR)).thenReturn(condition);
        when(elementActions.isElementVisible(webElement)).thenReturn(true);
        WaitResult<List<WebElement>> result = mock(WaitResult.class);
        when(waitActions.wait(searchContext, TIMEOUT, condition, false)).thenReturn(result);
        when(result.getData()).thenReturn(List.of(webElement));
        search.setCaseSensitiveSearch(true);
        search.search(searchContext, new SearchParameters(URL));
        WebJavascriptActions javascriptActions = mock(WebJavascriptActions.class);
        verify(javascriptActions, never()).scrollIntoView(webElement, true);
    }

    @Test
    void shouldThrowExceptionIfMatchesIsInvoked()
    {
        assertThrows(UnsupportedOperationException.class, () -> search.matches(null, null));
    }

    private void mockGetCurrentUrl()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(SIMPLE_URL);
    }
}
