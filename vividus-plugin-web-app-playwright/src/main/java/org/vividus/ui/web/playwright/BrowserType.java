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

package org.vividus.ui.web.playwright;

import java.util.function.Function;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

public enum BrowserType
{
    CHROMIUM(Playwright::chromium),
    FIREFOX(Playwright::firefox),
    WEB_KIT(Playwright::webkit);

    private final Function<Playwright, com.microsoft.playwright.BrowserType> factory;

    BrowserType(Function<Playwright, com.microsoft.playwright.BrowserType> factory)
    {
        this.factory = factory;
    }

    public Browser launchBrowser(Playwright playwright,
            com.microsoft.playwright.BrowserType.LaunchOptions launchOptions)
    {
        return factory.apply(playwright).launch(launchOptions);
    }
}
