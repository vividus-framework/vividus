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

package org.vividus.ui.web.playwright.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.model.HttpMessagePart;
import org.vividus.ui.web.playwright.network.NetworkContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class NetworkValidationStepsTests
{
    private static final String URL = "https://www.example.com/";
    private static final Pattern URL_PATTERN = Pattern.compile(".*example\\.com.*");
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String VARIABLE_NAME = "variableName";
    private static final String WAIT_ASSERTION_MESSAGE = String
            .format("waiting for HTTP GET request with URL pattern %s", URL_PATTERN);
    private static final String COLLECTION_MATCHER_MSG = "a collection with size <1>";

    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private WaitActions waitActions;
    @Mock private NetworkContext networkContext;
    @Mock private SoftAssert softAssert;
    @Mock private VariableContext variableContext;

    @InjectMocks private NetworkValidationSteps networkValidationSteps;

    @Test
    void shouldCaptureRequestAndSaveHttpMessagePart()
    {
        mockRequestsRecordings();
        networkValidationSteps.captureRequestAndSaveHttpMessagePart(new LinkedHashSet<>(List.of(GET, POST)),
                URL_PATTERN, HttpMessagePart.URL, Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
        verify(softAssert).assertThat(eq("Number of HTTP GET, POST requests matching URL pattern " + URL_PATTERN),
                any(), argThat(arg -> COLLECTION_MATCHER_MSG.equals(arg.toString())));
        verify(variableContext).putVariable(Set.of(VariableScope.SCENARIO), VARIABLE_NAME, URL);
    }

    @Test
    void shouldCaptureRequestAndSaveHttpMessagePartWithInappropriatePattern()
    {
        List<Request> requestsCollection = List.of();
        when(networkContext.getNetworkRecordings()).thenReturn(requestsCollection);
        networkValidationSteps.captureRequestAndSaveHttpMessagePart(Set.of(GET), URL_PATTERN, HttpMessagePart.URL,
                Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
        verify(softAssert).assertThat(eq("Number of HTTP GET requests matching URL pattern " + URL_PATTERN),
                eq(requestsCollection), argThat(arg -> COLLECTION_MATCHER_MSG.equals(arg.toString())));
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldWaitRequestInNetworkRecordingsWithSuccessfullyAssertion()
    {
        BrowserContext browserContext = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        mockRequestsRecordings();
        doNothing().when(waitActions).runWithTimeoutAssertion(
                eq(WAIT_ASSERTION_MESSAGE),
                argThat(runnable ->
                {
                    runnable.run();
                    return true;
                }));
        networkValidationSteps.waitRequestIsCaptured(Set.of(GET), URL_PATTERN);
        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(browserContext).waitForCondition(conditionCaptor.capture());
        BooleanSupplier condition = conditionCaptor.getValue();
        assertTrue(condition.getAsBoolean());
    }

    @Test
    void shouldWaitRequestInNetworkRecordingsWithFailedAssertion()
    {
        BrowserContext browserContext = mock();
        when(networkContext.getNetworkRecordings()).thenReturn(List.of());
        when(browserContextProvider.get()).thenReturn(browserContext);
        doNothing().when(waitActions).runWithTimeoutAssertion(
                eq(WAIT_ASSERTION_MESSAGE),
                argThat(runnable ->
                {
                    runnable.run();
                    return true;
                }));
        networkValidationSteps.waitRequestIsCaptured(Set.of(GET), URL_PATTERN);
        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(browserContext).waitForCondition(conditionCaptor.capture());
        BooleanSupplier condition = conditionCaptor.getValue();
        assertFalse(condition.getAsBoolean());
    }

    @Test
    void shouldClearNetworkRecordings()
    {
        List<Request> recordings = new ArrayList<>(mockRequestsRecordings());
        when(networkContext.getNetworkRecordings()).thenReturn(recordings);
        assertEquals(2, recordings.size());
        networkValidationSteps.clearNetworkRecordings();
        assertEquals(0, recordings.size());
    }

    private List<Request> mockRequestsRecordings()
    {
        Request request1 = mock(Request.class);
        lenient().when(request1.url()).thenReturn(URL);
        lenient().when(request1.method()).thenReturn(GET);
        Request request2 = mock(Request.class);
        lenient().when(request2.url()).thenReturn("https://www.other.com");
        lenient().when(request2.method()).thenReturn(POST);
        List<Request> requestsRecordings = List.of(request1, request2);
        when(networkContext.getNetworkRecordings()).thenReturn(requestsRecordings);
        return requestsRecordings;
    }
}
