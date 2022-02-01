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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelfHealingUiContext extends UiContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfHealingUiContext.class);

    @Override
    public SearchContext getSearchContext()
    {
        SearchContext searchContext = super.getSearchContext();
        return wrap(searchContext, SearchContext.class, super::getSearchContext);
    }

    @Override
    public <T extends SearchContext> T getSearchContext(Class<T> clazz)
    {
        return wrap(super.getSearchContext(clazz), clazz, () -> super.getSearchContext(clazz));
    }

    @SuppressWarnings({ "unchecked", "AvoidHidingCauseException" })
    private <T extends SearchContext> T wrap(T searchContext, Class<?> clazz, Supplier<T> searchContextGetter)
    {
        if (!(searchContext instanceof WebElement) || !clazz.isInterface())
        {
            return searchContext;
        }
        Set<Class<?>> proxied = new HashSet<>();
        proxied.add(WrapsElement.class);
        proxied.add(WebElement.class);
        proxied.add(clazz);
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
            proxied.toArray(new Class[proxied.size()]), (proxy, method, args) -> {
                if ("getWrappedElement".equals(method.getName()))
                {
                    return searchContext;
                }
                try
                {
                    return method.invoke(searchContext, args);
                }
                catch (InvocationTargetException invocationException)
                {
                    Throwable cause = invocationException.getCause();
                    if (cause instanceof StaleElementReferenceException)
                    {
                        LOGGER.atInfo().setCause(cause).log("Got an exception trying to reset context");
                        getSearchContextData().getSearchContextSetter().setSearchContext();
                        try
                        {
                            return method.invoke(searchContextGetter.get(), args);
                        }
                        catch (InvocationTargetException e)
                        {
                            throw e.getCause();
                        }
                    }
                    throw cause;
                }
            });
    }
}
