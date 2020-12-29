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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class JavascriptActionsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;
    @InjectMocks private JavascriptActions javascriptActions;

    @Test
    void shouldExecuteSript()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        String script = "script";
        String arg1 = "arg1";
        String arg2 = "arg2";
        javascriptActions.executeScript(script, arg1, arg2);
        verify((JavascriptExecutor) webDriver).executeScript(script, arg1, arg2);
    }
}
