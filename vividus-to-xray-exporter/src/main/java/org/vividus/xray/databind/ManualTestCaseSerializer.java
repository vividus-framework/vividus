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
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vividus.xray.configuration.JiraFieldsMapping;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.ManualTestStep;

@Component
public class ManualTestCaseSerializer extends JsonSerializer<ManualTestCase>
{
    private static final String FIELDS = "fields";
    private static final String NAME = "name";

    @Autowired private JiraFieldsMapping jiraFieldsMapping;

    @Override
    public void serialize(ManualTestCase manualTest, JsonGenerator generator, SerializerProvider serializers)
            throws IOException
    {
        generator.writeStartObject();
        generator.writeObjectFieldStart(FIELDS);

        writeObjectWithField(generator, "project", "key", manualTest.getProjectKey());

        String assignee = manualTest.getAssignee();
        if (assignee != null)
        {
            writeObjectWithField(generator, "assignee", NAME, assignee);
        }

        writeObjectWithField(generator, "issuetype", NAME, "Test");

        writeObjectWithField(generator, jiraFieldsMapping.getTestCaseType(), "value", "Manual");

        generator.writeStringField("summary", manualTest.getSummary());

        writeJsonArray(generator, "labels", manualTest.getLabels(), false);

        writeJsonArray(generator, "components", manualTest.getComponents(), true);

        List<ManualTestStep> manualTestSteps = manualTest.getManualTestSteps();

        generator.writeObjectFieldStart(jiraFieldsMapping.getManualSteps());
        generator.writeArrayFieldStart("steps");
        for (int stepIndex = 0; stepIndex < manualTestSteps.size(); stepIndex++)
        {
            ManualTestStep manualTestStep = manualTestSteps.get(stepIndex);
            generator.writeStartObject();
            generator.writeNumberField("index", stepIndex + 1);
            generator.writeObjectFieldStart(FIELDS);
            generator.writeStringField("Action", manualTestStep.getAction());
            generator.writeStringField("Data", manualTestStep.getData());
            generator.writeStringField("Expected Result", manualTestStep.getExpectedResult());
            generator.writeEndObject();
            generator.writeEndObject();
        }
        generator.writeEndArray();

        generator.writeEndObject();
        generator.writeEndObject();
    }

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
}
