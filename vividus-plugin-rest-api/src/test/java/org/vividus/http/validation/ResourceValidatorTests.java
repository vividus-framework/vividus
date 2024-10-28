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

package org.vividus.http.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
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
        when(httpClient.doHttpHead(FIRST)).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(200);
        when(softAssert.assertThat(eq(PASSED_CHECK_MESSAGE), eq(OK), argThat(MATCHER))).thenReturn(true);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, result.getCheckStatus());
    }

    @Test
    void shouldValidateResourceAndReuseCachedResultForTheSameUrl() throws IOException
    {
        when(httpClient.doHttpHead(FIRST)).thenReturn(httpResponse);
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
        when(httpClient.doHttpHead(FIRST)).thenReturn(httpResponse);
        var forbidden = 401;
        when(httpResponse.getStatusCode()).thenReturn(forbidden);
        String failure = "Status code for https://vividus.org is 401. expected one of [200]";
        when(softAssert.assertThat(eq(failure), eq(forbidden), argThat(MATCHER))).thenReturn(false);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.FAILED, result.getCheckStatus());
        var cachedResult = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.FAILED, cachedResult.getCheckStatus());
        verify(softAssert, times(2)).assertThat(eq(failure), eq(forbidden), argThat(MATCHER));
        verify(httpClient).doHttpHead(FIRST);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            403,
            404,
            405,
            501,
            503
    })
    void shouldValidateResourceAndRetryWithGetWhenHeadStatusCodeInNotAllowedSet(int statusCode) throws IOException
    {
        when(httpClient.doHttpHead(FIRST)).thenReturn(httpResponse);
        when(httpClient.doHttpGet(FIRST)).thenReturn(httpResponse);
        when(softAssert.assertThat(eq(PASSED_CHECK_MESSAGE), eq(OK), argThat(MATCHER))).thenReturn(true);
        when(httpResponse.getStatusCode()).thenReturn(statusCode).thenReturn(OK);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.PASSED, result.getCheckStatus());
    }

    @Test
    void shouldValidateResourceAndAttachResponseBodyIfUnexpectedStatusCode() throws IOException
    {
        resourceValidator.setPublishResponseBody(true);

        String notFound = "Not found";
        URI notFoundUrl = URI.create("https://vividus.org/not-found");
        var response404 = mock(HttpResponse.class);
        when(httpClient.doHttpHead(notFoundUrl)).thenReturn(response404);
        when(httpClient.doHttpGet(notFoundUrl)).thenReturn(response404);
        when(response404.getStatusCode()).thenReturn(404);
        when(response404.getResponseBodyAsString()).thenReturn(notFound);

        URI protectedUrl = URI.create("https://vividus.org/protected-page");
        var response401 = mock(HttpResponse.class);
        when(httpClient.doHttpHead(protectedUrl)).thenReturn(response401);
        when(response401.getStatusCode()).thenReturn(401);

        when(httpClient.doHttpHead(FIRST)).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(200);
        when(softAssert.assertThat(eq(PASSED_CHECK_MESSAGE), eq(OK), argThat(MATCHER))).thenReturn(true);

        URI notAllowedUrl = URI.create("https://vividus.org/not-allowed");
        var response405 = mock(HttpResponse.class);
        when(httpClient.doHttpHead(notAllowedUrl)).thenReturn(response405);
        when(response405.getStatusCode()).thenReturn(405);
        when(httpClient.doHttpGet(notAllowedUrl)).thenReturn(httpResponse);
        when(softAssert.assertThat(eq("Status code for https://vividus.org/not-allowed is 200. expected one of [200]"),
                eq(OK), argThat(MATCHER))).thenReturn(true);

        var resourceValidationNotFoundUrl = new ResourceValidation(notFoundUrl);
        var resourceValidationProtectedUrl = new ResourceValidation(protectedUrl);
        var resourceValidationSuccessful = new ResourceValidation(FIRST);
        var resourceValidationNotAllowedSuccessful = new ResourceValidation(notAllowedUrl);

        var resultSuccessfulWithoutBody = resourceValidator.perform(resourceValidationSuccessful);
        var resultSuccessfulNotAllowedWithoutBody = resourceValidator.perform(resourceValidationNotAllowedSuccessful);
        var resultNotFound  = resourceValidator.perform(resourceValidationNotFoundUrl);
        var resultProtected = resourceValidator.perform(resourceValidationProtectedUrl);

        assertEquals(notFound, resultNotFound.getResponseBody());
        assertNull(resultProtected.getResponseBody());
        assertNull(resultSuccessfulWithoutBody.getResponseBody());
        assertNull(resultSuccessfulNotAllowedWithoutBody.getResponseBody());

        resourceValidator.setPublishResponseBody(false);
        var resultNotFoundDisabledPublishing  = resourceValidator.perform(resourceValidationNotFoundUrl);
        assertNull(resultNotFoundDisabledPublishing.getResponseBody());
    }

    @Test
    void shouldMarkValidationAsBrokenIfExceptionOccurs() throws IOException
    {
        var ioException = new IOException();
        when(httpClient.doHttpHead(FIRST)).thenThrow(ioException);
        var resourceValidation = new ResourceValidation(FIRST);
        var result = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.BROKEN, result.getCheckStatus());
        var errMsgPart = "Exception occured during check of: https://vividus.org";
        verify(softAssert).recordFailedAssertion(errMsgPart, ioException);
        var cachedResult = resourceValidator.perform(resourceValidation);
        assertEquals(CheckStatus.BROKEN, cachedResult.getCheckStatus());
        verify(softAssert).recordFailedAssertion(errMsgPart + System.lineSeparator() + ioException);
        verify(httpClient).doHttpHead(FIRST);
    }
}
