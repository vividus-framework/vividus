/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.azure.resourcemanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.variable.VariableScope;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AbstractAzureResourceManagementStepsTests
{
    private static final String URL_PATH = "subscriptions/sub-id/resourceGroups/rgname/providers/Microsoft"
            + ".KeyVault/vaults/sample-vault";
    private static final String API_VERSION = "2021-10-01";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VAR_NAME = "varName";
    private static final String REQUEST_BODY = "{\"azure-resource\":\"body\"}";

    @Mock private HttpPipeline httpPipeline;
    @Mock private ISoftAssert softAssert;
    @Mock private VariableContext variableContext;

    @Test
    void shouldSaveHttpGetResponseInCaseOfSuccess()
    {
        var response = testHttpRequestExecution(200,
                steps -> steps.saveHttpGetResponseAsVariable(URL_PATH, API_VERSION, SCOPES, VAR_NAME),
                httpRequest -> assertEquals(HttpMethod.GET, httpRequest.getHttpMethod()));
        verify(variableContext).putVariable(SCOPES, VAR_NAME, response);
    }

    @Test
    void shouldSaveHttpPostResponseInCaseOfSuccess()
    {
        var response = testHttpRequestExecution(200,
                steps -> steps.saveHttpPostResponseAsVariable(URL_PATH, API_VERSION, REQUEST_BODY, SCOPES, VAR_NAME),
                httpRequest -> assertHttpRequestWithBody(HttpMethod.POST, httpRequest));
        verify(variableContext).putVariable(SCOPES, VAR_NAME, response);
    }

    @Test
    void shouldRecordFailedAssertionInCaseOfFailure()
    {
        var response = testHttpRequestExecution(404,
                steps -> steps.saveHttpGetResponseAsVariable(URL_PATH, API_VERSION, SCOPES, VAR_NAME),
                httpRequest -> assertEquals(HttpMethod.GET, httpRequest.getHttpMethod()));
        verify(softAssert).recordFailedAssertion("Azure REST API HTTP request execution is failed: " + response);
    }

    @Test
    void shouldExecuteHttpPutRequest()
    {
        testHttpRequestExecution(200, steps -> steps.executeHttpPut(URL_PATH, API_VERSION, REQUEST_BODY),
                httpRequest -> assertHttpRequestWithBody(HttpMethod.PUT, httpRequest));
    }

    @Test
    void shouldExecuteHttpDeleteRequest()
    {
        testHttpRequestExecution(200, steps -> steps.executeHttpDelete(URL_PATH, API_VERSION),
                httpRequest -> assertEquals(HttpMethod.DELETE, httpRequest.getHttpMethod()));
    }

    private String testHttpRequestExecution(int statusCode, Consumer<AbstractAzureResourceManagementSteps> test,
            Consumer<HttpRequest> httpRequestValidator)
    {
        var responseAsString = "{\"key\":\"value\"}";
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusCode()).thenReturn(statusCode);
        when(httpResponse.getBodyAsString()).thenReturn(Mono.just(responseAsString));
        var httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(httpPipeline.send(httpRequestCaptor.capture())).thenReturn(Mono.just(httpResponse));

        var resourceManagerEndpoint = "https://management.azure.com/";
        var steps = new TestSteps(httpPipeline, resourceManagerEndpoint, softAssert, variableContext);
        test.accept(steps);
        var httpRequest = httpRequestCaptor.getValue();
        assertEquals(resourceManagerEndpoint + URL_PATH + "?api-version=" + API_VERSION,
                httpRequest.getUrl().toString());
        httpRequestValidator.accept(httpRequest);
        return responseAsString;
    }

    private void assertHttpRequestWithBody(HttpMethod httpMethod, HttpRequest httpRequest)
    {
        assertEquals(httpMethod, httpRequest.getHttpMethod());
        assertEquals(REQUEST_BODY, new String(httpRequest.getBody().blockFirst().array(), StandardCharsets.UTF_8));
        var expectedHeaders = Map.of(
                "Content-Type", "application/json",
                "Content-Length", Integer.toString(REQUEST_BODY.length())
        );
        assertEquals(expectedHeaders, httpRequest.getHeaders().toMap());
    }

    private static class TestSteps extends AbstractAzureResourceManagementSteps
    {
        protected TestSteps(HttpPipeline httpPipeline, String resourceManagerEndpoint, ISoftAssert softAssert,
                VariableContext variableContext)
        {
            super(httpPipeline, resourceManagerEndpoint, softAssert, variableContext);
        }
    }
}
