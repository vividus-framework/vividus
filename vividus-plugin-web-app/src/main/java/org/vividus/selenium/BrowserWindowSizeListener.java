/*
 * Copyright 2019-2020 the original author or authors.
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

import com.google.common.eventbus.Subscribe;

import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.event.WebDriverCreateEvent;

public class BrowserWindowSizeListener
{
    private IWebDriverManager webDriverManager;
    private IWebDriverProvider webDriverProvider;
    private IBrowserWindowSizeProvider browserWindowSizeProvider;

    @Subscribe
    public void onWebDriverCreate(WebDriverCreateEvent event)
    {
        webDriverManager.resize(browserWindowSizeProvider.getBrowserWindowSize(webDriverProvider.isRemoteExecution()));
    }

    public void setWebDriverManager(IWebDriverManager webDriverManager)
    {
        this.webDriverManager = webDriverManager;
    }

    public void setWebDriverProvider(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    public void setBrowserWindowSizeProvider(IBrowserWindowSizeProvider browserWindowSizeProvider)
    {
        this.browserWindowSizeProvider = browserWindowSizeProvider;
    }
}
