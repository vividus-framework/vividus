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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfigurableServiceUnavailableRetryStrategyTests
{
    @Test
    void shouldSetRetryInterval()
    {
        int retryInterval = 123;
        ConfigurableServiceUnavailableRetryStrategy strategy = new ConfigurableServiceUnavailableRetryStrategy(1,
                Duration.ofMillis(retryInterval), new int[0]);
        assertEquals(retryInterval, strategy.getRetryInterval());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 500, true",
            "1, 418, false",
            "2, 500, false"
    })
    void shouldTakeIntoAccountParametersWhileCheckingForRetry(int executionCount, int statusCode, boolean retry)
    {
        ConfigurableServiceUnavailableRetryStrategy strategy = new ConfigurableServiceUnavailableRetryStrategy(1,
                Duration.ofMillis(1), new int[] { statusCode });
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(500);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusLine()).thenReturn(statusLine);
        assertEquals(retry, strategy.retryRequest(response, executionCount, null));
    }
}
