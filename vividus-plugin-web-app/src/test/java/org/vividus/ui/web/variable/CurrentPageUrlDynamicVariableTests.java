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
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.variable.DynamicVariableCalculationResult;

@ExtendWith(MockitoExtension.class)
class CurrentPageUrlDynamicVariableTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private CurrentPageUrlDynamicVariable dynamicVariable;

    @Test
    void shouldReturnCurrentUrl()
    {
        var url = "https://docs.vividus.dev/";
        var driver = mock(WebDriver.class);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);

        assertEquals(DynamicVariableCalculationResult.withValue(url), dynamicVariable.calculateValue());
    }

    @Test
    void shouldReturnErrorInCaseOfNotStartedSession()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        assertEquals(DynamicVariableCalculationResult.withError("browser is not started"),
                dynamicVariable.calculateValue());
    }
}
