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

package org.vividus.xray.facade;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.exception.HttpRequestBuildException;
import org.vividus.util.json.JsonPathUtils;

public class XrayCloudClient implements XrayClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XrayCloudClient.class);
    private static final String AUTHORIZATION = "Authorization";
    private static final String XRAY_CLOUD_API = "Xray Cloud API";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String apiBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final IHttpClient httpClient;
    private final AtomicReference<String> cachedToken = new AtomicReference<>();

    public XrayCloudClient(String apiBaseUrl, String clientId, String clientSecret, IHttpClient httpClient)
    {
        this.apiBaseUrl = apiBaseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = httpClient;
    }

    @Override
    public String importExecution(String executionJson) throws IOException
    {
        String token = getToken();
        HttpResponse response = post(apiBaseUrl + "/import/execution", executionJson, token);
        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
        {
            invalidateToken(token);
            response = post(apiBaseUrl + "/import/execution", executionJson, getToken());
        }
        ensureSuccessful(response);
        return JsonPathUtils.getData(response.getResponseBodyAsString(), "$.key");
    }

    @Override
    public void addTestsToTestSet(String testSetKey, List<String> testCaseKeys) throws IOException
    {
        LOGGER.atDebug().addArgument(testCaseKeys).addArgument(testSetKey)
              .log("Resolving Xray IDs for tests {} and test set {}");
        String testSetIssueId = resolveTestSetIssueId(testSetKey);
        List<String> testIssueIds = resolveTestIssueIds(testCaseKeys);
        addTestsToTestSetById(testSetIssueId, testIssueIds);
    }

    private String resolveTestSetIssueId(String testSetKey) throws IOException
    {
        String query = String.format("{ getTestSets(jql: \"issueKey = %s\", limit: 1) { results { issueId } } }",
                testSetKey);
        String response = executeGraphQL(query);
        return JsonPathUtils.getData(response, "$.data.getTestSets.results[0].issueId");
    }

    private List<String> resolveTestIssueIds(List<String> testCaseKeys) throws IOException
    {
        String jql = String.join(", ", testCaseKeys);
        String query = String.format(
                "{ getTests(jql: \"issueKey in (%s)\", limit: %d) { results { issueId jira(fields: [\"key\"]) } } }",
                jql, testCaseKeys.size());
        String response = executeGraphQL(query);

        List<String> issueIds = JsonPathUtils.getData(response, "$.data.getTests.results[*].issueId");
        List<String> keys = JsonPathUtils.getData(response, "$.data.getTests.results[*].jira.key");

        Map<String, String> keyToIssueId = new HashMap<>();
        for (int i = 0; i < keys.size(); i++)
        {
            keyToIssueId.put(keys.get(i), issueIds.get(i));
        }

        return testCaseKeys.stream().map(key ->
        {
            String issueId = keyToIssueId.get(key);
            if (issueId == null)
            {
                throw new IllegalArgumentException("Could not find Xray issue ID for test case key: " + key);
            }
            return issueId;
        }).collect(Collectors.toList());
    }

    private void addTestsToTestSetById(String testSetIssueId, List<String> testIssueIds) throws IOException
    {
        String testIdsParam = testIssueIds.stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(", "));
        String mutation = String.format(
                "mutation { addTestsToTestSet(issueId: \"%s\", testIssueIds: [%s]) { addedTests warning } }",
                testSetIssueId, testIdsParam);
        String response = executeGraphQL(mutation);
        String warning = JsonPathUtils.getData(response, "$.data.addTestsToTestSet.warning");
        if (warning != null)
        {
            LOGGER.atWarn().addArgument(warning).log("Xray Cloud addTestsToTestSet warning: {}");
        }
    }

    private String executeGraphQL(String query) throws IOException
    {
        String token = getToken();
        String bodyJson = OBJECT_MAPPER.createObjectNode().put("query", query).toString();
        HttpResponse response = post(apiBaseUrl + "/graphql", bodyJson, token);
        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
        {
            invalidateToken(token);
            response = post(apiBaseUrl + "/graphql", bodyJson, getToken());
        }
        ensureSuccessful(response);
        return response.getResponseBodyAsString();
    }

    private HttpResponse post(String url, String body, String token) throws IOException
    {
        try
        {
            return httpClient.execute(HttpRequestBuilder.create()
                    .withHttpMethod(HttpMethod.POST)
                    .withEndpoint(url)
                    .withContent(body, ContentType.APPLICATION_JSON)
                    .withHeaders(List.of(new BasicHeader(AUTHORIZATION, "Bearer " + token)))
                    .build());
        }
        catch (HttpRequestBuildException e)
        {
            throw new IOException(e);
        }
    }

    private String getToken() throws IOException
    {
        String token = cachedToken.get();
        if (token == null)
        {
            String newToken = authenticate();
            if (!cachedToken.compareAndSet(null, newToken))
            {
                return cachedToken.get();
            }
            return newToken;
        }
        return token;
    }

    private void invalidateToken(String knownStaleToken)
    {
        cachedToken.compareAndSet(knownStaleToken, null);
    }

    private String authenticate() throws IOException
    {
        String body = OBJECT_MAPPER.createObjectNode()
                .put("client_id", clientId)
                .put("client_secret", clientSecret)
                .toString();
        try
        {
            HttpResponse response = httpClient.execute(HttpRequestBuilder.create()
                    .withHttpMethod(HttpMethod.POST)
                    .withEndpoint(apiBaseUrl + "/authenticate")
                    .withContent(body, ContentType.APPLICATION_JSON)
                    .build());
            if (response.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new IOException("Xray Cloud authentication failed with status " + response.getStatusCode()
                        + ": " + response.getResponseBodyAsString());
            }
            String tokenJson = response.getResponseBodyAsString();
            // Response is a JSON string like "eyJ..." — strip surrounding quotes
            return tokenJson.substring(1, tokenJson.length() - 1);
        }
        catch (HttpRequestBuildException e)
        {
            throw new IOException(e);
        }
    }

    private void ensureSuccessful(HttpResponse response) throws IOException
    {
        int status = response.getStatusCode();
        if (status < HttpStatus.SC_OK || status >= HttpStatus.SC_MULTIPLE_CHOICES)
        {
            LOGGER.atError().addArgument(XRAY_CLOUD_API).addArgument(response).log("{} response: {}");
            throw new IOException("Xray Cloud API responded with unexpected status " + status + ": "
                    + response.getResponseBodyAsString());
        }
    }
}
