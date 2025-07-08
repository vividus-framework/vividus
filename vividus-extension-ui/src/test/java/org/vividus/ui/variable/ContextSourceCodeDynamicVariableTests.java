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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.ContextSourceCodeProvider;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class ContextSourceCodeDynamicVariableTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(ContextSourceCodeDynamicVariable.class);

    @Mock private ContextSourceCodeProvider contextSourceCodeProvider;
    @InjectMocks private ContextSourceCodeDynamicVariable dynamicVariable;

    @Test
    void shouldReturnSourceCodeAsResultIfApplicationStarted()
    {
        var sourceCode = Optional.of("<html/>");
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(sourceCode);
        var actualResult = dynamicVariable.calculateValue();
        assertEquals(sourceCode, actualResult.getValueOrHandleError(null));
    }

    @Test
    void shouldReturnErrorAsResultIfApplicationNotStarted()
    {
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(Optional.empty());
        var actualResult = dynamicVariable.calculateValue();
        actualResult.getValueOrHandleError(logger::error);
        assertEquals(List.of(error("application is not started")), logger.getLoggingEvents());
    }
}
