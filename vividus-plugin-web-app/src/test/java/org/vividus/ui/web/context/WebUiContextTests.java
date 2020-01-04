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

package org.vividus.ui.web.context;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
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
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class WebUiContextTests
{
    private static final String EXPECTED_SEARCH_CONTEXT_OF_INTERFACE = "Expected search context of interface ";

    private final TestContext context = new SimpleTestContext()
    {
        private final Map<Object, Object> map = new HashMap<>();

        @Override
        public void clear()
        {
            map.clear();
        }

        @Override
        public int size()
        {
            return map.size();
        }

        @Override
        public void remove(Object key)
        {
            map.remove(key);
        }

        @Override
        public void putAll(Map<Object, Object> map)
        {
            // Implementation is not needed
        }

        @Override
        public void put(Object key, Object value)
        {
            map.put(key, value);
        }

        @Override
        public void copyAllTo(Map<Object, Object> map)
        {
            // Implementation is not needed
        }

        @Override
        public <T> T get(Object key, Class<T> type)
        {
            return type.cast(map.get(key));
        }

        @Override
        public <T> T get(Object key, Supplier<T> initialValueSupplier)
        {
            // Implementation is not needed
            return null;
        }

        @Override
        public <T> T get(Object key)
        {
            // Implementation is not needed
            return null;
        }

        @Override
        public void putInitValueSupplier(Object key, Supplier<Object> initialValueSupplier)
        {
            // Implementation is not needed
        }
    };

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
    private WebUiContext stepContext;

    @Test
    void testGetSearchContext()
    {
        stepContext.setTestContext(context);
        stepContext.putSearchContext(searchContextMock, searchContextSetter);
        SearchContext actualSearchContext = stepContext.getSearchContext();
        assertEquals(searchContextMock, actualSearchContext);
    }

    @Test
    void testGetSearchContextNotFound()
    {
        stepContext.setTestContext(context);
        assertNull(stepContext.getSearchContext());
    }

    @Test
    void testGetTypedSearchContext()
    {
        stepContext.setTestContext(context);
        when(webDriverProviderMock.get()).thenReturn(mockedWebDriver);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(true);
        stepContext.reset();
        Class<WebDriver> searchContextClass = WebDriver.class;
        WebDriver actualSearchContext = stepContext.getSearchContext(searchContextClass);
        assertEquals(mockedWebDriver, actualSearchContext);
    }

    @Test
    void testGetTypedSearchContextWithException()
    {
        stepContext.setTestContext(context);
        when(webDriverProviderMock.get()).thenReturn(mockedWebDriver);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(true);
        stepContext.reset();
        IllegalSearchContextException exception = assertThrows(IllegalSearchContextException.class,
            () -> stepContext.getSearchContext(WebElement.class));
        assertThat(exception.getMessage(), containsString(EXPECTED_SEARCH_CONTEXT_OF_INTERFACE
                + "org.openqa.selenium.WebElement, but was class org.openqa.selenium.WebDriver"));
    }

    @Test
    void testGetNullSearchContext()
    {
        stepContext.setTestContext(context);
        stepContext.putSearchContext(null, searchContextSetter);
        assertNull(stepContext.getSearchContext(WebDriver.class));
        verify(softAssert).recordFailedAssertion(
                EXPECTED_SEARCH_CONTEXT_OF_INTERFACE + "org.openqa.selenium.WebDriver, but was null search context");
    }

    @Test
    void testGetSearchContextSetter()
    {
        stepContext.setTestContext(context);
        stepContext.putSearchContext(searchContextMock, searchContextSetter);
        SearchContextSetter result = stepContext.getSearchContextSetter();
        assertEquals(searchContextSetter, result);
    }

    @Test
    void testGetSearchContextSetterNotFound()
    {
        stepContext.setTestContext(context);
        assertNull(stepContext.getSearchContextSetter());
    }

    @Test
    void testPutSearchContext()
    {
        stepContext.setTestContext(context);
        stepContext.putSearchContext(searchContextMock, searchContextSetter);
        assertNotNull(stepContext.getSearchContext());
        assertNotNull(stepContext.getSearchContextSetter());
    }

    @Test
    void testResetWhenWebDriverIsInitialized()
    {
        stepContext.setTestContext(context);
        when(webDriverProviderMock.get()).thenReturn(mockedWebDriver);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(true);
        stepContext.reset();
        assertNotNull(stepContext.getSearchContext(WebDriver.class));
    }

    @Test
    void testResetWhenWebDriverIsNotInitialized()
    {
        stepContext.setTestContext(context);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProviderMock.isWebDriverInitialized()).thenReturn(false);
        stepContext.reset();
        assertNull(stepContext.getSearchContext());
    }

    @Test
    void testClear()
    {
        stepContext.setTestContext(context);
        stepContext.clear();
        assertNull(stepContext.getSearchContext());
    }
}
