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

package org.vividus.http.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpRedirectsProvider;
import org.vividus.http.steps.RedirectValidationSteps.ExpectedRedirect;
import org.vividus.http.steps.RedirectValidationSteps.RedirectValidationState;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.FailableRunnable;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class RedirectValidationStepsTests
{
    private static final URI URL_WITH_REDIRECT_RESPONSE = URI.create("http://with-redirect.com/");
    private static final URI REDIRECT_URL = URI.create("http://redirect.com/");
    private static final String ASSERT_MESSAGE_FORMAT = "'%s' redirected to '%s'";
    private static final String ASSERT_FAIL_MESSAGE_FORMAT = "'%s' should redirect to '%s', ";
    private static final String ASSERT_ACTUAL_REDIRECT = "but actually redirected to: '%s'";
    private static final String FAIL = "Fail: ";
    private static final String FAIL_MESSAGE_FOR_URLS = FAIL + "different redirect URL's";
    private static final String REDIRECT_VALIDATION = "Redirects validation test report";
    private static final String REDIRECTS_NUMBER_MESSAGE = "Redirects number";
    private static final int REDIRECTS_NUMBER = 1;
    private static final String PASSED = "Passed: ";
    private static final String NO_REDIRECTS_EXPECTED_MESSAGE = "no redirection expected";
    private static final String PASSED_MESSAGE = "Passed";

    @Mock private ISoftAssert softAssert;

    @Mock private IAttachmentPublisher attachmentPublisher;

    @Mock private HttpRedirectsProvider redirectsProvider;

    @Captor private ArgumentCaptor<Map<String, List<RedirectValidationState>>> resultCaptor;

    @InjectMocks private RedirectValidationSteps redirectValidationSteps;

    @BeforeEach
    void beforeEach()
    {
        doAnswer(a ->
        {
            FailableRunnable<?> runnable = a.getArgument(0);
            runnable.run();
            return null;
        }).when(softAssert).runIgnoringTestFailFast(any());
    }

    @Test
    void shouldSuccessValidateRedirects() throws IOException
    {
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(Collections.singletonList(REDIRECT_URL));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_MESSAGE_FORMAT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        verify(softAssert, never()).recordFailedAssertion(anyString());
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, REDIRECT_URL);
        assertEquals(REDIRECTS_NUMBER, result.getExpectedRedirect().getRedirectsNumber());
        assertEquals(REDIRECTS_NUMBER, result.getActualRedirectsNumber());
        assertTrue(result.isPassed());
        assertEquals(PASSED_MESSAGE, result.getResultMessage());
    }

    @Test
    void shouldSuccessValidateRedirectsWithoutRedirectsNumber() throws IOException
    {
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(Collections.singletonList(REDIRECT_URL));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, null));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_MESSAGE_FORMAT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        verify(softAssert, never()).recordFailedAssertion(anyString());
        var redirectValidationState = mock(RedirectValidationState.class);
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, REDIRECT_URL);
        assertTrue(result.isPassed());
        verify(redirectValidationState, never()).getActualRedirectsNumber();
        assertEquals(PASSED_MESSAGE, result.getResultMessage());
    }

    @Test
    void shouldValidateRedirectsWithWrongRedirectUrl() throws IOException
    {
        when(redirectsProvider.getRedirects(any(URI.class)))
                .thenReturn(Collections.singletonList(URL_WITH_REDIRECT_RESPONSE));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER));
        verify(softAssert).recordFailedAssertion(String.format(ASSERT_FAIL_MESSAGE_FORMAT + ASSERT_ACTUAL_REDIRECT,
                URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL, URL_WITH_REDIRECT_RESPONSE));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, URL_WITH_REDIRECT_RESPONSE);
        verify(attachmentPublisher).publishAttachment(anyString(), any(), anyString());
        assertEquals(REDIRECTS_NUMBER, result.getExpectedRedirect().getRedirectsNumber());
        assertFalse(result.isPassed());
        assertEquals(FAIL_MESSAGE_FOR_URLS, result.getResultMessage());
    }

    @Test
    void shouldValidateRedirectsWithNoRedirectResponse()
    {
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER));
        verify(softAssert).recordFailedAssertion(String.format(ASSERT_FAIL_MESSAGE_FORMAT
                + "but actually no redirect returned", URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        List<URI> emptyUrlChain = List.of();
        verifyStartEndActualUrlSet(result, REDIRECT_URL, URL_WITH_REDIRECT_RESPONSE);
        assertFalse(result.isPassed());
        assertEquals("Fail: no redirect returned", result.getResultMessage());
        assertEquals(emptyUrlChain, result.getRedirects());
    }

    @Test
    void shouldValidateRedirectsWithCircularRedirectException() throws IOException
    {
        String circularExceptionMsg = "Circular exception appears";
        var ioException = new IOException(circularExceptionMsg);
        when(redirectsProvider.getRedirects(any(URI.class))).thenThrow(ioException);
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER));
        verify(softAssert).recordFailedAssertion(ArgumentMatchers.isA(IOException.class));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, null);
        assertFalse(result.isPassed());
        assertEquals(FAIL + circularExceptionMsg, result.getResultMessage());
    }

    @Test
    void shouldValidateRedirectsWithWrongRedirectNumber() throws IOException
    {
        int wrongRedirectsNumber = 2;
        int actualRedirectsNumber = 1;
        when(redirectsProvider.getRedirects(any(URI.class)))
                .thenReturn(Collections.singletonList(URL_WITH_REDIRECT_RESPONSE));
        redirectValidationSteps
                .validateRedirects(initParameters(URL_WITH_REDIRECT_RESPONSE, wrongRedirectsNumber));
        verify(softAssert).recordPassedAssertion(
                String.format(ASSERT_MESSAGE_FORMAT, URL_WITH_REDIRECT_RESPONSE, URL_WITH_REDIRECT_RESPONSE));
        verify(softAssert).recordFailedAssertion(String.format(
                "%s from '%s' to '%s' is expected to be '%d' but got '%d'", REDIRECTS_NUMBER_MESSAGE,
                URL_WITH_REDIRECT_RESPONSE, URL_WITH_REDIRECT_RESPONSE, wrongRedirectsNumber, actualRedirectsNumber));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, URL_WITH_REDIRECT_RESPONSE, URL_WITH_REDIRECT_RESPONSE);
        assertEquals(wrongRedirectsNumber, result.getExpectedRedirect().getRedirectsNumber());
        assertEquals(REDIRECTS_NUMBER, result.getActualRedirectsNumber());
        assertFalse(result.isPassed());
        assertEquals(FAIL + "different redirects number", result.getResultMessage());
    }

    @Test
    void shouldValidateRedirectsWithWrongRedirectUrlAndRedirectsNumber() throws IOException
    {
        int wrongRedirectsNumber = 2;
        when(redirectsProvider.getRedirects(any(URI.class)))
                .thenReturn(Collections.singletonList(URL_WITH_REDIRECT_RESPONSE));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, wrongRedirectsNumber));
        verify(softAssert).recordFailedAssertion(String.format(ASSERT_FAIL_MESSAGE_FORMAT + ASSERT_ACTUAL_REDIRECT,
                URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL, URL_WITH_REDIRECT_RESPONSE));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, URL_WITH_REDIRECT_RESPONSE);

        assertEquals(wrongRedirectsNumber, result.getExpectedRedirect().getRedirectsNumber());
        assertFalse(result.isPassed());
        assertEquals(FAIL_MESSAGE_FOR_URLS, result.getResultMessage());
    }

    @Test
    void shouldValidateRedirectsWithNoRedirect() throws IOException
    {
        int redirectsNumber = 0;
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(List.of());
        redirectValidationSteps
                .validateRedirects(initParameters(URL_WITH_REDIRECT_RESPONSE, redirectsNumber));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_FAIL_MESSAGE_FORMAT + NO_REDIRECTS_EXPECTED_MESSAGE,
                        URL_WITH_REDIRECT_RESPONSE, URL_WITH_REDIRECT_RESPONSE));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);

        verifyStartEndActualUrlSet(result, URL_WITH_REDIRECT_RESPONSE, URL_WITH_REDIRECT_RESPONSE);
        assertEquals(redirectsNumber, result.getActualRedirectsNumber());
        assertTrue(result.isPassed());
        assertEquals(PASSED + NO_REDIRECTS_EXPECTED_MESSAGE, result.getResultMessage());
    }

    private List<RedirectValidationState> verifyAttachmentAndCaptureResult()
    {
        verify(attachmentPublisher).publishAttachment(
                eq("/org/vividus/http/steps/attachment/redirect-validation-test-report.ftl"),
                resultCaptor.capture(), eq(REDIRECT_VALIDATION));
        return resultCaptor.getValue().get("results");
    }

    private static List<ExpectedRedirect> initParameters(URI endUrl, Integer redirectsNumber)
    {
        ExpectedRedirect expectedRedirect = new ExpectedRedirect();
        expectedRedirect.setStartUrl(URL_WITH_REDIRECT_RESPONSE);
        expectedRedirect.setEndUrl(endUrl);
        expectedRedirect.setRedirectsNumber(redirectsNumber);
        return Collections.singletonList(expectedRedirect);
    }

    private static void verifyStartEndActualUrlSet(RedirectValidationState result, URI end, URI actual)
    {
        assertEquals(URL_WITH_REDIRECT_RESPONSE, result.getExpectedRedirect().getStartUrl());
        assertEquals(end, result.getExpectedRedirect().getEndUrl());
        assertEquals(actual, result.getActualEndUrl());
    }
}
