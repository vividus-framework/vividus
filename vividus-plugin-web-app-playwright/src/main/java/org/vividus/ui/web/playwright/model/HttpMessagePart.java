/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.model;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

public enum HttpMessagePart
{
    URL
    {
        @Override
        public Object get(Request request)
        {
            return request.url();
        }
    },
    URL_QUERY
    {
        @Override
        public Object get(Request request)
        {
            return getQueryParameters(request);
        }
    },
    REQUEST_DATA
    {
        @Override
        public Object get(Request request)
        {
            return Map.of(
                    "query", getQueryParameters(request),
                    "resourceType", request.resourceType(),
                    "requestBody", createBodyData(request.headerValue(CONTENT_TYPE), request.postData()),
                    "responseStatus", request.response().status()
            );
        }
    },
    RESPONSE_DATA
    {
        @Override
        public Object get(Request request)
        {
            Response response = request.response();
            return Map.of("responseBody", createBodyData(response.headerValue(CONTENT_TYPE), response.text()));
        }
    };

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String NOT_DEFINED = "not defined";

    public abstract Object get(Request request);

    private static Map<String, List<String>> getQueryParameters(Request request)
    {
        try
        {
            List<NameValuePair> params = new URIBuilder(request.url()).getQueryParams();
            return params.stream()
                    .collect(groupingBy(NameValuePair::getName, mapping(NameValuePair::getValue, toList())));
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private static Map<String, String> createBodyData(String contentType, String text)
    {
        return Map.of(
                "contentType", contentType != null ? contentType : NOT_DEFINED,
                "text", text != null ? text : NOT_DEFINED
        );
    }
}
