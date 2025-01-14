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

package org.vividus.proxy.model;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.sstoehr.harreader.model.HarContent;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarPostData;
import de.sstoehr.harreader.model.HarPostDataParam;
import de.sstoehr.harreader.model.HarQueryParam;
import de.sstoehr.harreader.model.HarRequest;

public enum HttpMessagePart
{
    URL
    {
        @Override
        public Object get(HarEntry harEntry)
        {
            return harEntry.getRequest().getUrl();
        }
    },
    URL_QUERY
    {
        @Override
        public Object get(HarEntry harEntry)
        {
            return getQueryParameters(harEntry.getRequest());
        }
    },
    REQUEST_DATA
    {
        @Override
        public Object get(HarEntry harEntry)
        {
            HarRequest request = harEntry.getRequest();
            HarPostData postData = request.getPostData();
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("query", getQueryParameters(request));
            requestData.put("responseStatus", harEntry.getResponse().getStatus());

            // As per spec text and params fields are mutually exclusive.
            if (postData.getText() != null)
            {
                requestData.put("requestBody", createBodyData(postData.getMimeType(), postData.getText()));
            }
            else
            {
                requestData.put("requestBodyParameters", getRequestBodyParameters(postData));
            }

            return requestData;
        }
    },
    RESPONSE_DATA
    {
        @Override
        public Object get(HarEntry harEntry)
        {
            HarContent harContext = harEntry.getResponse().getContent();
            return Map.of("responseBody",
                    createBodyData(harContext.getMimeType(), harContext.getText()));
        }
    };

    public abstract Object get(HarEntry harEntry);

    private static Map<String, List<String>> getQueryParameters(HarRequest request)
    {
        return request.getQueryString().stream().collect(
                groupingBy(HarQueryParam::getName, mapping(HarQueryParam::getValue, toList())));
    }

    private static Map<String, String> createBodyData(String mimeType, String text)
    {
        return Map.of(
                "mimeType", mimeType,
                "text", text
        );
    }

    private static Map<String, List<String>> getRequestBodyParameters(HarPostData postData)
    {
        return postData.getParams().stream().collect(
                groupingBy(HarPostDataParam::getName, mapping(HarPostDataParam::getValue, toList())));
    }
}
