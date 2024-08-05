/*
 * Copyright 2019-2024 the original author or authors.
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

public enum PlaywrightLocatorType
{
    ID
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return new PlaywrightLocator("id", value);
        }
    },
    CSS
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createCssLocator(value);
        }
    },
    XPATH
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return new PlaywrightLocator("xpath", value);
        }
    },
    TAG_NAME
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createCssLocator(value);
        }
    },
    CLASS_NAME
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createCssLocator("." + value);
        }
    };

    public abstract PlaywrightLocator createLocator(String value);

    private static PlaywrightLocator createCssLocator(String value)
    {
        return new PlaywrightLocator("css", value);
    }
}
