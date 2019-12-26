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

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.protocol.HttpClientContext;
import org.vividus.http.client.ExternalServiceException;
import org.vividus.http.client.IHttpClient;

public class HttpRedirectsProvider
{
    private IHttpClient httpClient;

    /**
     * Executes HEAD request to get redirects.
     * Throws IllegalStateException in case of status code outside of "200-207"
     * @param from URI to issue HEAD request
     * @return List of redirects. {@code null} if there are no redirects.
     */
    public List<URI> getRedirects(URI from)
    {
        try
        {
            HttpClientContext httpContext = HttpClientContext.create();
            httpClient.doHttpHead(from, httpContext).verifyStatusCodeInRange(HttpStatus.SC_OK,
                    HttpStatus.SC_MULTI_STATUS);
            return httpContext.getRedirectLocations();
        }
        catch (IOException | ExternalServiceException e)
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
            throw new IllegalStateException(exceptionMsg, e);
        }
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }
}
