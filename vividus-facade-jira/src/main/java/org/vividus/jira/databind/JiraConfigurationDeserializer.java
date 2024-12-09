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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.vividus.http.client.BasicAuthConfig;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.jira.model.JiraConfiguration;

public class JiraConfigurationDeserializer extends JsonDeserializer<JiraConfiguration>
{
    @Override
    public JiraConfiguration deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException
    {
        ObjectMapper codec = (ObjectMapper) parser.getCodec();
        JsonNode jsonNode = codec.readTree(parser);

        JiraConfiguration configuration = new JiraConfiguration();

        JsonNode projectKeyNode = jsonNode.get("project-key-regex");
        Pattern projectKeyPattern = codec.treeToValue(projectKeyNode, Pattern.class);
        configuration.setProjectKeyRegex(projectKeyPattern);

        String endpoint = Optional.ofNullable(jsonNode.get("endpoint")).map(JsonNode::asText).orElse(null);
        configuration.setEndpoint(endpoint);

        JsonNode mappingNode = jsonNode.get("fields-mapping");
        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() { };
        Map<String, String> fieldsMapping = codec.treeToValue(mappingNode, typeRef);
        configuration.setFieldsMapping(fieldsMapping);

        JsonNode httpNode = jsonNode.get("http");
        if (httpNode != null)
        {
            HttpClientConfig httpClientConfig = codec.treeToValue(httpNode, HttpClientConfig.class);
            configuration.setHttpClientConfig(httpClientConfig);

            JsonNode authNode = httpNode.get("auth");
            BasicAuthConfig authConfig = codec.treeToValue(authNode, BasicAuthConfig.class);
            authConfig.setOrigin(endpoint);
            httpClientConfig.setBasicAuthConfig(Map.of("jira", authConfig));
        }

        return configuration;
    }
}
