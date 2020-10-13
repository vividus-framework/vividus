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

package org.vividus.xray.databind;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.vividus.xray.configuration.JiraFieldsMapping;
import org.vividus.xray.model.AbstractTestCase;

public abstract class AbstractTestCaseSerializer<T extends AbstractTestCase> extends JsonSerializer<T>
{
    private static final String NAME = "name";

    @Autowired private JiraFieldsMapping jiraFieldsMapping;

    @Override
    public void serialize(T testCase, JsonGenerator generator, SerializerProvider serializers) throws IOException
    {
        generator.writeStartObject();
        generator.writeObjectFieldStart("fields");

        writeObjectWithField(generator, "project", "key", testCase.getProjectKey());

        String assignee = testCase.getAssignee();
        if (assignee != null)
        {
            writeObjectWithField(generator, "assignee", NAME, assignee);
        }

        writeObjectWithField(generator, "issuetype", NAME, "Test");

        writeObjectWithValueField(generator, jiraFieldsMapping.getTestCaseType(), testCase.getType());

        generator.writeStringField("summary", testCase.getSummary());

        writeJsonArray(generator, "labels", testCase.getLabels(), false);

        writeJsonArray(generator, "components", testCase.getComponents(), true);

        serializeCustomFields(testCase, generator);

        generator.writeEndObject();
        generator.writeEndObject();
    }

    protected void writeObjectWithValueField(JsonGenerator generator, String objectKey, String fieldValue)
            throws IOException
    {
        writeObjectWithField(generator, objectKey, "value", fieldValue);
    }

    protected abstract void serializeCustomFields(T testCase, JsonGenerator generator) throws IOException;

    private static void writeJsonArray(JsonGenerator generator, String startField, Collection<String> values,
            boolean wrapValuesAsObjects) throws IOException
    {
        if (values != null)
        {
            generator.writeArrayFieldStart(startField);
            for (String value : values)
            {
                if (wrapValuesAsObjects)
                {
                    generator.writeStartObject();
                    generator.writeStringField(NAME, value);
                    generator.writeEndObject();
                }
                else
                {
                    generator.writeString(value);
                }
            }
            generator.writeEndArray();
        }
    }

    private static void writeObjectWithField(JsonGenerator generator, String objectKey, String fieldName,
            String fieldValue) throws IOException
    {
        generator.writeObjectFieldStart(objectKey);
        generator.writeStringField(fieldName, fieldValue);
        generator.writeEndObject();
    }

    protected JiraFieldsMapping getJiraFieldsMapping()
    {
        return jiraFieldsMapping;
    }
}
