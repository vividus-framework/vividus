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

package org.vividus.steps.api;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.TypeSafeMatcher;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.json.steps.JsonSteps;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.SubSteps;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class JsonResponseValidationStepsTests
{
    private static final String JSON_PATH = "$.test.name";
    private static final String SIMPLE_JSON = "{\"name\":\"value\"}";
    private static final String COMPLEX_JSON = "{\"test\":" + SIMPLE_JSON + "}";

    private static final String NUMBER_OF_JSON_ELEMENTS_ASSERTION = "The number of JSON elements by JSON path: ";
    private static final String HTML =
            "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>";

    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private HttpTestContext httpTestContext;
    @Mock private IAttachmentPublisher attachmentPublisher;

    private JsonResponseValidationSteps steps;

    @BeforeEach
    void beforeEach()
    {
        JsonPathUtils.setJacksonConfiguration();
        JsonUtils jsonUtils = new JsonUtils();
        var jsonSteps = new JsonSteps(new FluentEnumConverter(), httpTestContext, variableContext, jsonUtils,
                softAssert, attachmentPublisher, Map.of());
        steps = new JsonResponseValidationSteps(httpTestContext, jsonSteps, softAssert);
    }

    @Test
    void shouldWaitForJsonElement()
    {
        var stepsToExecute = mock(SubSteps.class);
        when(httpTestContext.getResponse())
                .thenReturn(createHttpResponse(HTML))
                .thenReturn(createHttpResponse(SIMPLE_JSON))
                .thenReturn(new HttpResponse())
                .thenReturn(createHttpResponse(COMPLEX_JSON));
        var retryTimes = 10;
        steps.waitForJsonElement(JSON_PATH, Duration.ofSeconds(2), retryTimes, stepsToExecute);
        verify(stepsToExecute, atLeast(4)).execute(Optional.empty());
        verify(stepsToExecute, atMost(retryTimes)).execute(Optional.empty());
        verifyAssertion();
    }

    @Test
    void shouldWaitForJsonElementWithPollingInterval()
    {
        var stepsToExecute = mock(SubSteps.class);
        when(httpTestContext.getResponse())
                .thenReturn(createHttpResponse(HTML))
                .thenReturn(createHttpResponse(SIMPLE_JSON))
                .thenReturn(new HttpResponse())
                .thenReturn(createHttpResponse(COMPLEX_JSON));
        var retryTimes = 4;
        steps.waitForJsonElementWithPollingInterval(JSON_PATH, Duration.ofSeconds(1), retryTimes, stepsToExecute);
        verify(stepsToExecute, times(retryTimes)).execute(Optional.empty());
        verifyAssertion();
    }

    private HttpResponse createHttpResponse(String body)
    {
        var response = new HttpResponse();
        response.setResponseTimeInMs(0);
        response.setResponseBody(body.getBytes(StandardCharsets.UTF_8));
        return response;
    }

    private void verifyAssertion()
    {
        var clazz = TypeSafeMatcher.class;
        verify(softAssert).assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION + JSON_PATH), eq(1),
                argThat(arg -> clazz.isInstance(arg) && clazz.cast(arg).matches(1)));
    }

    @Test
    void shouldFailToWaitForJsonElementWithPollingInterval()
    {
        var stepsToExecute = mock(SubSteps.class);
        when(httpTestContext.getResponse()).thenReturn(new HttpResponse());
        var retryTimes = 1;
        steps.waitForJsonElementWithPollingInterval(JSON_PATH, Duration.ofSeconds(1), retryTimes, stepsToExecute);
        verify(stepsToExecute, times(retryTimes)).execute(Optional.empty());
        verify(softAssert).recordFailedAssertion("HTTP response body is not present");
    }

    @Test
    void shouldNotWaitForJsonElementWhenHttpResponseIsNull()
    {
        var stepsToExecute = mock(SubSteps.class);
        when(httpTestContext.getResponse()).thenReturn(null);
        var retryTimes = 3;
        steps.waitForJsonElement(JSON_PATH, Duration.ofSeconds(1), retryTimes, stepsToExecute);
        verify(stepsToExecute, times(1)).execute(Optional.empty());
        verifyNoInteractions(softAssert);
    }
}
