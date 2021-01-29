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

package org.vividus.xray.factory;

import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.model.jbehave.Example;
import org.vividus.bdd.model.jbehave.Examples;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.bdd.model.jbehave.Step;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.model.TestExecution;
import org.vividus.xray.model.TestExecutionItem;
import org.vividus.xray.model.TestExecutionItemStatus;

@ExtendWith(MockitoExtension.class)
class TestExecutionFactoryTests
{
    private static final String SUCCESS = "successful";
    private static final String FAILED = "failed";
    private static final String COMMENT = "comment";
    private static final String KEY = "TEST-EXEC-KEY";

    @Spy private XrayExporterOptions xrayExporterOptions;
    @InjectMocks private TestExecutionFactory factory;

    @Test
    void shouldCreateTestExecution()
    {
        List<Entry<String, Scenario>> scenarios = List.of(
            entry("PLAIN_SUCCESS", createPlainScenario(SUCCESS)),
            entry("PLAIN_FAILED", createPlainScenario(FAILED)),
            entry("MANUAL", createManualScenario()),
            entry("EXAMPLE_SUCCESS", createExamplesScenario(SUCCESS)),
            entry("EXAMPLE_FAILED", createExamplesScenario(FAILED))
        );
        xrayExporterOptions.setTestExecutionKey(KEY);

        TestExecution execution = factory.create(scenarios);

        assertEquals(KEY, execution.getTestExecutionKey());
        List<TestExecutionItem> tests = execution.getTests();
        assertThat(tests, hasSize(5));
        assertEquals(TestExecutionItemStatus.PASS, tests.get(0).getStatus());
        assertEquals(TestExecutionItemStatus.FAIL, tests.get(1).getStatus());
        assertEquals(TestExecutionItemStatus.TODO, tests.get(2).getStatus());
        assertEquals(TestExecutionItemStatus.PASS, tests.get(3).getStatus());
        assertEquals(TestExecutionItemStatus.FAIL, tests.get(4).getStatus());
    }

    private static Scenario createPlainScenario(String outcome)
    {
        Scenario scenario = new Scenario();
        scenario.setSteps(List.of(createStep(SUCCESS), createStep(outcome)));
        return scenario;
    }

    private static Scenario createManualScenario()
    {
        Scenario scenario = new Scenario();
        scenario.setSteps(List.of(createStep(COMMENT), createStep(COMMENT)));
        return scenario;
    }

    private static Scenario createExamplesScenario(String outcome)
    {
        Scenario scenario = new Scenario();
        Examples examples = new Examples();
        scenario.setExamples(examples);
        Example example1 = new Example();
        example1.setSteps(List.of(createStep(SUCCESS), createStep(SUCCESS)));
        Example example2 = new Example();
        example2.setSteps(List.of(createStep(SUCCESS), createStep(outcome)));
        examples.setExamples(List.of(example1, example2));
        return scenario;
    }

    private static Step createStep(String outcome)
    {
        Step step = new Step();
        step.setOutcome(outcome);
        return step;
    }
}
