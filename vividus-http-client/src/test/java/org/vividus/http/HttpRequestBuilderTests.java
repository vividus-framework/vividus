/*
 * Copyright 2019-2023 the original author or authors.
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.vividus.http.exception.HttpRequestBuildException;

class HttpRequestBuilderTests
{
    private static final String ENDPOINT = "https://www.example.com/endpoint";
    private static final String CONTENT = "content";

    private final HttpRequestBuilder builder = HttpRequestBuilder.create();

    @Test
    void shouldValidateEndpoint()
    {
        var builder = HttpRequestBuilder.create().withHttpMethod(HttpMethod.GET);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("Endpoint must be not null", exception.getMessage());
    }

    @Test
    void shouldValidateHttpMethod()
    {
        var builder = HttpRequestBuilder.create().withEndpoint(ENDPOINT);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("HTTP method must be not null", exception.getMessage());
    }

    @Test
    void buildGetWithoutContent() throws HttpRequestBuildException, URISyntaxException
    {
        var request = builder.withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT).build();
        assertRequest(request, HttpMethod.GET.name(), ENDPOINT);
    }

    @Test
    void buildPostWithContent() throws IOException, ParseException, URISyntaxException
    {
        var request = builder.withHttpMethod(HttpMethod.POST).withEndpoint(ENDPOINT).withContent(CONTENT).build();
        assertRequestWithContent(request, HttpMethod.POST.name());
    }

    @Test
    void buildPostWithContentAndContentType() throws IOException, ParseException, URISyntaxException
    {
        var contentType = ContentType.APPLICATION_FORM_URLENCODED;
        var request = builder.withHttpMethod(HttpMethod.POST).withEndpoint(ENDPOINT).withContent(CONTENT, contentType)
                .build();
        assertRequestWithContent(request, HttpMethod.POST.name());
        var expectedContentType = contentType.getMimeType() + "; charset=" + contentType.getCharset();
        assertEquals(expectedContentType, request.getEntity().getContentType());
    }

    @Test
    void buildGetWithContent()
    {
        var builder = HttpRequestBuilder.create().withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT).withContent(
                CONTENT);
        assertThrows(HttpRequestBuildException.class, builder::build);
    }

    @Test
    void buildPatchWithoutContent()
    {
        var builder = HttpRequestBuilder.create().withHttpMethod(HttpMethod.PATCH).withEndpoint(ENDPOINT);
        assertThrows(HttpRequestBuildException.class, builder::build);
    }

    @Test
    void buildGetWithMalformedUrl()
    {
        var builder = HttpRequestBuilder.create().withHttpMethod(HttpMethod.GET).withEndpoint("malformed.url");
        assertThrows(HttpRequestBuildException.class, builder::build);
    }

    @Test
    void buildGetToRelativeUrl() throws HttpRequestBuildException, URISyntaxException
    {
        var relativeUrl = "/relativeUrl";
        var request = builder.withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT).withRelativeUrl(relativeUrl)
                .build();
        assertRequest(request, HttpMethod.GET.name(), ENDPOINT + relativeUrl);
    }

    @Test
    void buildGetWithHeaders() throws HttpRequestBuildException, URISyntaxException
    {
        var header = mock(Header.class);
        var request = builder.withHttpMethod(HttpMethod.GET).withEndpoint(ENDPOINT).withHeaders(List.of(header))
                .build();
        assertEquals(header, request.getHeaders()[0]);
        assertRequest(request, HttpMethod.GET.name(), ENDPOINT);
    }

    private void assertRequestWithContent(ClassicHttpRequest request, String method)
            throws IOException, ParseException, URISyntaxException
    {
        assertEquals(CONTENT, EntityUtils.toString(request.getEntity(), StandardCharsets.UTF_8));
        assertRequest(request, method, ENDPOINT);
    }

    private void assertRequest(ClassicHttpRequest request, String method, String url) throws URISyntaxException
    {
        assertEquals(method, request.getMethod());
        assertEquals(URI.create(url), request.getUri());
    }
}
