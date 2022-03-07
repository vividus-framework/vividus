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

import java.util.Set;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.variable.VariableScope;

import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class AbstractAzureResourceManagementSteps
{
    private final HttpPipeline httpPipeline;
    private final String resourceManagerEndpoint;
    private final String apiVersion;
    private final SoftAssert softAssert;
    private final VariableContext variableContext;

    protected AbstractAzureResourceManagementSteps(HttpPipeline httpPipeline, String resourceManagerEndpoint,
            String apiVersion, SoftAssert softAssert, VariableContext variableContext)
    {
        this.httpPipeline = httpPipeline;
        this.resourceManagerEndpoint = resourceManagerEndpoint;
        this.apiVersion = apiVersion;
        this.softAssert = softAssert;
        this.variableContext = variableContext;
    }

    protected void saveHttpResponseAsVariable(String urlPath, Set<VariableScope> scopes, String variableName)
    {
        // Workaround for https://github.com/Azure/azure-sdk-for-java/issues/27268
        String url = resourceManagerEndpoint + urlPath + "?api-version=" + apiVersion;
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, url);
        try (HttpResponse httpResponse = httpPipeline.send(httpRequest).block())
        {
            String responseBody = httpResponse.getBodyAsString().block();
            if (httpResponse.getStatusCode() == HttpResponseStatus.OK.code())
            {
                variableContext.putVariable(scopes, variableName, responseBody);
            }
            else
            {
                softAssert.recordFailedAssertion("Azure REST API HTTP request execution is failed: " + responseBody);
            }
        }
    }
}
