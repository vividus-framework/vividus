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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.model.jbehave.Example;
import org.vividus.model.jbehave.Examples;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.model.TestExecution;
import org.vividus.xray.model.TestExecutionItem;
import org.vividus.xray.model.TestExecutionItemStatus;

@ExtendWith(MockitoExtension.class)
class TestExecutionFactoryTests
{
    private static final ZoneOffset OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now());
    private static final OffsetDateTime START = OffsetDateTime.of(1977, 5, 25, 0, 0, 0, 0, OFFSET);
    private static final OffsetDateTime FINISH = OffsetDateTime.of(1993, 4, 16, 0, 0, 0, 0, OFFSET);

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
            entry("PLAIN_SUCCESS", createPlainScenario(SUCCESS, List.of(), List.of())),
            entry("PLAIN_FAILED", createPlainScenario(FAILED, List.of(), List.of())),
            entry("MANUAL", createManualScenario()),
            entry("EXAMPLE_SUCCESS", createExamplesScenario(SUCCESS, List.of(), List.of())),
            entry("EXAMPLE_FAILED", createExamplesScenario(FAILED, List.of(), List.of())),
            entry("PLAIN_FAILED_IN_AFTER_STEPS", createPlainScenario(SUCCESS, List.of(), List.of(createStep(FAILED)))),
            entry("PLAIN_FAILED_IN_BEFORE_STEPS", createPlainScenario(SUCCESS, List.of(createStep(FAILED)),
                    List.of())),
            entry("EXAMPLE_FAILED_IN_AFTER_STEPS", createExamplesScenario(SUCCESS, List.of(),
                    List.of(createStep(FAILED)))),
            entry("EXAMPLE_FAILED_IN_BEFORE_STEPS", createExamplesScenario(SUCCESS, List.of(createStep(FAILED)),
                    List.of()))
        );
        xrayExporterOptions.setTestExecutionKey(KEY);

        TestExecution execution = factory.create(scenarios);

        assertEquals(KEY, execution.getTestExecutionKey());
        List<TestExecutionItem> tests = execution.getTests();
        assertThat(tests, hasSize(9));
        assertExecutedItem(tests.get(0), TestExecutionItemStatus.PASS);
        assertExecutedItem(tests.get(1), TestExecutionItemStatus.FAIL);
        assertItem(tests.get(2), TestExecutionItemStatus.TODO, null, null);
        assertExecutedItem(tests.get(3), TestExecutionItemStatus.PASS);
        assertExecutedItem(tests.get(4), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(5), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(6), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(7), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(8), TestExecutionItemStatus.FAIL);
    }

    private static void assertExecutedItem(TestExecutionItem item, TestExecutionItemStatus status)
    {
        assertItem(item, status, START.toString(), FINISH.toString());
    }

    private static void assertItem(TestExecutionItem item, TestExecutionItemStatus status, String start, String finish)
    {
        assertEquals(status, item.getStatus());
        assertEquals(start, item.getStart());
        assertEquals(finish, item.getFinish());
    }

    private static Scenario createPlainScenario(String outcome, List<Step> beforeScenarioSteps,
            List<Step> afterScenarioSteps)
    {
        Scenario scenario = createScenario();
        scenario.setBeforeUserScenarioSteps(beforeScenarioSteps);
        scenario.setSteps(List.of(createStep(SUCCESS), createStep(outcome)));
        scenario.setAfterUserScenarioSteps(afterScenarioSteps);
        return scenario;
    }

    private static Scenario createManualScenario()
    {
        Scenario scenario = createScenario();
        scenario.setSteps(List.of(createStep(COMMENT), createStep(COMMENT)));
        return scenario;
    }

    private static Scenario createExamplesScenario(String outcome, List<Step> beforeScenarioSteps,
            List<Step> afterScenarioSteps)
    {
        Scenario scenario = createScenario();
        Examples examples = new Examples();
        scenario.setExamples(examples);

        Example example1 = new Example();
        example1.setBeforeUserScenarioSteps(beforeScenarioSteps);
        example1.setSteps(List.of(createStep(SUCCESS), createStep(SUCCESS)));
        example1.setAfterUserScenarioSteps(List.of(createStep(SUCCESS), createStep(SUCCESS)));

        Example example2 = new Example();
        example2.setBeforeUserScenarioSteps(List.of(createStep(SUCCESS), createStep(SUCCESS)));
        example2.setSteps(List.of(createStep(SUCCESS), createStep(outcome)));
        example2.setAfterUserScenarioSteps(afterScenarioSteps);

        examples.setExamples(List.of(example1, example2));
        return scenario;
    }

    private static Step createStep(String outcome)
    {
        Step step = new Step();
        step.setOutcome(outcome);
        return step;
    }

    private static Scenario createScenario()
    {
        Scenario scenario = new Scenario();
        scenario.setStart(START.toInstant().toEpochMilli());
        scenario.setEnd(FINISH.toInstant().toEpochMilli());
        return scenario;
    }
}
