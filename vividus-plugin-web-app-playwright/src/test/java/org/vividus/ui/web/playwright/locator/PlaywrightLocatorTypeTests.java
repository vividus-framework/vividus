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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PlaywrightLocatorTypeTests
{
    private static final String CSS_CLASS_NAME_LOCATOR = ".class";

    @Test
    void shouldCreateLocatorByCss()
    {
        var cssSelector = CSS_CLASS_NAME_LOCATOR;
        assertEquals(createCssLocator(cssSelector), PlaywrightLocatorType.CSS.createLocator(cssSelector));
    }

    @Test
    void shouldCreateLocatorByClassName()
    {
        var className = "class";
        assertEquals(createCssLocator(CSS_CLASS_NAME_LOCATOR),
                PlaywrightLocatorType.CLASS_NAME.createLocator(className));
    }

    @Test
    void shouldCreateLocatorByXpath()
    {
        var xpath = "//div";
        assertEquals(new PlaywrightLocator("xpath", xpath), PlaywrightLocatorType.XPATH.createLocator(xpath));
    }

    @Test
    void shouldCreateLocatorById()
    {
        var id = "id_locator";
        assertEquals(new PlaywrightLocator("id", id), PlaywrightLocatorType.ID.createLocator(id));
    }

    @Test
    void shouldCreateLocatorByTagName()
    {
        var tagName = "div";
        assertEquals(createCssLocator(tagName), PlaywrightLocatorType.TAG_NAME.createLocator(tagName));
    }

    private PlaywrightLocator createCssLocator(String value)
    {
        return new PlaywrightLocator("css", value);
    }
}
