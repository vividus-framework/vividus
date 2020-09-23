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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.ResourceUtils;
import org.vividus.xray.configuration.JiraFieldsMapping;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.ManualTestStep;

@ExtendWith(MockitoExtension.class)
class ManualTestCaseSerializerTests
{
    @Spy private JiraFieldsMapping fields;
    @InjectMocks private ManualTestCaseSerializer serializer;

    @BeforeEach
    void init()
    {
        fields.setManualSteps("manual-steps-field");
        fields.setTestCaseType("test-case-type-field");
    }

    @Test
    void shouldSerializeManualTest() throws IOException
    {
        ManualTestCase test = createBaseTest();
        test.setLabels(new LinkedHashSet<>(List.of("label 1", "label 2")));
        test.setComponents(new LinkedHashSet<>(List.of("component 1", "component 2")));
        verifySerializedForm(test, "expected.json");
    }

    @Test
    void shouldSerializeManualTestWithNullLabelsAndComponents() throws IOException
    {
        verifySerializedForm(createBaseTest(), "nullable.json");
    }

    private void verifySerializedForm(ManualTestCase test, String expectedJsonPath) throws IOException
    {
        try (StringWriter writer = new StringWriter())
        {
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(writer);

            serializer.serialize(test, generator, null);

            generator.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actual = mapper.readTree(writer.toString());
            JsonNode expected = mapper.readTree(ResourceUtils.loadResource(getClass(), expectedJsonPath));
            assertEquals(expected, actual);
        }
    }

    private static ManualTestCase createBaseTest()
    {
        ManualTestCase test = new ManualTestCase();
        test.setProjectKey("project key");
        test.setSummary("summary");
        test.setManualTestSteps(List.of(
            createStep("action 1", "data 1", "result 1"),
            createStep("action 2", "data 2", "result 2")
        ));
        return test;
    }

    private static ManualTestStep createStep(String action, String data, String result)
    {
        ManualTestStep step = new ManualTestStep(action);
        step.setData(data);
        step.setExpectedResult(result);
        return step;
    }
}
