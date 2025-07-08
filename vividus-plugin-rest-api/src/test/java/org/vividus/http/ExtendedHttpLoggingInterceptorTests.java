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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.http.client.HttpClient;
import org.vividus.http.client.HttpResponse;

@ExtendWith(TestLoggerFactoryExtension.class)
class ExtendedHttpLoggingInterceptorTests
{
    private static final String ENDPOINT = "https://uri.com/";
    private static final URI ENDPOINT_URI = URI.create(ENDPOINT);
    private static final String METHOD = "method";
    private static final String STRING = "string";
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_HEADER_VALUE = "Basic a2Vr";

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String IMAGE_PNG = "image/png";
    private static final String TEXT_PLAIN = "text/plain";

    private static final String REQUEST_HEADERS = String.format("Request: {}%nHeaders:%n{}");
    private static final String REQUEST_HEADERS_WITH_BODY = String.format(REQUEST_HEADERS + "%nBody:%n{}");
    private static final String RESPONSE_HEADERS = String.format("Response: status code {}, {}%nHeaders:%n{}");

    private static final ExtendedHttpLoggingInterceptor LOGGING_INTERCEPTOR = new ExtendedHttpLoggingInterceptor(false);
    private static final ExtendedHttpLoggingInterceptor LOGGING_INTERCEPTOR_EXT = new ExtendedHttpLoggingInterceptor(
            true);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HttpClient.class);

    @Test
    void shouldLogRequestInfoWithoutAdditionalInformationAndProtocolVersion() throws IOException
    {
        ClassicHttpRequest httpRequest = new BasicClassicHttpRequest(METHOD, ENDPOINT);
        LOGGING_INTERCEPTOR.process(httpRequest, null, null);
        assertThat(logger.getLoggingEvents(), is(List.of(info("Request: {}", httpRequest))));
    }

    @Test
    void shouldLogRequestInfoWithoutAdditionalInformation() throws IOException
    {
        ClassicHttpRequest httpRequest = new BasicClassicHttpRequest(METHOD, ENDPOINT);
        ProtocolVersion protocolVersion = new ProtocolVersion("TLS", 1, 1);
        httpRequest.setVersion(protocolVersion);
        LOGGING_INTERCEPTOR.process(httpRequest, null, null);
        assertThat(logger.getLoggingEvents(), is(List.of(info("Request: {} {}", protocolVersion, httpRequest))));
    }

    @Test
    void shouldLogRequestInfoWithAdditionalInformationNotHttpEntityContainer() throws IOException
    {
        Header authHeader = new BasicHeader(AUTHORIZATION, AUTH_HEADER_VALUE);
        BasicHttpRequest httpRequest = new BasicHttpRequest(METHOD, ENDPOINT);
        httpRequest.setHeader(authHeader);
        LOGGING_INTERCEPTOR_EXT.process(httpRequest, null, null);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(REQUEST_HEADERS, httpRequest, authHeader.toString()))));
    }

    @Test
    void shouldLogRequestInfoWithAdditionalInformationNoBody() throws IOException
    {
        Header authHeader = new BasicHeader(AUTHORIZATION, AUTH_HEADER_VALUE);
        ClassicHttpRequest httpRequest = new BasicClassicHttpRequest(METHOD, ENDPOINT);
        httpRequest.setHeader(authHeader);
        LOGGING_INTERCEPTOR_EXT.process(httpRequest, null, null);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(REQUEST_HEADERS, httpRequest, authHeader.toString()))));
    }

    @Test
    void shouldLogRequestInfoWithAdditionalInformationWithNotTextBody() throws IOException
    {
        Header contentHeader = new BasicHeader(CONTENT_TYPE, IMAGE_PNG);
        ClassicHttpRequest httpRequest = new BasicClassicHttpRequest(METHOD, ENDPOINT);
        httpRequest.setHeader(contentHeader);
        HttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(new byte[] { 1, 2 }), ContentType.IMAGE_PNG);
        httpRequest.setEntity(entity);
        LOGGING_INTERCEPTOR_EXT.process(httpRequest, null, null);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(REQUEST_HEADERS + System.lineSeparator() + "Body: {} bytes of binary data",
                        httpRequest, contentHeader.toString(), 2))));
    }

    @Test
    void shouldLogRequestInfoWithAdditionalInformationWithTextBody() throws IOException
    {
        Header contentHeader = new BasicHeader(CONTENT_TYPE, TEXT_PLAIN);
        ClassicHttpRequest httpRequest = new BasicClassicHttpRequest(METHOD, ENDPOINT);
        httpRequest.setHeader(contentHeader);
        HttpEntity entity = new StringEntity(STRING);
        httpRequest.setEntity(entity);
        LOGGING_INTERCEPTOR_EXT.process(httpRequest, null, null);
        assertThat(logger.getLoggingEvents(), is(List.of(info(REQUEST_HEADERS_WITH_BODY, httpRequest,
                contentHeader.toString(), new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)))));
    }

    @Test
    void shouldLogRequestInfoWithAdditionalInformationWithTextBodyAndIoException() throws IOException
    {
        String ioExceptionMessage = "exception message";
        Header contentHeader = new BasicHeader(CONTENT_TYPE, TEXT_PLAIN);
        ClassicHttpRequest httpRequest = new BasicClassicHttpRequest(METHOD, ENDPOINT);
        httpRequest.setHeader(contentHeader);
        var entity = mock(StringEntity.class);
        var inputStream = mock(ByteArrayInputStream.class);
        when(entity.getContent()).thenReturn(inputStream);
        var ioException = new IOException(ioExceptionMessage);
        when(inputStream.readAllBytes()).thenThrow(ioException);
        httpRequest.setEntity(entity);
        LOGGING_INTERCEPTOR_EXT.process(httpRequest, null, null);
        assertThat(logger.getLoggingEvents(), is(List.of(info(REQUEST_HEADERS_WITH_BODY, httpRequest,
                contentHeader.toString(), "Unable to get body content: " + ioExceptionMessage))));
    }

    @Test
    void shouldLogResponseInfoWithoutAdditionalInformation()
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setFrom(ENDPOINT_URI);
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        LOGGING_INTERCEPTOR.handle(httpResponse);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Response: status code {}, {}", HttpStatus.SC_OK, ENDPOINT_URI))));
    }

    @Test
    void shouldLogResponseInfoWithAdditionalInformationNoBody()
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setFrom(ENDPOINT_URI);
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        Header authHeader = new BasicHeader(AUTHORIZATION, AUTH_HEADER_VALUE);
        httpResponse.setResponseHeaders(authHeader);
        LOGGING_INTERCEPTOR_EXT.handle(httpResponse);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(RESPONSE_HEADERS, HttpStatus.SC_OK, ENDPOINT_URI, authHeader.toString()))));
    }

    @Test
    void shouldLogResponseInfoWithAdditionalInformationWithNotTextBody()
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setFrom(ENDPOINT_URI);
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        Header contentHeader = new BasicHeader(CONTENT_TYPE, IMAGE_PNG);
        httpResponse.setResponseHeaders(contentHeader);
        httpResponse.setResponseBody(new byte[] { 1 });
        LOGGING_INTERCEPTOR_EXT.handle(httpResponse);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(RESPONSE_HEADERS, HttpStatus.SC_OK, ENDPOINT_URI, contentHeader.toString()))));
    }

    @ParameterizedTest
    @CsvSource({
            TEXT_PLAIN, "text/html", "text/xml", "application/json", "application/xml"
    })
    void shouldLogResponseInfoWithAdditionalInformationWithBody(String contentType)
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setFrom(ENDPOINT_URI);
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        Header contentHeader = new BasicHeader(CONTENT_TYPE, contentType);
        httpResponse.setResponseHeaders(contentHeader);
        httpResponse.setResponseBody(STRING.getBytes(StandardCharsets.UTF_8));
        LOGGING_INTERCEPTOR_EXT.handle(httpResponse);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(String.format("Response: status code {}, {}%nHeaders:%n{}%nBody:%n{}"),
                        HttpStatus.SC_OK, ENDPOINT_URI, contentHeader.toString(), STRING))));
    }
}
