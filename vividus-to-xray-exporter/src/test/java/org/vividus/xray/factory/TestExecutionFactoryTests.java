/*
 * Copyright 2019-2023 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import org.vividus.xray.model.TestExecutionInfo;
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
    private static final String SUMMARY = "summary";

    @Spy private XrayExporterOptions xrayExporterOptions;
    @InjectMocks private TestExecutionFactory factory;

    @Test
    void shouldCreateTestExecution()
    {
        List<Entry<String, Scenario>> scenarios = List.of(
            entry("PLAIN_SUCCESS", createPlainScenario(SUCCESS, List.of(), List.of(), List.of(), List.of())),
            entry("PLAIN_FAILED", createPlainScenario(FAILED, List.of(), List.of(), List.of(), List.of())),
            entry("MANUAL", createManualScenario()),
            entry("EXAMPLE_SUCCESS", createExamplesScenario(SUCCESS, List.of(), List.of(), List.of(), List.of())),
            entry("EXAMPLE_FAILED", createExamplesScenario(FAILED, List.of(), List.of(), List.of(), List.of())),
            entry("PLAIN_FAILED_IN_AFTER_USER_STEPS", createPlainScenario(SUCCESS, List.of(), List.of(),
                    List.of(createStep(FAILED)), List.of())),
            entry("PLAIN_FAILED_IN_BEFORE_USER_STEPS", createPlainScenario(SUCCESS, List.of(),
                    List.of(createStep(FAILED)), List.of(), List.of())),
            entry("EXAMPLE_FAILED_IN_AFTER_USER_STEPS", createExamplesScenario(SUCCESS, List.of(), List.of(),
                    List.of(createStep(FAILED)), List.of())),
            entry("EXAMPLE_FAILED_IN_BEFORE_USER_STEPS", createExamplesScenario(SUCCESS, List.of(),
                    List.of(createStep(FAILED)), List.of(), List.of())),
            entry("EXAMPLE_FAILED_IN_AFTER_SYSTEM_STEPS", createExamplesScenario(SUCCESS, List.of(), List.of(),
                    List.of(), List.of(createStep(FAILED)))),
            entry("PLAIN_FAILED_IN_AFTER_SYSTEM_STEPS", createPlainScenario(SUCCESS, List.of(), List.of(), List.of(),
                    List.of(createStep(FAILED)))),
            entry("EXAMPLE_FAILED_IN_BEFORE_SYSTEM_STEPS", createExamplesScenario(SUCCESS, List.of(createStep(FAILED)),
                    List.of(), List.of(), List.of())),
            entry("PLAIN_FAILED_IN_BEFORE_SYSTEM_STEPS", createPlainScenario(SUCCESS, List.of(createStep(FAILED)),
                    List.of(), List.of(), List.of()))
        );
        xrayExporterOptions.setTestExecutionKey(KEY);
        xrayExporterOptions.setTestExecutionSummary(SUMMARY);

        TestExecution execution = factory.create(scenarios);

        assertEquals(KEY, execution.getTestExecutionKey());
        TestExecutionInfo info = execution.getInfo();
        assertNotNull(info);
        assertEquals(SUMMARY, info.getSummary());
        List<TestExecutionItem> tests = execution.getTests();
        assertThat(tests, hasSize(13));
        assertExecutedItem(tests.get(0), TestExecutionItemStatus.PASS);
        assertExecutedItem(tests.get(1), TestExecutionItemStatus.FAIL);
        assertItem(tests.get(2), TestExecutionItemStatus.TODO, null, null);
        assertExecutedItem(tests.get(3), TestExecutionItemStatus.PASS);
        assertExecutedItem(tests.get(4), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(5), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(6), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(7), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(8), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(9), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(10), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(11), TestExecutionItemStatus.FAIL);
        assertExecutedItem(tests.get(12), TestExecutionItemStatus.FAIL);
    }

    @Test
    void createTestExecutionWithKeyOnly()
    {
        xrayExporterOptions.setTestExecutionKey(KEY);
        xrayExporterOptions.setTestExecutionSummary(null);

        TestExecution execution = factory.create(List.of());

        assertEquals(KEY, execution.getTestExecutionKey());
        assertNull(execution.getInfo());
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

    private static Scenario createPlainScenario(String outcome, List<Step> beforeSystemScenarioSteps,
            List<Step> beforeUserScenarioSteps, List<Step> afterUserScenarioSteps, List<Step> afterSystemScenarioSteps)
    {
        Scenario scenario = createScenario();
        scenario.setBeforeSystemScenarioSteps(beforeSystemScenarioSteps);
        scenario.setBeforeUserScenarioSteps(beforeUserScenarioSteps);
        scenario.setSteps(List.of(createStep(SUCCESS), createStep(outcome), createStep(COMMENT)));
        scenario.setAfterUserScenarioSteps(afterUserScenarioSteps);
        scenario.setAfterSystemScenarioSteps(afterSystemScenarioSteps);
        return scenario;
    }

    private static Scenario createManualScenario()
    {
        Scenario scenario = createScenario();
        scenario.setSteps(List.of(createStep(COMMENT), createStep(COMMENT)));
        return scenario;
    }

    private static Scenario createExamplesScenario(String outcome, List<Step> beforeSystemScenarioSteps,
            List<Step> beforeUserScenarioSteps, List<Step> afterUserScenarioSteps, List<Step> afterSystemScenarioSteps)
    {
        Scenario scenario = createScenario();
        Examples examples = new Examples();
        scenario.setExamples(examples);

        Example example1 = new Example();
        example1.setBeforeSystemScenarioSteps(beforeSystemScenarioSteps);
        example1.setBeforeUserScenarioSteps(beforeUserScenarioSteps);
        example1.setSteps(List.of(createStep(SUCCESS), createStep(SUCCESS), createStep(COMMENT)));
        example1.setAfterUserScenarioSteps(List.of(createStep(SUCCESS), createStep(SUCCESS)));
        example1.setAfterSystemScenarioSteps(afterSystemScenarioSteps);

        Example example2 = new Example();
        example2.setBeforeSystemScenarioSteps(beforeSystemScenarioSteps);
        example2.setBeforeUserScenarioSteps(List.of(createStep(COMMENT), createStep(SUCCESS), createStep(SUCCESS)));
        example2.setSteps(List.of(createStep(SUCCESS), createStep(outcome)));
        example2.setAfterUserScenarioSteps(afterUserScenarioSteps);
        example2.setAfterSystemScenarioSteps(afterSystemScenarioSteps);

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
