/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.expressions;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLoggerFactoryExtension.class)
class LoggingExpressionResolverMonitorTests
{
    private final LoggingExpressionResolverMonitor monitor = new LoggingExpressionResolverMonitor();
    private final TestLogger logger = TestLoggerFactory.getTestLogger(LoggingExpressionResolverMonitor.class);

    @Test
    void shouldLogErrorMessageOnUnresolvableExpression()
    {
        RuntimeException runtimeException = mock();
        monitor.onExpressionProcessingError("{expressio()}", runtimeException);
        assertThat(logger.getLoggingEvents(),
                is(equalTo(List.of(error("{}", "Unable to process expression(s) '{expressio()}'")))));
    }
}
