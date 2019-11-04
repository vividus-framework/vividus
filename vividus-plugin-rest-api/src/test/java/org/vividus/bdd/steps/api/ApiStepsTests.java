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

package org.vividus.bdd.steps.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class ApiStepsTests
{
    private static final String URL = "http://www.example.com/";
    private static final String RELATIVE_URL = "/relativeUrl";
    private static final String CONTENT = "content";
    private static final String CR_LF = "\r\n";

    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private HttpRequestExecutor httpRequestExecutor;

    @InjectMocks
    private ApiSteps apiSteps;

    @Test
    void testRequest()
    {
        apiSteps.request(CONTENT);
        verifyPutRequestEntity(CONTENT);
    }

    @Test
    void testRequestUtf8Content()
    {
        String content = "{\"storeUserNewPassword\": \"зЛЖЛВеБЛги!\"}";
        apiSteps.request(content);
        verifyPutRequestEntity(content);
    }

    @ParameterizedTest
    @CsvSource({
            // CHECKSTYLE:OFF
            "'|name|type|value|\n|partName|STRING|content|',                                      text/plain; charset=ISO-8859-1",
            "'|name|type|value|contentType|\n|partName|STRING|content|text/html; charset=utf-8|', text/html; charset=utf-8"
            // CHECKSTYLE:ON
    })
    void testPutMultipartRequest(String tableAsString, String contentType)
    {
        ExamplesTable requestParts = new ExamplesTable(tableAsString);
        apiSteps.putMultipartRequest(requestParts);
        verify(httpTestContext).putRequestEntity(argThat(entity ->
        {
            try
            {
                return IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8)
                        .contains("Content-Disposition: form-data; name=\"partName\"" + CR_LF
                                + "Content-Type: " + contentType + CR_LF
                                + "Content-Transfer-Encoding: 8bit" + CR_LF + CR_LF + CONTENT + CR_LF);
            }
            catch (@SuppressWarnings("unused") IOException e)
            {
                return false;
            }
        }));
    }

    @Test
    void testSetUpRequestHeaders()
    {
        ExamplesTable headers = new ExamplesTable("|name|value|\n|name1|value1|");
        apiSteps.setUpRequestHeaders(headers);
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

    @ParameterizedTest
    @EnumSource(HttpMethod.class)
    void testWhenIIssueHttpRequest(HttpMethod method) throws IOException
    {
        apiSteps.whenIDoHttpRequest(method, URL);
        verify(httpRequestExecutor).executeHttpRequest(method, URL, Optional.empty());
    }

    @Test
    void testWhenIDoHttpRequestToRelativeURL() throws IOException
    {
        apiSteps.setApiEndpoint(URL);
        apiSteps.whenIDoHttpRequestToRelativeURL(HttpMethod.GET, RELATIVE_URL);
        verify(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, URL, Optional.of(RELATIVE_URL));
    }

    @Test
    void testWhenIDoHttpRequestToRelativeURLWithContent() throws IOException
    {
        apiSteps.setApiEndpoint(URL);
        apiSteps.whenIDoHttpRequestToRelativeURL(HttpMethod.GET, RELATIVE_URL, CONTENT);
        verify(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, URL, Optional.of(RELATIVE_URL));
        verifyPutRequestEntity(CONTENT);
    }

    @Test
    void testSetCustomRequestConfig() throws ReflectiveOperationException
    {
        ExamplesTable configItems = new ExamplesTable("|redirectsEnabled|connectionRequestTimeout|cookieSpec|"
                + "\n|false|2|superCookie|");
        apiSteps.setCustomRequestConfig(configItems);
        verify(httpTestContext).putRequestConfig(argThat(config -> !config.isRedirectsEnabled()));
    }

    @Test
    void testSetCustomRequestConfigNoField()
    {
        ExamplesTable configItems = new ExamplesTable("|nonExistentField|\n|any|");
        NoSuchFieldException exception = assertThrows(NoSuchFieldException.class,
            () -> apiSteps.setCustomRequestConfig(configItems));
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
