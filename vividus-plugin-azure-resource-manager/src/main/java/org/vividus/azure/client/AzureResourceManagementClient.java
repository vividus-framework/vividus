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

import java.util.Optional;
import java.util.function.Consumer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

import org.apache.commons.lang3.Strings;

import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;

public class AzureResourceManagementClient
{
    private static final String EXECUTION_FAILED = "Azure REST API HTTP request execution is failed";

    private final HttpPipeline httpPipeline;
    private final AzureProfile azureProfile;

    public AzureResourceManagementClient(AzureProfile azureProfile, TokenCredential tokenCredential)
    {
        this.azureProfile = azureProfile;
        this.httpPipeline = HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile);
    }

    /**
     * Executes an HTTP request to the specified Azure resource using the provided parameters.
     * This method constructs the URL for the request, sends the HTTP request, and handles the response
     * or errors. It uses the provided responseBodyConsumer to handle successful responses and errorCallback
     * to handle error scenarios.
     *
     * @param method                  The HTTP method to be used for the request (e.g., GET, POST, PUT, DELETE).
     * @param azureResourceIdentifier This is a VIVIDUS-only term. It's used to specify Azure resource uniquely. From
     *                                the technical perspective it's a part of Azure resource REST API URL path. For
     *                                example, if the full Azure resource URL is
     *                                <br>
     *                                <code>https://management.azure.com/subscriptions/
     *                                00000000-0000-0000-0000-000000000000/resourceGroups/sample-resource-group/
     *                                providers/Microsoft.KeyVault/vaults/sample-vault?api-version=2021-10-01</code>,
     *                                <br>
     *                                then resource identifier will be
     *                                <br>
     *                                <code>resourceGroups/sample-resource-group/providers/Microsoft.KeyVault/
     *                                vaults/sample-vault</code>.
     * @param apiVersion              Azure resource provider API version. Note API versions may vary depending on
     *                                the resource type.
     * @param azureResourceBody       The Azure resource configuration in JSON format.
     * @param responseBodyConsumer    The Consumer that handles the response body if the request is successful.
     * @param errorCallback           The Consumer that handles the error scenario if the request fails.
     */
    public void executeHttpRequest(HttpMethod method, String azureResourceIdentifier, String apiVersion,
                                   Optional<String> azureResourceBody, Consumer<String> responseBodyConsumer,
                                   Consumer<String> errorCallback)
    {
        String url = String.format("%ssubscriptions/%s%s?api-version=%s",
                azureProfile.getEnvironment().getResourceManagerEndpoint(), azureProfile.getSubscriptionId(),
                Strings.CS.prependIfMissing(azureResourceIdentifier, "/"), apiVersion);
        executeHttpRequest(method, url, azureResourceBody, responseBodyConsumer, errorCallback);
    }

    /**
     * Executes an HTTP request to the specified Azure resource using the provided parameters.
     * This method constructs and sends an HTTP request based on the given method and URL. If an optional JSON body
     * (azureResourceBody) is provided, it is included in the request. It uses the provided responseBodyConsumer
     * to handle successful responses and errorCallback to handle error scenarios.
     *
     * @param method               The HTTP method to be used for the request (e.g., GET, POST, PUT, DELETE).
     * @param azureResourceUrl     It's used to specify Azure resource uniquely. For example:
     *                             <ul>
     *                             <li><code>https://management.azure.com/subscriptions/
     *                             00000000-0000-0000-0000-000000000000/resourceGroups/sample-resource-group/
     *                             providers/Microsoft.KeyVault/vaults/sample-vault?api-version=2021-10-01</code>
     *                             <br><i>or</i>
     *                             <br>
     *                             <li><code>https://api.loganalytics.io/v1/workspaces/00000000-0000-0000-0000-000000000000/
     *                             query?query=Syslog</code>
     *                             </ul>
     * @param azureResourceBody    The Azure resource configuration in JSON format.
     * @param responseBodyConsumer The Consumer that handles the response body if the request is successful.
     * @param errorCallback        The Consumer that handles the error scenario if the request fails.
     */
    public void executeHttpRequest(HttpMethod method, String azureResourceUrl, Optional<String> azureResourceBody,
                                   Consumer<String> responseBodyConsumer, Consumer<String> errorCallback)
    {
        HttpRequest httpRequest = new HttpRequest(method, azureResourceUrl);
        azureResourceBody.ifPresent(requestBody -> {
            httpRequest.setBody(requestBody);
            httpRequest.setHeader(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON);
        });

        try (HttpResponse httpResponse = httpPipeline.send(httpRequest).block())
        {
            Optional.ofNullable(httpResponse).map(HttpResponse::getBodyAsString).map(Mono::block).ifPresentOrElse(
                    responseBody -> {
                        if (httpResponse.getStatusCode() == HttpResponseStatus.OK.code())
                        {
                            responseBodyConsumer.accept(responseBody);
                        }
                        else
                        {
                            errorCallback.accept(EXECUTION_FAILED + ": " + responseBody);
                        }
                    }, () -> errorCallback.accept(EXECUTION_FAILED + " with empty body and status code: "
                            + httpResponse.getStatusCode()
                    )
            );
        }
    }
}
