/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.jackson.databind.email;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.vividus.bdd.email.model.EmailServerConfiguration;

@Named
public class EmailServerConfigurationDeserializer extends JsonDeserializer<EmailServerConfiguration>
{
    @Override
    public EmailServerConfiguration deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException
    {
        JsonNode node = p.getCodec().readTree(p);

        String username = getSafely(node, "username");
        String password = getSafely(node, "password");

        Map<String, String> properties = new HashMap<>();
        JsonNode propertiesNode = node.findPath("properties");
        if (!propertiesNode.isMissingNode())
        {
            gatherProperties(StringUtils.EMPTY, propertiesNode, properties);
        }

        return new EmailServerConfiguration(username, password, properties);
    }

    private void gatherProperties(String baseKey, JsonNode root, Map<String, String> container)
    {
        String separator = baseKey.isEmpty() ? StringUtils.EMPTY : ".";
        root.fields().forEachRemaining(e ->
        {
            JsonNode node = e.getValue();
            if (node.isValueNode())
            {
                container.put(baseKey + separator + e.getKey(), node.asText());
            }
            if (node.isObject())
            {
                gatherProperties(e.getKey(), node, container);
            }
        });
    }

    private String getSafely(JsonNode root, String property)
    {
        JsonNode node = root.findPath(property);
        Validate.isTrue(!node.isMissingNode(), "Required property '%s' is not set", property);
        return node.asText();
    }
}
