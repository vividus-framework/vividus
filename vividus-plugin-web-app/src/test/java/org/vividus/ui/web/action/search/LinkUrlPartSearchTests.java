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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class LinkUrlPartSearchTests
{
    private static final String HREF = "href";
    private static final String URL_PART = "urlPart";
    private static final String URL = "url" + URL_PART;
    private static final String OTHER_URL = "otherUrl";
    private static final By LINK_URL_PART_LOCATOR = By.xpath(".//a[contains (translate(@href,"
            + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), \"urlpart\")]");
    private static final String LINK_WITH_PART_URL_PATTERN = ".//a[contains(@href, %s)]";
    private static final String TOTAL_NUMBER_OF_ELEMENTS = "Total number of elements found {} is equal to {}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementSearchAction.class);

    private List<WebElement> webElements;

    @Mock
    private WebElement webElement;

    @Mock
    private SearchContext searchContext;

    @Mock
    private SearchParameters parameters;

    @Spy
    private LinkUrlPartSearch spy;

    @InjectMocks
    private LinkUrlPartSearch search;

    @BeforeEach
    void beforeEach()
    {
        webElements = new ArrayList<>();
        webElements.add(webElement);
    }

    @Test
    void testSearchLinksByUrlPart()
    {
        assertEquals(webElements, captureFoundElements(true, URL, HREF, URL_PART));
    }

    @Test
    void testSearchLinksByUrlPartCaseInsensetive()
    {
        assertEquals(webElements, captureFoundElements(false, URL, HREF, URL_PART));
    }

    @Test
    void testSearchLinksByUrlPartNotMatch()
    {
        assertTrue(captureFoundElements(true, OTHER_URL, HREF, URL_PART).isEmpty());
    }

    @Test
    void testSearchLinksByUrlPartNotMatchCaseInsensetive()
    {
        assertTrue(captureFoundElements(false, OTHER_URL, HREF, URL_PART).isEmpty());
    }

    @Test
    void testSearchLinksByUrlPartNoHref()
    {
        when(webElement.getAttribute(HREF)).thenReturn(null);
        List<WebElement> foundElements = search.filter(webElements, URL_PART);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchContextCaseSensitive()
    {
        when(parameters.getValue()).thenReturn(URL_PART);
        search.setCaseSensitiveSearch(true);
        spy = Mockito.spy(search);
        spy.search(searchContext, parameters);
        By locator = LocatorUtil.getXPathLocator(LINK_WITH_PART_URL_PATTERN, URL_PART);
        verify(spy).findElements(searchContext, locator, parameters);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 0))));
    }

    @Test
    void testSearchContextCaseInsensitive()
    {
        when(parameters.getValue()).thenReturn(URL_PART);
        search.setCaseSensitiveSearch(false);
        spy = Mockito.spy(search);
        spy.search(searchContext, parameters);
        verify(spy).findElements(searchContext, LINK_URL_PART_LOCATOR, parameters);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, LINK_URL_PART_LOCATOR, 0))));
    }

    @Test
    void testSearchContextNull()
    {
        when(parameters.getValue()).thenReturn(URL_PART);
        List<WebElement> foundElements = search.search(null, parameters);
        assertTrue(foundElements.isEmpty());
    }

    private List<WebElement> captureFoundElements(Boolean equals, String url, String actualHref, String currentUrl)
    {
        search.setCaseSensitiveSearch(equals);
        when(webElement.getAttribute(actualHref)).thenReturn(url);
        return search.filter(webElements, currentUrl);
    }
}
