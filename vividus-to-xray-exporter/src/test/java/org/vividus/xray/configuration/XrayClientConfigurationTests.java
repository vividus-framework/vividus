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

package org.vividus.xray.configuration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;
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
        IHttpClientFactory httpClientFactory = mock(IHttpClientFactory.class);
        IHttpClient httpClient = mock(IHttpClient.class);
        when(httpClientFactory.buildHttpClient(any(HttpClientConfig.class))).thenReturn(httpClient);

        XrayExporterOptions options = new XrayExporterOptions();
        XrayExporterOptions.CloudOptions cloud = new XrayExporterOptions.CloudOptions();
        cloud.setClientId("client-id");
        cloud.setClientSecret("client-secret");
        options.setCloudOptions(cloud);

        assertInstanceOf(XrayCloudClient.class,
                new XrayClientConfiguration().xrayCloudClient(options, httpClientFactory));
    }

    @Test
    void shouldCreateXrayServerClientWithoutJiraInstanceKey()
    {
        JiraClientProvider jiraClientProvider = mock(JiraClientProvider.class);
        assertInstanceOf(XrayServerClient.class,
                new XrayClientConfiguration().xrayServerClient(jiraClientProvider, ""));
    }

    @Test
    void shouldCreateXrayServerClientWithJiraInstanceKey()
    {
        JiraClientProvider jiraClientProvider = mock(JiraClientProvider.class);
        assertInstanceOf(XrayServerClient.class,
                new XrayClientConfiguration().xrayServerClient(jiraClientProvider, "my-jira"));
    }
}
