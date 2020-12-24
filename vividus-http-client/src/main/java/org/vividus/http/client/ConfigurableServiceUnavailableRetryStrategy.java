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

import java.time.Duration;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

public class ConfigurableServiceUnavailableRetryStrategy extends DefaultServiceUnavailableRetryStrategy
{
    private final int maxRetries;
    private final int[] statusCodes;

    public ConfigurableServiceUnavailableRetryStrategy(int maxRetries, Duration retryInterval, int[] statusCodes)
    {
        super(maxRetries, Math.toIntExact(retryInterval.toMillis()));
        this.maxRetries = maxRetries;
        this.statusCodes = ArrayUtils.clone(statusCodes);
    }

    @Override
    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context)
    {
        return executionCount <= maxRetries && ArrayUtils.contains(statusCodes,
                response.getStatusLine().getStatusCode());
    }
}
