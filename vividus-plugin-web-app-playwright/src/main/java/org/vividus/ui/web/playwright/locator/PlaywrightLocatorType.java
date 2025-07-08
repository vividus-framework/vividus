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
            return createXpathLocator(value);
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
    },
    LINK_TEXT
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createXpathLocator(".//a[text()='%1$s' or @*='%1$s' or *='%1$s']", value);
        }
    },
    LINK_URL
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createCssLocator("a[href='%s']", value);
        }
    },
    LINK_URL_PART
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createCssLocator("a[href*='%s']", value);
        }
    },
    IMAGE_SRC
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createCssLocator("img[src='%s']", value);
        }
    },
    IMAGE_SRC_PART
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createCssLocator("img[src*='%s']", value);
        }
    },
    FIELD_NAME
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            // due to firefox bug, we can't use name() and must use local-name()
            // as workaround 'body' represents CKE editor
            return createXpathLocator(".//*[(local-name() = 'input' or local-name() = 'textarea' or "
                    + "local-name()='body') and ((@* | text())='%1$s' or @id=(//label[text() = '%1$s']/@for))]", value);
        }
    },
    RADIO_BUTTON
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createXpathLocator(".//input[@type='radio' and ((@* | text())='%1$s' or "
                                        + "@id=(//label[text() = '%1$s']/@for))]", value);
        }
    },
    NAME
    {
        @Override
        public PlaywrightLocator createLocator(String value)
        {
            return createXpathLocator(".//*[@*='%1$s' or text()='%1$s']", value);
        }
    };

    public abstract PlaywrightLocator createLocator(String value);

    private static PlaywrightLocator createCssLocator(String value)
    {
        return new PlaywrightLocator("css", value);
    }

    private static PlaywrightLocator createCssLocator(String locatorPattern, String value)
    {
        return createCssLocator(String.format(locatorPattern, value));
    }

    private static PlaywrightLocator createXpathLocator(String locatorPattern, Object... args)
    {
        return new PlaywrightLocator("xpath", String.format(locatorPattern, args));
    }
}
