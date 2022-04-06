/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.zephyr.databind;

import static org.vividus.exporter.databind.JsonSerializer.writeJsonArray;
import static org.vividus.exporter.databind.JsonSerializer.writeObjectWithField;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraConfigurationProvider;
import org.vividus.zephyr.model.ZephyrTestCase;

@Component
public class TestCaseSerializer extends JsonSerializer<ZephyrTestCase>
{
    private static final String NAME = "name";
    private static final String STORY_TYPE_FIELD_KEY = "story-type";

    @Autowired private JiraConfigurationProvider jiraConfigurationProvider;

    @Override
    public void serialize(ZephyrTestCase zephyrTestCase, JsonGenerator generator, SerializerProvider serializers)
            throws IOException
    {
        generator.writeStartObject();

        generator.writeObjectFieldStart("fields");

        String projectKey = zephyrTestCase.getProjectKey();

        writeObjectWithField(generator, "project", "key", zephyrTestCase.getProjectKey());

        writeObjectWithField(generator, "issuetype", NAME, "Test");

        writeJsonArray(generator, "labels", zephyrTestCase.getLabels(), false);

        writeJsonArray(generator, "components", zephyrTestCase.getComponents(), true);

        generator.writeStringField("summary", zephyrTestCase.getSummary());

        try
        {
            Map<String, String> mapping = jiraConfigurationProvider.getFieldsMappingByProjectKey(projectKey);
            String storyType = mapping.get(STORY_TYPE_FIELD_KEY);
            if (storyType != null)
            {
                writeObjectWithField(generator, mapping.get(STORY_TYPE_FIELD_KEY), "value", "Task");
            }
        }
        catch (JiraConfigurationException e)
        {
            throw new IllegalStateException(e);
        }

        generator.writeEndObject();
        generator.writeEndObject();
    }
}
