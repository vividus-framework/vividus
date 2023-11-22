/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.spring;

import org.springframework.core.convert.converter.Converter;
import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.locator.LocatorConverter;

public class StringToLocatorConverter implements Converter<String, Locator>
{
    private final LocatorConverter conversionUtils;

    public StringToLocatorConverter(LocatorConverter conversionUtils)
    {
        this.conversionUtils = conversionUtils;
    }

    @Override
    public Locator convert(String source)
    {
        return conversionUtils.convertToLocator(source);
    }
}
