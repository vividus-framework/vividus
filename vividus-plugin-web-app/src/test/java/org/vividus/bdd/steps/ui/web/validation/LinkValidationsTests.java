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

package org.vividus.bdd.steps.ui.web.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.ui.web.action.search.ActionAttributeType.LINK_TEXT;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class LinkValidationsTests
{
    private static final String LINK_WITH_TEXT = "Link with text 'text'";
    private static final String URL_WITH_PORT = "http://page.com:8080/test/test/url";
    private static final String LINK_HAS_CORRECT_URL = "Link has correct URL, actual was: %1$s, expected: %2$s";
    private static final String URL_PAGE_COM = "http://page.com";
    private static final String URL_NODE_DOC_PRODUCT1 = "http://page.com/node/doc/product1";
    private static final String URL_DOC_PRODUCT1 = "http://page.com/doc/product1";
    private static final String RELATIVE_URL_NO_SLASH = "doc/product1";
    private static final String CURRENT_TEST_URL = "http://page.com/node";
    private static final String TEST_URL = "http://page.com/test/test/url";
    private static final String SOME_URL = "/someUrl";
    private static final String SIMPLE_URL = "http://example.com";
    private static final String SLASH = "/";
    private static final String HREF_ATTRIBUTE_WAS_NOT_FOUND_IN_ELEMENT = "Href attribute was not found in element";
    private static final String HREF = "href";
    private static final String STRING_TEXT = "text";
    private static final String STRING_LINK_URL = "url";
    private static final String STRING_LINK_TOOLTIP = "tooltip";

    @Mock
    private BaseValidations mockedBaseValidations;

    @Mock
    private IHighlightingSoftAssert mockedIHighlightingSoftAssert;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebDriver mockedWebDriver;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private WebElement mockedLink;

    @Mock
    private List<WebElement> mockedWebElementList;

    @Mock
    private ISearchActions searchActions;

    @Mock
    private IDescriptiveSoftAssert descriptiveSoftAssert;

    @InjectMocks
    private LinkValidations linkValidations;

    @Test
    void testAssertIfLinkWithTextExistsText()
    {
        linkValidations.assertIfLinkWithTextExists(mockedWebElement, STRING_TEXT);
        verify(mockedBaseValidations).assertIfElementExists(LINK_WITH_TEXT, mockedWebElement,
                new SearchAttributes(LINK_TEXT, STRING_TEXT));
    }

    @Test
    void testAssertIfLinkWithAttributesExists()
    {
        String mockForSearchAttributes = "Mock for SearchAttributes";
        when(mockedBaseValidations.assertIfElementExists(contains(mockForSearchAttributes), eq(mockedWebElement),
                any(SearchAttributes.class))).thenReturn(mockedWebElement);
        SearchAttributes attributes = mock(SearchAttributes.class);
        WebElement foundElement = linkValidations.assertIfLinkExists(mockedWebElement, attributes);
        ArgumentCaptor<SearchAttributes> captor = ArgumentCaptor.forClass(SearchAttributes.class);
        verify(mockedBaseValidations).assertIfElementExists(contains(mockForSearchAttributes),
                eq(mockedWebElement), captor.capture());
        SearchAttributes capturedAttributes = captor.getValue();
        assertEquals(mockedWebElement, foundElement);
        assertEquals(attributes, capturedAttributes);
    }

    @Test
    void testAssertIfLinkWithTextNotExistsFailed()
    {
        boolean isFound = false;
        SearchAttributes attributes = new SearchAttributes(LINK_TEXT, STRING_TEXT);
        when(mockedBaseValidations.assertIfElementDoesNotExist(LINK_WITH_TEXT, mockedWebElement, attributes))
                .thenReturn(isFound);
        assertEquals(isFound, linkValidations.assertIfLinkWithTextNotExists(mockedWebElement, STRING_TEXT));
    }

    @Test
    void testAssertIfLinkDoesNotExist()
    {
        SearchAttributes attributes = mock(SearchAttributes.class);
        assertFalse(linkValidations.assertIfLinkDoesNotExist(mockedWebElement, attributes));
    }

    @Test
    void testAssertIfLinkWithTextNotExistsNullSC()
    {
        boolean isFound = linkValidations.assertIfLinkWithTextNotExists(null, STRING_TEXT);
        verify(mockedBaseValidations).assertIfElementDoesNotExist(LINK_WITH_TEXT, (SearchContext) null,
                new SearchAttributes(LINK_TEXT, STRING_TEXT));
        assertFalse(isFound);
    }

    @Test
    void testAssertIfLinkWithTooltipExistsPassed()
    {
        linkValidations.assertIfLinkWithTooltipExists(mockedWebElement, STRING_LINK_TOOLTIP);
        verify(mockedBaseValidations).assertIfElementExists("Link with tooltip 'tooltip'", mockedWebElement,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(".//a[@title='%s']", STRING_LINK_TOOLTIP)));
    }

    @Test
    void testHrefFullPathWithSlashCurrentUrlWithSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(TEST_URL, TEST_URL, CURRENT_TEST_URL + SLASH);
    }

    @Test
    void testHrefFullPathCurrentUrlWithSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(TEST_URL + SLASH, TEST_URL + SLASH, CURRENT_TEST_URL);
    }

    @Test
    void testHrefStartsWithSlashCurrentUrlWithSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(SLASH + RELATIVE_URL_NO_SLASH, URL_DOC_PRODUCT1, "http://page.com/node/some/");
    }

    @Test
    void testHrefStartsWithSlashCurrentUrlWithoutSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(SLASH + RELATIVE_URL_NO_SLASH, URL_DOC_PRODUCT1, CURRENT_TEST_URL);
    }

    @Test
    void testHrefStartsWithoutSlashCurrentUrlWithSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(RELATIVE_URL_NO_SLASH, URL_NODE_DOC_PRODUCT1, CURRENT_TEST_URL + SLASH);
    }

    @Test
    void testHrefStartsWithoutSlashCurrentUrlWithoutSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(RELATIVE_URL_NO_SLASH, URL_NODE_DOC_PRODUCT1, "http://page.com/node/some");
    }

    @Test
    void testBaseUrlHrefWithSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(SOME_URL, "http://page.com/someUrl", URL_PAGE_COM);
    }

    @Test
    void testBaseUrlHrefWithSharp()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL("#someUrl", "http://page.com#someUrl", URL_PAGE_COM);
    }

    @Test
    void testBaseUrlHrefWithSlashAndOtherDomain()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedWebElement))
                .thenReturn(descriptiveSoftAssert);
        String actualHref = "http://page_XX.com/someUrl";
        when(mockedWebElement.getAttribute(HREF)).thenReturn(actualHref);
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL_PAGE_COM);
        linkValidations.assertIfLinkHrefMatchesURL(mockedWebElement, SOME_URL, true);
        String expected = String.format(LINK_HAS_CORRECT_URL, actualHref, SOME_URL);
        verify(descriptiveSoftAssert).assertEquals(eq(expected), eq(true), eq(false));
    }

    @Test
    void testHrefFullPathWithPortCurrentUrlWithSlash()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        testAssertIfLinkHrefMatchesURL(URL_WITH_PORT, URL_WITH_PORT, CURRENT_TEST_URL + SLASH);
    }

    @Test
    void testAssertIfLinkHrefMatchesURLWithWrongHrefAndUrl()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        mockHrefAttribute(SIMPLE_URL, SIMPLE_URL);
        boolean ifFound = linkValidations.assertIfLinkHrefMatchesURL(mockedLink, STRING_LINK_URL, true);
        assertFalse(ifFound);
        verify(descriptiveSoftAssert, never()).recordFailedAssertion(HREF_ATTRIBUTE_WAS_NOT_FOUND_IN_ELEMENT);
        String description = String.format(LINK_HAS_CORRECT_URL, SIMPLE_URL, STRING_LINK_URL);
        verify(descriptiveSoftAssert).assertEquals(description, true, false);
    }

    @Test
    void testAssertIfLinkHrefMatchesURLFalseEquals()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        mockHrefAttribute(URL_WITH_PORT, CURRENT_TEST_URL + SLASH);
        linkValidations.assertIfLinkHrefMatchesURL(mockedLink, TEST_URL, false);
        verifyLinkAssertion(TEST_URL, false);
    }

    @Test
    void testAssertIfLinkHrefMatchesURLWhenCurrentUrlIsMalformed()
    {
        mockHrefAttribute(SIMPLE_URL, "123");
        assertThrows(IllegalStateException.class,
            () -> linkValidations.assertIfLinkHrefMatchesURL(mockedLink, STRING_LINK_URL, true));
    }

    @Test
    void testAssertIfLinkHrefMatchesURLWhenCurrentUrlHasPort()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        mockHrefAttribute(URL_WITH_PORT, URL_WITH_PORT);
        linkValidations.assertIfLinkHrefMatchesURL(mockedLink, URL_WITH_PORT, false);
        verifyLinkAssertion(URL_WITH_PORT, true);
    }

    void verifyLinkAssertion(String expectedUrl, boolean actual)
    {
        verify(descriptiveSoftAssert).assertEquals(
                "Link does not have specified URL, actual was: " + URL_WITH_PORT + "," + " expected: " + expectedUrl,
                false, actual);
    }

    @Test
    void testAssertIfLinkHrefMatchesURLNullHref()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        mockHrefAttribute(URL_WITH_PORT, CURRENT_TEST_URL + SLASH);
        when(mockedLink.getAttribute(HREF)).thenReturn(null);
        linkValidations.assertIfLinkHrefMatchesURL(mockedLink, TEST_URL, true);
        verify(descriptiveSoftAssert).recordFailedAssertion(HREF_ATTRIBUTE_WAS_NOT_FOUND_IN_ELEMENT);
    }

    @Test
    void testAssertIfLinkHrefMatchesURLNullHrefFalseEquals()
    {
        mockHrefAttribute(URL_WITH_PORT, CURRENT_TEST_URL);
        when(mockedLink.getAttribute(HREF)).thenReturn(null);
        assertTrue(linkValidations.assertIfLinkHrefMatchesURL(mockedLink, TEST_URL, false));
        verify(descriptiveSoftAssert, never()).recordFailedAssertion(HREF_ATTRIBUTE_WAS_NOT_FOUND_IN_ELEMENT);
    }

    @Test
    void testAssertIfLinkHrefMatchesURLNullLink()
    {
        assertFalse(linkValidations.assertIfLinkHrefMatchesURL(null, TEST_URL, true));
        verifyZeroInteractions(descriptiveSoftAssert);
    }

    @Test
    void testAssertIfLinkHrefMatchesURLNullUrl()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedLink))
                .thenReturn(descriptiveSoftAssert);
        mockHrefAttribute(URL_WITH_PORT, CURRENT_TEST_URL);
        assertFalse(linkValidations.assertIfLinkHrefMatchesURL(mockedLink, null, true));
    }

    private void testAssertIfLinkHrefMatchesURL(String url, String actualHref, String currentUrl)
    {
        mockHrefAttribute(actualHref, currentUrl);
        linkValidations.assertIfLinkHrefMatchesURL(mockedLink, url, true);
        String description = String.format(LINK_HAS_CORRECT_URL, url, url);
        verify(descriptiveSoftAssert).assertEquals(description, true, true);
    }

    private void mockHrefAttribute(String actualHref, String currentUrl)
    {
        when(mockedLink.getAttribute(HREF)).thenReturn(actualHref);
        Mockito.lenient().when(webDriverProvider.get()).thenReturn(mockedWebDriver);
        Mockito.lenient().when(mockedWebDriver.getCurrentUrl()).thenReturn(currentUrl);
    }
}
