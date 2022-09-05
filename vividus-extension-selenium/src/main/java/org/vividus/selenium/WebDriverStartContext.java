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

package org.vividus.selenium;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.function.Supplier;

import com.google.common.eventbus.Subscribe;

import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.selenium.manager.AbstractWebDriverManagerContext;
import org.vividus.testcontext.TestContext;

public class WebDriverStartContext extends AbstractWebDriverManagerContext<WebDriverStartParameter>
{
    public WebDriverStartContext(TestContext testContext)
    {
        super(testContext, WebDriverStartContext.class);
    }

    @Override
    public <T> T get(WebDriverStartParameter parameter)
    {
        Supplier<T> initialValueSupplier = parameter.getInitialValueSupplier();
        return initialValueSupplier == null ? super.get(parameter) : super.get(parameter, initialValueSupplier);
    }

    @Override
    public <T> T get(WebDriverStartParameter key, Supplier<T> initialValueSupplier)
    {
        isTrue(key.getInitialValueSupplier() == null, "Initial value supplier for parameter '%s' is not null",
                key);
        return super.get(key, initialValueSupplier);
    }

    @Subscribe
    public void onWebDriverCreate(WebDriverCreateEvent event)
    {
        reset();
    }
}
