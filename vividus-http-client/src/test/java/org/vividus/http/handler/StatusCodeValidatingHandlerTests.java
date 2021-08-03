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

package org.vividus.http.handler;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.http.client.HttpResponse;

@ExtendWith(TestLoggerFactoryExtension.class)
class StatusCodeValidatingHandlerTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(StatusCodeValidatingHandler.class);

    @Test
    void shouldFailIfMinStatusCodeIsGreaterThanMaxStatusCode()
    {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new StatusCodeValidatingHandler(100, 1, null));
        assertEquals("Min allowed status code must be less than max status code", thrown.getMessage());
    }

    @Test
    void shouldFailIfMinStatusIsLessThanAllowedValue()
    {
        int invalidCode = 99;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new StatusCodeValidatingHandler(invalidCode, 400, null));
        assertEquals("Min status code must be greater than or equal to 100, but got " + invalidCode,
                thrown.getMessage());
    }

    @Test
    void shouldFailIfMaxStatusIsGreeaterThanAllowedValue()
    {
        int invalidCode = 600;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new StatusCodeValidatingHandler(100, invalidCode, null));
        assertEquals("Min status code must be less than or equal to 599, but got " + invalidCode, thrown.getMessage());
    }

    @Test
    void shouldHandleResponseWithValidStatusCode() throws IOException
    {
        HttpResponse response = spy(HttpResponse.class);
        response.setStatusCode(HttpStatus.SC_OK);

        StatusCodeValidatingHandler handler = new StatusCodeValidatingHandler(HttpStatus.SC_OK,
                HttpStatus.SC_MULTIPLE_CHOICES, null);
        handler.handle(response);

        assertThat(logger.getLoggingEvents(), is(empty()));
        verify(response).getStatusCode();
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST })
    void shouldFailOnUnexpectedStatusCode(int statusCode) throws IOException
    {
        HttpResponse response = spy(HttpResponse.class);
        response.setStatusCode(statusCode);
        String serviceName = "Rishikesh";

        StatusCodeValidatingHandler handler = new StatusCodeValidatingHandler(HttpStatus.SC_CREATED,
                HttpStatus.SC_MULTIPLE_CHOICES, serviceName);
        IOException thrown = assertThrows(IOException.class, () -> handler.handle(response));

        assertEquals("The status code is expected to be between " + HttpStatus.SC_CREATED + " and "
                + HttpStatus.SC_MULTIPLE_CHOICES + " inclusively, but got: " + statusCode, thrown.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of(error("{} response: {}", serviceName, response))));
        verify(response).getStatusCode();
    }
}
