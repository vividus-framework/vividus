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

import java.util.Map;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.appservice.AppServiceManager;

public class FunctionService
{
    private final AzureProfile azureProfile;

    public FunctionService(AzureProfile azureProfile)
    {
        this.azureProfile = azureProfile;
    }

    private AppServiceManager createManager(ResponseCapturingHttpPipelinePolicy responseCapturingHttpPipelinePolicy)
    {
        TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(azureProfile.getEnvironment().getActiveDirectoryEndpoint()).build();
        return AppServiceManager.configure()
                                .withLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                                .withPolicy(responseCapturingHttpPipelinePolicy)
                                .authenticate(credential, azureProfile);
    }

    public Map<String, Object> triggerFunction(String resourceGroup, String appName, String functionName,
            Object payload)
    {
        ResponseCapturingHttpPipelinePolicy responseCapturingHttpPipelinePolicy =
                new ResponseCapturingHttpPipelinePolicy(functionName);
        createManager(responseCapturingHttpPipelinePolicy)
               .functionApps()
               .getByResourceGroup(resourceGroup, appName)
               .triggerFunction(functionName, payload);
        return responseCapturingHttpPipelinePolicy.getResponses();
    }
}
