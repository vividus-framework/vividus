/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.steps.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.steps.DataWrapper;

@ExtendWith(MockitoExtension.class)
class HttpRequestStepsTests
{
    private static final String URL = "http://www.example.com/";
    private static final String RELATIVE_URL = "/relativeUrl";
    private static final String CONTENT = "content";

    @Mock private HttpTestContext httpTestContext;
    @Mock private HttpRequestExecutor httpRequestExecutor;
    @InjectMocks private HttpRequestSteps httpRequestSteps;

    @Test
    void testRequest()
    {
        httpRequestSteps.request(new DataWrapper(CONTENT));
        verifyPutRequestEntity(CONTENT);
    }

    @Test
    void testRequestUtf8Content()
    {
        String content = "{\"storeUserNewPassword\": \"зЛЖЛВеБЛги!\"}";
        httpRequestSteps.request(new DataWrapper(content));
        verifyPutRequestEntity(content);
    }

    @Test
    void testRequestBinaryContent() throws IOException
    {
        var content = "{there is some binary content}";
        httpRequestSteps.request(new DataWrapper(content.getBytes(StandardCharsets.UTF_8)));
        var captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpTestContext).putRequestEntity(captor.capture());
        var result = IOUtils.toString(captor.getValue().getContent(), StandardCharsets.UTF_8);
        assertEquals(content, result);
    }

    @SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
    static Stream<Arguments> multipartRequests()
    {
        return Stream.of(
                arguments("|name|type|value|\n|part|STRING|content|",
                        "Content-Disposition: form-data; name=\"part\"\r\n"
                                + "Content-Type: text/plain; charset=ISO-8859-1\r\n"
                                + "Content-Transfer-Encoding: 8bit\r\n\r\ncontent\r\n"),
                arguments("|name|type|value|contentType|\n|part|STRING|content|text/html; charset=utf-8|",
                        "Content-Disposition: form-data; name=\"part\"\r\n"
                                + "Content-Type: text/html; charset=utf-8\r\n"
                                + "Content-Transfer-Encoding: 8bit\r\n\r\ncontent\r\n"),
                arguments("|name|type|value|fileName|\n|part|FILE|/requestBody.txt|file.txt|",
                        "Content-Disposition: form-data; name=\"part\"; filename=\"file.txt\"\r\n"
                                + "Content-Type: application/octet-stream\r\n"
                                + "Content-Transfer-Encoding: binary\r\n\r\n{body}\r\n"),
                arguments("|name|type|value|contentType|\n|part|FILE|/requestBody.txt|text/plain; charset=utf-8|",
                        "Content-Disposition: form-data; name=\"part\"; filename=\"requestBody.txt\"\r\n"
                                + "Content-Type: text/plain; charset=utf-8\r\n"
                                + "Content-Transfer-Encoding: binary\r\n\r\n{body}\r\n"),
                arguments("|name|type|value|fileName|\n|part|BINARY|data|file.bin|",
                        "Content-Disposition: form-data; name=\"part\"; filename=\"file.bin\"\r\n"
                                + "Content-Type: application/octet-stream\r\n"
                                + "Content-Transfer-Encoding: binary\r\n\r\ndata\r\n")
        );
    }

    @ParameterizedTest
    @MethodSource("multipartRequests")
    void shouldPutMultipartRequestFromString(String tableAsString, String content) throws IOException
    {
        ExamplesTable requestParts = new ExamplesTable(tableAsString);
        httpRequestSteps.putMultipartRequest(requestParts);
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpTestContext).putRequestEntity(captor.capture());
        assertThat(IOUtils.toString(captor.getValue().getContent(), StandardCharsets.UTF_8), containsString(content));
    }

    @Test
    void testSetUpRequestHeaders()
    {
        ExamplesTable headers = new ExamplesTable("|name|value|\n|name1|value1|");
        httpRequestSteps.setUpRequestHeaders(headers);
        verify(httpTestContext).putRequestHeaders(argThat(actual ->
        {
            if (actual.size() == 1)
            {
                Header header = actual.get(0);
                return "name1".equals(header.getName()) && "value1".equals(header.getValue());
            }
            return false;
        }));
    }

    @Test
    void testAddRequestHeaders()
    {
        ExamplesTable headers = new ExamplesTable("|name|value|\n|newName|newValue|");
        httpRequestSteps.addRequestHeaders(headers);
        verify(httpTestContext).addRequestHeaders(argThat(actual ->
        {
            if (actual.size() == 1)
            {
                Header header = actual.get(0);
                return "newName".equals(header.getName()) && "newValue".equals(header.getValue());
            }
            return false;
        }));
    }

    @ParameterizedTest
    @EnumSource(HttpMethod.class)
    void testWhenIIssueHttpRequest(HttpMethod method) throws IOException
    {
        httpRequestSteps.whenIDoHttpRequest(method, URL);
        verify(httpRequestExecutor).executeHttpRequest(method, URL, Optional.empty());
    }

    @Test
    void testWhenIDoHttpRequestToRelativeURL() throws IOException
    {
        httpRequestSteps.setApiEndpoint(URL);
        httpRequestSteps.whenIDoHttpRequestToRelativeURL(HttpMethod.GET, RELATIVE_URL);
        verify(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, URL, Optional.of(RELATIVE_URL));
    }

    @Test
    void testSetCustomRequestConfig() throws ReflectiveOperationException
    {
        ExamplesTable configItems = new ExamplesTable("|redirectsEnabled|connectionRequestTimeout|cookieSpec|"
                + "\n|false|2|superCookie|");
        httpRequestSteps.setCustomRequestConfig(configItems);
        verify(httpTestContext).putRequestConfig(argThat(config -> !config.isRedirectsEnabled()));
    }

    @Test
    void testSetCustomRequestConfigNoField()
    {
        ExamplesTable configItems = new ExamplesTable("|nonExistentField|\n|any|");
        NoSuchFieldException exception = assertThrows(NoSuchFieldException.class,
            () -> httpRequestSteps.setCustomRequestConfig(configItems));
        assertEquals("nonExistentField", exception.getMessage());
    }

    private void verifyPutRequestEntity(String expectedContent)
    {
        verify(httpTestContext).putRequestEntity(argThat(entity ->
        {
            try
            {
                return entity != null
                        && expectedContent.equals(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));
            }
            catch (@SuppressWarnings("unused") IOException e)
            {
                return false;
            }
        }));
    }
}
