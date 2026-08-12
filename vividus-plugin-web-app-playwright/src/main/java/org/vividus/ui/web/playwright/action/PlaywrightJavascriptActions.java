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
import java.util.regex.Pattern;

import com.microsoft.playwright.Page;

import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.playwright.UiContext;

public class PlaywrightJavascriptActions implements JavascriptActions
{
    private static final Pattern PLAYWRIGHT_DECORATED_PATTERN = Pattern.compile("^(?:async\\s*)?"
            + "(?:\\(\\s*(?:\\{[^}]*\\}|\\[[^\\]]*\\]|[^,()]+(?:,\\s*[^,()]+)?)?\\s*\\)|[^()\\s]+)\\s*"
            + "=>\\s*(?:.|\\{|\\n)");

    private final UiContext uiContext;

    public PlaywrightJavascriptActions(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T executeScript(String script, Object... args)
    {
        boolean hasArgs = args.length > 0;
        String playwrightScript = isPlaywrightDecorated(script) ? script : decorateScript(script, hasArgs);
        Page currentPage = uiContext.getCurrentPage();
        return (T) (hasArgs ? currentPage.evaluate(playwrightScript, List.of(args))
                : currentPage.evaluate(playwrightScript));
    }

    private boolean isPlaywrightDecorated(String script)
    {
        return PLAYWRIGHT_DECORATED_PATTERN.matcher(script).find();
    }

    private String decorateScript(String script, boolean hasArgs)
    {
        String playwrightFormat = hasArgs ? "arguments => {%n%s%n}" : "async () => {%n%s%n}";
        return playwrightFormat.formatted(script);
    }
}
