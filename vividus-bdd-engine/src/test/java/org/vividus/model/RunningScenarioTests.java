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

package org.vividus.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.Scenario;
import org.junit.jupiter.api.Test;

class RunningScenarioTests
{
    @Test
    void testGetNonNullRunningScenarioTitle()
    {
        String title  = "runningScenarioTitle";
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(new Scenario(null, List.of()));
        runningScenario.setTitle(title);
        assertEquals(title, runningScenario.getTitle());
    }

    @Test
    void testGetNullRunningScenarioTitle()
    {
        String title  = "scenarioTitle";
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(new Scenario(title, List.of()));
        assertEquals(title, runningScenario.getTitle());
    }

    @Test
    void shouldReturnEmptyExampleByDefault()
    {
        assertEquals(Map.of(), new RunningScenario().getExample());
    }
}
