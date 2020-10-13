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

package org.vividus.bdd.model.jbehave;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScenarioTests
{
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
}
