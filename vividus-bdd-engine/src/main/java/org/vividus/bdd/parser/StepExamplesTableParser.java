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

package org.vividus.bdd.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.embedder.MatchingStepMonitor;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Step;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.spring.ExtendedConfiguration;

public class StepExamplesTableParser implements IStepExamplesTableParser
{
    private static final String STEP_COLUMN_NAME = "step";

    private final ExtendedConfiguration configuration;
    private final InjectableStepsFactory stepsFactory;

    public StepExamplesTableParser(ExtendedConfiguration configuration, InjectableStepsFactory stepsFactory,
            IBddRunContext bddRunContext)
    {
        this.configuration = configuration;
        this.stepsFactory = stepsFactory;
    }

    @Override
    public List<Step> parse(ExamplesTable stepsAsTable, Map<String, String> currentExample)
    {
        Validate.isTrue(List.of(STEP_COLUMN_NAME).equals(stepsAsTable.getHeaders()),
                "The steps examples table must have only one column with the name '%s', but got %s", STEP_COLUMN_NAME,
                getErrorMessage(stepsAsTable.getHeaders()));

        String storyAsText = stepsAsTable.getRows().stream()
                .map(p -> p.get(STEP_COLUMN_NAME))
                .collect(Collectors.joining(System.lineSeparator()));
        List<String> stepsAsText = new ArrayList<>();
        if (!storyAsText.isEmpty())
        {
            stepsAsText.addAll(configuration.storyParser().parseStory(storyAsText).getScenarios().get(0).getSteps());
        }
        Scenario scenario = new Scenario(stepsAsText);
        MatchingStepMonitor monitor = new MatchingStepMonitor(configuration.stepMonitor());
        return configuration.stepCollector().collectScenarioSteps(stepsFactory.createCandidateSteps(),
                scenario, currentExample, monitor);
    }

    private static String getErrorMessage(List<String> headers)
    {
        return headers.isEmpty() ? "empty table" : String.join(", ", headers);
    }
}
