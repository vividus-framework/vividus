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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.http.HttpMethod;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AzureDevOpsClientTests
{
    private static final String PATH = "path-";
    private static final String VALUE = "value-";
    private static final String BASE_URL = "https://dev.azure.com/organization/project/_apis/wit/workitems";
    private static final String PAYLOAD_JSON = "[{\"op\":\"add\",\"path\":\"path-1\",\"value\":\"value-1\"},"
            + "{\"op\":\"add\",\"path\":\"path-2\",\"value\":\"value-2\"},"
            + "{\"op\":\"add\",\"path\":\"path-3\",\"value\":\"value-3\"}]";
    private static final String PAYLOAD_LOG = "Azure DevOps request: {}";
    private static final String VERSION = "?api-version=6.1-preview.3";

    @Captor private ArgumentCaptor<HttpUriRequest> requestCaptor;
    @Mock private HttpResponse httpResponse;
    @Mock private IHttpClient httpClient;
    private AzureDevOpsClient client;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AzureDevOpsClient.class);

    @BeforeEach
    void init()
    {
        AzureDevOpsExporterOptions options = new AzureDevOpsExporterOptions();
        options.setOrganization("organization");
        options.setProject("project");
        client = new AzureDevOpsClient(httpClient, options);
    }

    @Test
    void shouldCreateTestCase() throws IOException
    {
        String body = "{\"id\": 1}";
        when(httpResponse.getResponseBodyAsString()).thenReturn(body);
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        String response = client.createTestCase(List.of(
            new AddOperation(PATH + 1, VALUE + 1),
            new AddOperation(PATH + 2, VALUE + 2),
            new AddOperation(PATH + 3, VALUE + 3)
        ));

        assertEquals(body, response);
        assertThat(logger.getLoggingEvents(), is(List.of(info(PAYLOAD_LOG, PAYLOAD_JSON))));
        HttpUriRequest request = requestCaptor.getValue();
        assertEquals(HttpMethod.POST.name(), request.getMethod());
        assertEquals(BASE_URL + "/$test%20case" + VERSION, request.getURI().toString());
    }

    @Test
    void shouldUpdateTestCase() throws IOException
    {
        when(httpResponse.getResponseBodyAsString()).thenReturn("{}");
        doReturn(httpResponse).when(httpClient).execute(requestCaptor.capture());

        String id = "1";
        client.updateTestCase(id, List.of(
            new AddOperation(PATH + 1, VALUE + 1),
            new AddOperation(PATH + 2, VALUE + 2),
            new AddOperation(PATH + 3, VALUE + 3)
        ));

        assertThat(logger.getLoggingEvents(), is(List.of(info(PAYLOAD_LOG, PAYLOAD_JSON))));
        HttpUriRequest request = requestCaptor.getValue();
        assertEquals(HttpMethod.PATCH.name(), request.getMethod());
        assertEquals(BASE_URL + "/" + id + VERSION, request.getURI().toString());
    }
}
