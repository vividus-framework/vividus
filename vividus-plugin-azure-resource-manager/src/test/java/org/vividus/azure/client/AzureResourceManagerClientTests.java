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

package org.vividus.azure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
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
import org.vividus.softassert.ISoftAssert;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AzureResourceManagerClientTests
{
    private static final String SUBSCRIPTION_ID_PROPERTY_NAME = "AZURE_SUBSCRIPTION_ID";
    private static final String SUBSCRIPTION_ID_PROPERTY_VALUE = "subscription-id";

    private static final String API_VERSION_NAME = "?api-version=";
    private static final String API_VERSION_VALUE = "2021-10-01";
    private static final String AZURE_RESOURCE_IDENTIFIER = "resourceGroups/my-rg/providers/Microsoft"
            + ".KeyVault/vaults/my-kv";
    private static final String URL_PATH =
            "subscriptions/" + SUBSCRIPTION_ID_PROPERTY_VALUE + "/" + AZURE_RESOURCE_IDENTIFIER;
    private static final String URL_DOMAIN = "https://management.azure.com/";
    private static final String REQUEST_BODY = "{\"resource\": \"body\"}";
    private static final String RESPONSE_BODY = "{\"key\":\"value\"}";

    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Mock private ISoftAssert softAssert;

    private Stream<Named<BiConsumer<Consumer<AzureResourceManagementClient>,
            String>>> createTestsForResourceIdentifier()
    {
        return Stream.of(
                named("successfulOperation", (test, expectedUrlPath) -> {
                    testHttpRequestExecution(test, 200, expectedUrlPath, Optional.of(RESPONSE_BODY),
                            httpRequest -> assertHttpRequestWithBody(HttpMethod.POST, httpRequest)
                    );
                }),
                named("failedOperation", (test, expectedUrlPath) -> {
                    testHttpRequestExecution(test, 404, expectedUrlPath, Optional.of(RESPONSE_BODY),
                            httpRequest -> assertHttpRequestWithBody(HttpMethod.POST, httpRequest)
                    );
                    verifyFailedHttpRequestExecutionWithResponseBody(RESPONSE_BODY);
                })
        );
    }

    @TestFactory
    @SetSystemProperty(key = SUBSCRIPTION_ID_PROPERTY_NAME, value = SUBSCRIPTION_ID_PROPERTY_VALUE)
    Stream<DynamicTest> testExecuteRequestUsingAzureResourceIdentifier()
    {
        return DynamicTest.stream(createTestsForResourceIdentifier(), test -> test.accept(
                client -> client.executeHttpRequest(HttpMethod.POST, AZURE_RESOURCE_IDENTIFIER, API_VERSION_VALUE,
                        Optional.of(REQUEST_BODY), mock(Consumer.class), softAssert::recordFailedAssertion),
                URL_PATH)
        );
    }

    private Stream<Named<BiConsumer<Consumer<AzureResourceManagementClient>, String>>> createTestsForResourceUrl()
    {
        return Stream.of(
                named("successfulCase", (test, expectedUrlPath) -> {
                    testHttpRequestExecution(test, 200, expectedUrlPath, Optional.of(RESPONSE_BODY),
                            httpRequest -> assertEquals(HttpMethod.GET, httpRequest.getHttpMethod()));
                }),
                named("failedWithBody", (test, expectedUrlPath) -> {
                    testHttpRequestExecution(test, 404, expectedUrlPath, Optional.of(RESPONSE_BODY),
                            httpRequest -> assertEquals(HttpMethod.GET, httpRequest.getHttpMethod()));
                    verifyFailedHttpRequestExecutionWithResponseBody(RESPONSE_BODY);
                }),
                named("failedWithoutBody", (test, expectedUrlPath) -> {
                    testHttpRequestExecution(test, 404, expectedUrlPath, Optional.empty(),
                            httpRequest -> assertEquals(HttpMethod.GET, httpRequest.getHttpMethod()));
                    verifyFailedHttpRequestExecutionWithoutResponseBody(404);
                })
        );
    }

    @TestFactory
    @SetSystemProperty(key = SUBSCRIPTION_ID_PROPERTY_NAME, value = SUBSCRIPTION_ID_PROPERTY_VALUE)
    Stream<DynamicTest> testExecuteRequestUsingResourceUrl()
    {
        return DynamicTest.stream(createTestsForResourceUrl(), test -> test.accept(
                client -> client.executeHttpRequest(HttpMethod.GET, URL_DOMAIN + URL_PATH + API_VERSION_NAME
                        + API_VERSION_VALUE, Optional.of(REQUEST_BODY), mock(Consumer.class),
                        softAssert::recordFailedAssertion),
                URL_PATH)
        );
    }

    @Test
    void testExecuteHttpRequestWithException()
    {
        AzureResourceManagementClient clientWithoutSoftAssert = new AzureResourceManagementClient(azureProfile,
                tokenCredential);
        Consumer<String> responseBodyConsumer = mock(Consumer.class);
        assertThrows(RuntimeException.class, () -> {
            clientWithoutSoftAssert.executeHttpRequest(HttpMethod.GET, URL_DOMAIN + URL_PATH
                    + API_VERSION_NAME + API_VERSION_VALUE, Optional.of(REQUEST_BODY), responseBodyConsumer,
                    softAssert::recordFailedAssertion);
        });
        verify(responseBodyConsumer, never()).accept(anyString());
    }

    @SuppressWarnings("PMD.CloseResource")
    private void testHttpRequestExecution(Consumer<AzureResourceManagementClient> test, int statusCode,
                                          String expectedUrlPath, Optional<String> responseAsString,
                                          Consumer<HttpRequest> httpRequestValidator)
    {
        var azureProfile = new AzureProfile(AzureEnvironment.AZURE);
        try (MockedStatic<HttpPipelineProvider> httpPipelineProviderMock = mockStatic(HttpPipelineProvider.class))
        {
            var httpPipeline = mock(HttpPipeline.class);
            httpPipelineProviderMock.when(() -> HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile))
                    .thenReturn(httpPipeline);
            var httpResponse = mock(HttpResponse.class);
            when(httpResponse.getStatusCode()).thenReturn(statusCode);
            responseAsString.ifPresent(s -> when(httpResponse.getBodyAsString()).thenReturn(Mono.just(s)));
            var httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            when(httpPipeline.send(httpRequestCaptor.capture())).thenReturn(Mono.just(httpResponse));

            var client = new AzureResourceManagementClient(azureProfile, tokenCredential);
            test.accept(client);
            var httpRequest = httpRequestCaptor.getValue();
            assertEquals(URL_DOMAIN + expectedUrlPath + API_VERSION_NAME + API_VERSION_VALUE,
                    httpRequest.getUrl().toString());
            httpRequestValidator.accept(httpRequest);
        }
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

    private void verifyFailedHttpRequestExecutionWithResponseBody(String response)
    {
        verify(softAssert).recordFailedAssertion(
                "Azure REST API HTTP request execution is failed: " + response);
    }

    private void verifyFailedHttpRequestExecutionWithoutResponseBody(int responseCode)
    {
        verify(softAssert).recordFailedAssertion(
                "Azure REST API HTTP request execution is failed with empty body and status code: " + responseCode);
    }
}
