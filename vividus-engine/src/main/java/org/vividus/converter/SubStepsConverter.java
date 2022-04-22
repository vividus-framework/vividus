/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.converter;

import static java.util.stream.Collectors.collectingAndThen;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Step;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.parser.IStepExamplesTableParser;
import org.vividus.steps.SubSteps;

@Named
public class SubStepsConverter extends AbstractParameterConverter<String, SubSteps>
{
    @Inject private Configuration configuration;
    @Inject private RunContext runContext;
    @Inject private Embedder embedder;
    @Inject private IStepExamplesTableParser stepExamplesTableParser;

    @Override
    public SubSteps convertValue(String value, Type type)
    {
        StoryReporter storyReporter = configuration.storyReporter(
                runContext.getRootRunningStory().getStory().getPath());
        if (storyReporter instanceof DelegatingStoryReporter)
        {
            // Need to exclude JUnitScenarioReporter from reporting of sub steps
            storyReporter = ((DelegatingStoryReporter) storyReporter).getDelegates()
                    .stream()
                    .filter(e -> !(e instanceof ConcurrentStoryReporter))
                    .collect(collectingAndThen(Collectors.toList(), DelegatingStoryReporter::new));
        }
        ExamplesTable subStepsTable = configuration.examplesTableFactory().createExamplesTable(value);

        Map<String, String> parameters = Optional.ofNullable(runContext.getRunningStory().getRunningScenario())
                .map(RunningScenario::getExample)
                .orElseGet(Map::of);

        List<Step> steps = stepExamplesTableParser.parse(subStepsTable, parameters);
        return new SubSteps(storyReporter, embedder, steps);
    }
}
