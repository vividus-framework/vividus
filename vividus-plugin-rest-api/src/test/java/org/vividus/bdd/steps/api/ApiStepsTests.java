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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.io.IOUtils;
import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.exception.HttpRequestBuildException;
import org.vividus.softassert.ISoftAssert;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
@SuppressWarnings("checkstyle:LineLength")
class ApiStepsTests
{
    private static final String URL = "http://www.example.com/";
    private static final String RELATIVE_URL = "/relativeUrl";
    private static final String RESOURCE_FULL_URL = "http://www.example.com/relativeUrl";
    private static final String CONTENT = "content";
    private static final String CR_LF = "\r\n";
    private static final long RESPONSE_TIME_IN_MS = 100;
    private static final String LOG_MESSAGE_FORMAT = "Response time: {} ms";

    @Mock
    private IHttpClient mockedHttpClient;

    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private ApiSteps apiSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ApiSteps.class);

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
            "'|name|type|value|\n|partName|STRING|content|',                                      text/plain; charset=ISO-8859-1",
            "'|name|type|value|contentType|\n|partName|STRING|content|text/html; charset=utf-8|', text/html; charset=utf-8"
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

    @Test
    void testThenTheResponseCodeShouldBeEqualTo() throws IOException
    {
        mockHttpResponse(HttpGet.class, URL);
        apiSteps.whenIDoHttpRequest(HttpMethod.GET, URL);
        assertThat(logger.getLoggingEvents(),
                equalTo(singletonList(info(LOG_MESSAGE_FORMAT, RESPONSE_TIME_IN_MS))));
    }

    @Test
    void shouldUseCookieStoreFromContext() throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseTimeInMs(RESPONSE_TIME_IN_MS);
        CookieStore cookieStore = mock(CookieStore.class);
        when(httpTestContext.getCookieStore()).thenReturn(Optional.of(cookieStore));
        when(mockedHttpClient.execute(argThat(e -> e instanceof HttpGet && URL.equals(e.getURI().toString())),
                argThat(context -> ((HttpClientContext) context).getCookieStore() == cookieStore)))
                        .thenReturn(httpResponse);
        apiSteps.whenIDoHttpRequest(HttpMethod.GET, URL);
        assertThat(logger.getLoggingEvents(), equalTo(singletonList(info(LOG_MESSAGE_FORMAT, RESPONSE_TIME_IN_MS))));
    }

    @Test
    void testWhenIIssuePostRequest() throws IOException
    {
        mockHttpResponse(HttpPost.class, URL);
        mockPullRequestEntity();
        apiSteps.whenIDoHttpRequest(HttpMethod.POST, URL);
        assertThat(logger.getLoggingEvents(),
                equalTo(singletonList(info(LOG_MESSAGE_FORMAT, RESPONSE_TIME_IN_MS))));
    }

    @Test
    void testWhenIIssuePostRequestWithoutContent() throws IOException
    {
        apiSteps.whenIDoHttpRequest(HttpMethod.POST, URL);
        verifyException(HttpRequestBuildException.class,
                "java.lang.IllegalStateException: HTTP POST request must include body");
    }

    @Test
    void testWhenIIssueGetRequest() throws IOException
    {
        mockHttpResponse(HttpGet.class, URL);
        apiSteps.whenIDoHttpRequest(HttpMethod.GET, URL);
        assertThat(logger.getLoggingEvents(),
                equalTo(singletonList(info(LOG_MESSAGE_FORMAT, RESPONSE_TIME_IN_MS))));
    }

    @Test
    void testWhenIIssueGetRequestWithContent() throws IOException
    {
        mockPullRequestEntity();
        apiSteps.whenIDoHttpRequest(HttpMethod.GET, URL);
        verifyException(HttpRequestBuildException.class,
                "java.lang.IllegalStateException: HTTP GET request can't include body");
    }

    @Test
    void testWhenIDoHttpRequestWithIllegalUrl() throws IOException
    {
        String url = "malformed.url";
        apiSteps.whenIDoHttpRequest(HttpMethod.GET, url);
        verifyException(HttpRequestBuildException.class,
                "java.lang.IllegalArgumentException: Scheme is missing in URL: " + url);
    }

    @Test
    void testWhenIDoHttpRequestConnectionClosedException() throws IOException
    {
        when(mockedHttpClient.execute(argThat(e -> e instanceof HttpGet && URL.equals(e.getURI().toString())),
                nullable(HttpContext.class))).thenThrow(new ConnectionClosedException());
        apiSteps.whenIDoHttpRequest(HttpMethod.GET, URL);
        verifyException(ConnectionClosedException.class, "Connection is closed");
    }

    private void verifyException(Class<? extends Exception> clazz, String message)
    {
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> clazz.isInstance(arg) && message.equals(((Exception) arg).getMessage())));
    }

    @Test
    void testWhenIDoHttpRequestToRelativeURL() throws IOException
    {
        apiSteps.setApiEndpoint(URL);
        mockHttpResponse(HttpGet.class, RESOURCE_FULL_URL);
        apiSteps.whenIDoHttpRequestToRelativeURL(HttpMethod.GET, RELATIVE_URL);
        assertThat(logger.getLoggingEvents(),
                equalTo(singletonList(info(LOG_MESSAGE_FORMAT, RESPONSE_TIME_IN_MS))));
    }

    @Test
    void testWhenIDoHttpRequestToRelativeURLWithContent() throws IOException
    {
        apiSteps.setApiEndpoint(URL);
        mockHttpResponse(HttpGet.class, RESOURCE_FULL_URL);
        apiSteps.whenIDoHttpRequestToRelativeURL(HttpMethod.GET, RELATIVE_URL, CONTENT);
        verifyPutRequestEntity(CONTENT);
        assertThat(logger.getLoggingEvents(),
                equalTo(singletonList(info(LOG_MESSAGE_FORMAT, RESPONSE_TIME_IN_MS))));
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

    @Test
    void shouldUseRequestConfigFromContext() throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseTimeInMs(RESPONSE_TIME_IN_MS);
        RequestConfig requestConfig = mock(RequestConfig.class);
        when(httpTestContext.getRequestConfig()).thenReturn(Optional.of(requestConfig));
        when(mockedHttpClient.execute(argThat(e -> e instanceof HttpGet && URL.equals(e.getURI().toString())),
                argThat(context -> ((HttpClientContext) context).getRequestConfig() == requestConfig)))
                        .thenReturn(httpResponse);
        apiSteps.whenIDoHttpRequest(HttpMethod.GET, URL);
        assertThat(logger.getLoggingEvents(), equalTo(singletonList(info(LOG_MESSAGE_FORMAT, RESPONSE_TIME_IN_MS))));
    }

    private void mockPullRequestEntity()
    {
        when(httpTestContext.pullRequestEntity())
                .thenReturn(Optional.of(new StringEntity(CONTENT, (ContentType) null)));
    }

    private void mockHttpResponse(Class<? extends HttpUriRequest> requestClass, String url) throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseTimeInMs(RESPONSE_TIME_IN_MS);
        when(mockedHttpClient.execute(argThat(e -> requestClass.isInstance(e) && url.equals(e.getURI().toString())),
                nullable(HttpContext.class))).thenReturn(httpResponse);
    }

    private void verifyPutRequestEntity(String expectedContent)
    {
        verify(httpTestContext).putRequestEntity(argThat(entity ->
        {
            try
            {
                return expectedContent.equals(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));
            }
            catch (@SuppressWarnings("unused") IOException e)
            {
                return false;
            }
        }));
    }
}
