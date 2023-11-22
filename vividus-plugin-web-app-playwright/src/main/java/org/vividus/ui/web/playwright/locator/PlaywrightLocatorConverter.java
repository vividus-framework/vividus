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

package org.vividus.ui.web.playwright.locator;

import java.util.Set;

import org.vividus.ui.locator.LocatorParser;

public final class PlaywrightLocatorConverter
{
    private PlaywrightLocatorConverter()
    {
    }

    public static Set<PlaywrightLocator> convertToLocatorSet(String locatorsAsString)
    {
        return LocatorParser.parseLocatorsCollection(locatorsAsString, PlaywrightLocatorConverter::convertToLocator);
    }

    public static PlaywrightLocator convertToLocator(String locatorAsString)
    {
        return LocatorParser.parseSingleLocator(locatorAsString, PlaywrightLocatorConverter::convertToLocator);
    }

    private static PlaywrightLocator convertToLocator(String type, String value)
    {
        return new PlaywrightLocator(type, value);
    }
}
