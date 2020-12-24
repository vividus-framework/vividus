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

package org.vividus.xray.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.ListUtils;
import org.vividus.bdd.model.jbehave.Example;
import org.vividus.bdd.model.jbehave.Lifecycle;
import org.vividus.bdd.model.jbehave.Parameters;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.bdd.model.jbehave.Story;

public final class StoryUtils
{
    private StoryUtils()
    {
    }

    public static List<Scenario> getFoldedScenarios(Story story)
    {
        Lifecycle lifecycle = story.getLifecycle();
        List<Scenario> scenarios = story.getScenarios();

        if (lifecycle == null || lifecycle.getParameters() == null)
        {
            return scenarios;
        }

        Parameters lifecycleParameters = lifecycle.getParameters();
        int partition = scenarios.size() <= 1 ? 1 : scenarios.size() / lifecycleParameters.getValues().size();
        List<List<Scenario>> scenariosPerExample = ListUtils.partition(scenarios, partition);

        return IntStream.range(0, partition)
                        .mapToObj(index -> scenariosPerExample.stream()
                                                              .map(l -> l.get(index))
                                                              .collect(Collectors.toList()))
                        .map(s -> s.stream()
                                   .reduce(StoryUtils::merge))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .peek(s -> adjustScenarioWithLifecycle(s.getExamples().getParameters(), lifecycleParameters))
                        .collect(Collectors.toList());
    }

    private static Scenario merge(Scenario left, Scenario right)
    {
        List<Example> examples = left.getExamples().getExamples();
        examples.addAll(right.getExamples().getExamples());
        return left;
    }

    private static void adjustScenarioWithLifecycle(Parameters scenarioParameters, Parameters lifecycleParameters)
    {
        List<List<String>> lifecycleValues = lifecycleParameters.getValues();
        scenarioParameters.getNames().addAll(lifecycleParameters.getNames());

        List<List<String>> baseValues = scenarioParameters.getValues();
        if (baseValues.isEmpty())
        {
            baseValues.addAll(lifecycleValues);
            return;
        }

        List<List<String>> adjustedValues = new ArrayList<>(lifecycleValues.size() * baseValues.size());
        lifecycleValues.forEach(lifecycleRow -> baseValues
                .forEach(scenarioRow -> Stream.concat(scenarioRow.stream(), lifecycleRow.stream())
                        .collect(Collectors.collectingAndThen(Collectors.toList(), adjustedValues::add))));
        scenarioParameters.setValues(adjustedValues);
    }
}
