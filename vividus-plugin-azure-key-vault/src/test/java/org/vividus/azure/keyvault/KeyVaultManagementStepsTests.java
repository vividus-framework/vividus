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

package org.vividus.azure.keyvault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class KeyVaultManagementStepsTests
{
    private static final String SUBSCRIPTION_ID_PROPERTY_NAME = "AZURE_SUBSCRIPTION_ID";
    private static final String SUBSCRIPTION_ID_PROPERTY_VALUE = "subscription-id";

    private static final String RESOURCE_GROUP_NAME = "resourceGroupName";
    private static final String KEY_VAULT_NAME = "keyVaultName";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VAR_NAME = "varName";

    @Mock private TokenCredential tokenCredential;
    @Mock private SoftAssert softAssert;
    @Mock private VariableContext variableContext;

    private Stream<Named<BiConsumer<Consumer<KeyVaultManagementSteps>, String>>> createStreamOfTests()
    {
        return Stream.of(
                named("success", (test, expectedUrlPath) -> {
                    var response = testHttpRequestExecution(test, 200, expectedUrlPath);
                    verify(variableContext).putVariable(SCOPES, VAR_NAME, response);
                }),
                named("failure", (test, expectedUrlPath) -> {
                    var response = testHttpRequestExecution(test, 404, expectedUrlPath);
                    verify(softAssert).recordFailedAssertion(
                            "Azure REST API HTTP request execution is failed: " + response);
                })
        );
    }

    @TestFactory
    @SetSystemProperty(key = SUBSCRIPTION_ID_PROPERTY_NAME, value = SUBSCRIPTION_ID_PROPERTY_VALUE)
    Stream<DynamicTest> shouldRetrieveKeyVaultProperties()
    {
        var urlPath = String.format(
                "subscriptions/%s/resourceGroups/%s/providers/Microsoft.KeyVault/vaults/%s",
                SUBSCRIPTION_ID_PROPERTY_VALUE, RESOURCE_GROUP_NAME, KEY_VAULT_NAME);
        return DynamicTest.stream(createStreamOfTests(), test -> test.accept(
                steps -> steps.retrieveKeyVaultProperties(KEY_VAULT_NAME, RESOURCE_GROUP_NAME, SCOPES, VAR_NAME),
                urlPath)
        );
    }

    @SuppressWarnings("PMD.CloseResource")
    private String testHttpRequestExecution(Consumer<KeyVaultManagementSteps> test, int statusCode,
            String expectedUrlPath)
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

            var steps = new KeyVaultManagementSteps(azureProfile, tokenCredential, softAssert, variableContext);
            test.accept(steps);
            var httpRequest = httpRequestCaptor.getValue();
            assertEquals(HttpMethod.GET, httpRequest.getHttpMethod());
            assertEquals("https://management.azure.com/" + expectedUrlPath + "?api-version=2021-10-01",
                    httpRequest.getUrl().toString());
            return responseAsString;
        }
    }
}
