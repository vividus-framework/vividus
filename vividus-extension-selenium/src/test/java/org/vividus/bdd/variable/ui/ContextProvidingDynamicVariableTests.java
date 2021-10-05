/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.variable.ui;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.vividus.ui.context.UiContext;

class ContextProvidingDynamicVariableTests
{
    private static final TestLogger LOGGER = TestLoggerFactory
            .getTestLogger(AbstractContextProvidingDynamicVariable.class);

    @Test
    void shouldReturnMinuseOneInCaseOfMissingContext()
    {
        var dynamicVariable = new AbstractContextProvidingDynamicVariable(mock(UiContext.class))
        {
            @Override
            public String getValue()
            {
                return getContextRectValue(r -> {
                    throw new NotImplementedException();
                });
            }
        };
        assertEquals("-1", dynamicVariable.getValue());
        assertThat(LOGGER.getLoggingEvents(), is(List
                .of(error("Unable to get coordinate, context is not set"))));
    }
}
