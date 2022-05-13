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

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import javax.inject.Inject;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.testcontext.TestContext;

public class UiContext implements IUiContext
{
    private static final Object KEY = SearchContextData.class;
    private static final String ASSERTING_ELEMENTS_KEY = "AssertingElements";

    private TestContext testContext;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private ISoftAssert softAssert;

    @Override
    public SearchContext getSearchContext()
    {
        return getSearchContextImpl();
    }

    @Override
    public Optional<SearchContext> getOptionalSearchContext()
    {
        return getSearchContext(SearchContext.class);
    }

    @Override
    public <T extends SearchContext> Optional<T> getSearchContext(Class<T> clazz)
    {
        SearchContext searchContext = getSearchContextImpl();
        if (clazz.isInstance(searchContext))
        {
            return Optional.of(clazz.cast(searchContext));
        }
        if (searchContext != null)
        {
            String exceptionMessage =
                    "Expected search context of " + clazz + ", but was " + searchContext.getClass() + " search context";
            throw new IllegalSearchContextException(exceptionMessage);
        }
        softAssert.recordFailedAssertion("Search context is not set");
        return Optional.empty();
    }

    @Override
    public SearchContextSetter getSearchContextSetter()
    {
        SearchContextData searchContextData = getSearchContextData();
        if (searchContextData != null)
        {
            return searchContextData.getSearchContextSetter();
        }
        return null;
    }

    @Override
    public void putSearchContext(SearchContext searchContext, SearchContextSetter setter)
    {
        SearchContextData searchContextData = new SearchContextData();
        searchContextData.setSearchContext(searchContext);
        searchContextData.setSearchContextSetter(setter);
        testContext.put(KEY, searchContextData);
    }

    @Override
    public void reset()
    {
        clear();
        if (webDriverProvider.isWebDriverInitialized())
        {
            putSearchContext(webDriverProvider.get(), this::reset);
        }
    }

    @Override
    public void clear()
    {
        testContext.remove(KEY);
    }

    @Override
    public List<WebElement> getAssertingWebElements()
    {
        return testContext.get(ASSERTING_ELEMENTS_KEY, List::of);
    }

    @Override
    public boolean withAssertingWebElements(List<WebElement> elements, BooleanSupplier asserter)
    {
        try
        {
            testContext.put(ASSERTING_ELEMENTS_KEY, elements);
            return asserter.getAsBoolean();
        }
        finally
        {
            testContext.put(ASSERTING_ELEMENTS_KEY, List.of());
        }
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    private SearchContext getSearchContextImpl()
    {
        SearchContextData searchContext = getSearchContextData();
        if (searchContext != null)
        {
            return searchContext.getSearchContext();
        }
        return null;
    }

    protected SearchContextData getSearchContextData()
    {
        return testContext.get(KEY, SearchContextData.class);
    }

    protected static class SearchContextData
    {
        private SearchContext searchContext;
        private SearchContextSetter searchContextSetter;

        public SearchContext getSearchContext()
        {
            return searchContext;
        }

        public void setSearchContext(SearchContext searchContext)
        {
            this.searchContext = searchContext;
        }

        public SearchContextSetter getSearchContextSetter()
        {
            return searchContextSetter;
        }

        public void setSearchContextSetter(SearchContextSetter searchContextSetter)
        {
            this.searchContextSetter = searchContextSetter;
        }
    }
}
