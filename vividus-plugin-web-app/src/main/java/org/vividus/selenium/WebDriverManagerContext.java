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

package org.vividus.selenium;

import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;
import org.vividus.testcontext.TestContext;

public class WebDriverManagerContext implements IWebDriverManagerContext
{
    @Inject private TestContext testContext;

    @Override
    public <T> T getParameter(WebDriverManagerParameter parameter)
    {
        String key = parameter.getContextKey();
        Supplier<T> initialValueSupplier = parameter.getInitialValueSupplier();
        return initialValueSupplier == null ? testContext.get(key) : testContext.get(key, initialValueSupplier);
    }

    @Override
    public <T> void putParameter(WebDriverManagerParameter parameter, T value)
    {
        testContext.put(parameter.getContextKey(), value);
    }

    @Override
    public void reset()
    {
        Stream.of(WebDriverManagerParameter.values()).forEach(this::reset);
    }

    @Override
    public void reset(WebDriverManagerParameter parameter)
    {
        testContext.remove(parameter.getContextKey());
    }
}
