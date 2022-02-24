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

package org.vividus.http.client;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class LoggingHttpRequestInterceptorTests
{
    @Mock private HttpRequest request;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(LoggingHttpRequestInterceptor.class);

    @Test
    void shouldLogMessageWithLoggerByClass() throws HttpException, IOException
    {
        LoggingHttpRequestInterceptor interceptor = new LoggingHttpRequestInterceptor(
                LoggingHttpRequestInterceptor.class);
        interceptor.process(request, null);
        verifyLogger();
    }

    @Test
    void shouldLogMessageWithLoggerByName() throws HttpException, IOException
    {
        LoggingHttpRequestInterceptor interceptor = new LoggingHttpRequestInterceptor(
                LoggingHttpRequestInterceptor.class.getName());
        interceptor.process(request, null);
        verifyLogger();
    }

    private void verifyLogger()
    {
        assertThat(logger.getLoggingEvents(), is(List.of(info("{}", request))));
    }
}
