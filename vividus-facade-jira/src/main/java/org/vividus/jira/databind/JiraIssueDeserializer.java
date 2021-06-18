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

package org.vividus.jira.databind;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.vividus.jira.model.JiraIssue;

public class JiraIssueDeserializer extends JsonDeserializer<JiraIssue>
{
    @Override
    public JiraIssue deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        JsonNode root = jsonParser.getCodec().readTree(jsonParser);

        JiraIssue issue = new JiraIssue();
        issue.setId(root.path("id").asText());

        JsonNode fields = root.path("fields");
        issue.setStatus(getFieldValue(fields, "status"));
        issue.setResolution(getFieldValue(fields, "resolution"));
        return issue;
    }

    private String getFieldValue(JsonNode root, String field)
    {
        return root.path(field).path("name").asText("undefined");
    }
}
