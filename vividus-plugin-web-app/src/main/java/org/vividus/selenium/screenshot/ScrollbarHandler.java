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

package org.vividus.selenium.screenshot;

import java.util.function.Supplier;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;

public class ScrollbarHandler implements IScrollbarHandler
{
    private IWebDriverProvider webDriverProvider;
    private IWebDriverManager webDriverManager;

    @Override
    public <T> T performActionWithHiddenScrollbars(Supplier<T> action)
    {
        return performWithHiddenScrollabrs(action, null);
    }

    @Override
    public <T> T performActionWithHiddenScrollbars(Supplier<T> action, WebElement scrollableElement)
    {
        return performWithHiddenScrollabrs(action, scrollableElement);
    }

    private <T> T performWithHiddenScrollabrs(Supplier<T> action, WebElement scrollableElement)
    {
        String parameter = scrollableElement == null ? "document.documentElement" : "arguments[0]";
        if (webDriverManager.isMobile())
        {
            return action.get();
        }
        Object originalStyleOverflow = null;
        JavascriptExecutor executor = (JavascriptExecutor) webDriverProvider.get();
        try
        {
            originalStyleOverflow = executor.executeScript(String.format(
                    "var originalStyleOverflow = %1$s.style.overflow;"
                  + "%1$s.style.overflow='hidden';"
                  + "return originalStyleOverflow;", parameter), scrollableElement);
            return action.get();
        }
        finally
        {
            if (null != originalStyleOverflow)
            {
                executor.executeScript(parameter + ".style.overflow=arguments[1];", scrollableElement,
                        originalStyleOverflow);
            }
        }
    }

    public void setWebDriverProvider(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    public void setWebDriverManager(IWebDriverManager webDriverManager)
    {
        this.webDriverManager = webDriverManager;
    }
}
