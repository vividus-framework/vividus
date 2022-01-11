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

package org.vividus.steps.ui.web.listener;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.vividus.selenium.logging.BrowserLogManager;

public class BrowserLogCleanningListener extends AbstractWebDriverEventListener
{
    @Override
    public void beforeNavigateBack(WebDriver driver)
    {
        resetBrowserLogBuffer(driver);
    }

    @Override
    public void beforeNavigateForward(WebDriver driver)
    {
        resetBrowserLogBuffer(driver);
    }

    @Override
    public void beforeNavigateRefresh(WebDriver driver)
    {
        resetBrowserLogBuffer(driver);
    }

    @Override
    public void beforeNavigateTo(String url, WebDriver driver)
    {
        resetBrowserLogBuffer(driver);
    }

    private static void resetBrowserLogBuffer(WebDriver driver)
    {
        BrowserLogManager.resetBuffer(driver, true);
    }
}
