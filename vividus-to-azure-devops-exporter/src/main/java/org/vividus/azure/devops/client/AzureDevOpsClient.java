/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.azure.devops.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.IHttpClient;

public class AzureDevOpsClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureDevOpsClient.class);

    private static final String API_VERSION = "?api-version=6.1-preview.3";

    private final IHttpClient httpClient;
    private final String endpoint;
    private final ObjectMapper mapper;
    private final String basePath;

    public AzureDevOpsClient(IHttpClient httpClient, AzureDevOpsExporterOptions options)
    {
        this.httpClient = httpClient;
        this.endpoint = "https://dev.azure.com/";
        this.mapper = new ObjectMapper();
        this.basePath = String.format("/%s/%s/_apis/wit/workitems", options.getOrganization(), options.getProject());
    }

    public String createTestCase(List<AddOperation> operations) throws IOException
    {
        return execute(HttpMethod.POST, basePath + "/$test case" + API_VERSION, operations);
    }

    public void updateTestCase(String testCaseId, List<AddOperation> operations) throws IOException
    {
        execute(HttpMethod.PATCH, basePath + '/' + testCaseId + API_VERSION, operations);
    }

    private String execute(HttpMethod method, String relativeUrl, List<AddOperation> operations) throws IOException
    {
        String payload = mapper.writeValueAsString(operations);
        LOGGER.atInfo().addArgument(payload).log("Azure DevOps request: {}");

        HttpRequestBase httpRequest = HttpRequestBuilder.create()
                .withHttpMethod(method)
                .withEndpoint(endpoint)
                .withRelativeUrl(relativeUrl)
                .withContent(payload, ContentType.create("application/json-patch+json", StandardCharsets.UTF_8))
                .build();

        return httpClient.execute(httpRequest).getResponseBodyAsString();
    }
}
