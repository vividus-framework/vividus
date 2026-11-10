/*
 * Copyright 2019-2025 the original author or authors.
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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.Header;

public class HttpResponse
{
    private URI from;
    private String method;
    private int statusCode;
    private long responseTimeInMs;
    private byte[] responseBody;
    private Header[] responseHeaders;
    private RedirectLocations redirectLocations;

    public Optional<Header> getHeaderByName(String headerName)
    {
        return getHeadersByName(headerName).findFirst();
    }

    public Stream<Header> getHeadersByName(String headerName)
    {
        return Stream.of(responseHeaders).filter(header -> header.getName().equalsIgnoreCase(headerName));
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public long getResponseTimeInMs()
    {
        return responseTimeInMs;
    }

    public void setResponseTimeInMs(long responseTimeInMs)
    {
        this.responseTimeInMs = responseTimeInMs;
    }

    public byte[] getResponseBody()
    {
        return ArrayUtils.clone(responseBody);
    }

    public void setResponseBody(byte[] responseBody)
    {
        this.responseBody = ArrayUtils.clone(responseBody);
    }

    public String getResponseBodyAsString()
    {
        return responseBody != null ? new String(responseBody, StandardCharsets.UTF_8) : null;
    }

    public Header[] getResponseHeaders()
    {
        return ArrayUtils.clone(responseHeaders);
    }

    public void setResponseHeaders(Header... responseHeaders)
    {
        this.responseHeaders = ArrayUtils.clone(responseHeaders);
    }

    public URI getFrom()
    {
        return from;
    }

    public void setFrom(URI from)
    {
        this.from = from;
    }

    public void setRedirectLocations(RedirectLocations redirectLocations)
    {
        this.redirectLocations = redirectLocations;
    }

    public RedirectLocations getRedirectLocations()
    {
        return redirectLocations;
    }

    @Override
    public String toString()
    {
        return statusCode + " : " + getResponseBodyAsString();
    }
}
