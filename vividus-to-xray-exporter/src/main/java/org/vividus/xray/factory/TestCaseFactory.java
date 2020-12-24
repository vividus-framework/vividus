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

import org.vividus.xray.facade.AbstractTestCaseParameters;
import org.vividus.xray.facade.CucumberTestCaseParameters;
import org.vividus.xray.facade.ManualTestCaseParameters;
import org.vividus.xray.model.AbstractTestCase;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.ManualTestCase;

public class TestCaseFactory
{
    private final String projectKey;
    private final String assignee;

    public TestCaseFactory(String projectKey, String assignee)
    {
        this.projectKey = projectKey;
        this.assignee = assignee;
    }

    public ManualTestCase createManualTestCase(ManualTestCaseParameters parameters)
    {
        ManualTestCase testCase = new ManualTestCase();
        fillTestCase(parameters, testCase);
        testCase.setManualTestSteps(parameters.getSteps());
        return testCase;
    }

    public CucumberTestCase createCucumberTestCase(CucumberTestCaseParameters parameters)
    {
        CucumberTestCase testCase = new CucumberTestCase();
        fillTestCase(parameters, testCase);
        testCase.setScenarioType(parameters.getScenarioType());
        testCase.setScenario(parameters.getScenario());
        return testCase;
    }

    private void fillTestCase(AbstractTestCaseParameters parameters, AbstractTestCase testCase)
    {
        testCase.setType(parameters.getType().getValue());
        testCase.setProjectKey(projectKey);
        testCase.setAssignee(assignee);
        testCase.setSummary(parameters.getSummary());
        testCase.setLabels(parameters.getLabels());
        testCase.setComponents(parameters.getComponents());
    }
}
