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

import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
class PlaywrightJavascriptActionsTests
{
    private static final String JS_SCRIPT = "document.querySelector('[name=\"vividus-logo\"]').remove()";

    @Mock private UiContext uiContext;

    @InjectMocks private PlaywrightJavascriptActions playwrightJavascriptActions;

    @Test
    void shouldExecuteScript()
    {
        var page = mock(Page.class);
        when(uiContext.getCurrentPage()).thenReturn(page);
        playwrightJavascriptActions.executeScript(JS_SCRIPT);
        verify(page).evaluate(JS_SCRIPT, null);
    }

    @Test
    void shouldExecuteScriptWithArgument()
    {
        var page = mock(Page.class);
        var script = "script";
        var arg = "arg";
        when(uiContext.getCurrentPage()).thenReturn(page);
        playwrightJavascriptActions.executeScript(script, arg);
        verify(page).evaluate(script, arg);
    }
}
