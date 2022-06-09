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

package org.vividus.ui.web.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.variable.DynamicVariableCalculationResult;

@ExtendWith(MockitoExtension.class)
class BrowserWindowWidthDynamicVariableTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IGenericWebDriverManager webDriverManager;
    @InjectMocks private BrowserWindowWidthDynamicVariable dynamicVariable;

    @Test
    void shouldReturnWindowWidth()
    {
        var dimension = mock(Dimension.class);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverManager.getSize()).thenReturn(dimension);
        when(dimension.getWidth()).thenReturn(408);

        assertEquals(DynamicVariableCalculationResult.withValue("408"), dynamicVariable.calculateValue());
    }

    @Test
    void shouldReturnErrorInCaseOfNotStartedSession()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        assertEquals(DynamicVariableCalculationResult.withError("browser is not started"),
                dynamicVariable.calculateValue());
    }
}
