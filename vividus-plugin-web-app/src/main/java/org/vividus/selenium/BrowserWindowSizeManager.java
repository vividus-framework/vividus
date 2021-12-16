/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.openqa.selenium.WebDriver.Window;

public class BrowserWindowSizeManager implements IBrowserWindowSizeManager
{
    private final IWebDriverProvider webDriverProvider;
    private final IBrowserWindowSizeProvider browserWindowSizeProvider;

    public BrowserWindowSizeManager(IWebDriverProvider webDriverProvider,
                       IBrowserWindowSizeProvider browserWindowSizeProvider)
    {
        this.webDriverProvider = webDriverProvider;
        this.browserWindowSizeProvider = browserWindowSizeProvider;
    }

    @Override
    public void resizeBrowserWindow(BrowserWindowSize targetSize, Predicate<BrowserWindowSize> screenSizeCheck,
                                    Consumer<Window> actionsIfNoScreen)
    {
        resize(targetSize, browserWindowSizeProvider.getMaximumBrowserWindowSize(webDriverProvider.isRemoteExecution()),
                screenSizeCheck, actionsIfNoScreen);
    }

    @Override
    public void resizeBrowserWindow(BrowserWindowSize targetSize, Consumer<Window> actionsIfEmpty)
    {
        resize(targetSize, Optional.ofNullable(targetSize), screenSize -> true, actionsIfEmpty);
    }

    private void resize(BrowserWindowSize targetSize, Optional<BrowserWindowSize> screenSize,
                        Predicate<BrowserWindowSize> screenSizeCheck, Consumer<Window> actionsIfNoScreen)
    {
        Window window = webDriverProvider.get().manage().window();
        screenSize.ifPresentOrElse(size ->
        {
            if (screenSizeCheck.test(size))
            {
                window.setSize(targetSize.toDimension());
            }
        },
            () -> actionsIfNoScreen.accept(window));
    }
}
