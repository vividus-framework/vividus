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

package org.vividus.xray.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.vividus.xray.facade.AbstractTestCaseParameters;
import org.vividus.xray.facade.CucumberTestCaseParameters;
import org.vividus.xray.facade.ManualTestCaseParameters;
import org.vividus.xray.model.AbstractTestCase;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.ManualTestStep;
import org.vividus.xray.model.TestCaseType;

class TestCaseFactoryTests
{
    private static final String PROJECT_KEY = "project-key";
    private static final String ASSIGNEE = "assignee";

    private final TestCaseFactory factory = new TestCaseFactory(PROJECT_KEY, ASSIGNEE);

    @Test
    void shouldCreateManualTestCase()
    {
        ManualTestStep step = Mockito.mock(ManualTestStep.class);
        ManualTestCaseParameters parameters = createTestCaseParameters(TestCaseType.MANUAL,
                ManualTestCaseParameters::new);
        parameters.setSteps(List.of(step));

        ManualTestCase testCase = factory.createManualTestCase(parameters);

        assertEquals(List.of(step), testCase.getManualTestSteps());
        verifyTestCase(parameters, testCase);
    }

    @Test
    void shouldCreateCucumberTestCase()
    {
        CucumberTestCaseParameters parameters = createTestCaseParameters(TestCaseType.CUCUMBER,
                CucumberTestCaseParameters::new);
        parameters.setScenarioType("scenario-type");
        parameters.setScenario("scenario");

        CucumberTestCase testCase = factory.createCucumberTestCase(parameters);

        assertEquals(parameters.getScenarioType(), testCase.getScenarioType());
        assertEquals(parameters.getScenario(), testCase.getScenario());
        verifyTestCase(parameters, testCase);
    }

    private void verifyTestCase(AbstractTestCaseParameters parameters, AbstractTestCase testCase)
    {
        assertEquals(PROJECT_KEY, testCase.getProjectKey());
        assertEquals(ASSIGNEE, testCase.getAssignee());
        assertEquals(parameters.getLabels(), testCase.getLabels());
        assertEquals(parameters.getComponents(), testCase.getComponents());
        assertEquals(parameters.getSummary(), testCase.getSummary());
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractTestCaseParameters> T createTestCaseParameters(TestCaseType type,
            Supplier<T> factory)
    {
        AbstractTestCaseParameters testCase = factory.get();
        testCase.setType(type);
        testCase.setSummary("summary");
        testCase.setLabels(Set.of("labels-1"));
        testCase.setComponents(Set.of("components-1"));
        return (T) testCase;
    }
}
