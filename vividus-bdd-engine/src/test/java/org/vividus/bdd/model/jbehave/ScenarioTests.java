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

package org.vividus.bdd.model.jbehave;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScenarioTests
{
    private static final String META = "meta";
    private static final String VALUE = "value";

    @Mock private Step step;

    @Test
    void shouldFindStepsInScenarioWithoutExamples()
    {
        Scenario scenario = new Scenario();
        scenario.setSteps(List.of(step));
        assertEquals(List.of(step), scenario.collectSteps());
    }

    @Test
    void shouldFindStepsInScenarioWithExamples()
    {
        Scenario scenario = new Scenario();
        Examples examples = new Examples();
        scenario.setExamples(examples);
        Example example = new Example();
        examples.setExamples(List.of(example));
        example.setSteps(List.of(step));
        assertEquals(List.of(step), scenario.collectSteps());
    }

    @Test
    void shouldReturnAllMetaValues()
    {
        Scenario scenario = new Scenario();
        scenario.setMeta(List.of(
            createMeta(META, " value3 ; value1 ; value2 "),
            createMeta("cars", "geely; LADA; Moskvitch")
        ));
        Set<String> values = scenario.getMetaValues(META);
        List<String> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);
        assertEquals(List.of("value1", "value2", "value3"), sortedValues);
    }

    @Test
    void shouldReturnUniqueMetaValue() throws NotUniqueMetaValueException
    {
        Scenario scenario = new Scenario();
        scenario.setMeta(List.of(createMeta(META, VALUE)));
        Optional<String> metaValue = scenario.getUniqueMetaValue(META);
        assertTrue(metaValue.isPresent());
        assertEquals(VALUE, metaValue.get());
    }

    @Test
    void shouldThrowAnExceptionIfMoreThanValueFoundButExpectedUnique() throws NotUniqueMetaValueException
    {
        Scenario scenario = new Scenario();
        scenario.setMeta(List.of(createMeta(META, "value1;value2")));
        NotUniqueMetaValueException thrown = assertThrows(NotUniqueMetaValueException.class,
            () -> scenario.getUniqueMetaValue(META));
        assertThat(thrown.getMessage(), matchesPattern(
                "Expected only one value for the 'meta' meta, but got: (value1, value2|value2, value1)"));
    }

    @Test
    void shouldReturnUniqueMetaValueWhenValueIsNotFound() throws NotUniqueMetaValueException
    {
        Scenario scenario = new Scenario();
        scenario.setMeta(List.of());
        assertTrue(scenario.getUniqueMetaValue(META).isEmpty());
    }

    @Test
    void shouldFilterOutMetaWithEmptyValues()
    {
        Scenario scenario = new Scenario();
        scenario.setMeta(List.of(createMeta(META, "")));
        assertThat(scenario.getMetaValues(META), hasSize(0));
    }

    @Test
    void shouldCheckIfScenarioHasMetaWithName()
    {
        Scenario scenario = new Scenario();
        scenario.setMeta(List.of(createMeta(META, VALUE)));
        assertTrue(scenario.hasMetaWithName(META));
    }

    private static Meta createMeta(String name, String value)
    {
        Meta meta = new Meta();
        meta.setName(name);
        meta.setValue(value);
        return meta;
    }
}
