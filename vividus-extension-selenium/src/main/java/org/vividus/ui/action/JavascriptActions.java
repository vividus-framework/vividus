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

package org.vividus.ui.action;

import org.openqa.selenium.JavascriptExecutor;
import org.vividus.selenium.IWebDriverProvider;

public class JavascriptActions
{
    private final IWebDriverProvider webDriverProvider;

    public JavascriptActions(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    @SuppressWarnings("unchecked")
    public <T> T executeScript(String script, Object... args)
    {
        return (T) getJavascriptExecutor().executeScript(script, args);
    }

    protected JavascriptExecutor getJavascriptExecutor()
    {
        return (JavascriptExecutor) webDriverProvider.get();
    }
}
