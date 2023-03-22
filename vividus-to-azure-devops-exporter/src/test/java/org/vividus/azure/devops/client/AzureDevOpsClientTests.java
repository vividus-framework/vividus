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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.client.model.ShallowReference;
import org.vividus.azure.devops.client.model.TestResult;
import org.vividus.azure.devops.client.model.TestRun;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.http.HttpMethod;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AzureDevOpsClientTests
{
    private static final String PATH = "path-";
    private static final String VALUE = "value-";
    private static final String BASE_URL = "https://dev.azure.com/organization/project/_apis";
    private static final String WORKITEMS_BASE_URL = BASE_URL + "/wit/workitems";
    private static final String PAYLOAD_JSON = "[{\"op\":\"add\",\"path\":\"path-1\",\"value\":\"value-1\"},"
            + "{\"op\":\"add\",\"path\":\"path-2\",\"value\":\"value-2\"},"
            + "{\"op\":\"add\",\"path\":\"path-3\",\"value\":\"value-3\"}]";
    private static final String PAYLOAD_LOG = "Azure DevOps request: {}";
    private static final String VERSION = "?api-version=7.1-preview.2";
    private static final Integer TEST_CASE_ID = 1;
    private static final String WORK_ITEM_RESPONSE = "{\"id\": 1, \"rev\": 1}";
    private static final String ENTITY_RESPONSE = "{\"id\": 1}";

    @Captor private ArgumentCaptor<ClassicHttpRequest> requestCaptor;
    @Mock private HttpResponse httpResponse;
    @Mock private IHttpClient httpClient;
    private AzureDevOpsClient client;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AzureDevOpsClient.class);

    @BeforeEach
    void init()
    {
        var options = new AzureDevOpsExporterOptions();
        options.setOrganization("organization");
        options.setProject("project");
        client = new AzureDevOpsClient(httpClient, options);
    }

    @Test
    void shouldCreateTestCase() throws IOException, URISyntaxException
    {
        when(httpResponse.getResponseBodyAsString()).thenReturn(WORK_ITEM_RESPONSE);
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        var workItem = client.createTestCase(List.of(
            new AddOperation(PATH + 1, VALUE + 1),
            new AddOperation(PATH + 2, VALUE + 2),
            new AddOperation(PATH + 3, VALUE + 3)
        ));

        assertEquals(1, workItem.getId());
        assertEquals(1, workItem.getRev());
        validateRequestWithPayload(HttpMethod.POST, WORKITEMS_BASE_URL + "/$test%20case" + VERSION, PAYLOAD_JSON);
    }

    @Test
    void shouldUpdateTestCase() throws IOException, URISyntaxException
    {
        when(httpResponse.getResponseBodyAsString()).thenReturn(WORK_ITEM_RESPONSE);
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        client.updateTestCase(TEST_CASE_ID, List.of(
            new AddOperation(PATH + 1, VALUE + 1),
            new AddOperation(PATH + 2, VALUE + 2),
            new AddOperation(PATH + 3, VALUE + 3)
        ));

        validateRequestWithPayload(HttpMethod.PATCH, WORKITEMS_BASE_URL + '/' + TEST_CASE_ID + VERSION, PAYLOAD_JSON);
    }

    @Test
    void shouldGetWorkItem() throws IOException, URISyntaxException
    {
        when(httpResponse.getResponseBodyAsString()).thenReturn(WORK_ITEM_RESPONSE);
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        var workItem = client.getWorkItem(TEST_CASE_ID);

        assertEquals(1, workItem.getId());
        assertEquals(1, workItem.getRev());
        assertThat(logger.getLoggingEvents(), is(List.of()));
        validateRequest(HttpMethod.GET, WORKITEMS_BASE_URL + '/' + TEST_CASE_ID + VERSION);
    }

    @Test
    void shouldQueryTestPoints() throws IOException, URISyntaxException
    {
        when(httpResponse.getResponseBodyAsString()).thenReturn("{\"points\":[{\"id\":40,\"testCase\":"
                + "{\"id\":\"test-case-id\"},\"testPlan\":{\"id\":\"test-plan-id\"}}]}");
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        var testPoints = client.queryTestPoints(Set.of(TEST_CASE_ID));

        assertThat(testPoints, hasSize(1));
        var testPoint = testPoints.get(0);
        assertEquals(40, testPoint.getId());
        assertEquals("test-case-id", testPoint.getTestCase().getId());
        assertEquals("test-plan-id", testPoint.getTestPlan().getId());
        validateRequestWithPayload(HttpMethod.POST, BASE_URL + "/test/points" + VERSION,
                "{\"PointsFilter\":{\"TestcaseIds\":[1]}}");
    }

    @Test
    void shouldCreateTestRun() throws IOException, URISyntaxException
    {
        when(httpResponse.getResponseBodyAsString()).thenReturn(ENTITY_RESPONSE);
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        var testRun = new TestRun();
        testRun.setName("test-run-name");
        testRun.setAutomated(false);
        testRun.setPlan(new ShallowReference("plan-ref"));

        var entity = client.createTestRun(testRun);

        assertEquals(1, entity.getId());
        validateRequestWithPayload(HttpMethod.POST, BASE_URL + "/test/runs" + VERSION,
                "{\"name\":\"test-run-name\",\"automated\":false,\"plan\":{\"id\":\"plan-ref\"}}");
    }

    @Test
    void shouldAddTestResults() throws IOException, URISyntaxException
    {
        when(httpResponse.getResponseBodyAsString()).thenReturn("{}");
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        var offset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
        var testResult = new TestResult();
        var startDate = OffsetDateTime.of(1977, 5, 25, 3, 2, 1, 0, offset);
        testResult.setStartedDate(startDate);
        var completeDate = OffsetDateTime.of(1993, 4, 16, 3, 2, 1, 0, offset);
        testResult.setCompletedDate(completeDate);
        testResult.setOutcome("outcome");
        testResult.setState("state");
        testResult.setTestCaseTitle("test-case-title");
        testResult.setRevision(1);
        testResult.setTestPoint(new ShallowReference("test-point-ref"));

        client.addTestResults(1, List.of(testResult));

        var payload = "[{\"startedDate\":\"" + startDate + "\",\"completedDate\":\"" + completeDate
                + "\",\"outcome\":\"outcome\",\"state\":\"state\",\"testCaseTitle\":\"test-case-title\","
                + "\"revision\":1,\"testPoint\":{\"id\":\"test-point-ref\"}}]";
        validateRequestWithPayload(HttpMethod.POST, BASE_URL + "/test/Runs/" + 1 + "/results" + VERSION, payload);
    }

    private void validateRequestWithPayload(HttpMethod method, String url, String payload) throws URISyntaxException
    {
        assertThat(logger.getLoggingEvents(), is(List.of(info(PAYLOAD_LOG, payload))));
        validateRequest(method, url);
    }

    private void validateRequest(HttpMethod method, String url) throws URISyntaxException
    {
        var request = requestCaptor.getValue();
        assertEquals(method.name(), request.getMethod());
        assertEquals(URI.create(url), request.getUri());
    }
}
