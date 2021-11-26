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

package org.vividus.xray.converter;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.vividus.model.jbehave.Example;
import org.vividus.model.jbehave.Examples;
import org.vividus.model.jbehave.Parameters;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.xray.converter.CucumberScenarioConverter.CucumberScenario;

class CucumberScenarioConverterTests
{
    private static final String STEP_VALUE = "When I perform action ";
    private static final String KEY = "key ";
    private static final String VALUE = "value ";

    @Test
    void shouldConvertPlainScenario()
    {
        Scenario scenario = new Scenario();
        List<Step> steps = List.of(
            createStep(STEP_VALUE + 1),
            createStep(STEP_VALUE + 2),
            createStep("!-- " + STEP_VALUE + 3),
            createStep(STEP_VALUE + 4)
        );
        scenario.setSteps(steps);

        CucumberScenario outcome = CucumberScenarioConverter.convert(scenario);
        assertEquals("Scenario", outcome.getType());
        String scenarioAsString = STEP_VALUE + 1 + lineSeparator()
            + STEP_VALUE + 2 + lineSeparator()
            + "# " + STEP_VALUE + 3 + lineSeparator()
            + STEP_VALUE + 4;
        assertEquals(scenarioAsString, outcome.getScenario());
    }

    @Test
    void shouldConvertParameterizedScenario()
    {
        Scenario scenario = new Scenario();
        Examples examples = new Examples();
        Parameters parameters = new Parameters();
        parameters.setNames(List.of(KEY + 1, KEY + 2));
        parameters.setValues(List.of(
            List.of(VALUE + 11, VALUE + 12),
            List.of(VALUE + 21, VALUE + 22)
        ));
        examples.setParameters(parameters);
        scenario.setExamples(examples);
        Example example = new Example();
        List<Step> steps = List.of(
            createStep(STEP_VALUE + 1),
            createStep(STEP_VALUE + 2),
            createStep(STEP_VALUE + 3)
        );
        example.setSteps(steps);
        examples.setExamples(List.of(example));

        CucumberScenario outcome = CucumberScenarioConverter.convert(scenario);
        assertEquals("Scenario Outline", outcome.getType());
        String scenarioAsString = STEP_VALUE + 1 + lineSeparator()
            + STEP_VALUE + 2 + lineSeparator()
            + STEP_VALUE + 3 + lineSeparator()
            + "Examples:" + lineSeparator()
            + "|key 1|key 2|" + lineSeparator()
            + "|value 11|value 12|" + lineSeparator()
            + "|value 21|value 22|" + lineSeparator();
        assertEquals(scenarioAsString, outcome.getScenario());
    }

    private Step createStep(String value)
    {
        Step step = new Step();
        step.setValue(value);
        step.setOutcome("successful");
        return step;
    }
}
