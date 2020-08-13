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

package org.vividus.bdd.converter;

import static java.util.stream.Collectors.collectingAndThen;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.jbehave.core.steps.Step;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.parser.IStepExamplesTableParser;
import org.vividus.bdd.spring.ExtendedConfiguration;
import org.vividus.bdd.steps.SubSteps;

@Named
public class SubStepsConverter extends FunctionalParameterConverter<SubSteps>
{
    public SubStepsConverter(ExtendedConfiguration configuration, IBddRunContext bddRunContext,
            IStepExamplesTableParser stepExamplesTableParser)
    {
        super(subSteps -> {
            StoryReporter storyReporter = configuration.storyReporter(
                    bddRunContext.getRootRunningStory().getStory().getPath());
            if (storyReporter instanceof DelegatingStoryReporter)
            {
                // Need to exclude JUnitScenarioReporter from reporting of sub steps
                storyReporter = ((DelegatingStoryReporter) storyReporter).getDelegates()
                        .stream()
                        .filter(e -> !(e instanceof ConcurrentStoryReporter))
                        .collect(collectingAndThen(Collectors.toList(), DelegatingStoryReporter::new));
            }
            ExamplesTable subStepsTable = configuration.examplesTableFactory().createExamplesTable(subSteps);
            List<Step> steps = stepExamplesTableParser.parse(subStepsTable);
            return new SubSteps(configuration, storyReporter, steps);
        });
    }
}
