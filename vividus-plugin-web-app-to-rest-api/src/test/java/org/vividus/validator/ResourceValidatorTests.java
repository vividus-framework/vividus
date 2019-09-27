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

package org.vividus.validator;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.softassert.SoftAssert;
import org.vividus.validator.model.CheckStatus;
import org.vividus.validator.model.ResourceValidation;

@ExtendWith(MockitoExtension.class)
class ResourceValidatorTests
{
    private static final String PASSED_CHECK_MESSAGE =
            "Status code for https://vividus.org is 200. expected one of [200]";
    private static final int OK = 200;
    private static final String HEAD = "HEAD";
    private static final URI FIRST = URI.create("https://vividus.org");
    private static final String CSS_SELECTOR = "a";
    private static final ArgumentMatcher<Matcher<? super Integer>> MATCHER =
        m -> "is one of {<200>}".equals(m.toString());

    @Mock
    private IHttpClient httpClient;

    @Mock
    private SoftAssert softAssert;

    @Mock
    private HttpResponse httpResponse;

    @InjectMocks
    private ResourceValidator resourceValidator;

    @Test
    void shouldValidateResource() throws IOException
    {
        when(httpClient.execute(argThat(r -> HEAD.equals(r.getMethod())), any(HttpContext.class)))
            .thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(200);
        ResourceValidation resourceValidation = new ResourceValidation(FIRST, CSS_SELECTOR);
        ResourceValidation result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, result.getCheckStatus());
        verify(httpClient).execute(any(HttpUriRequest.class), any(HttpContext.class));
        verify(softAssert).assertThat(eq(PASSED_CHECK_MESSAGE),
                eq(OK), argThat(MATCHER));
    }

    @Test
    void shouldValidateResourceAndReuseCachedResultForTheSameUrl() throws IOException
    {
        when(httpClient.execute(argThat(r -> HEAD.equals(r.getMethod())), any(HttpContext.class)))
            .thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(200);
        ResourceValidation resourceValidation = new ResourceValidation(FIRST, CSS_SELECTOR);
        ResourceValidation first = resourceValidator.perform(resourceValidation);
        ResourceValidation second = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, first.getCheckStatus());
        verify(httpClient).execute(any(HttpUriRequest.class), any(HttpContext.class));
        verify(softAssert).assertThat(eq(PASSED_CHECK_MESSAGE),
                eq(OK), argThat(MATCHER));
        assertThat(first, not(sameInstance(second)));
        assertEquals(CheckStatus.SKIPPED, second.getCheckStatus());
        assertEquals(first.getUri(), second.getUri());
    }

    @Test
    void shouldValidateResourceAndNotRetryWithGetIfStatusCodeNotInNotAllowedSet() throws IOException
    {
        when(httpClient.execute(argThat(r -> HEAD.equals(r.getMethod())), any(HttpContext.class)))
            .thenReturn(httpResponse);
        int forbidden = 403;
        when(httpResponse.getStatusCode()).thenReturn(forbidden);
        ResourceValidation resourceValidation = new ResourceValidation(FIRST, CSS_SELECTOR);
        ResourceValidation result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.FAILED, result.getCheckStatus());
        verify(httpClient).execute(any(HttpUriRequest.class), any(HttpContext.class));
        verify(softAssert).assertThat(eq("Status code for https://vividus.org is 403. expected one of [200]"),
                eq(forbidden), argThat(MATCHER));
    }

    @Test
    void shouldValidateResourceAndRetryWithGetWhenHeadStatusCodeInNotAllowedSet() throws IOException
    {
        doReturn(httpResponse).when(httpClient).execute(argThat(r -> HEAD.equals(r.getMethod())),
                any(HttpContext.class));
        doReturn(httpResponse).when(httpClient).execute(argThat(r -> "GET".equals(r.getMethod())),
                any(HttpContext.class));
        int notFound = 404;
        when(httpResponse.getStatusCode()).thenReturn(notFound).thenReturn(OK);
        ResourceValidation resourceValidation = new ResourceValidation(FIRST, CSS_SELECTOR);
        ResourceValidation result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, result.getCheckStatus());
        verify(httpClient, times(2)).execute(any(HttpUriRequest.class), any(HttpContext.class));
        verify(softAssert).assertThat(eq(PASSED_CHECK_MESSAGE), eq(OK), argThat(MATCHER));
    }

    @Test
    void shouldMarkValidationAsBrokenIfExceptionOccurs() throws IOException
    {
        IOException ioException = new IOException();
        when(httpClient.execute(argThat(r -> HEAD.equals(r.getMethod())), any(HttpContext.class)))
            .thenThrow(ioException);
        ResourceValidation resourceValidation = new ResourceValidation(FIRST, CSS_SELECTOR);
        ResourceValidation result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.BROKEN, result.getCheckStatus());
        verify(httpClient).execute(any(HttpUriRequest.class), any(HttpContext.class));
        verify(softAssert).recordFailedAssertion(eq("Exception occured during check of: https://vividus.org"),
                eq(ioException));
    }
}
