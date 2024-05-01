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

package org.vividus.ui.web.listener;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.support.events.WebDriverListener;
import org.vividus.ui.context.IUiContext;

public class WebUiContextListener implements WebDriverListener
{
    private final WebDriver webDriver;
    private final IUiContext uiContext;
    private String currentWindowIdentifier = StringUtils.EMPTY;

    public WebUiContextListener(WebDriver webDriver, IUiContext uiContext)
    {
        this.webDriver = webDriver;
        this.uiContext = uiContext;
    }

    @Override
    public void beforeAnyNavigationCall(Navigation navigation, Method method, Object[] args)
    {
        uiContext.reset();
    }

    @Override
    public void beforeWindow(TargetLocator targetLocator, String nameOrHandle)
    {
        if (!currentWindowIdentifier.isEmpty())
        {
            Set<String> windowHandles = webDriver.getWindowHandles();
            if (!windowHandles.contains(currentWindowIdentifier))
            {
                webDriver.switchTo().window(windowHandles.iterator().next());
                uiContext.reset();
            }
        }
    }

    @Override
    public void afterWindow(TargetLocator targetLocator, String nameOrHandle, WebDriver driver)
    {
        currentWindowIdentifier = Optional.ofNullable(nameOrHandle).orElseGet(driver::getWindowHandle);
    }

    public static class Factory implements WebDriverListenerFactory
    {
        private final IUiContext uiContext;

        public Factory(IUiContext uiContext)
        {
            this.uiContext = uiContext;
        }

        @Override
        public WebUiContextListener createListener(WebDriver webDriver)
        {
            return new WebUiContextListener(webDriver, uiContext);
        }
    }
}
