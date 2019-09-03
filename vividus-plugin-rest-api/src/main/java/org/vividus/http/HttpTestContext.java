/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.vividus.http.client.HttpResponse;
import org.vividus.testcontext.TestContext;

public class HttpTestContext
{
    private static final Object KEY = HttpTestContextData.class;

    private final TestContext testContext;

    public HttpTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void putRequestEntity(HttpEntity requestEntity)
    {
        getData().requestEntity = requestEntity;
    }

    public void putRequestHeaders(List<Header> requestHeaders)
    {
        getData().requestHeaders = requestHeaders;
    }

    public void putCookieStore(CookieStore cookieStore)
    {
        getData().cookieStore = cookieStore;
    }

    public void putConnectionDetails(ConnectionDetails connectionDetails)
    {
        getData().connectionDetails = connectionDetails;
    }

    public void putResponse(HttpResponse response)
    {
        HttpTestContextData data = getData();
        data.response = response;
        data.jsonElement = Optional.empty();
    }

    public void putJsonContext(String jsonElement)
    {
        getData().jsonElement = Optional.ofNullable(jsonElement);
    }

    public void putRequestConfig(RequestConfig requestConfig)
    {
        getData().requestConfig = requestConfig;
    }

    Optional<HttpEntity> getRequestEntity()
    {
        return Optional.ofNullable(getData().requestEntity);
    }

    List<Header> getRequestHeaders()
    {
        return getData().requestHeaders;
    }

    public ConnectionDetails getConnectionDetails()
    {
        return getData().connectionDetails;
    }

    public HttpResponse getResponse()
    {
        return getData().response;
    }

    public String getJsonContext()
    {
        return getData().jsonElement.orElse(getResponse() == null ? null : getResponse().getResponseBodyAsString());
    }

    public Optional<CookieStore> getCookieStore()
    {
        return Optional.ofNullable(getData().cookieStore);
    }

    public Optional<RequestConfig> getRequestConfig()
    {
        return Optional.ofNullable(getData().requestConfig);
    }

    void releaseRequestData()
    {
        putRequestEntity(null);
        putRequestHeaders(new ArrayList<>());
    }

    private HttpTestContextData getData()
    {
        return testContext.get(KEY, HttpTestContextData::new);
    }

    private static class HttpTestContextData
    {
        private HttpEntity requestEntity;
        private List<Header> requestHeaders = new ArrayList<>();
        private CookieStore cookieStore;
        private ConnectionDetails connectionDetails;

        private HttpResponse response;
        private Optional<String> jsonElement = Optional.empty();
        private RequestConfig requestConfig;
    }
}
