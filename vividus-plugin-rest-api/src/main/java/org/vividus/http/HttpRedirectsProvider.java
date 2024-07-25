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

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.hc.client5.http.CircularRedirectException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.HttpStatus;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

public class HttpRedirectsProvider
{
    private static final int STATUS_CODE_LEFT_BOUNDARY = HttpStatus.SC_OK;
    private static final int STATUS_CODE_RIGHT_BOUNDARY = HttpStatus.SC_MULTI_STATUS;

    private IHttpClient httpClient;

    /**
     * Executes HEAD request to get redirects.
     * Throws HttpResponseException in case of status code outside "200-207" range
     *
     * @param from URI to issue HEAD request
     * @return List of redirects. Empty list if there are no redirects.
     */
    public List<URI> getRedirects(URI from) throws IOException
    {
        HttpResponse httpResponse;
        try
        {
            httpResponse = httpClient.doHttpHead(from);
        }
        catch (IOException e)
        {
            String exceptionMsg;
            if (e.getCause() instanceof CircularRedirectException)
            {
                exceptionMsg = e.getCause().getMessage() + " Circular redirects are forbidden by default. "
                        + "To allow them, please set property "
                        + "'http.redirects-provider.circular-redirects-allowed=true'";
            }
            else
            {
                exceptionMsg = e.getMessage();
            }
            throw new IOException(exceptionMsg, e);
        }

        int statusCode = httpResponse.getStatusCode();
        if (statusCode < STATUS_CODE_LEFT_BOUNDARY || statusCode > STATUS_CODE_RIGHT_BOUNDARY)
        {
            String message = "HTTP response status code is expected to be in range [%d - %d], but was %d".formatted(
                    STATUS_CODE_LEFT_BOUNDARY, STATUS_CODE_RIGHT_BOUNDARY, statusCode);
            throw new HttpResponseException(statusCode, message);
        }
        return httpResponse.getRedirectLocations().getAll();
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }
}
