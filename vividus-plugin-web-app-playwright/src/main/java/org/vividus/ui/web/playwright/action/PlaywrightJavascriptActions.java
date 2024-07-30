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

package org.vividus.ui.web.playwright.action;

import java.util.List;

import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.playwright.UiContext;

public class PlaywrightJavascriptActions implements JavascriptActions
{
    private final UiContext uiContext;

    public PlaywrightJavascriptActions(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T executeScript(String script, Object... args)
    {
        if (args.length == 0)
        {
            return (T) uiContext.getCurrentPage().evaluate("async () => {%n%s%n}".formatted(script));
        }
        return (T) uiContext.getCurrentPage().evaluate(script, List.of(args));
    }
}
