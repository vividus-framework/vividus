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

package org.vividus.xray.databind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraConfigurationProvider;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.TestCaseType;

import test.util.JsonVerificationUtils;

@ExtendWith(MockitoExtension.class)
class CucumberTestCaseSerializerTests
{
    private static final String PROJECT_KEY = "project-key";

    @Mock private JiraConfigurationProvider jiraConfigurationProvider;
    @InjectMocks private CucumberTestCaseSerializer serializer;

    @Test
    void shouldSerializeCucumberTest() throws IOException, JiraConfigurationException
    {
        when(jiraConfigurationProvider.getFieldsMappingByProjectKey(PROJECT_KEY)).thenReturn(Map.of(
            "cucumber-scenario-type", "cucumber-scenario-type-field",
            "cucumber-scenario", "cucumber-scenario-field",
            "test-case-type", "test-case-type-field"
        ));

        verifySerializedForm();
    }

    @Test
    void shouldFailIfTestCaseTypeMappingIsNotSet() throws JiraConfigurationException, IOException
    {
        when(jiraConfigurationProvider.getFieldsMappingByProjectKey(PROJECT_KEY)).thenReturn(Map.of());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, this::verifySerializedForm);
        assertEquals("The mapping for the 'test-case-type' field must be configured", thrown.getMessage());
    }

    @Test
    void shouldRethrowJiraConfigurationException() throws JiraConfigurationException
    {
        JiraConfigurationException jiraConfigurationException = mock(JiraConfigurationException.class);
        doThrow(jiraConfigurationException).when(jiraConfigurationProvider).getFieldsMappingByProjectKey(PROJECT_KEY);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, this::verifySerializedForm);
        assertEquals(jiraConfigurationException, thrown.getCause());
    }

    private void verifySerializedForm() throws IOException
    {
        JsonVerificationUtils.verifySerializedForm(serializer, createTestCase(), getClass(), "cucumber.json");
    }

    private static CucumberTestCase createTestCase()
    {
        CucumberTestCase testCase = new CucumberTestCase();
        testCase.setType(TestCaseType.CUCUMBER.getValue());
        testCase.setProjectKey(PROJECT_KEY);
        testCase.setSummary("summary");
        testCase.setAssignee("test-assignee");
        testCase.setLabels(new LinkedHashSet<>(List.of("label-1", "label-2")));
        testCase.setComponents(new LinkedHashSet<>(List.of("component-1", "component-2")));
        testCase.setScenarioType("scenario-type");
        testCase.setScenario("scenario");
        return testCase;
    }
}
