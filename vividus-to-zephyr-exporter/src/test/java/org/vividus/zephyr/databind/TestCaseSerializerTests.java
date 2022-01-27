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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraConfigurationProvider;
import org.vividus.util.ResourceUtils;
import org.vividus.zephyr.model.ZephyrTestCase;

@ExtendWith(MockitoExtension.class)
public class TestCaseSerializerTests
{
    private static final String PROJECT_KEY = "project key";
    private static final String STORY_TYPE = "story-type";

    @InjectMocks private TestCaseSerializer serializer;
    @Mock private JiraConfigurationProvider jiraConfigurationProvider;

    @Test
    void shouldSerializeTest() throws IOException, JiraConfigurationException
    {
        ZephyrTestCase test = createBaseTest();
        when(jiraConfigurationProvider.getFieldsMappingByProjectKey(PROJECT_KEY)).thenReturn(Map.of(
                STORY_TYPE, STORY_TYPE));

        verifySerialization(test);
    }

    @Test
    void shouldRethrowJiraConfigurationException() throws JiraConfigurationException
    {
        JiraConfigurationException jiraConfigurationException = mock(JiraConfigurationException.class);
        doThrow(jiraConfigurationException).when(jiraConfigurationProvider).getFieldsMappingByProjectKey(PROJECT_KEY);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,  this::verifySerialization);
        assertEquals(jiraConfigurationException, thrown.getCause());
    }

    private void verifySerialization() throws IOException
    {
        ZephyrTestCase test = createBaseTest();
        verifySerialization(test);
    }

    private static ZephyrTestCase createBaseTest()
    {
        ZephyrTestCase test = new ZephyrTestCase();
        test.setProjectKey(PROJECT_KEY);
        test.setSummary("summary");
        test.setLabels(new LinkedHashSet<>(List.of("label 1", "label 2")));
        test.setComponents(new LinkedHashSet<>(List.of("component 1", "component 2")));
        return test;
    }

    private void verifySerialization(ZephyrTestCase test) throws IOException
    {
        try (StringWriter writer = new StringWriter())
        {
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(writer);

            serializer.serialize(test, generator, null);

            generator.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actual = mapper.readTree(writer.toString());
            JsonNode expected = mapper.readTree(ResourceUtils.loadResource(getClass(), "report.json"));
            assertEquals(expected, actual);
        }
    }
}
