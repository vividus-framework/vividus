/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.selenium.manager;

import java.util.function.Supplier;

import org.openqa.selenium.remote.DesiredCapabilities;

public enum WebDriverManagerParameter
{
    DESIRED_CAPABILITIES("desiredCapabilities", DesiredCapabilities::new),
    SCREEN_SIZE("screenSize", null);

    private final String contextKey;
    private transient Supplier<?> initialValueSupplier;

    <T> WebDriverManagerParameter(String contextKey, Supplier<T> initialValueSupplier)
    {
        this.contextKey = "WebDriverManagerContextParam-" + contextKey;
        this.initialValueSupplier = initialValueSupplier;
    }

    public String getContextKey()
    {
        return contextKey;
    }

    @SuppressWarnings("unchecked")
    public <T> Supplier<T> getInitialValueSupplier()
    {
        return (Supplier<T>) initialValueSupplier;
    }
}
