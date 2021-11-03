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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vividus.bdd.model.jbehave.AbstractStepsContainer;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.model.TestExecution;
import org.vividus.xray.model.TestExecutionItem;
import org.vividus.xray.model.TestExecutionItemStatus;

@Component
public class TestExecutionFactory
{
    @Autowired private XrayExporterOptions xrayExporterOptions;

    public TestExecution create(List<Entry<String, Scenario>> scenarios)
    {
        TestExecution testExecution = new TestExecution();
        testExecution.setTestExecutionKey(xrayExporterOptions.getTestExecutionKey());

        List<TestExecutionItem> tests = scenarios.stream()
                                    .map(TestExecutionFactory::createTestInfo)
                                    .collect(Collectors.toList());
        testExecution.setTests(tests);

        return testExecution;
    }

    private static TestExecutionItem createTestInfo(Entry<String, Scenario> scenarioEntry)
    {
        TestExecutionItem test = new TestExecutionItem();
        test.setTestKey(scenarioEntry.getKey());

        Scenario scenario = scenarioEntry.getValue();
        if (scenario.isManual())
        {
            test.setStatus(TestExecutionItemStatus.TODO);
            return test;
        }

        test.setStart(asOffsetDateTime(scenario.getStart()));
        test.setFinish(asOffsetDateTime(scenario.getEnd()));

        if (scenario.getExamples() == null)
        {
            test.setStatus(calculateStatus(scenario));
        }
        else
        {
            List<TestExecutionItemStatus> exampleStatuses = scenario.getExamples().getExamples().stream()
                    .map(TestExecutionFactory::calculateStatus)
                    .collect(Collectors.toList());
            test.setExamples(exampleStatuses);

            TestExecutionItemStatus status = exampleStatuses.stream()
                                               .filter(ts -> ts == TestExecutionItemStatus.FAIL)
                                               .findFirst()
                                               .orElse(TestExecutionItemStatus.PASS);
            test.setStatus(status);
        }

        return test;
    }

    private static TestExecutionItemStatus calculateStatus(AbstractStepsContainer steps)
    {
        return Stream.of(steps.getBeforeUserScenarioSteps(), steps.getSteps(), steps.getAfterUserScenarioSteps())
                     .flatMap(List::stream)
                     .allMatch(step -> "successful".equals(step.getOutcome())) ? TestExecutionItemStatus.PASS
                             : TestExecutionItemStatus.FAIL;
    }

    private static String asOffsetDateTime(long millis)
    {
        long seconds = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
        Instant instant = Instant.ofEpochSecond(seconds);
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()).toString();
    }
}
