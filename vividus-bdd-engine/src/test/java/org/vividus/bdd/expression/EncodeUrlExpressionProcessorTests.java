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

package org.vividus.bdd.expression;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(TestLoggerFactoryExtension.class)
class EncodeUrlExpressionProcessorTests
{
    private final IExpressionProcessor<String> processor = new EncodeUrlExpressionProcessor();

    private final TestLogger logger = TestLoggerFactory.getTestLogger(EncodeUrlExpressionProcessor.class);

    @Test
    void testExecuteWithUnsupportedException()
    {
        assertEquals(Optional.empty(), processor.execute("encodeUri(value)"));
        assertThat(logger.getLoggingEvents(), equalTo(List.of()));
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @CsvSource({
        "encodeUrl(/wiki/w3schools), %2Fwiki%2Fw3schools",
        "encodeUrl(/wls/recipes#!/), %2Fwls%2Frecipes%23%21%2F"
    })
    void testExecute(String expression, String expected)
    {
        assertEquals(Optional.of(expected), processor.execute(expression));
        assertThat(logger.getLoggingEvents(), equalTo(List.of(warn(
                "#{encodeUrl(..)} expression is deprecated, use set of #{encodeUri<Part>(..)} expressions instead"))));
    }
}
