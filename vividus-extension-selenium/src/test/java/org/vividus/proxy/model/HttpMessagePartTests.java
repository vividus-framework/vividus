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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import de.sstoehr.harreader.model.HarContent;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarPostData;
import de.sstoehr.harreader.model.HarPostDataParam;
import de.sstoehr.harreader.model.HarQueryParam;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HarResponse;
import de.sstoehr.harreader.model.HttpMethod;

class HttpMessagePartTests
{
    private static final String URL = "www.test.com";
    private static final String KEY1 = "key1";
    private static final String VALUE1 = "value1";
    private static final String KEY2 = "key2";
    private static final String VALUE2 = "value2";
    private static final String MIME_TYPE = "mimeType";
    private static final String TEXT = "text";
    private static final String REQUEST_BODY = "requestBody";
    private static final String REQUEST_BODY_PARAMS = "requestBodyParameters";

    @Test
    void shouldReturnRequestUrl()
    {
        HarEntry harEntry = createHarEntry(HttpMethod.POST, HttpStatus.SC_OK, d -> { });
        assertEquals(URL, HttpMessagePart.URL.get(harEntry));
    }

    @Test
    void shouldReturnUrlQuery()
    {
        HarEntry harEntry = createHarEntry(HttpMethod.POST, HttpStatus.SC_OK, d -> { });
        Map<String, List<String>> queryParameters = Map.of(
            KEY1, List.of(VALUE1, VALUE2),
            KEY2, List.of(VALUE2)
        );
        assertEquals(queryParameters, HttpMessagePart.URL_QUERY.get(harEntry));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnRequestDataWithText()
    {
        HarEntry harEntry = createHarEntry(HttpMethod.POST, HttpStatus.SC_OK, data ->
        {
            data.setText(TEXT);
            data.setMimeType(MIME_TYPE);
        });
        Map<String, Object> requestData = (Map<String, Object>) HttpMessagePart.REQUEST_DATA.get(harEntry);
        Map<String, String> requestBody = (Map<String, String>) requestData.get(REQUEST_BODY);
        Map<String, List<String>> formData = (Map<String, List<String>>) requestData.get(REQUEST_BODY_PARAMS);
        assertRequestData(requestData);
        assertAll(
            () -> assertEquals(MIME_TYPE, requestBody.get(MIME_TYPE)),
            () -> assertEquals(TEXT, requestBody.get(TEXT)),
            () -> assertNull(formData)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnRequestDataWithParams()
    {
        HarEntry harEntry = createHarEntry(HttpMethod.POST, HttpStatus.SC_OK,
                data -> data.setParams(List.of(
                        createHarPostDataParam(KEY1, VALUE1),
                        createHarPostDataParam(KEY1, VALUE2),
                        createHarPostDataParam(KEY2, VALUE2))));
        Map<String, Object> requestData = (Map<String, Object>) HttpMessagePart.REQUEST_DATA.get(harEntry);
        Map<String, String> requestBody = (Map<String, String>) requestData.get(REQUEST_BODY);
        Map<String, List<String>> formData = (Map<String, List<String>>) requestData.get(REQUEST_BODY_PARAMS);
        assertRequestData(requestData);
        assertAll(
            () -> assertNull(requestBody),
            () -> assertEquals(List.of(VALUE1, VALUE2), formData.get(KEY1)),
            () -> assertEquals(List.of(VALUE2), formData.get(KEY2))
        );
    }

    private void assertRequestData(Map<String, Object> requestData)
    {
        Map<String, List<String>> urlQuery = (Map<String, List<String>>) requestData.get("query");
        Integer responseStatus = (Integer) requestData.get("responseStatus");
        assertAll(
            () -> assertEquals(List.of(VALUE1, VALUE2), urlQuery.get(KEY1)),
            () -> assertEquals(List.of(VALUE2), urlQuery.get(KEY2)),
            () -> assertEquals(HttpStatus.SC_OK, responseStatus)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnResponseData()
    {
        HarEntry harEntry = createHarEntry(HttpMethod.POST, HttpStatus.SC_OK, d -> { });
        Map<String, Object> responseData = (Map<String, Object>) HttpMessagePart.RESPONSE_DATA.get(harEntry);
        Map<String, String> responseBody = (Map<String, String>) responseData.get("responseBody");
        assertAll(
            () -> assertEquals(MIME_TYPE, responseBody.get(MIME_TYPE)),
            () -> assertEquals(TEXT, responseBody.get(TEXT))
        );
    }

    private HarEntry createHarEntry(HttpMethod httpMethod, int statusCode, Consumer<HarPostData> harPostDataConfigurer)
    {
        HarPostData postData = new HarPostData();
        postData.setMimeType(MIME_TYPE);
        harPostDataConfigurer.accept(postData);

        HarRequest request = new HarRequest();
        request.setMethod(httpMethod);
        request.setUrl(URL);
        request.setQueryString(List.of(
                createHarQueryParam(KEY1, VALUE1),
                createHarQueryParam(KEY1, VALUE2),
                createHarQueryParam(KEY2, VALUE2)
        ));
        request.setPostData(postData);

        HarContent harContent = new HarContent();
        harContent.setMimeType(MIME_TYPE);
        harContent.setText(TEXT);

        HarResponse response = new HarResponse();
        response.setStatus(statusCode);
        response.setContent(harContent);

        HarEntry harEntry = new HarEntry();
        harEntry.setRequest(request);
        harEntry.setResponse(response);
        return harEntry;
    }

    private HarQueryParam createHarQueryParam(String key, String value)
    {
        HarQueryParam harQueryParam = new HarQueryParam();
        harQueryParam.setName(key);
        harQueryParam.setValue(value);
        return harQueryParam;
    }

    private HarPostDataParam createHarPostDataParam(String key, String value)
    {
        HarPostDataParam postDataParam = new HarPostDataParam();
        postDataParam.setName(key);
        postDataParam.setValue(value);
        return postDataParam;
    }
}
