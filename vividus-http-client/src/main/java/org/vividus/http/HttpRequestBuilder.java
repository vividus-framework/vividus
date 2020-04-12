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

package org.vividus.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.vividus.http.exception.HttpRequestBuildException;
import org.vividus.util.UriUtils;

public final class HttpRequestBuilder
{
    private HttpMethod httpMethod;
    private String endpoint;
    private String relativeUrl;
    private HttpEntity requestEntity;
    private List<Header> headers = new ArrayList<>();

    private HttpRequestBuilder()
    {
    }

    public static HttpRequestBuilder create()
    {
        return new HttpRequestBuilder();
    }

    public HttpRequestBuilder withHttpMethod(HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
        return this;
    }

    public HttpRequestBuilder withEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
        return this;
    }

    public HttpRequestBuilder withRelativeUrl(String relativeUrl)
    {
        this.relativeUrl = relativeUrl;
        return this;
    }

    public HttpRequestBuilder withContent(String content)
    {
        return withContent(content, null);
    }

    public HttpRequestBuilder withContent(String content, ContentType contentType)
    {
        return withContent(new StringEntity(content, contentType));
    }

    public HttpRequestBuilder withContent(HttpEntity requestEntity)
    {
        this.requestEntity = requestEntity;
        return this;
    }

    public HttpRequestBuilder withHeaders(List<Header> headers)
    {
        this.headers = Collections.unmodifiableList(headers);
        return this;
    }

    public HttpRequestBase build() throws HttpRequestBuildException
    {
        Validate.isTrue(endpoint != null, "Endpoint must be not null");
        Validate.isTrue(httpMethod != null, "HTTP method must be not null");
        HttpRequestBase request;
        try
        {
            URI uri = getUrl();
            request = requestEntity != null ? httpMethod.createEntityEnclosingRequest(uri, requestEntity)
                    : httpMethod.createRequest(uri);
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            throw new HttpRequestBuildException(e);
        }
        headers.forEach(request::addHeader);
        return request;
    }

    private URI getUrl()
    {
        String url = endpoint;
        if (relativeUrl != null)
        {
            url = url.concat(relativeUrl);
        }
        return UriUtils.createUri(url).normalize();
    }
}
