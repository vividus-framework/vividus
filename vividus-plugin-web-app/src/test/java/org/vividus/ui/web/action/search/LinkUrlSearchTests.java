/*
 * Copyright 2019-2022 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.search.AbstractElementAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class LinkUrlSearchTests
{
    private static final String HREF = "href";
    private static final String URL_PATH = "/urlPath";
    private static final String SIMPLE_URL = "http://example.com";
    private static final String URL = SIMPLE_URL + URL_PATH;
    private static final String URL_WITH_SLASH = SIMPLE_URL + "/";
    private static final String URL_WITH_QUERY = SIMPLE_URL + "/?q=uri";
    private static final String URL_OPAQUE = "tel:1234567";
    private static final String SOME_URL = "/someUrl";
    private static final String PORT = ":8080";
    private static final String SIMPLE_URL_WITH_PATH = SIMPLE_URL + SOME_URL;
    private static final String PART = "#part";
    private static final By LINK_URL_LOCATOR_CASE_INSENSITIVE = By.xpath(".//a[normalize-space(translate(@href,"
            + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))=\"" + URL.toLowerCase() + "\"" + "]");

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementAction.class);

    @Mock private WebDriver webDriver;
    @Mock private WebElement webElement;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private SearchContext searchContext;
    @Mock private ElementActions elementActions;
    @InjectMocks private LinkUrlSearch linkUrlSearch;

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
        linkUrlSearch.setCaseSensitiveSearch(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(currentUrl);
        var webElements = List.of(webElement);
        var foundElements = linkUrlSearch.filter(webElements, expected);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByTextByLinkText()
    {
        linkUrlSearch.setCaseSensitiveSearch(true);
        var parameters = new SearchParameters(URL, Visibility.VISIBLE, false);
        var webElements = List.of(webElement);
        String xpath = ".//a[normalize-space(@href)=\"" + URL + "\"]";
        when(searchContext.findElements(By.xpath(xpath))).thenReturn(webElements);
        when(elementActions.isElementVisible(webElement)).thenReturn(true);
        var foundElements = linkUrlSearch.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info("The total number of elements found by \"{}\" is {}, the number of {} elements is {}",
                        "xpath: " + xpath, 1, Visibility.VISIBLE.getDescription(), 1)
        )));
    }

    @Test
    void testFindLinksByCaseInsensitiveUrl()
    {
        linkUrlSearch.setCaseSensitiveSearch(false);
        var parameters = new SearchParameters(URL, Visibility.ALL, false);
        var webElements = List.of(webElement);
        when(searchContext.findElements(LINK_URL_LOCATOR_CASE_INSENSITIVE)).thenReturn(webElements);
        var foundElements = linkUrlSearch.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByTextNullSearchContext()
    {
        var foundElements = linkUrlSearch.search(null, new SearchParameters(URL));
        assertEquals(List.of(), foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                error("Unable to locate elements, because search context is not set")
        )));
    }

    @Test
    void testFilterLinksNullHrefAttribute()
    {
        when(webElement.getAttribute(HREF)).thenReturn(null);
        mockGetCurrentUrl();
        var webElements = List.of(webElement);
        var foundElements = linkUrlSearch.filter(webElements, URL_PATH);
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testFilterLinksByHttpsUrl()
    {
        when(webElement.getAttribute(HREF)).thenReturn(SIMPLE_URL_WITH_PATH);
        linkUrlSearch.setCaseSensitiveSearch(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(SIMPLE_URL);
        var webElements = List.of(webElement);
        var foundElements = linkUrlSearch.filter(webElements, "https://example.com/someUrl");
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testGetCurrentHrefMalformedUrl()
    {
        when(webElement.getAttribute(HREF)).thenReturn(URL);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn("data,;");
        var webElements = List.of(webElement);
        assertThrows(IllegalStateException.class, () -> linkUrlSearch.filter(webElements, URL_PATH));
    }

    @Test
    void testSearchLinksByUrlPartNotMatchCaseInsensitive()
    {
        linkUrlSearch.setCaseSensitiveSearch(false);
        when(webElement.getAttribute(HREF)).thenReturn(SIMPLE_URL);
        mockGetCurrentUrl();
        var foundElements = linkUrlSearch.filter(List.of(webElement), URL_PATH);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchLinksByUrlPartNotMatch()
    {
        linkUrlSearch.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(SIMPLE_URL);
        mockGetCurrentUrl();
        var foundElements = linkUrlSearch.filter(List.of(webElement), URL_PATH);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchLinksByUrlNoLinkUrl()
    {
        var foundElements = linkUrlSearch.filter(List.of(webElement), null);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrl()
    {
        linkUrlSearch.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL);
        mockGetCurrentUrl();
        var webElements = List.of(webElement);
        var foundElements = linkUrlSearch.filter(webElements, URL);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlCaseInsensitive()
    {
        linkUrlSearch.setCaseSensitiveSearch(false);
        when(webElement.getAttribute(HREF)).thenReturn(URL);
        mockGetCurrentUrl();
        var webElements = List.of(webElement);
        var foundElements = linkUrlSearch.filter(webElements, URL);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlAbsolute()
    {
        linkUrlSearch.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL_WITH_SLASH);
        mockGetCurrentUrl();
        var foundElements = linkUrlSearch.filter(List.of(webElement), SIMPLE_URL);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlWithQuery()
    {
        linkUrlSearch.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL_WITH_QUERY);
        mockGetCurrentUrl();
        var webElements = List.of(webElement);
        var foundElements = linkUrlSearch.filter(webElements, URL_WITH_QUERY);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchLinksByUrlHrefEqualLinkUrlOpaque()
    {
        linkUrlSearch.setCaseSensitiveSearch(true);
        when(webElement.getAttribute(HREF)).thenReturn(URL_OPAQUE);
        mockGetCurrentUrl();
        var webElements = List.of(webElement);
        var foundElements = linkUrlSearch.filter(webElements, URL_OPAQUE);
        assertEquals(webElements, foundElements);
    }

    @Test
    void shouldThrowExceptionIfMatchesIsInvoked()
    {
        assertThrows(UnsupportedOperationException.class, () -> linkUrlSearch.matches(null, null));
    }

    private void mockGetCurrentUrl()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(SIMPLE_URL);
    }
}
