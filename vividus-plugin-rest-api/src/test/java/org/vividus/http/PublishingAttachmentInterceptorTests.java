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
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
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
    private static final String ENDPOINT = "uri";
    private static final String METHOD = "method";
    private static final String API_MESSAGE_FTL = "/org/vividus/http/attachment/api-message.ftl";
    private static final String RESPONSE = "Response: method uri";
    private static final String REQUEST = "Request: method uri";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_PLAIN = "text/plain";
    private static final byte[] DATA = "data".getBytes(StandardCharsets.UTF_8);

    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private PublishingAttachmentInterceptor interceptor;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(PublishingAttachmentInterceptor.class);

    @Test
    void testHttpRequestIsAttachedSuccessfully() throws IOException
    {
        Header entityContentTypeHeader = mockContentTypeHeader();
        when(entityContentTypeHeader.getElements()[0].getParameters()).thenReturn(new NameValuePair[0]).getMock();
        testHttpRequestIsAttachedSuccessfully(new Header[] {entityContentTypeHeader}, entityContentTypeHeader);
    }

    @Test
    void testHttpRequestWithNullBodyIsAttachedSuccessfully()
    {
        HttpEntityEnclosingRequest httpRequest = mockHttpEntityEnclosingRequest(new Header[] {}, null);
        testHttpRequestIsAttachedSuccessfully(httpRequest);
    }

    @ParameterizedTest
    @ValueSource(strings = {CONTENT_TYPE, "content-type"})
    void testHttpRequestIsAttachedSuccessfullyWhenContentTypeIsSet(String contentTypeHeaderName) throws IOException
    {
        Header contentTypeHeader = mockContentTypeHeader();
        when(contentTypeHeader.getName()).thenReturn(contentTypeHeaderName);
        when(contentTypeHeader.getValue()).thenReturn(TEXT_PLAIN);
        var httpEntity = mock(HttpEntity.class);
        testHttpRequestIsAttachedSuccessfully(contentTypeHeader, httpEntity);
        verify(httpEntity).getContentLength();
        verify(httpEntity).writeTo(any(ByteArrayOutputStream.class));
    }

    @Test
    void testHttpRequestIsAttachedSuccessfullyWhenContentTypeIsUnset() throws IOException
    {
        testHttpRequestIsAttachedSuccessfully(new Header[] { mock(Header.class) }, null);
    }

    @Test
    void testHttpRequestIsAttachedSuccessfullyWhenContentTypeIsEmpty() throws IOException
    {
        Header contentTypeHeader = mock(Header.class);
        when(contentTypeHeader.getName()).thenReturn(CONTENT_TYPE);
        testHttpRequestIsAttachedSuccessfully(new Header[] { contentTypeHeader }, null);
    }

    @Test
    void testHttpRequestBodyIsAttachedSuccessfullyWithContentTypeIsUnknown() throws IOException
    {
        testHttpRequestIsAttachedSuccessfully(new Header[] {}, null);
    }

    @Test
    void testNoHttpRequestBodyIsAttached()
    {
        HttpEntityEnclosingRequest httpRequest = mockHttpEntityEnclosingRequest(new Header[] {},
                mock(HttpEntity.class));
        testNoHttpRequestBodyIsAttached(httpRequest, empty());
    }

    @Test
    void testHttpRequestBodyAttachingIsFailed() throws IOException
    {
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContentType()).thenReturn(null);
        IOException ioException = new IOException();
        doThrow(ioException).when(httpEntity).writeTo(any(ByteArrayOutputStream.class));
        HttpEntityEnclosingRequest httpRequest = mockHttpEntityEnclosingRequest(new Header[] { mock(Header.class) },
                httpEntity);
        testNoHttpRequestBodyIsAttached(httpRequest,
                equalTo(List.of(error(ioException, "Error is occurred at HTTP message parsing"))));
    }

    @Test
    void testHttpResponseIsAttachedSuccessfully() throws IOException
    {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getResponseBody()).thenReturn(DATA);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getMethod()).thenReturn(METHOD);
        when(httpResponse.getFrom()).thenReturn(URI.create(ENDPOINT));
        when(httpResponse.getResponseHeaders()).thenReturn(new Header[] { mock(Header.class) });
        interceptor.handle(httpResponse);
        ArgumentCaptor<Map<String, Integer>> argumentCaptor = verifyPublishAttachment(RESPONSE);
        assertEquals(HttpStatus.SC_OK, argumentCaptor.getValue().get("statusCode").intValue());
    }

    @Test
    void testNoHttpResponseBodyIsAttached() throws IOException
    {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getResponseBody()).thenReturn(null);
        when(httpResponse.getMethod()).thenReturn(METHOD);
        when(httpResponse.getFrom()).thenReturn(URI.create(ENDPOINT));
        when(httpResponse.getResponseHeaders()).thenReturn(new Header[0]);
        interceptor.handle(httpResponse);
        verifyPublishAttachment(RESPONSE);
    }

    private Header mockContentTypeHeader()
    {
        HeaderElement headerElement = mock(HeaderElement.class);
        when(headerElement.getName()).thenReturn(TEXT_PLAIN);
        return when(mock(Header.class).getElements()).thenReturn(new HeaderElement[] { headerElement }).getMock();
    }

    private void testHttpRequestIsAttachedSuccessfully(Header contentTypeHeader, HttpEntity httpEntity)
    {
        HttpEntityEnclosingRequest httpRequest = mockHttpEntityEnclosingRequest(new Header[] { contentTypeHeader },
                httpEntity);
        testHttpRequestIsAttachedSuccessfully(httpRequest);
    }

    private void testHttpRequestIsAttachedSuccessfully(Header[] allRequestHeaders, Header entityContentTypeHeader)
            throws IOException
    {
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContentType()).thenReturn(entityContentTypeHeader);
        HttpEntityEnclosingRequest httpRequest = mockHttpEntityEnclosingRequest(allRequestHeaders, httpEntity);
        testHttpRequestIsAttachedSuccessfully(httpRequest);
        verify(httpEntity).getContentLength();
        verify(httpEntity).writeTo(any(ByteArrayOutputStream.class));
    }

    private void testHttpRequestIsAttachedSuccessfully(HttpEntityEnclosingRequest httpRequest)
    {
        HttpContext httpContext = mock(HttpContext.class);
        interceptor.process(httpRequest, httpContext);
        verifyPublishAttachment(REQUEST);
        verifyNoInteractions(httpContext);
        assertThat(logger.getLoggingEvents(), empty());
    }

    private HttpEntityEnclosingRequest mockHttpEntityEnclosingRequest(Header[] allRequestHeaders, HttpEntity httpEntity)
    {
        HttpEntityEnclosingRequest httpRequest = mock(HttpEntityEnclosingRequest.class);
        RequestLine requestLine = mock(RequestLine.class);
        when(httpRequest.getAllHeaders()).thenReturn(allRequestHeaders);
        when(httpRequest.getEntity()).thenReturn(httpEntity);
        when(httpRequest.getRequestLine()).thenReturn(requestLine);
        when(requestLine.getMethod()).thenReturn(METHOD);
        when(requestLine.getUri()).thenReturn(ENDPOINT);
        return httpRequest;
    }

    private void testNoHttpRequestBodyIsAttached(HttpRequest httpRequest,
            Matcher<Collection<? extends LoggingEvent>> loggingEventsMatcher)
    {
        HttpContext httpContext = mock(HttpContext.class);
        verifyNoInteractions(httpContext);
        interceptor.process(httpRequest, httpContext);
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
