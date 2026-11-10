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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class XrayCloudClientTests
{
    private static final String BASE_URL = "https://xray.cloud.getxray.app/";
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String TOKEN = "******";
    private static final String NEW_TOKEN = "new-token";
    private static final String AUTH_PATH = "/authenticate";
    private static final String IMPORT_PATH = "/import/execution";
    private static final String GRAPHQL_PATH = "/graphql";
    private static final String XRAY_API_BASE = "https://xray.cloud.getxray.app/api/v2";
    private static final String AUTH_URL = XRAY_API_BASE + AUTH_PATH;
    private static final String IMPORT_URL = XRAY_API_BASE + IMPORT_PATH;
    private static final String GRAPHQL_URL = XRAY_API_BASE + GRAPHQL_PATH;
    private static final String DOUBLE_QUOTE = "\"";
    private static final String IMPORT_KEY = "TEST-1";
    private static final String IMPORT_RESPONSE_JSON = "{\"key\":\"" + IMPORT_KEY + "\"}";
    private static final String EXECUTION_JSON = "{\"tests\":[]}";
    private static final String TEST_SET_KEY = "TS-1";
    private static final String TEST_CASE_KEY = "TC-1";
    private static final String TEST_SET_ISSUE_ID = "uuid-set-1";
    private static final String TEST_ISSUE_ID = "uuid-test-1";
    private static final String TEST_SET_RESPONSE_TEMPLATE =
            "{\"data\":{\"getTestSets\":{\"results\":[{\"issueId\":\"%s\"}]}}}";
    private static final String SINGLE_TEST_RESPONSE_TEMPLATE =
            "{\"data\":{\"getTests\":{\"results\":[{\"issueId\":\"%s\",\"jira\":{\"key\":\"%s\"}}]}}}";
    private static final String MUTATION_RESPONSE_PREFIX = "{\"data\":{\"addTestsToTestSet\":{\"addedTests\":[\"";
    private static final String MUTATION_RESPONSE_SUFFIX = "\"],\"warning\":null}}}";

    @Mock private IHttpClient httpClient;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(XrayCloudClient.class);

    private XrayCloudClient createClient()
    {
        return new XrayCloudClient(BASE_URL, CLIENT_ID, CLIENT_SECRET, httpClient);
    }

    private HttpResponse response(int status, String body)
    {
        HttpResponse r = new HttpResponse();
        r.setStatusCode(status);
        r.setResponseBody(body.getBytes(StandardCharsets.UTF_8));
        return r;
    }

    private HttpResponse authResponse()
    {
        return response(HttpStatus.SC_OK, DOUBLE_QUOTE + TOKEN + DOUBLE_QUOTE);
    }

    @Test
    void shouldAuthenticateAndImportExecution() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse());
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(IMPORT_PATH))))
                .thenReturn(response(HttpStatus.SC_OK, IMPORT_RESPONSE_JSON));

        XrayCloudClient client = createClient();
        String key = client.importExecution(EXECUTION_JSON);

        assertEquals(IMPORT_KEY, key);
        verify(httpClient).execute(argThat(req -> req != null && requestMatchesUrl(req, AUTH_URL)));
        verify(httpClient).execute(argThat(req -> req != null
                && requestMatchesUrl(req, IMPORT_URL)
                && requestHasBearerToken(req, TOKEN)));
    }

    @Test
    void shouldReuseTokenOnSecondCall() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse());
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(IMPORT_PATH))))
                .thenReturn(response(HttpStatus.SC_OK, IMPORT_RESPONSE_JSON));

        XrayCloudClient client = createClient();
        client.importExecution(EXECUTION_JSON);
        client.importExecution(EXECUTION_JSON);

        // authenticate should be called only once
        verify(httpClient, times(1)).execute(argThat(req -> req != null && requestMatchesUrl(req, AUTH_URL)));
        verify(httpClient, times(2)).execute(argThat(req -> req != null
                && requestMatchesUrl(req, IMPORT_URL)
                && requestHasBearerToken(req, TOKEN)));
    }

    @Test
    void shouldRetryOnUnauthorizedResponse() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse())
                .thenReturn(response(HttpStatus.SC_OK, DOUBLE_QUOTE + NEW_TOKEN + DOUBLE_QUOTE));
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(IMPORT_PATH))))
                .thenReturn(response(HttpStatus.SC_UNAUTHORIZED, ""))
                .thenReturn(response(HttpStatus.SC_OK, IMPORT_RESPONSE_JSON));

        XrayCloudClient client = createClient();
        String key = client.importExecution(EXECUTION_JSON);

        assertEquals(IMPORT_KEY, key);
        verify(httpClient, times(2)).execute(argThat(req -> req != null && requestMatchesUrl(req, AUTH_URL)));
        verify(httpClient, times(2)).execute(argThat(req -> req != null && requestMatchesUrl(req, IMPORT_URL)));
        verify(httpClient, times(1)).execute(argThat(req -> req != null
                && requestMatchesUrl(req, IMPORT_URL)
                && requestHasBearerToken(req, TOKEN)));
        verify(httpClient, times(1)).execute(argThat(req -> req != null
                && requestMatchesUrl(req, IMPORT_URL)
                && requestHasBearerToken(req, NEW_TOKEN)));
    }

    @Test
    void shouldThrowIoExceptionOnAuthenticationFailure() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(response(HttpStatus.SC_FORBIDDEN, "Forbidden"));

        XrayCloudClient client = createClient();
        IOException thrown = assertThrows(IOException.class, () -> client.importExecution(EXECUTION_JSON));

        assertTrue(thrown.getMessage().contains("authentication failed"));
    }

    @Test
    void shouldThrowIoExceptionOnNon2xxResponse() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse());
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(IMPORT_PATH))))
                .thenReturn(response(HttpStatus.SC_BAD_REQUEST, "bad request"));

        XrayCloudClient client = createClient();
        IOException thrown = assertThrows(IOException.class, () -> client.importExecution(EXECUTION_JSON));

        assertTrue(thrown.getMessage().contains(String.valueOf(HttpStatus.SC_BAD_REQUEST)));
    }

    @Test
    void shouldThrowIoExceptionOnInformationalResponse() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse());
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(IMPORT_PATH))))
                .thenReturn(response(HttpStatus.SC_CONTINUE, "continue"));

        XrayCloudClient client = createClient();
        IOException thrown = assertThrows(IOException.class, () -> client.importExecution(EXECUTION_JSON));

        assertTrue(thrown.getMessage().contains(String.valueOf(HttpStatus.SC_CONTINUE)));
    }

    @Test
    void shouldAddTestsToTestSet() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse());

        String testIssueId2 = "uuid-test-2";
        String testKey2 = "TC-2";

        // First GraphQL: resolve test set issueId
        String testSetResponse = String.format(TEST_SET_RESPONSE_TEMPLATE, TEST_SET_ISSUE_ID);
        // Second GraphQL: resolve test case issueIds
        String testsResponse = String.format(
                "{\"data\":{\"getTests\":{\"results\":["
                + "{\"issueId\":\"%s\",\"jira\":{\"key\":\"%s\"}},"
                + "{\"issueId\":\"%s\",\"jira\":{\"key\":\"%s\"}}"
                + "]}}}", TEST_ISSUE_ID, TEST_CASE_KEY, testIssueId2, testKey2);
        // Third GraphQL: mutation
        String mutationResponse = MUTATION_RESPONSE_PREFIX + TEST_ISSUE_ID
                + "\",\"" + testIssueId2 + MUTATION_RESPONSE_SUFFIX;

        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(GRAPHQL_PATH))))
                .thenReturn(response(HttpStatus.SC_OK, testSetResponse))
                .thenReturn(response(HttpStatus.SC_OK, testsResponse))
                .thenReturn(response(HttpStatus.SC_OK, mutationResponse));

        XrayCloudClient client = createClient();
        client.addTestsToTestSet(TEST_SET_KEY, List.of(TEST_CASE_KEY, testKey2));

        verify(httpClient, times(3)).execute(argThat(req -> req != null && requestMatchesUrl(req, GRAPHQL_URL)));
        verify(httpClient, times(3)).execute(argThat(req -> req != null
                && requestMatchesUrl(req, GRAPHQL_URL)
                && requestHasBearerToken(req, TOKEN)));
    }

    @Test
    void shouldLogWarningWhenAddTestsToTestSetReturnsWarning() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse());

        String testSetResponse = String.format(TEST_SET_RESPONSE_TEMPLATE, TEST_SET_ISSUE_ID);
        String testsResponse = String.format(SINGLE_TEST_RESPONSE_TEMPLATE, TEST_ISSUE_ID, TEST_CASE_KEY);
        String mutationResponse = "{\"data\":{\"addTestsToTestSet\":{\"addedTests\":[],"
                + "\"warning\":\"Some tests already belong to the test set\"}}}";

        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(GRAPHQL_PATH))))
                .thenReturn(response(HttpStatus.SC_OK, testSetResponse))
                .thenReturn(response(HttpStatus.SC_OK, testsResponse))
                .thenReturn(response(HttpStatus.SC_OK, mutationResponse));

        createClient().addTestsToTestSet(TEST_SET_KEY, List.of(TEST_CASE_KEY));

        assertThat(logger.getLoggingEvents(), hasItem(
                LoggingEvent.warn("Warning received from Xray Cloud while adding tests to test set: {}",
                        "Some tests already belong to the test set")));
    }

    @Test
    void shouldThrowExceptionWhenTestKeyNotFoundInGraphQLResponse() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse());

        String testSetResponse = String.format(TEST_SET_RESPONSE_TEMPLATE, TEST_SET_ISSUE_ID);
        String testsResponse = "{\"data\":{\"getTests\":{\"results\":[{\"issueId\":\"uuid-test-2\","
                + "\"jira\":{\"key\":\"TC-2\"}}]}}}";

        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(GRAPHQL_PATH))))
                .thenReturn(response(HttpStatus.SC_OK, testSetResponse))
                .thenReturn(response(HttpStatus.SC_OK, testsResponse));

        XrayCloudClient client = createClient();
        List<String> keys = List.of(TEST_CASE_KEY);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> client.addTestsToTestSet(TEST_SET_KEY, keys));

        assertTrue(thrown.getMessage().contains(TEST_CASE_KEY));
    }

    @Test
    void shouldRetryExecuteGraphQLOnUnauthorizedResponse() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(AUTH_PATH))))
                .thenReturn(authResponse())
                .thenReturn(response(HttpStatus.SC_OK, DOUBLE_QUOTE + NEW_TOKEN + DOUBLE_QUOTE));

        String testSetResponse = String.format(TEST_SET_RESPONSE_TEMPLATE, TEST_SET_ISSUE_ID);
        String testsResponse = String.format(SINGLE_TEST_RESPONSE_TEMPLATE, TEST_ISSUE_ID, TEST_CASE_KEY);
        String mutationResponse = MUTATION_RESPONSE_PREFIX + TEST_ISSUE_ID + MUTATION_RESPONSE_SUFFIX;

        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith(GRAPHQL_PATH))))
                .thenReturn(response(HttpStatus.SC_UNAUTHORIZED, ""))
                .thenReturn(response(HttpStatus.SC_OK, testSetResponse))
                .thenReturn(response(HttpStatus.SC_OK, testsResponse))
                .thenReturn(response(HttpStatus.SC_OK, mutationResponse));

        createClient().addTestsToTestSet(TEST_SET_KEY, List.of(TEST_CASE_KEY));

        verify(httpClient, times(2)).execute(argThat(req -> req != null && requestMatchesUrl(req, AUTH_URL)));
        verify(httpClient, times(4)).execute(argThat(req -> req != null && requestMatchesUrl(req, GRAPHQL_URL)));
        verify(httpClient, times(1)).execute(argThat(req -> req != null
                && requestMatchesUrl(req, GRAPHQL_URL)
                && requestHasBearerToken(req, TOKEN)));
        verify(httpClient, times(3)).execute(argThat(req -> req != null
                && requestMatchesUrl(req, GRAPHQL_URL)
                && requestHasBearerToken(req, NEW_TOKEN)));
    }

    @Test
    void shouldThrowIoExceptionWhenAuthRequestBuildFails()
    {
        XrayCloudClient client = new XrayCloudClient("http://host:invalid_port", CLIENT_ID, CLIENT_SECRET, httpClient);
        assertThrows(IOException.class, () -> client.importExecution(EXECUTION_JSON));
    }

    private static boolean requestMatchesUrl(ClassicHttpRequest request, String url)
    {
        try
        {
            return url.equals(request.getUri().toString());
        }
        catch (URISyntaxException e)
        {
            return false;
        }
    }

    private static boolean requestHasBearerToken(ClassicHttpRequest request, String token)
    {
        var header = request.getFirstHeader("Authorization");
        return header != null && ("Bearer " + token).equals(header.getValue());
    }
}
