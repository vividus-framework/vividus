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

import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.vividus.http.client.HttpResponse;

public interface IApiTestContext
{
    void putRequestEntity(HttpEntity requestEntity);

    void putRequestHeaders(List<Header> requestHeaders);

    void putConnectionDetails(ConnectionDetails connectionDetails);

    void putResponse(HttpResponse response);

    void putJsonContext(String jsonElement);

    void putCookieStore(CookieStore cookieStore);

    void putRequestConfig(RequestConfig requestConfig);

    Optional<HttpEntity> pullRequestEntity();

    List<Header> pullRequestHeaders();

    ConnectionDetails getConnectionDetails();

    HttpResponse getResponse();

    String getJsonContext();

    Optional<CookieStore> getCookieStore();

    Optional<RequestConfig> getRequestConfig();
}
