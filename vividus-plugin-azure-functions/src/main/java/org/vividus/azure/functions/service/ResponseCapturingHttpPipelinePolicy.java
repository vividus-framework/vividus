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

package org.vividus.azure.functions.service;

import java.util.HashMap;
import java.util.Map;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;

import reactor.core.publisher.Mono;

public class ResponseCapturingHttpPipelinePolicy implements HttpPipelinePolicy
{
    private final Map<String, Object> recorded = new HashMap<>();
    private final String funcitonName;

    public ResponseCapturingHttpPipelinePolicy(String funcitonName)
    {
        this.funcitonName = funcitonName;
    }

    public Map<String, Object> getResponses()
    {
        return new HashMap<>(recorded);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next)
    {
        return next.process().doOnSuccess(this::saveResponse);
    }

    private void saveResponse(HttpResponse response)
    {
        String url = response.getRequest().getUrl().toString();
        if (url.endsWith(funcitonName))
        {
            recorded.put("url", url);
            recorded.put("status-code", response.getStatusCode());
            recorded.put("body", response.getBodyAsString());
            recorded.put("headers", response.getHeaders().toMap());
        }
    }
}
