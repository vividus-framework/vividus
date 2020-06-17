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

package org.vividus.ui.web.action;

import java.util.List;
import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public final class ExpectedSearchContextConditions extends AbstractExpectedConditions<By>
{
    @Override
    protected List<WebElement> findElements(SearchContext searchContext, By by)
    {
        return searchInforming(() -> searchContext.findElements(by));
    }

    @Override
    protected WebElement findElement(SearchContext searchContext, By by)
    {
        return searchInforming(() -> searchContext.findElement(by));
    }

    private <E> E searchInforming(Supplier<E> search)
    {
        try
        {
            return search.get();
        }
        catch (StaleElementReferenceException toWrap)
        {
            throw new StaleContextException(toWrap);
        }
    }

    @Override
    protected String toStringParameters(By locator)
    {
        return "located by " + locator;
    }

    public static final class StaleContextException extends StaleElementReferenceException
    {
        private static final long serialVersionUID = 2449857900556530700L;

        private StaleContextException(Throwable throwable)
        {
            super("Search context used for search is stale.\n"
                + "Please double check the tests.\n"
                + "You have a few options:\n"
                + "1. Reset context;\n"
                + "2. Synchronize the tests to wait for context's stabilization;", throwable);
        }
    }
}
