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

package org.vividus.ui.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class UiContextTests extends UiContextTestsBase
{
    private static final String EXPECTED_SEARCH_CONTEXT_OF_INTERFACE = "Expected search context of interface ";
    private static final String ASSERTING_ELEMENTS_KEY = "AssertingElements";

    @Mock
    private IWebDriverProvider webDriverProviderMock;

    @Mock
    private SearchContext searchContextMock;

    @Mock
    private SearchContextSetter searchContextSetter;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private WebDriver mockedWebDriver;

    @InjectMocks
    private UiContext uiContext;

    @Test
    void testGetSearchContext()
    {
        uiContext.setTestContext(getContext());
        uiContext.putSearchContext(searchContextMock, searchContextSetter);
        SearchContext actualSearchContext = uiContext.getSearchContext();
        assertEquals(searchContextMock, actualSearchContext);
    }

    @Test
    void testGetSearchContextNotFound()
    {
        uiContext.setTestContext(getContext());
        assertNull(uiContext.getSearchContext());
    }

    @Test
    void testGetTypedSearchContext()
    {
        uiContext.setTestContext(getContext());
        when(webDriverProviderMock.get()).thenReturn(mockedWebDriver);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(true);
        uiContext.reset();
        Class<WebDriver> searchContextClass = WebDriver.class;
        WebDriver actualSearchContext = uiContext.getSearchContext(searchContextClass);
        assertEquals(mockedWebDriver, actualSearchContext);
    }

    @Test
    void testGetTypedSearchContextWithException()
    {
        uiContext.setTestContext(getContext());
        when(webDriverProviderMock.get()).thenReturn(mockedWebDriver);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(true);
        uiContext.reset();
        IllegalSearchContextException exception = assertThrows(IllegalSearchContextException.class,
            () -> uiContext.getSearchContext(WebElement.class));
        assertThat(exception.getMessage(), containsString(EXPECTED_SEARCH_CONTEXT_OF_INTERFACE
                + "org.openqa.selenium.WebElement, but was class org.openqa.selenium.WebDriver"));
    }

    @Test
    void testGetNullSearchContext()
    {
        uiContext.setTestContext(getContext());
        uiContext.putSearchContext(null, searchContextSetter);
        assertNull(uiContext.getSearchContext(WebDriver.class));
        verify(softAssert).recordFailedAssertion(
                EXPECTED_SEARCH_CONTEXT_OF_INTERFACE + "org.openqa.selenium.WebDriver, but was null search context");
    }

    @Test
    void testGetSearchContextSetter()
    {
        uiContext.setTestContext(getContext());
        uiContext.putSearchContext(searchContextMock, searchContextSetter);
        SearchContextSetter result = uiContext.getSearchContextSetter();
        assertEquals(searchContextSetter, result);
    }

    @Test
    void testGetSearchContextSetterNotFound()
    {
        uiContext.setTestContext(getContext());
        assertNull(uiContext.getSearchContextSetter());
    }

    @Test
    void testPutSearchContext()
    {
        uiContext.setTestContext(getContext());
        uiContext.putSearchContext(searchContextMock, searchContextSetter);
        assertNotNull(uiContext.getSearchContext());
        assertNotNull(uiContext.getSearchContextSetter());
    }

    @Test
    void testResetWhenWebDriverIsInitialized()
    {
        uiContext.setTestContext(getContext());
        when(webDriverProviderMock.get()).thenReturn(mockedWebDriver);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(true);
        uiContext.reset();
        assertNotNull(uiContext.getSearchContext(WebDriver.class));
    }

    @Test
    void testResetWhenWebDriverIsNotInitialized()
    {
        uiContext.setTestContext(getContext());
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(false);
        uiContext.reset();
        assertNull(uiContext.getSearchContext());
    }

    @Test
    void testClear()
    {
        uiContext.setTestContext(getContext());
        uiContext.clear();
        assertNull(uiContext.getSearchContext());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAssertingWebElements()
    {
        TestContext testContext = mock(TestContext.class);
        uiContext.setTestContext(testContext);
        uiContext.getAssertingWebElements();
        verify(testContext).get(eq(ASSERTING_ELEMENTS_KEY), any(Supplier.class));
        verifyNoMoreInteractions(testContext);
    }

    @Test
    void testWithAssertingWebElements()
    {
        BooleanSupplier supplier = mock(BooleanSupplier.class);
        when(supplier.getAsBoolean()).thenReturn(true);
        TestContext testContext = mock(TestContext.class);
        uiContext.setTestContext(testContext);
        WebElement element = mock(WebElement.class);
        assertTrue(uiContext.withAssertingWebElements(List.of(element), supplier));
        verify(testContext).put(ASSERTING_ELEMENTS_KEY, List.of(element));
        verify(testContext).put(ASSERTING_ELEMENTS_KEY, List.of());
        verifyNoMoreInteractions(testContext);
    }
}
