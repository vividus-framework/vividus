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

package org.vividus.ui.web.action;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.event.PageLoadEndEvent;

public class FrameActions
{
    private final IWebDriverProvider webDriverProvider;
    private final TestContext testContext;
    private final WebJavascriptActions javascriptActions;

    public FrameActions(IWebDriverProvider webDriverProvider, TestContext testContext,
            WebJavascriptActions javascriptActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.testContext = testContext;
        this.javascriptActions = javascriptActions;
    }

    public void switchToFrame(WebElement frame)
    {
        getSwitcher().frame(frame);
        testContext.put(InFrame.class, true);
    }

    public void switchToRoot()
    {
        getSwitcher().defaultContent();
        testContext.put(InFrame.class, false);
    }

    @Subscribe
    public void handle(PageLoadEndEvent event)
    {
        if (!testContext.get(InFrame.class, () -> false))
        {
            return;
        }

        if (event.isNewPageLoaded())
        {
            testContext.remove(InFrame.class);
            return;
        }

        while (!isVisible())
        {
            getSwitcher().parentFrame();
        }

        boolean inFrame = javascriptActions.executeScript("return window != window.parent;");
        testContext.put(InFrame.class, inFrame);
    }

    private boolean isVisible()
    {
        return javascriptActions.executeScript(
                "return document.body && document.body.clientHeight > 0 && document.body.clientWidth > 0;");
    }

    private TargetLocator getSwitcher()
    {
        return webDriverProvider.get().switchTo();
    }

    private interface InFrame
    {
    }
}
