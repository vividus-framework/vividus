/*
 * Copyright 2019-2023 the original author or authors.
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
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.client.model.Entity;
import org.vividus.azure.devops.client.model.PointsQuery;
import org.vividus.azure.devops.client.model.TestPoint;
import org.vividus.azure.devops.client.model.TestPointContainer;
import org.vividus.azure.devops.client.model.TestResult;
import org.vividus.azure.devops.client.model.TestRun;
import org.vividus.azure.devops.client.model.WorkItem;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.json.JsonUtils;

public class AzureDevOpsClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureDevOpsClient.class);

    private static final String API_VERSION = "?api-version=7.1-preview.2";

    private final IHttpClient httpClient;
    private final JsonUtils jsonUtils;
    private final String endpoint;
    private final String basePath;
    private final String workItemsBasePath;

    public AzureDevOpsClient(IHttpClient httpClient, AzureDevOpsExporterOptions options)
    {
        this.httpClient = httpClient;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.findAndRegisterModules();
        this.jsonUtils = new JsonUtils(mapper);
        this.endpoint = "https://dev.azure.com/";
        this.basePath = String.format("/%s/%s/_apis/", options.getOrganization(), options.getProject());
        this.workItemsBasePath = basePath + "wit/workitems/";
    }

    public WorkItem createTestCase(List<AddOperation> operations) throws IOException
    {
        return executeOnWorkItem(HttpMethod.POST, "$test case", operations);
    }

    public WorkItem getWorkItem(Integer workItemId) throws IOException
    {
        return asWorkItem(execute(HttpMethod.GET, workItemsBasePath + workItemId, null));
    }

    public List<TestPoint> queryTestPoints(Set<Integer> testCaseIds) throws IOException
    {
        PointsQuery query = new PointsQuery(testCaseIds);
        String response = execute(HttpMethod.POST, basePath + "test/points", ContentType.APPLICATION_JSON, query);
        return jsonUtils.toObject(response, TestPointContainer.class).getPoints();
    }

    public void updateTestCase(Integer testCaseId, List<AddOperation> operations) throws IOException
    {
        executeOnWorkItem(HttpMethod.PATCH, testCaseId.toString(), operations);
    }

    public Entity createTestRun(TestRun testRun) throws IOException
    {
        String testRunResponse = execute(HttpMethod.POST, basePath + "test/runs", ContentType.APPLICATION_JSON,
                testRun);
        return jsonUtils.toObject(testRunResponse, Entity.class);
    }

    public void addTestResults(int runId, List<TestResult> testResults) throws IOException
    {
        execute(HttpMethod.POST, basePath + "test/Runs/" + runId + "/results", ContentType.APPLICATION_JSON,
                testResults);
    }

    private WorkItem executeOnWorkItem(HttpMethod method, String urlPart, List<AddOperation> operations)
            throws IOException
    {
        String response = execute(method, workItemsBasePath + urlPart,
                ContentType.create("application/json-patch+json", StandardCharsets.UTF_8), operations);
        return asWorkItem(response);
    }

    private WorkItem asWorkItem(String data)
    {
        return jsonUtils.toObject(data, WorkItem.class);
    }

    private String execute(HttpMethod method, String relativeUrl, ContentType contentType, Object payloadObject)
            throws IOException
    {
        String payload = jsonUtils.toJson(payloadObject);
        LOGGER.atInfo().addArgument(payload).log("Azure DevOps request: {}");

        return execute(method, relativeUrl, new StringEntity(payload, contentType));
    }

    private String execute(HttpMethod method, String relativeUrl, HttpEntity payload) throws IOException
    {
        ClassicHttpRequest httpRequest = HttpRequestBuilder.create()
                .withHttpMethod(method)
                .withEndpoint(endpoint)
                .withRelativeUrl(relativeUrl + API_VERSION)
                .withContent(payload)
                .build();

        return httpClient.execute(httpRequest).getResponseBodyAsString();
    }
}
