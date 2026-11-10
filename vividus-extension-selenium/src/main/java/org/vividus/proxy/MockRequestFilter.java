/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.proxy;

import java.util.ArrayList;
import java.util.List;

import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class MockRequestFilter implements RequestFilter
{
    private final List<ProxyMock> proxyMocks = new ArrayList<>();

    @Override
    public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents,
            HttpMessageInfo messageInfo)
    {
        return proxyMocks.stream()
                .filter(proxyMock -> proxyMock.messageFilters()
                        .stream()
                        .allMatch(p -> p.test(messageInfo))
                )
                .findFirst()
                .map(proxyMock -> {
                    HttpResponse response = proxyMock.requestProcessor().apply(request);
                    if (response != null)
                    {
                        response.headers().add("Connection", "close");
                    }
                    return response;
                })
                .orElse(null);
    }

    public List<ProxyMock> getProxyMocks()
    {
        return proxyMocks;
    }
}
