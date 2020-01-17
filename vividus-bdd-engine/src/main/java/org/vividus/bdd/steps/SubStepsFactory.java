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

package org.vividus.bdd.steps;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.Step;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.parser.IStepExamplesTableParser;
import org.vividus.bdd.spring.Configuration;

public class SubStepsFactory implements ISubStepsFactory
{
    @Inject private Configuration configuration;
    @Inject private IBddRunContext bddRunContext;
    @Inject private IStepExamplesTableParser stepExamplesTableParser;
    @Inject private ISubStepsListener subStepsListener;

    @Override
    public SubSteps createSubSteps(ExamplesTable stepsToExecute)
    {
        StoryReporter storyReporter = configuration.storyReporter(
                bddRunContext.getRootRunningStory().getStory().getPath());
        if (storyReporter instanceof DelegatingStoryReporter)
        {
            // Need to exclude JUnitScenarioReporter from reporting of sub steps
            Collection<StoryReporter> filteredDelegates = ((DelegatingStoryReporter) storyReporter).getDelegates()
                    .stream()
                    .filter(e -> !(e instanceof ConcurrentStoryReporter))
                    .collect(Collectors.toList());
            storyReporter = new DelegatingStoryReporter(filteredDelegates);
        }
        List<Step> steps = stepExamplesTableParser.parse(stepsToExecute);
        return new SubSteps(configuration, storyReporter, steps, subStepsListener);
    }
}
