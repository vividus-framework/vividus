/*
 * Copyright 2019-2024 the original author or authors.
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

import org.apache.commons.lang3.StringUtils;
import org.vividus.softassert.ISoftAssert;

import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;

public class AzureHttpClient
{
    private static final String EXECUTION_FAILED = "Azure REST API HTTP request execution is failed";

    private final HttpPipeline httpPipeline;
    private final AzureProfile azureProfile;
    private final ISoftAssert softAssert;

    public AzureHttpClient(AzureProfile azureProfile, TokenCredential tokenCredential, ISoftAssert softAssert)
    {
        this.azureProfile = azureProfile;
        this.httpPipeline = HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile);
        this.softAssert = softAssert;
    }

    public void executeHttpRequest(HttpMethod method, String azureResourceIdentifier, String apiVersion,
                                   Optional<String> azureResourceBody, Consumer<String> responseBodyConsumer)
    {
        String url = String.format("%ssubscriptions/%s%s?api-version=%s",
                azureProfile.getEnvironment().getResourceManagerEndpoint(), azureProfile.getSubscriptionId(),
                StringUtils.prependIfMissing(azureResourceIdentifier, "/"), apiVersion);
        executeHttpRequest(method, url, azureResourceBody, responseBodyConsumer);
    }

    public void executeHttpRequest(HttpMethod method, String azureResourceUrl, Optional<String> azureResourceBody,
                                   Consumer<String> responseBodyConsumer)
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
                            softAssert.recordFailedAssertion(EXECUTION_FAILED + ": " + responseBody);
                        }
                    }, () -> softAssert.recordFailedAssertion(EXECUTION_FAILED + " with empty body and status code: "
                            + httpResponse.getStatusCode()
                    )
            );
        }
    }
}
