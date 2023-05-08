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

package org.vividus.http.validation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpStatus;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.validation.model.AbstractResourceValidation;
import org.vividus.http.validation.model.CheckStatus;
import org.vividus.softassert.ISoftAssert;

public class ResourceValidator<T extends AbstractResourceValidation<T>>
{
    private final IHttpClient httpClient;

    private final ISoftAssert softAssert;

    private final Set<Integer> allowedStatusCodes = Set.of(HttpStatus.SC_OK);
    private final Set<Integer> notAllowedHeadStatusCodes = Set.of(HttpStatus.SC_METHOD_NOT_ALLOWED,
                                                                  HttpStatus.SC_SERVICE_UNAVAILABLE,
                                                                  HttpStatus.SC_NOT_FOUND,
                                                                  HttpStatus.SC_NOT_IMPLEMENTED);

    private final Map<URI, T> cache = new ConcurrentHashMap<>();

    public ResourceValidator(IHttpClient httpClient, ISoftAssert softAssert)
    {
        this.httpClient = httpClient;
        this.softAssert = softAssert;
    }

    public T perform(T resourceValidation)
    {
        return cache.compute(resourceValidation.getUriOrError().getLeft(), (uri, rv) -> Optional.ofNullable(rv)
                .map(r -> {
                    T cachedResult = r.copy();
                    cachedResult.setCheckStatus(CheckStatus.SKIPPED);
                    return cachedResult;
                }).orElseGet(() -> {
                    validateResource(uri, resourceValidation);
                    return resourceValidation;
                })
        );
    }

    private void validateResource(URI uri, T resourceValidation)
    {
        try
        {
            HttpClientContext httpClientContext = HttpClientContext.create();
            int statusCode = httpClient.doHttpHead(uri, httpClientContext).getStatusCode();
            if (notAllowedHeadStatusCodes.contains(statusCode))
            {
                statusCode = httpClient.doHttpGet(uri, httpClientContext).getStatusCode();
            }
            resourceValidation.setStatusCode(OptionalInt.of(statusCode));

            CheckStatus checkStatus = softAssert.assertThat(
                    String.format("Status code for %s is %d. expected one of %s", uri, statusCode, allowedStatusCodes),
                    statusCode, is(oneOf(allowedStatusCodes.toArray()))) ? CheckStatus.PASSED : CheckStatus.FAILED;
            resourceValidation.setCheckStatus(checkStatus);
        }
        catch (IOException e)
        {
            softAssert.recordFailedAssertion("Exception occured during check of: " + uri, e);
            resourceValidation.setCheckStatus(CheckStatus.BROKEN);
        }
    }
}
