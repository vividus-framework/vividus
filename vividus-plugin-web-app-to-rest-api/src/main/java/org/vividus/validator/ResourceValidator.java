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

package org.vividus.validator;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.hamcrest.Matcher;
import org.vividus.http.HttpMethod;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.softassert.SoftAssert;
import org.vividus.validator.model.CheckStatus;
import org.vividus.validator.model.ResourceValidation;

public class ResourceValidator
{
    @Inject
    @Named("resourceValidator")
    private IHttpClient httpClient;

    @Inject private SoftAssert softAssert;

    private final Set<Integer> allowedStatusCodes = Set.of(HttpStatus.SC_OK);
    private final Set<Integer> notAllowedHeadStatusCodes = Set.of(HttpStatus.SC_METHOD_NOT_ALLOWED,
                                                                  HttpStatus.SC_SERVICE_UNAVAILABLE,
                                                                  HttpStatus.SC_NOT_FOUND,
                                                                  HttpStatus.SC_NOT_IMPLEMENTED);

    private final Map<URI, ResourceValidation> cache = new ConcurrentHashMap<>();

    public ResourceValidation perform(ResourceValidation resourceValidation)
    {
        return cache.compute(resourceValidation.getUri(), (u, rv) -> {
            return Optional.ofNullable(rv).map(r -> {
                ResourceValidation cachedResult = r.copy();
                cachedResult.setCheckStatus(CheckStatus.SKIPPED);
                return cachedResult;
            }).orElseGet(() -> {
                try
                {
                    HttpClientContext httpClientContext = HttpClientContext.create();
                    int statusCode = checkResource(u, httpClientContext, HttpMethod.HEAD);
                    resourceValidation.setStatusCode(statusCode);
                    String message = String.format("Status code for %s is %d. expected one of %s", u,
                            statusCode, allowedStatusCodes);
                    Matcher<Object> oneOf = is(oneOf(allowedStatusCodes.toArray()));
                    resourceValidation.setCheckStatus(CheckStatus.get(oneOf.matches(statusCode)));
                    softAssert.assertThat(message, statusCode, oneOf);
                }
                catch (IOException toReport)
                {
                    softAssert.recordFailedAssertion("Exception occured during check of: " + u, toReport);
                    resourceValidation.setCheckStatus(CheckStatus.BROKEN);
                }
                return resourceValidation;
            });
        });
    }

    private int checkResource(URI uri, HttpClientContext httpClientContext, HttpMethod httpMethod)
            throws IOException
    {
        HttpResponse httpResponse = executeHttpMethod(httpMethod, httpClientContext, uri);
        int statusCode = httpResponse.getStatusCode();
        if (HttpMethod.GET == httpMethod || !notAllowedHeadStatusCodes.contains(statusCode))
        {
            return statusCode;
        }
        return checkResource(uri, httpClientContext, HttpMethod.GET);
    }

    private HttpResponse executeHttpMethod(HttpMethod httpMethod, HttpClientContext httpClientContext, URI uri)
            throws IOException
    {
        HttpRequestBase request = httpMethod.createRequest(uri);
        return httpClient.execute(request, httpClientContext);
    }
}
