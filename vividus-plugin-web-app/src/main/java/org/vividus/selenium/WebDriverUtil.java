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

import java.util.function.Function;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;

public final class WebDriverUtil
{
    private WebDriverUtil()
    {
    }

    public static <T> T unwrap(WebDriver webDriver, Class<T> clazz)
    {
        return unwrap(webDriver, WrapsDriver.class, WrapsDriver::getWrappedDriver, clazz);
    }

    public static <T> T unwrap(WebElement webElement, Class<T> clazz)
    {
        return unwrap(webElement, WrapsElement.class, WrapsElement::getWrappedElement, clazz);
    }

    private static <T, W, R> R unwrap(T toUnwrap, Class<W> wrapperClazz, Function<W, T> unwrapper, Class<R> targetClazz)
    {
        T unwrapped = toUnwrap;
        while (!targetClazz.isAssignableFrom(unwrapped.getClass()))
        {
            if (wrapperClazz.isInstance(unwrapped))
            {
                unwrapped = unwrapper.apply(wrapperClazz.cast(unwrapped));
            }
            else
            {
                break;
            }
        }
        return targetClazz.cast(unwrapped);
    }
}
