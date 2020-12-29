/*
 * Copyright 2019-2020 the original author or authors.
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
import java.util.Optional;

import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.exception.HttpRequestBuildException;
import org.vividus.softassert.ISoftAssert;

/**
 * Executor of HTTP requests which supports some exceptions handling, test context releasing
 * and requests repeat with quit conditions.
 */
public class HttpRequestExecutor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestExecutor.class);

    private final IHttpClient httpClient;
    private final HttpTestContext httpTestContext;
    private final ISoftAssert softAssert;

    public HttpRequestExecutor(IHttpClient httpClient, HttpTestContext httpTestContext, ISoftAssert softAssert)
    {
        this.httpClient = httpClient;
        this.httpTestContext = httpTestContext;
        this.softAssert = softAssert;
    }

    /**
     * Executes HTTP request and frees context request entity and headers.
     * Request is executed safely for unchecked exceptions: HttpRequestBuildException, ConnectionClosedException.
     * Assertion fails if exceptions occurred.
     * @param httpMethod HttpMethod to execute
     * @param endpoint Request endpoint
     * @param relativeURL Relative URL - optional
     * @throws IOException If an input or output exception occurred
     */
    public void executeHttpRequest(HttpMethod httpMethod, String endpoint, Optional<String> relativeURL)
            throws IOException
    {
        try
        {
            HttpRequestBuilder requestBuilder = HttpRequestBuilder.create()
                    .withHttpMethod(httpMethod)
                    .withEndpoint(endpoint)
                    .withRelativeUrl(relativeURL.orElse(null))
                    .withHeaders(httpTestContext.getRequestHeaders());
            httpTestContext.getRequestEntity().ifPresent(requestBuilder::withContent);
            HttpRequestBase request = requestBuilder.build();

            HttpClientContext context = new HttpClientContext();
            httpTestContext.getCookieStore().ifPresent(context::setCookieStore);
            httpTestContext.getRequestConfig().ifPresent(context::setRequestConfig);

            HttpResponse response = httpClient.execute(request, context);
            httpTestContext.putResponse(response);
            LOGGER.info("Response time: {} ms", response.getResponseTimeInMs());
        }
        catch (HttpRequestBuildException | ConnectionClosedException e)
        {
            softAssert.recordFailedAssertion(e);
        }
        finally
        {
            httpTestContext.releaseRequestData();
        }
    }
}
