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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.vividus.http.HttpMethod.GET;
import static org.vividus.http.HttpMethod.POST;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.junit.jupiter.api.Test;

class CurlUtilsTests
{
    private static final String ANY = "any";
    private static final Header[] EMPTY_HEADERS = {};
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Test
    void testRequestWithHeadersAndWithoutBody() throws URISyntaxException
    {
        BasicHttpRequest request = mock();
        when(request.getMethod()).thenReturn(GET.name());
        when(request.getUri()).thenReturn(new URI("http://get.example.org/"));
        Header[] requestHeaders =
        {
            new BasicHeader("Connection", "keep-alive"),
            new BasicHeader("Host", "example.org")
        };
        when(request.getHeaders()).thenReturn(requestHeaders);

        assertEquals(CurlUtils.buildCurlCommand(request, ANY, null, false),
                """
                        curl -X GET 'http://get.example.org/' \\
                        -H 'Connection: keep-alive' \\
                        -H 'Host: example.org'""");
    }

    @Test
    void testRequestWithHeadersAndWithBody() throws URISyntaxException
    {
        BasicHttpRequest request = mock();
        when(request.getMethod()).thenReturn(POST.name());
        when(request.getUri()).thenReturn(new URI("http://post.example.org/"));
        when(request.getHeaders()).thenReturn(EMPTY_HEADERS);
        String postBody = """
                {
                    "string": "<,',:,\\" &lt;, &#39;, &#58;, &#34;",
                    "number": 42,
                    "boolean": true
                }""";

        assertEquals(CurlUtils.buildCurlCommand(request, ANY, postBody.getBytes(CHARSET), false),
                """
                        curl -X POST 'http://post.example.org/' \\
                        -d '{
                            "string": "<,',:,\\" &lt;, &#39;, &#58;, &#34;",
                            "number": 42,
                            "boolean": true
                        }'""");
    }

    @Test
    void testRequestWithFormBody() throws URISyntaxException
    {
        BasicHttpRequest request = mock();
        when(request.getMethod()).thenReturn(POST.name());
        when(request.getUri()).thenReturn(new URI("http://post.form.data.example.org/"));
        when(request.getHeaders()).thenReturn(EMPTY_HEADERS);

        String formDataBody = "--Bbg5_2qfo5RoGjrGzlpR2MFzlAqzj2ie49bp7\r\n"
                + "Content-Disposition: form-data; name=\"file-key\"; filename=\"raw.txt\"\r\n"
                + "Content-Type: text/plain\r\n\r\n--Bbg5_2qfo5RoGjrGzlpR2MFzlAqzj2ie49bp7\r\n"
                + "Content-Disposition: form-data; name=\"string-key\"\r\nContent-Type: text/plain\r\n"
                + "\r\nstring1\r\n--Bbg5_2qfo5RoGjrGzlpR2MFzlAqzj2ie49bp7--\r\n";

        assertEquals(CurlUtils.buildCurlCommand(request, "multipart/form-data",
                        formDataBody.getBytes(CHARSET), false),
                """
                        curl -X POST 'http://post.form.data.example.org/' \\
                        -F 'file-key=@"<path-to-file>raw.txt"' \\
                        -F 'string-key="string1"'""");
    }

    @Test
    void testRequestWithBinaryContent() throws URISyntaxException
    {
        BasicHttpRequest request = mock();
        when(request.getMethod()).thenReturn(POST.name());
        when(request.getUri()).thenReturn(new URI("http://post.binary.example.org/"));
        when(request.getHeaders()).thenReturn(EMPTY_HEADERS);

        assertEquals(CurlUtils.buildCurlCommand(request, ANY, ANY.getBytes(CHARSET), true),
                """
                        curl -X POST 'http://post.binary.example.org/' \\
                        --data-binary '@<path_to_binary_content>'""");
    }
}
