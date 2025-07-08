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

package org.vividus.http.handler;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.http.client.HttpResponse;

@ExtendWith(TestLoggerFactoryExtension.class)
class StatusCodeValidatingHandlerTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(StatusCodeValidatingHandler.class);

    @Test
    void shouldFailIfMinStatusCodeIsGreaterThanMaxStatusCode()
    {
        var thrown = assertThrows(IllegalArgumentException.class,
            () -> new StatusCodeValidatingHandler(100, 1, null));
        assertEquals("Min allowed status code must be less than or equal to max status code", thrown.getMessage());
    }

    @Test
    void shouldFailIfMinStatusIsLessThanAllowedValue()
    {
        var invalidCode = 99;
        var thrown = assertThrows(IllegalArgumentException.class,
            () -> new StatusCodeValidatingHandler(invalidCode, 400, null));
        assertEquals("Min status code must be greater than or equal to 100, but got " + invalidCode,
                thrown.getMessage());
    }

    @Test
    void shouldFailIfMaxStatusIsGreaterThanAllowedValue()
    {
        var invalidCode = 600;
        var thrown = assertThrows(IllegalArgumentException.class,
            () -> new StatusCodeValidatingHandler(100, invalidCode, null));
        assertEquals("Min status code must be less than or equal to 599, but got " + invalidCode, thrown.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "200, 300, 200",
            "200, 300, 201",
            "200, 300, 300",
            "200, 200, 200"
    })
    void shouldHandleResponseWithValidStatusCode(int minInclusiveStatusCode, int maxInclusiveStatusCode, int statusCode)
            throws IOException
    {
        var response = new HttpResponse();
        response.setStatusCode(statusCode);

        var handler = new StatusCodeValidatingHandler(minInclusiveStatusCode, maxInclusiveStatusCode, null);
        handler.handle(response);

        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @ParameterizedTest
    @CsvSource({
            "201, 300, 200, 'The status code is expected to be between 201 and 300 inclusively, but got: 200'",
            "201, 300, 400, 'The status code is expected to be between 201 and 300 inclusively, but got: 400'",
            "201, 201, 200, 'The status code is expected to be 201, but got: 200'",
            "201, 201, 202, 'The status code is expected to be 201, but got: 202'"
    })
    void shouldFailOnUnexpectedStatusCodeFromRange(int minInclusiveStatusCode, int maxInclusiveStatusCode,
            int statusCode, String exceptionMessage)
    {
        var response = new HttpResponse();
        response.setStatusCode(statusCode);
        var serviceName = "Rishikesh";

        var handler = new StatusCodeValidatingHandler(minInclusiveStatusCode, maxInclusiveStatusCode, serviceName);
        var thrown = assertThrows(IOException.class, () -> handler.handle(response));

        assertEquals(exceptionMessage, thrown.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of(error("{} response: {}", serviceName, response))));
    }
}
