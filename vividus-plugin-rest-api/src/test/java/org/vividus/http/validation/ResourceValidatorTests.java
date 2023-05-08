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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.apache.hc.core5.http.protocol.HttpContext;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.validation.model.CheckStatus;
import org.vividus.http.validation.model.ResourceValidation;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class ResourceValidatorTests
{
    private static final String PASSED_CHECK_MESSAGE =
            "Status code for https://vividus.org is 200. expected one of [200]";
    private static final int OK = 200;
    private static final URI FIRST = URI.create("https://vividus.org");
    private static final ArgumentMatcher<Matcher<? super Integer>> MATCHER =
        m -> "is one of {<200>}".equals(m.toString());

    @Mock private IHttpClient httpClient;
    @Mock private ISoftAssert softAssert;
    @Mock private HttpResponse httpResponse;
    @InjectMocks private ResourceValidator<ResourceValidation> resourceValidator;

    @Test
    void shouldValidateResource() throws IOException
    {
        when(httpClient.doHttpHead(eq(FIRST), any(HttpContext.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(200);
        when(softAssert.assertThat(eq(PASSED_CHECK_MESSAGE), eq(OK), argThat(MATCHER))).thenReturn(true);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, result.getCheckStatus());
    }

    @Test
    void shouldValidateResourceAndReuseCachedResultForTheSameUrl() throws IOException
    {
        when(httpClient.doHttpHead(eq(FIRST), any(HttpContext.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(200);
        when(softAssert.assertThat(eq(PASSED_CHECK_MESSAGE), eq(OK), argThat(MATCHER))).thenReturn(true);
        var resourceValidation = new ResourceValidation(FIRST);
        var first = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, first.getCheckStatus());

        var second = resourceValidator.perform(resourceValidation);
        assertThat(first, not(sameInstance(second)));
        assertEquals(CheckStatus.SKIPPED, second.getCheckStatus());
        assertEquals(first.getUriOrError(), second.getUriOrError());
    }

    @Test
    void shouldValidateResourceAndNotRetryWithGetIfStatusCodeNotInNotAllowedSet() throws IOException
    {
        when(httpClient.doHttpHead(eq(FIRST), any(HttpContext.class))).thenReturn(httpResponse);
        var forbidden = 403;
        when(httpResponse.getStatusCode()).thenReturn(forbidden);
        when(softAssert.assertThat(eq("Status code for https://vividus.org is 403. expected one of [200]"),
                eq(forbidden), argThat(MATCHER))).thenReturn(false);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.FAILED, result.getCheckStatus());
    }

    @Test
    void shouldValidateResourceAndRetryWithGetWhenHeadStatusCodeInNotAllowedSet() throws IOException
    {
        when(httpClient.doHttpHead(eq(FIRST), any(HttpContext.class))).thenReturn(httpResponse);
        when(httpClient.doHttpGet(eq(FIRST), any(HttpContext.class))).thenReturn(httpResponse);
        when(softAssert.assertThat(eq(PASSED_CHECK_MESSAGE), eq(OK), argThat(MATCHER))).thenReturn(true);
        var notFound = 404;
        when(httpResponse.getStatusCode()).thenReturn(notFound).thenReturn(OK);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, result.getCheckStatus());
    }

    @Test
    void shouldMarkValidationAsBrokenIfExceptionOccurs() throws IOException
    {
        var ioException = new IOException();
        when(httpClient.doHttpHead(eq(FIRST), any(HttpContext.class))).thenThrow(ioException);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.BROKEN, result.getCheckStatus());
        verify(softAssert).recordFailedAssertion("Exception occured during check of: https://vividus.org", ioException);
    }
}
