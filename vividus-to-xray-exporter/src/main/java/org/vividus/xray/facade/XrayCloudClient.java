/*
 * Copyright 2019-2026 the original author or authors.
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
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.json.JsonPathUtils;

public class XrayCloudClient implements XrayClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XrayCloudClient.class);
    private static final String AUTHORIZATION = "Authorization";
    private static final String XRAY_CLOUD_API = "Xray Cloud API";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AUTHENTICATE_PATH = "/authenticate";
    private static final String IMPORT_EXECUTION_PATH = "/import/execution";
    private static final String GRAPHQL_PATH = "/graphql";
    private static final String SLASH = "/";
    private static final String DATA = "data";
    private static final String BACKSLASH = "\\";
    private static final String COMMA_SEPARATOR = ", ";
    private static final String DOUBLE_QUOTE = "\"";

    private final String apiBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final IHttpClient httpClient;
    private String cachedToken;

    public XrayCloudClient(String apiBaseUrl, String clientId, String clientSecret, IHttpClient httpClient)
    {
        this.apiBaseUrl = Strings.CS.appendIfMissing(apiBaseUrl, SLASH) + "api/v2";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = httpClient;
    }

    @Override
    public String importExecution(String executionJson) throws IOException
    {
        HttpResponse response = postWithRetry(apiBaseUrl + IMPORT_EXECUTION_PATH, executionJson);
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
        String jql = String.join(COMMA_SEPARATOR, testCaseKeys);
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
        }).toList();
    }

    private void addTestsToTestSetById(String testSetIssueId, List<String> testIssueIds) throws IOException
    {
        String testIdsParam = testIssueIds.stream()
                .map(id -> DOUBLE_QUOTE + id + DOUBLE_QUOTE)
                .collect(Collectors.joining(COMMA_SEPARATOR));
        String mutation = String.format(
                "mutation { addTestsToTestSet(issueId: \"%s\", testIssueIds: [%s]) { addedTests warning } }",
                testSetIssueId, testIdsParam);
        String response = executeGraphQL(mutation);
        String warning = JsonPathUtils.getData(response, "$.data.addTestsToTestSet.warning");
        if (warning != null)
        {
            LOGGER.atWarn().addArgument(warning)
                  .log("Warning received from Xray Cloud while adding tests to test set: {}");
        }
    }

    @Override
    public void addTestToRepository(String testCaseKey, String path) throws IOException
    {
        String jql = "issueKey = " + DOUBLE_QUOTE + escapeJql(testCaseKey) + DOUBLE_QUOTE;
        String query = String.format(
                "{ getTests(jql: %s, limit: 1) { results { issueId jira(fields: [\"project\"]) } } }", quote(jql));
        String response = executeGraphQL(query);
        JsonNode test = OBJECT_MAPPER.readTree(response).path(DATA).path("getTests").path("results").path(0);
        String testIssueId = test.path("issueId").asText(null);
        String projectId = test.path("jira").path("project").path("id").asText(null);
        if (testIssueId == null || projectId == null)
        {
            throw new IOException("Could not resolve Xray project and issue IDs for test case: " + testCaseKey);
        }

        String folderPath = SLASH + path.replaceAll("^/+|/+$", "");
        addTestToFolder(path, folderPath, testIssueId, projectId);
    }

    private void addTestToFolder(String path, String folderPath, String testIssueId, String projectId)
            throws IOException
    {
        String mutation = String.format(
                "mutation { addTestsToFolder(projectId: %s, path: %s, testIssueIds: [%s]) "
                        + "{ folder { path } warnings } }",
                quote(projectId), quote(folderPath), quote(testIssueId));
        JsonNode folderResponse = OBJECT_MAPPER.readTree(executeGraphQL(mutation));
        throwIfGraphQLErrors(folderResponse, path);
        logFolderWarnings(folderResponse);
    }

    private static void throwIfGraphQLErrors(JsonNode response, String path) throws IOException
    {
        JsonNode errors = response.path("errors");
        if (errors.isArray() && !errors.isEmpty())
        {
            String error = errors.path(0).path("message").asText();
            throw new IOException("Failed to add test case to Xray Test Repository folder '" + path + "': " + error);
        }
    }

    private static void logFolderWarnings(JsonNode response)
    {
        JsonNode warnings = response.path(DATA).path("addTestsToFolder").path("warnings");
        String warning = warnings.isArray() && !warnings.isEmpty() ? warnings.path(0).asText()
                : warnings.isTextual() ? warnings.asText() : null;
        if (warning != null)
        {
            LOGGER.atWarn().addArgument(warning)
                  .log("Warning received from Xray Cloud while adding test to Test Repository folder: {}");
        }
    }

    private static String quote(String value) throws IOException
    {
        return OBJECT_MAPPER.writeValueAsString(value);
    }

    private static String escapeJql(String value)
    {
        return value.replace(BACKSLASH, BACKSLASH + BACKSLASH)
                .replace(DOUBLE_QUOTE, BACKSLASH + DOUBLE_QUOTE);
    }

    private String executeGraphQL(String query) throws IOException
    {
        String bodyJson = OBJECT_MAPPER.createObjectNode().put("query", query).toString();
        HttpResponse response = postWithRetry(apiBaseUrl + GRAPHQL_PATH, bodyJson);
        ensureSuccessful(response);
        return response.getResponseBodyAsString();
    }

    private HttpResponse postWithRetry(String url, String body) throws IOException
    {
        HttpResponse response = post(url, body, getToken());
        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
        {
            invalidateToken();
            response = post(url, body, getToken());
        }
        return response;
    }

    private HttpResponse post(String url, String body, String token) throws IOException
    {
        return httpClient.execute(HttpRequestBuilder.create()
                .withHttpMethod(HttpMethod.POST)
                .withEndpoint(url)
                .withContent(body, ContentType.APPLICATION_JSON)
                .withHeaders(List.of(new BasicHeader(AUTHORIZATION, "Bearer " + token)))
                .build());
    }

    private String getToken() throws IOException
    {
        if (cachedToken == null)
        {
            cachedToken = authenticate();
        }
        return cachedToken;
    }

    private void invalidateToken()
    {
        cachedToken = null;
    }

    private String authenticate() throws IOException
    {
        String body = OBJECT_MAPPER.createObjectNode()
                .put("client_id", clientId)
                .put("client_secret", clientSecret)
                .toString();

        HttpResponse response = httpClient.execute(HttpRequestBuilder.create()
                .withHttpMethod(HttpMethod.POST)
                .withEndpoint(apiBaseUrl + AUTHENTICATE_PATH)
                .withContent(body, ContentType.APPLICATION_JSON)
                .build());
        if (response.getStatusCode() != HttpStatus.SC_OK)
        {
            throw new IOException(String.format("Xray Cloud authentication failed with status %d: %s",
                    response.getStatusCode(), response.getResponseBodyAsString()));
        }
        // Response is a JSON string like "eyJ..."
        return OBJECT_MAPPER.readValue(response.getResponseBodyAsString(), String.class);
    }

    private void ensureSuccessful(HttpResponse response) throws IOException
    {
        int status = response.getStatusCode();
        if (status < HttpStatus.SC_OK || status >= HttpStatus.SC_MULTIPLE_CHOICES)
        {
            LOGGER.atError().addArgument(XRAY_CLOUD_API).addArgument(response).log("{} response: {}");
            throw new IOException(String.format("Xray Cloud API responded with unexpected status %d: %s",
                    status, response.getResponseBodyAsString()));
        }
    }
}
