/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.azure.functions.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.AppServiceManager.Configurable;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionApps;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FunctionServiceTests
{
    private static final Map<Object, Object> PAYLOAD = Map.of();
    private static final String FUNCTION = "test";
    private static final String FUNCTION_APP = "vividus-tests";
    private static final String RESOURCE_GROUP = "vividus";
    private static final String ENDPOINT = "https://azure.dev";
    @Mock private AzureEnvironment azureEnvironment;
    @Mock private AzureProfile azureProfile;

    @InjectMocks private FunctionService functionService;

    @Test
    void shouldExecuteAFunction()
    {
        try (MockedStatic<AppServiceManager> appServiceMock = mockStatic(AppServiceManager.class);
             MockedConstruction<ResponseCapturingHttpPipelinePolicy> policy =
                     mockConstruction(ResponseCapturingHttpPipelinePolicy.class))
        {
            when(azureProfile.getEnvironment()).thenReturn(azureEnvironment);
            when(azureEnvironment.getActiveDirectoryEndpoint()).thenReturn(ENDPOINT);
            Configurable configurable = mock(Configurable.class);
            appServiceMock.when(() -> AppServiceManager.configure()).thenReturn(configurable);
            when(configurable.withLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)).thenReturn(configurable);
            when(configurable.withPolicy(any(ResponseCapturingHttpPipelinePolicy.class))).thenReturn(configurable);
            AppServiceManager serviceManager = mock(AppServiceManager.class);
            when(configurable.authenticate(any(TokenCredential.class), eq(azureProfile))).thenReturn(serviceManager);
            FunctionApps functionApps = mock(FunctionApps.class);
            when(serviceManager.functionApps()).thenReturn(functionApps);
            FunctionApp functionApp = mock(FunctionApp.class);
            when(functionApps.getByResourceGroup(RESOURCE_GROUP, FUNCTION_APP)).thenReturn(functionApp);

            functionService.triggerFunction(RESOURCE_GROUP, FUNCTION_APP, FUNCTION, PAYLOAD);

            verify(azureProfile).getEnvironment();
            verify(azureEnvironment).getActiveDirectoryEndpoint();
            verify(functionApp).triggerFunction(FUNCTION, PAYLOAD);
            verify(policy.constructed().get(0)).getResponses();
        }
    }
}
