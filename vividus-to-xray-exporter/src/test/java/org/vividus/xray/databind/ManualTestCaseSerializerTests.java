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

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraConfigurationProvider;
import org.vividus.output.ManualTestStep;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.TestCaseType;

import test.util.JsonVerificationUtils;

@ExtendWith(MockitoExtension.class)
class ManualTestCaseSerializerTests
{
    private static final String PROJECT_KEY = "project key";

    @Mock private JiraConfigurationProvider jiraConfigurationProvider;
    @InjectMocks private ManualTestCaseSerializer serializer;

    @BeforeEach
    void init() throws JiraConfigurationException
    {
        when(jiraConfigurationProvider.getFieldsMappingByProjectKey(PROJECT_KEY)).thenReturn(Map.of(
            "manual-steps", "manual-steps-field",
            "test-case-type", "test-case-type-field"
        ));
    }

    @Test
    void shouldSerializeManualTest() throws IOException
    {
        ManualTestCase test = createBaseTest();
        test.setAssignee("test-assignee");
        test.setLabels(new LinkedHashSet<>(List.of("label 1", "label 2")));
        test.setComponents(new LinkedHashSet<>(List.of("component 1", "component 2")));
        JsonVerificationUtils.verifySerializedForm(serializer, test, getClass(), "manual.json");
    }

    @Test
    void shouldSerializeManualTestWithNullLabelsAndComponents() throws IOException
    {
        JsonVerificationUtils.verifySerializedForm(serializer, createBaseTest(), getClass(), "nullable.json");
    }

    private static ManualTestCase createBaseTest()
    {
        ManualTestCase test = new ManualTestCase();
        test.setType(TestCaseType.MANUAL.getValue());
        test.setProjectKey(PROJECT_KEY);
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
