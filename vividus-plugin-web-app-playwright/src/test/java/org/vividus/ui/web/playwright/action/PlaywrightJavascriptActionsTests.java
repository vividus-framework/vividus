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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
class PlaywrightJavascriptActionsTests
{
    private static final String JS_SCRIPT = "document.querySelector('[name=\"vividus-logo\"]').remove()";
    private static final String NO_ARGS_FORMAT = "async () => {%n%s%n}";
    private static final String ARGS_FORMAT = "arguments => {%n%s%n}";

    @Mock private UiContext uiContext;
    @InjectMocks private PlaywrightJavascriptActions playwrightJavascriptActions;

    @Test
    void shouldExecuteScript()
    {
        var page = mock(Page.class);
        when(uiContext.getCurrentPage()).thenReturn(page);
        playwrightJavascriptActions.executeScript(JS_SCRIPT);
        verify(page).evaluate(NO_ARGS_FORMAT.formatted(JS_SCRIPT));
    }

    @Test
    void shouldExecuteScriptWithArgument()
    {
        var page = mock(Page.class);
        var arg = "arg";
        when(uiContext.getCurrentPage()).thenReturn(page);
        playwrightJavascriptActions.executeScript(JS_SCRIPT, arg);
        verify(page).evaluate(ARGS_FORMAT.formatted(JS_SCRIPT), List.of(arg));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "num => num",
            "object => object.foo",
            "(button, from) => button.textContent.substring(from)",
            "(button, from ) => button.textContent.substring(from)",
            "( button, from) => button.textContent.substring(from)",
            "(button,from) => button.textContent.substring(from)",
            "o => o.button1.textContent + o.button2.textContent",
            "({button1, button2}) => button1.textContent + button2.textContent",
            "({ button1, button2 }) => button1.textContent + button2.textContent",
            "( {button1, button2} ) => button1.textContent + button2.textContent",
            "( { button1, button2 } ) => button1.textContent + button2.textContent",
            "([b1, b2]) => b1.textContent + b2.textContent",
            "( [b1, b2] ) => b1.textContent + b2.textContent",
            "( [ b1, b2 ] ) => b1.textContent + b2.textContent",
            "x => x.button1.textContent + x.list[0].textContent + String(x.foo)",
            "() => {\\n const response = (() => asd) await fetch(location.href);\\n  return response.status;\\n}",
            "async () => {\\n const response = (() => asd) await fetch(location.href);\\n  return response.status;\\n}"
    })
    void shouldNotDecorateScript(String script)
    {
        var page = mock(Page.class);
        when(uiContext.getCurrentPage()).thenReturn(page);
        playwrightJavascriptActions.executeScript(script);
        verify(page).evaluate(script);
    }
}
