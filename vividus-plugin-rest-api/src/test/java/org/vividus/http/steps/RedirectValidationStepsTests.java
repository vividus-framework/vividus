/*
 * Copyright 2019-2025 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpRedirectsProvider;
import org.vividus.http.HttpTestContext;
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
    private static final String ASSERT_NO_REDIRECT = "but actually no redirect returned";
    private static final String FAIL = "Fail: ";
    private static final String FAIL_MESSAGE_FOR_URLS = FAIL + "different redirect URL's";
    private static final String FAIL_MESSAGE_FOR_STATUS_CODE = FAIL + "unexpected status codes of redirects";
    private static final String FAIL_MESSAGE_NO_REDIRECTS = FAIL + "no redirect returned";
    private static final String REDIRECT_VALIDATION = "Redirects validation test report";
    private static final String REDIRECTS_NUMBER_MESSAGE = "Redirects number";
    private static final int REDIRECTS_NUMBER = 1;
    private static final String PASSED = "Passed: ";
    private static final String NO_REDIRECTS_EXPECTED_MESSAGE = "no redirection expected";
    private static final String PASSED_MESSAGE = "Passed";
    private static final String STATUS_CODES_MESSAGE = "Status codes of redirects";
    private static final int STATUS_CODE = 301;

    @Mock private ISoftAssert softAssert;

    @Mock private IAttachmentPublisher attachmentPublisher;

    @Mock private HttpRedirectsProvider redirectsProvider;

    @Mock private HttpTestContext httpTestContext;

    @Captor private ArgumentCaptor<Map<String, List<RedirectValidationState>>> resultCaptor;

    @InjectMocks private RedirectValidationSteps redirectValidationSteps;

    @Test
    void shouldSuccessValidateRedirects() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(Collections.singletonList(REDIRECT_URL));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER, null));
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
    void shouldSuccessValidateRedirectsWithoutRedirectsNumberAndStatusCodes() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(Collections.singletonList(REDIRECT_URL));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, null, null));
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
        mockSoftAssertRunIgnoringTestFailFast();
        when(redirectsProvider.getRedirects(any(URI.class)))
                .thenReturn(Collections.singletonList(URL_WITH_REDIRECT_RESPONSE));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER, null));
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
        mockSoftAssertRunIgnoringTestFailFast();
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER, null));
        verify(softAssert).recordFailedAssertion(String.format(ASSERT_FAIL_MESSAGE_FORMAT
                + ASSERT_NO_REDIRECT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        List<URI> emptyUrlChain = List.of();
        verifyStartEndActualUrlSet(result, REDIRECT_URL, URL_WITH_REDIRECT_RESPONSE);
        assertFalse(result.isPassed());
        assertEquals(FAIL_MESSAGE_NO_REDIRECTS, result.getResultMessage());
        assertEquals(emptyUrlChain, result.getRedirects());
    }

    @Test
    void shouldValidateRedirectsWithCircularRedirectException() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        String circularExceptionMsg = "Circular exception appears";
        var ioException = new IOException(circularExceptionMsg);
        when(redirectsProvider.getRedirects(any(URI.class))).thenThrow(ioException);
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, REDIRECTS_NUMBER, null));
        verify(softAssert).recordFailedAssertion(ArgumentMatchers.isA(IOException.class));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, null);
        assertFalse(result.isPassed());
        assertEquals(FAIL + circularExceptionMsg, result.getResultMessage());
    }

    @Test
    void shouldValidateRedirectsWithWrongRedirectNumber() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        int wrongRedirectsNumber = 2;
        int actualRedirectsNumber = 1;
        when(redirectsProvider.getRedirects(any(URI.class)))
                .thenReturn(Collections.singletonList(URL_WITH_REDIRECT_RESPONSE));
        redirectValidationSteps
                .validateRedirects(initParameters(URL_WITH_REDIRECT_RESPONSE, wrongRedirectsNumber, null));
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
        mockSoftAssertRunIgnoringTestFailFast();
        int wrongRedirectsNumber = 2;
        when(redirectsProvider.getRedirects(any(URI.class)))
                .thenReturn(Collections.singletonList(URL_WITH_REDIRECT_RESPONSE));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, wrongRedirectsNumber, null));
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
        mockSoftAssertRunIgnoringTestFailFast();
        int redirectsNumber = 0;
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(List.of());
        redirectValidationSteps
                .validateRedirects(initParameters(URL_WITH_REDIRECT_RESPONSE, redirectsNumber, null));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_FAIL_MESSAGE_FORMAT + NO_REDIRECTS_EXPECTED_MESSAGE,
                        URL_WITH_REDIRECT_RESPONSE, URL_WITH_REDIRECT_RESPONSE));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);

        verifyStartEndActualUrlSet(result, URL_WITH_REDIRECT_RESPONSE, URL_WITH_REDIRECT_RESPONSE);
        assertEquals(redirectsNumber, result.getActualRedirectsNumber());
        assertTrue(result.isPassed());
        assertEquals(PASSED + NO_REDIRECTS_EXPECTED_MESSAGE, result.getResultMessage());
    }

    @Test
    void shouldSuccessValidateRedirectsWithStatusCode() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        List<Integer> expectedStatusCodes = List.of(STATUS_CODE);
        List<Integer> actualStatusCodes = List.of(STATUS_CODE);
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(Collections.singletonList(REDIRECT_URL));
        when(httpTestContext.getStatusCodes()).thenReturn(getHttpClientStatusCodes(actualStatusCodes));
        when(softAssert.assertEquals(STATUS_CODES_MESSAGE, expectedStatusCodes, actualStatusCodes)).thenReturn(true);
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, null, expectedStatusCodes));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_MESSAGE_FORMAT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        verify(softAssert, never()).recordFailedAssertion(anyString());
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, REDIRECT_URL);
        assertEquals(expectedStatusCodes, result.getExpectedRedirect().getStatusCodes());
        assertEquals(PASSED_MESSAGE, result.getResultMessage());
        assertTrue(result.isPassed());
    }

    @Test
    void shouldValidateRedirectsWithWrongStatusCode() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        List<Integer> expectedStatusCodes = List.of(STATUS_CODE);
        List<Integer> wrongStatusCodes = List.of(302);
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(Collections.singletonList(REDIRECT_URL));
        when(httpTestContext.getStatusCodes()).thenReturn(getHttpClientStatusCodes(wrongStatusCodes));
        when(softAssert.assertEquals(STATUS_CODES_MESSAGE, expectedStatusCodes, wrongStatusCodes)).thenReturn(false);
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, null, expectedStatusCodes));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_MESSAGE_FORMAT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, REDIRECT_URL);
        assertEquals(expectedStatusCodes, result.getExpectedRedirect().getStatusCodes());
        assertFalse(result.isPassed());
        assertEquals(FAIL_MESSAGE_FOR_STATUS_CODE, result.getResultMessage());
    }

    @Test
    void shouldSuccessValidateRedirectsWithMultipleStatusCodes() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        List<Integer> expectedStatusCodes = List.of(301, 302);
        List<Integer> actualStatusCodes = List.of(301, 302);
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(List.of(REDIRECT_URL, REDIRECT_URL));
        when(httpTestContext.getStatusCodes()).thenReturn(getHttpClientStatusCodes(actualStatusCodes));
        when(softAssert.assertEquals(STATUS_CODES_MESSAGE, expectedStatusCodes, actualStatusCodes)).thenReturn(true);
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, null, expectedStatusCodes));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_MESSAGE_FORMAT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        verify(softAssert, never()).recordFailedAssertion(anyString());
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, REDIRECT_URL);
        assertEquals(expectedStatusCodes, result.getExpectedRedirect().getStatusCodes());
        assertTrue(result.isPassed());
        assertEquals(PASSED_MESSAGE, result.getResultMessage());
    }

    @Test
    void shouldValidateStatusCodesWithNoRedirect() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        List<Integer> expectedStatusCodes = List.of(STATUS_CODE);
        when(httpTestContext.getStatusCodes()).thenReturn(getHttpClientStatusCodes(List.of()));
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, null, expectedStatusCodes));
        verify(softAssert).recordFailedAssertion(String.format(ASSERT_FAIL_MESSAGE_FORMAT
                + ASSERT_NO_REDIRECT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        List<URI> emptyUrlChain = List.of();
        verifyStartEndActualUrlSet(result, REDIRECT_URL, URL_WITH_REDIRECT_RESPONSE);
        assertFalse(result.isPassed());
        assertEquals(FAIL_MESSAGE_NO_REDIRECTS, result.getResultMessage());
        assertEquals(emptyUrlChain, result.getRedirects());
    }

    @Test
    void shouldValidateRedirectsWithWrongMultipleStatusCodes() throws IOException
    {
        mockSoftAssertRunIgnoringTestFailFast();
        List<Integer> expectedStatusCodes = List.of(301, 303);
        List<Integer> actualStatusCodes = List.of(301, 302);
        when(redirectsProvider.getRedirects(any(URI.class))).thenReturn(List.of(REDIRECT_URL, REDIRECT_URL));
        when(httpTestContext.getStatusCodes()).thenReturn(getHttpClientStatusCodes(actualStatusCodes));
        when(softAssert.assertEquals(STATUS_CODES_MESSAGE, expectedStatusCodes, actualStatusCodes)).thenReturn(false);
        redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, null, expectedStatusCodes));
        verify(softAssert)
                .recordPassedAssertion(String.format(ASSERT_MESSAGE_FORMAT, URL_WITH_REDIRECT_RESPONSE, REDIRECT_URL));
        RedirectValidationState result = verifyAttachmentAndCaptureResult().get(0);
        verifyStartEndActualUrlSet(result, REDIRECT_URL, REDIRECT_URL);
        assertEquals(expectedStatusCodes, result.getExpectedRedirect().getStatusCodes());
        assertFalse(result.isPassed());
        assertEquals(FAIL_MESSAGE_FOR_STATUS_CODE, result.getResultMessage());
    }

    @Test
    void shouldFailValidationWhenRedirectsNumberAndStatusCodesSpecified()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> redirectValidationSteps.validateRedirects(initParameters(REDIRECT_URL, 1, List.of(STATUS_CODE))));
        assertEquals("The 'redirectsNumber' and 'statusCodes' can't be specified simultaneously",
                exception.getMessage());
    }

    private List<RedirectValidationState> verifyAttachmentAndCaptureResult()
    {
        verify(attachmentPublisher).publishAttachment(
                eq("/org/vividus/http/steps/attachment/redirect-validation-test-report.ftl"),
                resultCaptor.capture(), eq(REDIRECT_VALIDATION));
        return resultCaptor.getValue().get("results");
    }

    private List<Integer> getHttpClientStatusCodes(List<Integer> redirectStatusCodes)
    {
        List<Integer> statusCodes = new java.util.ArrayList<>(redirectStatusCodes);
        statusCodes.add(200);
        return statusCodes;
    }

    private void mockSoftAssertRunIgnoringTestFailFast()
    {
        doAnswer(a ->
        {
            FailableRunnable<?> runnable = a.getArgument(0);
            runnable.run();
            return null;
        }).when(softAssert).runIgnoringTestFailFast(any());
    }

    private static List<ExpectedRedirect> initParameters(URI endUrl, Integer redirectsNumber, List<Integer> statusCodes)
    {
        ExpectedRedirect expectedRedirect = new ExpectedRedirect();
        expectedRedirect.setStartUrl(URL_WITH_REDIRECT_RESPONSE);
        expectedRedirect.setEndUrl(endUrl);
        expectedRedirect.setRedirectsNumber(redirectsNumber);
        expectedRedirect.setStatusCodes(statusCodes == null ? List.of() : statusCodes);
        return Collections.singletonList(expectedRedirect);
    }

    private static void verifyStartEndActualUrlSet(RedirectValidationState result, URI end, URI actual)
    {
        assertEquals(URL_WITH_REDIRECT_RESPONSE, result.getExpectedRedirect().getStartUrl());
        assertEquals(end, result.getExpectedRedirect().getEndUrl());
        assertEquals(actual, result.getActualEndUrl());
    }
}
