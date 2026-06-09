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

import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.jira.JiraClientProvider;
import org.vividus.xray.facade.XrayClient;
import org.vividus.xray.facade.XrayCloudClient;
import org.vividus.xray.facade.XrayServerClient;

@Configuration
public class XrayClientConfiguration
{
    @Bean
    @ConditionalOnProperty(name = "xray-exporter.cloud.enabled", havingValue = "false", matchIfMissing = true)
    public XrayClient xrayServerClient(JiraClientProvider jiraClientProvider,
            @Value("${xray-exporter.jira-instance-key:}") String jiraInstanceKey)
    {
        return new XrayServerClient(jiraClientProvider, jiraInstanceKey.isEmpty() ? null : jiraInstanceKey);
    }

    @Bean
    @ConditionalOnProperty(name = "xray-exporter.cloud.enabled", havingValue = "true")
    public XrayClient xrayCloudClient(XrayExporterOptions options, IHttpClientFactory httpClientFactory)
            throws GeneralSecurityException
    {
        XrayExporterOptions.CloudOptions cloudOptions = options.getCloudOptions();
        return new XrayCloudClient(
                cloudOptions.getApiBaseUrl(),
                cloudOptions.getClientId(),
                cloudOptions.getClientSecret(),
                httpClientFactory.buildHttpClient(new HttpClientConfig()));
    }
}
