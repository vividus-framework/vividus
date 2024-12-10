/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.jira.databind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import org.junit.jupiter.api.Test;
import org.vividus.http.client.BasicAuthConfig;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.jira.model.JiraConfiguration;

class JiraConfigurationDeserializerTests
{
    @Test
    void shouldDeserialize() throws IOException
    {
        JsonParser parser = new JavaPropsFactory().createParser("""
                project-key-regex=VVDS
                endpoint=https://jira.vividus.com
                fields-mapping.test-case-type=customfield_00001
                fields-mapping.manual-steps=customfield_00002
                http.auth.username=Bob
                http.auth.password=052ddff802a174847345
                http.socket-timeout=1
                """);
        parser.setCodec(new JavaPropsMapper().setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE));

        JiraConfigurationDeserializer deserializer = new JiraConfigurationDeserializer();
        JiraConfiguration configuration = deserializer.deserialize(parser, null);

        assertEquals("VVDS", configuration.getProjectKeyRegex().pattern());
        String endpoint = "https://jira.vividus.com";
        assertEquals(endpoint, configuration.getEndpoint());
        assertEquals(Map.of("test-case-type", "customfield_00001", "manual-steps", "customfield_00002"),
                configuration.getFieldsMapping());

        HttpClientConfig http = configuration.getHttpClientConfig();
        assertNotNull(http);
        assertEquals(1, http.getSocketTimeout());
        Map<String, BasicAuthConfig> authConfigs = http.getBasicAuthConfig();
        assertNotNull(authConfigs);
        BasicAuthConfig config = authConfigs.get("jira");
        assertNotNull(config);
        assertEquals("Bob", config.getUsername());
        assertEquals("052ddff802a174847345", config.getPassword());
        assertEquals(endpoint, config.getOrigin());
    }

    @Test
    void shouldDeserializeEmpty() throws IOException
    {
        JsonParser parser = new JavaPropsFactory().createParser("");
        parser.setCodec(new JavaPropsMapper().setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE));

        JiraConfigurationDeserializer deserializer = new JiraConfigurationDeserializer();
        JiraConfiguration configuration = deserializer.deserialize(parser, null);

        assertNull(configuration.getProjectKeyRegex());
        assertNull(configuration.getEndpoint());
        assertNull(configuration.getFieldsMapping());
        assertNull(configuration.getHttpClientConfig());
    }
}
