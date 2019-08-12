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

package org.vividus.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.vividus.http.client.HttpResponse;
import org.vividus.testcontext.TestContext;

public class ApiTestContext implements IApiTestContext
{
    private static final Object KEY = ApiTestContextData.class;

    private TestContext testContext;

    @Override
    public void putRequestEntity(HttpEntity requestEntity)
    {
        getData().requestEntity = requestEntity;
    }

    @Override
    public void putRequestHeaders(List<Header> requestHeaders)
    {
        getData().requestHeaders = requestHeaders;
    }

    @Override
    public void putCookieStore(CookieStore cookieStore)
    {
        getData().cookieStore = cookieStore;
    }

    @Override
    public void putConnectionDetails(ConnectionDetails connectionDetails)
    {
        getData().connectionDetails = connectionDetails;
    }

    @Override
    public void putResponse(HttpResponse response)
    {
        ApiTestContextData data = getData();
        data.response = response;
        data.jsonElement = Optional.empty();
    }

    @Override
    public void putJsonContext(String jsonElement)
    {
        getData().jsonElement = Optional.ofNullable(jsonElement);
    }

    @Override
    public void putRequestConfig(RequestConfig requestConfig)
    {
        getData().requestConfig = requestConfig;
    }

    @Override
    public Optional<HttpEntity> pullRequestEntity()
    {
        HttpEntity requestEntity = getData().requestEntity;
        putRequestEntity(null);
        return Optional.ofNullable(requestEntity);
    }

    @Override
    public List<Header> pullRequestHeaders()
    {
        List<Header> requestHeaders = getData().requestHeaders;
        putRequestHeaders(new ArrayList<>());
        return requestHeaders;
    }

    @Override
    public ConnectionDetails getConnectionDetails()
    {
        return getData().connectionDetails;
    }

    @Override
    public HttpResponse getResponse()
    {
        return getData().response;
    }

    @Override
    public String getJsonContext()
    {
        return getData().jsonElement.orElse(getResponse() == null ? null : getResponse().getResponseBodyAsString());
    }

    @Override
    public Optional<CookieStore> getCookieStore()
    {
        return Optional.ofNullable(getData().cookieStore);
    }

    @Override
    public Optional<RequestConfig> getRequestConfig()
    {
        return Optional.ofNullable(getData().requestConfig);
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    private ApiTestContextData getData()
    {
        return testContext.get(KEY, ApiTestContextData::new);
    }

    private static class ApiTestContextData
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
