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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

@ExtendWith(MockitoExtension.class)
class XrayCloudClientTests
{
    private static final String BASE_URL = "https://xray.cloud.getxray.app/api/v2";
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String TOKEN = "******";
    private static final String AUTH_URL = BASE_URL + "/authenticate";
    private static final String IMPORT_URL = BASE_URL + "/import/execution";
    private static final String GRAPHQL_URL = BASE_URL + "/graphql";

    @Mock private IHttpClient httpClient;

    private XrayCloudClient createClient()
    {
        return new XrayCloudClient(BASE_URL, CLIENT_ID, CLIENT_SECRET, httpClient);
    }

    private HttpResponse response(int status, String body)
    {
        HttpResponse r = new HttpResponse();
        r.setStatusCode(status);
        r.setResponseBody(body.getBytes());
        return r;
    }

    private HttpResponse authResponse()
    {
        return response(HttpStatus.SC_OK, "\"" + TOKEN + "\"");
    }

    @Test
    void shouldAuthenticateAndImportExecution() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/authenticate"))))
                .thenReturn(authResponse());
        String importResponseBody = "{\"key\":\"TEST-1\"}";
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/import/execution"))))
                .thenReturn(response(HttpStatus.SC_OK, importResponseBody));

        XrayCloudClient client = createClient();
        String key = client.importExecution("{\"tests\":[]}");

        assertEquals("TEST-1", key);
        verify(httpClient).execute(argThat(req -> req != null && requestMatchesUrl(req, AUTH_URL)));
        verify(httpClient).execute(argThat(req -> req != null && requestMatchesUrl(req, IMPORT_URL)));
    }

    @Test
    void shouldReuseTokenOnSecondCall() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/authenticate"))))
                .thenReturn(authResponse());
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/import/execution"))))
                .thenReturn(response(HttpStatus.SC_OK, "{\"key\":\"TEST-1\"}"));

        XrayCloudClient client = createClient();
        client.importExecution("{\"tests\":[]}");
        client.importExecution("{\"tests\":[]}");

        // authenticate should be called only once
        verify(httpClient, times(1)).execute(argThat(req -> req != null && requestMatchesUrl(req, AUTH_URL)));
        verify(httpClient, times(2)).execute(argThat(req -> req != null && requestMatchesUrl(req, IMPORT_URL)));
    }

    @Test
    void shouldRetryOnUnauthorizedResponse() throws IOException
    {
        String newToken = "new-token";
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/authenticate"))))
                .thenReturn(authResponse())
                .thenReturn(response(HttpStatus.SC_OK, "\"" + newToken + "\""));
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/import/execution"))))
                .thenReturn(response(HttpStatus.SC_UNAUTHORIZED, ""))
                .thenReturn(response(HttpStatus.SC_OK, "{\"key\":\"TEST-1\"}"));

        XrayCloudClient client = createClient();
        String key = client.importExecution("{\"tests\":[]}");

        assertEquals("TEST-1", key);
        verify(httpClient, times(2)).execute(argThat(req -> req != null && requestMatchesUrl(req, AUTH_URL)));
        verify(httpClient, times(2)).execute(argThat(req -> req != null && requestMatchesUrl(req, IMPORT_URL)));
    }

    @Test
    void shouldThrowIoExceptionOnAuthenticationFailure() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/authenticate"))))
                .thenReturn(response(HttpStatus.SC_FORBIDDEN, "Forbidden"));

        XrayCloudClient client = createClient();
        IOException thrown = assertThrows(IOException.class, () -> client.importExecution("{\"tests\":[]}"));

        assertEquals(true, thrown.getMessage().contains("authentication failed"));
    }

    @Test
    void shouldThrowIoExceptionOnNon2xxResponse() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/authenticate"))))
                .thenReturn(authResponse());
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/import/execution"))))
                .thenReturn(response(HttpStatus.SC_BAD_REQUEST, "bad request"));

        XrayCloudClient client = createClient();
        IOException thrown = assertThrows(IOException.class, () -> client.importExecution("{\"tests\":[]}"));

        assertEquals(true, thrown.getMessage().contains(String.valueOf(HttpStatus.SC_BAD_REQUEST)));
    }

    @Test
    void shouldAddTestsToTestSet() throws IOException
    {
        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/authenticate"))))
                .thenReturn(authResponse());

        String testSetIssueId = "uuid-set-1";
        String testIssueId1 = "uuid-test-1";
        String testIssueId2 = "uuid-test-2";
        String testSetKey = "TS-1";
        String testKey1 = "TC-1";
        String testKey2 = "TC-2";

        // First GraphQL: resolve test set issueId
        String testSetResponse = String.format(
                "{\"data\":{\"getTestSets\":{\"results\":[{\"issueId\":\"%s\"}]}}}", testSetIssueId);
        // Second GraphQL: resolve test case issueIds
        String testsResponse = String.format(
                "{\"data\":{\"getTests\":{\"results\":["
                + "{\"issueId\":\"%s\",\"jira\":{\"key\":\"%s\"}},"
                + "{\"issueId\":\"%s\",\"jira\":{\"key\":\"%s\"}}"
                + "]}}}", testIssueId1, testKey1, testIssueId2, testKey2);
        // Third GraphQL: mutation
        String mutationResponse = "{\"data\":{\"addTestsToTestSet\":{\"addedTests\":[\"" + testIssueId1
                + "\",\"" + testIssueId2 + "\"],\"warning\":null}}}";

        when(httpClient.execute(argThat(req -> req != null && req.getPath().endsWith("/graphql"))))
                .thenReturn(response(HttpStatus.SC_OK, testSetResponse))
                .thenReturn(response(HttpStatus.SC_OK, testsResponse))
                .thenReturn(response(HttpStatus.SC_OK, mutationResponse));

        XrayCloudClient client = createClient();
        client.addTestsToTestSet(testSetKey, List.of(testKey1, testKey2));

        verify(httpClient, times(3)).execute(argThat(req -> req != null && requestMatchesUrl(req, GRAPHQL_URL)));
    }

    private static boolean requestMatchesUrl(ClassicHttpRequest request, String url)
    {
        try
        {
            return url.equals(request.getUri().toString());
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
