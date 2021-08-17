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

package org.vividus.xray.databind;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.xray.configuration.JiraFieldsMapping;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.TestCaseType;

import test.util.JsonVerificationUtils;

@ExtendWith(MockitoExtension.class)
class CucumberTestCaseSerializerTests
{
    @Spy private JiraFieldsMapping fields;
    @InjectMocks private CucumberTestCaseSerializer serializer;

    @BeforeEach
    void init()
    {
        fields.setCucumberScenarioType("cucumber-scenario-type-field");
        fields.setCucumberScenario("cucumber-scenario-field");
        fields.setTestCaseType("test-case-type-field");
    }

    @Test
    void shouldSerializeCucumberTest() throws IOException
    {
        CucumberTestCase testCase = new CucumberTestCase();
        testCase.setType(TestCaseType.CUCUMBER.getValue());
        testCase.setProjectKey("project-key");
        testCase.setSummary("summary");
        testCase.setAssignee("test-assignee");
        testCase.setLabels(new LinkedHashSet<>(List.of("label-1", "label-2")));
        testCase.setComponents(new LinkedHashSet<>(List.of("component-1", "component-2")));
        testCase.setScenarioType("scenario-type");
        testCase.setScenario("scenario");

        JsonVerificationUtils.verifySerializedForm(serializer, testCase, getClass(), "cucumber.json");
    }
}
