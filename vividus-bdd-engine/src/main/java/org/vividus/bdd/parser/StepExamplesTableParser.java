/*
 * Copyright 2019 the original author or authors.
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

import org.jbehave.core.embedder.MatchingStepMonitor;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.Step;
import org.vividus.bdd.ExtendedEmbedder;
import org.vividus.bdd.spring.Configuration;

public class StepExamplesTableParser implements IStepExamplesTableParser
{
    private Configuration configuration;
    private ExtendedEmbedder embedder;

    @Override
    public List<Step> parse(ExamplesTable stepsAsTable)
    {
        String storyAsText = stepsAsTable.getRowsAsParameters().stream()
                .map(p -> p.<String>valueAs("step", String.class)).collect(Collectors.joining(System.lineSeparator()));
        List<String> stepsAsText = new ArrayList<>();
        if (!storyAsText.isEmpty())
        {
            stepsAsText.addAll(configuration.storyParser().parseStory(storyAsText).getScenarios().get(0).getSteps());
        }
        Scenario scenario = new Scenario(stepsAsText);
        MatchingStepMonitor monitor = new MatchingStepMonitor(configuration.stepMonitor());
        return configuration.stepCollector().collectScenarioSteps(embedder.stepsFactory().createCandidateSteps(),
                scenario, Map.of(), monitor);
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setEmbedder(ExtendedEmbedder embedder)
    {
        this.embedder = embedder;
    }
}
