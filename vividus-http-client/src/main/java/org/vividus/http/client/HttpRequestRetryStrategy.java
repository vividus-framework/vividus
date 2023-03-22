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

package org.vividus.http.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.util.TimeValue;

public class HttpRequestRetryStrategy extends DefaultHttpRequestRetryStrategy
{
    private List<String> idempotentMethodsSendingRequestBody;

    public HttpRequestRetryStrategy(int maxRetries, Duration retryInterval, Set<Integer> statusCodes)
    {
        super(maxRetries, TimeValue.of(retryInterval), Set.of(), Optional.ofNullable(statusCodes).orElseGet(Set::of));
    }

    @Override
    protected boolean handleAsIdempotent(final HttpRequest request)
    {
        boolean idempotent = super.handleAsIdempotent(request);
        if (idempotent || idempotentMethodsSendingRequestBody == null)
        {
            return idempotent;
        }
        String method = request.getMethod().toUpperCase();
        return idempotentMethodsSendingRequestBody.contains(method);
    }

    public void setIdempotentMethodsSendingRequestBody(List<String> idempotentMethodsSendingRequestBody)
    {
        this.idempotentMethodsSendingRequestBody = idempotentMethodsSendingRequestBody;
    }
}
