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
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.variable.VariableScope;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ResourceManagementStepsTests
{
    private static final String SUBSCRIPTION_ID_PROPERTY_NAME = "AZURE_SUBSCRIPTION_ID";
    private static final String SUBSCRIPTION_ID_PROPERTY_VALUE = "subscription-id";

    private static final String API_VERSION = "2021-10-01";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VAR_NAME = "varName";
    private static final String AZURE_RESOURCE_IDENTIFIER = "resourceGroups/my-rg/providers/Microsoft"
            + ".KeyVault/vaults/my-kv";
    private static final String URL_PATH =
            "subscriptions/" + SUBSCRIPTION_ID_PROPERTY_VALUE + "/" + AZURE_RESOURCE_IDENTIFIER;
    private static final String REQUEST_BODY = "{\"resource\": \"body\"}";

    @Mock private TokenCredential tokenCredential;
    @Mock private SoftAssert softAssert;
    @Mock private VariableContext variableContext;

    private Stream<Named<BiConsumer<Consumer<ResourceManagementSteps>, String>>> createTestsCreatingResources()
    {
        return Stream.of(
                named("successfulCreation", (test, expectedUrlPath) -> {
                    var response = testHttpRequestExecution(test, 200, expectedUrlPath,
                            httpRequest -> assertEquals(HttpMethod.GET, httpRequest.getHttpMethod()));
                    verify(variableContext).putVariable(SCOPES, VAR_NAME, response);
                }),
                named("failedCreation", (test, expectedUrlPath) -> {
                    var response = testHttpRequestExecution(test, 404, expectedUrlPath,
                            httpRequest -> assertEquals(HttpMethod.GET, httpRequest.getHttpMethod()));
                    verifyFailedHttpRequestExecution(response);
                })
        );
    }

    @TestFactory
    @SetSystemProperty(key = SUBSCRIPTION_ID_PROPERTY_NAME, value = SUBSCRIPTION_ID_PROPERTY_VALUE)
    Stream<DynamicTest> shouldSaveAzureResourceAsVariable()
    {
        return DynamicTest.stream(createTestsCreatingResources(), test -> test.accept(
                steps -> steps.saveAzureResourceAsVariable(AZURE_RESOURCE_IDENTIFIER, API_VERSION, SCOPES, VAR_NAME),
                URL_PATH)
        );
    }

    private Stream<Named<BiConsumer<Consumer<ResourceManagementSteps>, String>>> createTestsExecutionOperations()
    {
        return Stream.of(
                named("successfulOperation", (test, expectedUrlPath) -> {
                    var response = testHttpRequestExecution(test, 200, expectedUrlPath,
                            httpRequest -> assertHttRequestWithBody(HttpMethod.POST, httpRequest)
                    );
                    verify(variableContext).putVariable(SCOPES, VAR_NAME, response);
                }),
                named("failedOperation", (test, expectedUrlPath) -> {
                    var response = testHttpRequestExecution(test, 404, expectedUrlPath,
                            httpRequest -> assertHttRequestWithBody(HttpMethod.POST, httpRequest)
                    );
                    verifyFailedHttpRequestExecution(response);
                })
        );
    }

    @TestFactory
    @SetSystemProperty(key = SUBSCRIPTION_ID_PROPERTY_NAME, value = SUBSCRIPTION_ID_PROPERTY_VALUE)
    Stream<DynamicTest> shouldExecuteOperationAtAzureResource()
    {
        return DynamicTest.stream(createTestsExecutionOperations(), test -> test.accept(
                steps -> steps.executeOperationAtAzureResource(AZURE_RESOURCE_IDENTIFIER, API_VERSION, REQUEST_BODY,
                        SCOPES, VAR_NAME),
                URL_PATH)
        );
    }

    @Test
    @SetSystemProperty(key = SUBSCRIPTION_ID_PROPERTY_NAME, value = SUBSCRIPTION_ID_PROPERTY_VALUE)
    void shouldConfigureAzureResource()
    {
        testHttpRequestExecution(
                steps -> steps.configureAzureResource(AZURE_RESOURCE_IDENTIFIER, REQUEST_BODY, API_VERSION),
                200, URL_PATH, httpRequest -> assertHttRequestWithBody(HttpMethod.PUT, httpRequest)
        );
    }

    @Test
    @SetSystemProperty(key = SUBSCRIPTION_ID_PROPERTY_NAME, value = SUBSCRIPTION_ID_PROPERTY_VALUE)
    void shouldDeleteAzureResource()
    {
        testHttpRequestExecution(
                steps -> steps.deleteAzureResource(AZURE_RESOURCE_IDENTIFIER, API_VERSION),
                200, URL_PATH,
                httpRequest -> assertEquals(HttpMethod.DELETE, httpRequest.getHttpMethod())
        );
    }

    @SuppressWarnings("PMD.CloseResource")
    private String testHttpRequestExecution(Consumer<ResourceManagementSteps> test, int statusCode,
            String expectedUrlPath, Consumer<HttpRequest> httpRequestValidator)
    {
        var azureProfile = new AzureProfile(AzureEnvironment.AZURE);
        try (MockedStatic<HttpPipelineProvider> httpPipelineProviderMock = mockStatic(HttpPipelineProvider.class))
        {
            var httpPipeline = mock(HttpPipeline.class);
            httpPipelineProviderMock.when(() -> HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile))
                    .thenReturn(httpPipeline);
            var responseAsString = "{\"key\":\"value\"}";
            var httpResponse = mock(HttpResponse.class);
            when(httpResponse.getStatusCode()).thenReturn(statusCode);
            when(httpResponse.getBodyAsString()).thenReturn(Mono.just(responseAsString));
            var httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            when(httpPipeline.send(httpRequestCaptor.capture())).thenReturn(Mono.just(httpResponse));

            var steps = new ResourceManagementSteps(azureProfile, tokenCredential, softAssert, variableContext);
            test.accept(steps);
            var httpRequest = httpRequestCaptor.getValue();
            assertEquals("https://management.azure.com/" + expectedUrlPath + "?api-version=" + API_VERSION,
                    httpRequest.getUrl().toString());
            httpRequestValidator.accept(httpRequest);
            return responseAsString;
        }
    }

    private void assertHttRequestWithBody(HttpMethod httpMethod, HttpRequest httpRequest)
    {
        assertEquals(httpMethod, httpRequest.getHttpMethod());
        assertEquals(REQUEST_BODY, new String(httpRequest.getBody().blockFirst().array(), StandardCharsets.UTF_8));
        var expectedHeaders = Map.of(
                "Content-Type", "application/json",
                "Content-Length", Integer.toString(REQUEST_BODY.length())
        );
        assertEquals(expectedHeaders, httpRequest.getHeaders().toMap());
    }

    private void verifyFailedHttpRequestExecution(String response)
    {
        verify(softAssert).recordFailedAssertion(
                "Azure REST API HTTP request execution is failed: " + response);
    }
}
