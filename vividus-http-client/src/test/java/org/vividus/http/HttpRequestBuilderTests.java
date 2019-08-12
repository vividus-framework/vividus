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

package org.vividus.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.vividus.http.exception.HttpRequestBuildException;

class HttpRequestBuilderTests
{
    private static final String ENDPOINT = "http://www.example.com/endpoint";
    private static final String CONTENT = "content";

    private final HttpRequestBuilder builder = HttpRequestBuilder.create();

    @Test
    void buildGetWithoutContent()
    {
        HttpRequestBase request = builder.withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT).build();
        assertRequest(request, HttpMethod.GET.name(), ENDPOINT);
    }

    @Test
    void buildPostWithContent() throws IOException
    {
        HttpRequestBase request = builder.withHttpMethod(HttpMethod.POST).withEndpoint(ENDPOINT).withContent(CONTENT)
                .build();
        assertRequestWithContent(request, HttpMethod.POST.name(), ENDPOINT, CONTENT);
    }

    @Test
    void buildPostWithContentAndContentType() throws IOException
    {
        ContentType contentType = ContentType.APPLICATION_FORM_URLENCODED;
        HttpRequestBase request = builder.withHttpMethod(HttpMethod.POST).withEndpoint(ENDPOINT)
                .withContent(CONTENT, contentType).build();
        assertRequestWithContent(request, HttpMethod.POST.name(), ENDPOINT, CONTENT);
        String expectedContentType = contentType.getMimeType() + "; charset=" + contentType.getCharset();
        assertEquals(expectedContentType,
                ((HttpEntityEnclosingRequestBase) request).getEntity().getContentType().getValue());
    }

    @Test
    void buildGetWithContent()
    {
        assertThrows(HttpRequestBuildException.class,
            () -> builder.withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT).withContent(CONTENT).build());
    }

    @Test
    void buildPostWithoutContent()
    {
        assertThrows(HttpRequestBuildException.class,
            () -> builder.withHttpMethod(HttpMethod.POST).withEndpoint(ENDPOINT).build());
    }

    @Test
    void buildGetToRelativeUrl()
    {
        String relativeUrl = "/relativeUrl";
        HttpRequestBase request = builder.withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT)
                .withRelativeUrl(relativeUrl).build();
        assertRequest(request, HttpMethod.GET.name(), "http://www.example.com/endpoint/relativeUrl");
    }

    @Test
    void buildGetWithMalformedUrl()
    {
        assertThrows(HttpRequestBuildException.class,
            () -> builder.withHttpMethod(HttpMethod.GET).withEndpoint("malformed.url").build());
    }

    @Test
    void buildGetWithHeaders()
    {
        Header header = mock(Header.class);
        HttpRequestBase request = builder.withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT).withHeaders(
                List.of(header)).build();
        assertEquals(header, request.getAllHeaders()[0]);
        assertRequest(request, HttpMethod.GET.name(), ENDPOINT);
    }

    private void assertRequestWithContent(HttpRequestBase request, String method, String url, String content)
            throws IOException
    {
        assertEquals(content, IOUtils.toString(((HttpEntityEnclosingRequestBase) request).getEntity().getContent(),
                Charset.defaultCharset()));
        assertRequest(request, method, url);
    }

    private void assertRequest(HttpRequestBase request, String method, String url)
    {
        assertEquals(method, request.getMethod());
        assertEquals(url, request.getURI().toString());
    }
}
