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
import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.exception.HttpRequestBuildException;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

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
        executeHttpRequest(httpMethod, endpoint, relativeURL, response -> true, new WaitMode(Duration.ZERO, 1));
    }

    /**
     * Executes HTTP request <i>while predicate condition is true</i>
     * and frees context request entity and headers afterwards.
     * Request is executed safely for unchecked exceptions: HttpRequestBuildException, ConnectionClosedException.
     * Assertion fails if exceptions occurred.
     * @param httpMethod HttpMethod to execute
     * @param endpoint Request endpoint
     * @param relativeURL Relative URL - optional
     * @param stopCondition Condition used to check whether stop wait or proceed to the next iteration
     * @param waitMode Which time duration to wait and how many retry times
     * @throws IOException If an input or output exception occurred
     */
    public void executeHttpRequest(HttpMethod httpMethod, String endpoint, Optional<String> relativeURL,
            Predicate<HttpResponse> stopCondition, WaitMode waitMode) throws IOException
    {
        Waiter waiter = new DurationBasedWaiter(waitMode);
        try
        {
            waiter.wait(() -> executeHttpCallSafely(httpMethod, endpoint, relativeURL.orElse(null)), stopCondition);
        }
        finally
        {
            httpTestContext.releaseRequestData();
        }
    }

    private HttpResponse executeHttpCallSafely(HttpMethod httpMethod, String endpoint, String relativeURL)
            throws IOException
    {
        try
        {
            return execute(httpMethod, endpoint, relativeURL);
        }
        catch (HttpRequestBuildException | ConnectionClosedException e)
        {
            softAssert.recordFailedAssertion(e);
            return null;
        }
    }

    private HttpResponse execute(HttpMethod httpMethod, String endpoint, String relativeURL)
            throws HttpRequestBuildException, IOException
    {
        HttpRequestBase requestBase = prepareHttpRequestBase(httpMethod, endpoint, relativeURL);
        HttpClientContext httpClientContext = new HttpClientContext();
        httpTestContext.getCookieStore().ifPresent(httpClientContext::setCookieStore);
        httpTestContext.getRequestConfig().ifPresent(httpClientContext::setRequestConfig);

        HttpResponse httpResponse = httpClient.execute(requestBase, httpClientContext);
        httpTestContext.putResponse(httpResponse);
        LOGGER.info("Response time: {} ms", httpResponse.getResponseTimeInMs());
        return httpResponse;
    }

    private HttpRequestBase prepareHttpRequestBase(HttpMethod httpMethod, String endpoint, String relativeURL)
            throws HttpRequestBuildException
    {
        HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.create()
                .withHttpMethod(httpMethod)
                .withEndpoint(endpoint)
                .withRelativeUrl(relativeURL)
                .withHeaders(httpTestContext.getRequestHeaders());
        httpTestContext.getRequestEntity().ifPresent(httpRequestBuilder::withContent);
        return httpRequestBuilder.build();
    }
}
