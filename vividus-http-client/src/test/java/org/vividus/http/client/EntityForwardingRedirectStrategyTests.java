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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityForwardingRedirectStrategyTests
{
    private static final int STATUS_CODE = 301;
    private static final String URI = "http://example.com";
    private static final String REDIRECT_URI = "http://example.com/en";
    private static final String LOCATION = "location";

    private final HttpContext httpContext = new HttpClientContext();

    private final EntityForwardingRedirectStrategy redirectStrategy = new EntityForwardingRedirectStrategy();

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private BasicStatusLine statusLine;

    @Test
    void testGetRedirect() throws ProtocolException
    {
        mockHeader();
        when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        HttpEntityEnclosingRequest httpRequest = new HttpPost(URI);
        StringEntity requestStringEntity = new StringEntity("{key:value}", StandardCharsets.UTF_8);
        httpRequest.setEntity(requestStringEntity);
        HttpEntityEnclosingRequest actualRedirect = (HttpEntityEnclosingRequest) redirectStrategy
                .getRedirect(httpRequest, httpResponse, httpContext);
        assertEquals(REDIRECT_URI, actualRedirect.getRequestLine().getUri());
        assertEquals(requestStringEntity, actualRedirect.getEntity());
    }

    @Test
    void testGetRedirectHeadRequest() throws ProtocolException
    {
        mockHeader();
        HttpHead httpRequest = new HttpHead(URI);
        HttpUriRequest actualRedirect = redirectStrategy.getRedirect(httpRequest, httpResponse, httpContext);
        assertEquals(REDIRECT_URI, actualRedirect.getURI().toString());
    }

    private void mockHeader()
    {
        Header header = new BasicHeader(LOCATION, REDIRECT_URI);
        when(httpResponse.getFirstHeader(LOCATION)).thenReturn(header);
    }
}
