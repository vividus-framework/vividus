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

package org.vividus.azure.resourcemanager;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.azure.core.http.HttpMethod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.client.AzureResourceManagementClient;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.SoftAssert;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ResourceManagementStepsTests
{
    private static final String SUBSCRIPTION_ID_PROPERTY_VALUE = "subscription-id";

    private static final String API_VERSION_NAME = "?api-version=";
    private static final String API_VERSION_VALUE = "2021-10-01";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VAR_NAME = "varName";
    private static final String AZURE_RESOURCE_IDENTIFIER = "resourceGroups/my-rg/providers/Microsoft"
            + ".KeyVault/vaults/my-kv";
    private static final String URL_PATH =
            "subscriptions/" + SUBSCRIPTION_ID_PROPERTY_VALUE + "/" + AZURE_RESOURCE_IDENTIFIER;
    private static final String URL_DOMAIN = "https://management.azure.com/";
    private static final String REQUEST_BODY = "{\"resource\": \"body\"}";
    private static final String RESPONSE_BODY = "{\"key\":\"value\"}";
    private static final String EMPTY_RESPONSE_BODY = "{ }";
    private static final String TEMPLATE_NAME =
            "/vividus-plugin/azure/resourcemanager/attachments/resource-as-json.ftl";
    private static final String BODY = "body";
    private static final String CONTENT_TYPE = "bodyContentType";
    private static final String APPLICATION_JSON = "application/json";
    private static final String ATTACHMENT_TITLE = "Azure Resource JSON";

    private final ArgumentCaptor<Consumer<String>> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
    private final ArgumentCaptor<Consumer<String>> errorCallbackCaptor = ArgumentCaptor.forClass(Consumer.class);

    @Mock private AzureResourceManagementClient client;
    @Mock private VariableContext variableContext;
    @Mock private SoftAssert softAssert;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private ResourceManagementSteps steps;

    @Test
    void testSavingOfAzureResourceAsVariableUsingResourceUrl()
    {
        steps.saveAzureResourceAsVariableWithResourceUrl(URL_DOMAIN + URL_PATH + API_VERSION_NAME
                + API_VERSION_VALUE, SCOPES, VAR_NAME);
        verify(client).executeHttpRequest(eq(HttpMethod.GET), eq(URL_DOMAIN + URL_PATH + API_VERSION_NAME
                        + API_VERSION_VALUE), eq(Optional.empty()), consumerCaptor.capture(),
                errorCallbackCaptor.capture());
        consumerCaptor.getValue().accept(RESPONSE_BODY);
        verify(variableContext).putVariable(SCOPES, VAR_NAME, RESPONSE_BODY);
        verify(attachmentPublisher).publishAttachment(TEMPLATE_NAME,
                Map.of(BODY, RESPONSE_BODY, CONTENT_TYPE, APPLICATION_JSON), ATTACHMENT_TITLE);
        verifyErrorConsumer();
    }

    @Test
    void testSavingOfAzureResourceAsVariable()
    {
        steps.saveAzureResourceAsVariable(AZURE_RESOURCE_IDENTIFIER, API_VERSION_VALUE, SCOPES, VAR_NAME);
        verify(client).executeHttpRequest(eq(HttpMethod.GET), eq(AZURE_RESOURCE_IDENTIFIER),
                eq(API_VERSION_VALUE), eq(Optional.empty()), consumerCaptor.capture(),
                errorCallbackCaptor.capture());
        consumerCaptor.getValue().accept(RESPONSE_BODY);
        verify(variableContext).putVariable(SCOPES, VAR_NAME, RESPONSE_BODY);
        verify(attachmentPublisher).publishAttachment(TEMPLATE_NAME,
                Map.of(BODY, RESPONSE_BODY, CONTENT_TYPE, APPLICATION_JSON), ATTACHMENT_TITLE);
    }

    @Test
    void testExecutionOfOperationAtAzureResource()
    {
        steps.executeOperationAtAzureResource(AZURE_RESOURCE_IDENTIFIER, API_VERSION_VALUE,
                REQUEST_BODY, SCOPES, VAR_NAME);
        verify(client).executeHttpRequest(eq(HttpMethod.POST), eq(AZURE_RESOURCE_IDENTIFIER), eq(API_VERSION_VALUE),
                eq(Optional.of(REQUEST_BODY)), consumerCaptor.capture(), errorCallbackCaptor.capture());
        consumerCaptor.getValue().accept(EMPTY_RESPONSE_BODY);
        verifyNoInteractions(variableContext);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void shouldConfigureAzureResource()
    {
        steps.configureAzureResource(AZURE_RESOURCE_IDENTIFIER, REQUEST_BODY, API_VERSION_VALUE);
        verify(client).executeHttpRequest(eq(HttpMethod.PUT), eq(AZURE_RESOURCE_IDENTIFIER),
                eq(API_VERSION_VALUE), eq(Optional.of(REQUEST_BODY)), consumerCaptor.capture(),
                errorCallbackCaptor.capture());
        consumerCaptor.getValue().accept(EMPTY_RESPONSE_BODY);
        verifyNoInteractions(variableContext);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void shouldDeleteAzureResource()
    {
        steps.deleteAzureResource(AZURE_RESOURCE_IDENTIFIER, API_VERSION_VALUE);
        verify(client).executeHttpRequest(eq(HttpMethod.DELETE), eq(AZURE_RESOURCE_IDENTIFIER),
                eq(API_VERSION_VALUE), eq(Optional.empty()), consumerCaptor.capture(),
                errorCallbackCaptor.capture());
        consumerCaptor.getValue().accept(EMPTY_RESPONSE_BODY);
        verifyNoInteractions(variableContext);
        verifyNoInteractions(attachmentPublisher);
    }

    private void verifyErrorConsumer()
    {
        String simulatedError = "Simulated error";
        errorCallbackCaptor.getValue().accept(simulatedError);
        verify(softAssert, times(1)).recordFailedAssertion(simulatedError);
    }
}
