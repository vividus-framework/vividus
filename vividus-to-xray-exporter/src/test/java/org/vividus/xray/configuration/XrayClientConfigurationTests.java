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

package org.vividus.xray.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.jira.JiraClientProvider;
import org.vividus.xray.facade.XrayCloudClient;
import org.vividus.xray.facade.XrayServerClient;

class XrayClientConfigurationTests
{
    @Test
    void shouldCreateXrayCloudClient() throws GeneralSecurityException
    {
        IHttpClientFactory httpClientFactory = mock();
        IHttpClient httpClient = mock();
        when(httpClientFactory.buildHttpClient(any(HttpClientConfig.class))).thenReturn(httpClient);

        var apiBaseUrl = "https://xray.cloud.example.com";
        var clientId = "client-id";
        var clientSecret = "client-secret";

        var options = new XrayExporterOptions();
        var cloud = new XrayExporterOptions.CloudOptions();
        cloud.setApiBaseUrl(apiBaseUrl);
        cloud.setClientId(clientId);
        cloud.setClientSecret(clientSecret);
        options.setCloudOptions(cloud);

        XrayCloudClient client = assertInstanceOf(XrayCloudClient.class,
                new XrayClientConfiguration().xrayCloudClient(options, httpClientFactory));
        assertAll(
            () -> assertEquals(apiBaseUrl + "/api/v2", ReflectionTestUtils.getField(client, "apiBaseUrl")),
            () -> assertEquals(clientId, ReflectionTestUtils.getField(client, "clientId")),
            () -> assertEquals(clientSecret, ReflectionTestUtils.getField(client, "clientSecret")),
            () -> assertSame(httpClient, ReflectionTestUtils.getField(client, "httpClient"))
        );
    }

    @ParameterizedTest
    @CsvSource({
        "'',",
        "my-jira, my-jira"
    })
    void shouldCreateXrayServerClient(String inputKey, String expectedKey)
    {
        JiraClientProvider jiraClientProvider = mock();
        XrayServerClient client = assertInstanceOf(XrayServerClient.class,
                new XrayClientConfiguration().xrayServerClient(jiraClientProvider, inputKey));
        assertAll(
            () -> assertSame(jiraClientProvider, ReflectionTestUtils.getField(client, "jiraClientProvider")),
            () -> assertEquals(expectedKey, ReflectionTestUtils.getField(client, "jiraInstanceKey"))
        );
    }
}
