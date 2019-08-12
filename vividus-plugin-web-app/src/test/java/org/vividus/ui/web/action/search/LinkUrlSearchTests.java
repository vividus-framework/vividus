/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.IExpectedConditions;
import org.vividus.ui.web.action.IExpectedSearchContextCondition;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.action.WaitActions;
import org.vividus.ui.web.action.WaitResult;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class LinkUrlSearchTests
{
    private static final String HREF = "href";
    private static final String URL_PATH = "/urlPath";
    private static final String SIMPLE_URL = "https://example.com";
    private static final String URL = SIMPLE_URL + URL_PATH;
    private static final By LOCATOR = By.xpath(".//a[normalize-space(@href)=\"" + URL + "\"]");
    private static final String URL_WITH_SLASH = SIMPLE_URL + "/";
    private static final String URL_WITH_QUERY = SIMPLE_URL + "/?q=uri";
    private static final String URL_OPAQUE = "tel:1234567";
    private static final String TOTAL_NUMBER_OF_ELEMENTS = "Total number of elements found {} is equal to {}";
    private static final By LINK_URL_LOCATOR_CASE_INSENSITIVE = By.xpath(".//a[normalize-space(translate(@href,"
            + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))=\"" + URL.toLowerCase() + "\"" + "]");
    private static final Duration TIMEOUT = Duration.ofSeconds(0);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementSearchAction.class);

    @Mock
    private WebElement webElement;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private SearchContext searchContext;

    @Mock
    private WaitActions waitActions;

    @Mock
    private IExpectedConditions<By> expectedSearchContextConditions;

    @InjectMocks
    private LinkUrlSearch search;

    @Test
    void testFindLinksByTextByLinkTextHeightWidthPositive()
    {
        testJavascriptActionsWasCalled();
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, LOCATOR, 1))));
    }

    @Test
    void testFindLinksByTextByLinkTextHeightWidthNegative()
    {
        testJavascriptActionsWasCalled();
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, LOCATOR, 1))));
    }

    @Test
    void testFindLinksByTextByLinkText()
    {
        search.setCaseSensitiveSearch(true);
        LinkUrlSearch spy = Mockito.spy(search);
        SearchParameters parameters = new SearchParameters(URL);
        List<WebElement> webElements = List.of(webElement);
        doReturn(webElements).when(spy).findElements(searchContext, LOCATOR, parameters);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByCaseInsensitiveUrl()
    {
        search.setCaseSensitiveSearch(false);
        LinkUrlSearch spy = Mockito.spy(search);
        SearchParameters parameters = new SearchParameters(URL);
        List<WebElement> webElements = List.of(webElement);
        doReturn(webElements).when(spy).findElements(searchContext, LINK_URL_LOCATOR_CASE_INSENSITIVE, parameters);
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
    void testFilterLinksByUrl()
    {
        when(webElement.getAttribute(HREF)).thenReturn(URL);
        search.setCaseSensitiveSearch(true);
        mockGetCurrentUrl();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = search.filter(webElements, URL_PATH);
        assertEquals(webElements, foundElements);
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
        when(webElement.isDisplayed()).thenReturn(true);
        WaitResult<List<WebElement>> result = mock(WaitResult.class);
        when(waitActions.wait(searchContext, TIMEOUT, condition, false)).thenReturn(result);
        when(result.getData()).thenReturn(List.of(webElement));
        search.setCaseSensitiveSearch(true);
        search.search(searchContext, new SearchParameters(URL));
        JavascriptActions javascriptActions = mock(JavascriptActions.class);
        verify(javascriptActions, never()).scrollIntoView(webElement, true);
    }

    private void mockGetCurrentUrl()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(SIMPLE_URL);
    }
}
