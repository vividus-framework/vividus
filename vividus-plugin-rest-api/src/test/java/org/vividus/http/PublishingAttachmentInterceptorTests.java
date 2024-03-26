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

package org.vividus.http;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.reporter.event.IAttachmentPublisher;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class PublishingAttachmentInterceptorTests
{
    private static final String ENDPOINT = "https://uri.com/";
    private static final String METHOD = "method";
    private static final String API_MESSAGE_FTL = "/org/vividus/http/attachment/api-message.ftl";
    private static final String RESPONSE = "Response: method " + ENDPOINT;
    private static final String REQUEST = "Request: method " + ENDPOINT;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_PLAIN = "text/plain";
    private static final byte[] DATA = "data".getBytes(StandardCharsets.UTF_8);
    private static final String SPACE = " ";

    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private PublishingAttachmentInterceptor interceptor;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(PublishingAttachmentInterceptor.class);

    @Test
    void testHttpRequestIsAttachedSuccessfully() throws IOException
    {
        Header entityContentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN);
        var httpEntity = mock(HttpEntity.class);
        var httpRequest = createClassicHttpRequest(new Header[] {entityContentTypeHeader}, httpEntity);
        testHttpRequestIsAttachedSuccessfully(httpRequest);
        verify(httpEntity).getContentLength();
        verify(httpEntity).writeTo(any(ByteArrayOutputStream.class));
    }

    @Test
    void testHttpRequestWithNullBodyIsAttachedSuccessfully()
    {
        var httpRequest = createClassicHttpRequest(new Header[] {}, null);
        testHttpRequestIsAttachedSuccessfully(httpRequest);
    }

    @ParameterizedTest
    @ValueSource(strings = {CONTENT_TYPE, "content-type"})
    void testHttpRequestIsAttachedSuccessfullyWhenContentTypeIsSet(String contentTypeHeaderName) throws IOException
    {
        Header contentTypeHeader = new BasicHeader(contentTypeHeaderName, TEXT_PLAIN);
        var httpEntity = mock(ByteArrayEntity.class);
        testHttpRequestIsAttachedSuccessfully(contentTypeHeader, httpEntity);
        verify(httpEntity).getContentLength();
        verify(httpEntity).writeTo(any(ByteArrayOutputStream.class));
    }

    @Test
    void testHttpRequestIsAttachedSuccessfullyWhenContentTypeIsUnset() throws IOException
    {
        testHttpRequestIsAttachedSuccessfully(mock(Header.class));
    }

    @Test
    void testHttpRequestIsAttachedSuccessfullyWhenContentTypeIsEmpty() throws IOException
    {
        Header contentTypeHeader = mock();
        when(contentTypeHeader.getName()).thenReturn(CONTENT_TYPE);
        testHttpRequestIsAttachedSuccessfully(contentTypeHeader);
    }

    @Test
    void testHttpRequestBodyIsAttachedSuccessfullyWithContentTypeIsUnknown() throws IOException
    {
        testHttpRequestIsAttachedSuccessfully();
    }

    @Test
    void testNoHttpRequestBodyIsAttached()
    {
        HttpRequest httpRequest = mock();
        when(httpRequest.getHeaders()).thenReturn(new Header[] {});
        when(httpRequest.toString()).thenReturn(METHOD + SPACE + ENDPOINT);
        testNoHttpRequestBodyIsAttached(httpRequest, empty());
    }

    @Test
    void testHttpRequestBodyAttachingIsFailed() throws IOException
    {
        var httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContentType()).thenReturn(null);
        var ioException = new IOException();
        doThrow(ioException).when(httpEntity).writeTo(any(ByteArrayOutputStream.class));
        var httpRequest = createClassicHttpRequest(new Header[] { mock(Header.class) }, httpEntity);
        testNoHttpRequestBodyIsAttached(httpRequest,
                equalTo(List.of(error(ioException, "Error is occurred at HTTP message parsing"))));
    }

    @Test
    void testHttpResponseIsAttachedSuccessfully() throws IOException
    {
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.getResponseBody()).thenReturn(DATA);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getMethod()).thenReturn(METHOD);
        when(httpResponse.getFrom()).thenReturn(URI.create(ENDPOINT));
        when(httpResponse.getResponseHeaders()).thenReturn(new Header[] { mock(Header.class) });
        interceptor.handle(httpResponse);
        var argumentCaptor = verifyPublishAttachment(RESPONSE);
        assertEquals(HttpStatus.SC_OK, argumentCaptor.getValue().get("statusCode").intValue());
    }

    @Test
    void testNoHttpResponseBodyIsAttached() throws IOException
    {
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.getResponseBody()).thenReturn(null);
        when(httpResponse.getMethod()).thenReturn(METHOD);
        when(httpResponse.getFrom()).thenReturn(URI.create(ENDPOINT));
        when(httpResponse.getResponseHeaders()).thenReturn(new Header[0]);
        interceptor.handle(httpResponse);
        verifyPublishAttachment(RESPONSE);
    }

    @Test
    void testCurlBuildIsFailed() throws URISyntaxException
    {
        HttpRequest httpRequest = mock();
        when(httpRequest.getHeaders()).thenReturn(new Header[] {});
        when(httpRequest.toString()).thenReturn(METHOD + SPACE + ENDPOINT);
        String empty = "";
        var exception = new URISyntaxException(empty, empty);
        when(httpRequest.getUri()).thenThrow(exception);
        testNoHttpRequestBodyIsAttached(httpRequest,
                equalTo(List.of(error(exception, "Error is occurred on building cURL command"))));
    }

    private void testHttpRequestIsAttachedSuccessfully(Header contentTypeHeader, HttpEntity httpEntity)
    {
        var httpRequest = createClassicHttpRequest(new Header[] { contentTypeHeader }, httpEntity);
        testHttpRequestIsAttachedSuccessfully(httpRequest);
    }

    private void testHttpRequestIsAttachedSuccessfully(Header... allRequestHeaders) throws IOException
    {
        var httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContentType()).thenReturn(null);
        var httpRequest = createClassicHttpRequest(allRequestHeaders, httpEntity);
        testHttpRequestIsAttachedSuccessfully(httpRequest);
        verify(httpEntity).getContentLength();
        verify(httpEntity).writeTo(any(ByteArrayOutputStream.class));
    }

    private void testHttpRequestIsAttachedSuccessfully(ClassicHttpRequest httpRequest)
    {
        var httpContext = mock(HttpContext.class);
        interceptor.process(httpRequest, null, httpContext);
        verifyPublishAttachment(REQUEST);
        verifyNoInteractions(httpContext);
        assertThat(logger.getLoggingEvents(), empty());
    }

    private ClassicHttpRequest createClassicHttpRequest(Header[] requestHeaders, HttpEntity httpEntity)
    {
        ClassicHttpRequest httpRequest = new BasicClassicHttpRequest(METHOD, ENDPOINT);
        httpRequest.setHeaders(requestHeaders);
        httpRequest.setEntity(httpEntity);
        return httpRequest;
    }

    private void testNoHttpRequestBodyIsAttached(HttpRequest httpRequest,
            Matcher<Collection<? extends LoggingEvent>> loggingEventsMatcher)
    {
        var httpContext = mock(HttpContext.class);
        verifyNoInteractions(httpContext);
        interceptor.process(httpRequest, null, httpContext);
        verifyPublishAttachment(REQUEST);
        verifyNoMoreInteractions(attachmentPublisher);
        assertThat(logger.getLoggingEvents(), loggingEventsMatcher);
    }

    private ArgumentCaptor<Map<String, Integer>> verifyPublishAttachment(String title)
    {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Integer>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(attachmentPublisher).publishAttachment(eq(API_MESSAGE_FTL), argumentCaptor.capture(), eq(title));
        return argumentCaptor;
    }
}
