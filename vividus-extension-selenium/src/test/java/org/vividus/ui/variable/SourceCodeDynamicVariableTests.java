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

package org.vividus.ui.variable;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.variable.DynamicVariableCalculationResult;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class SourceCodeDynamicVariableTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(SourceCodeDynamicVariable.class);

    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private SourceCodeDynamicVariable dynamicVariable;

    @Test
    void shouldReturnPageSource()
    {
        var sources = "<html></html>";
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        var driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getPageSource()).thenReturn(sources);

        assertEquals(DynamicVariableCalculationResult.withValue(sources), dynamicVariable.calculateValue());
        assertEquals(List.of(warn(
                "The \"${source-code}\" dynamic variable is deprecated and will be removed in VIVIDUS 0.7.0. "
                + "Please use \"${context-source-code}\" dynamic variable instead.")), logger.getLoggingEvents());
    }

    @Test
    void shouldReturnErrorInCaseOfNotStartedSession()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        assertEquals(DynamicVariableCalculationResult.withError("application is not started"),
                dynamicVariable.calculateValue());
    }
}
