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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepResult;

public class SubSteps
{
    private final Configuration configuration;
    private final StoryReporter storyReporter;
    private final List<Step> steps;

    public SubSteps(Configuration configuration, StoryReporter storyReporter, List<Step> steps)
    {
        this.configuration = configuration;
        this.storyReporter = storyReporter;
        this.steps = steps;
    }

    public void execute(Optional<Supplier<String>> stepContextInfoProvider)
    {
        steps.forEach(step -> executeStep(stepContextInfoProvider, step));
    }

    private void executeStep(Optional<Supplier<String>> stepContextInfoProvider, Step step)
    {
        Optional<String> stepContextInfo = stepContextInfoProvider.map(Supplier::get);
        StepResult stepResult = step.perform(storyReporter, null);
        stepContextInfo.map(contextInfo -> step.asString(configuration.keywords()) + " [" + contextInfo + "]")
                .ifPresent(stepResult::withParameterValues);
        UUIDExceptionWrapper stepFailure = stepResult.getFailure();
        PerformableTree.State state = stepFailure == null ? new PerformableTree.FineSoFar()
                : new PerformableTree.SomethingHappened(stepFailure);
        for (Step composedStep : step.getComposedSteps())
        {
            state = state.run(composedStep, new ArrayList<>(), storyReporter);
        }
        stepResult.describeTo(storyReporter);
        UUIDExceptionWrapper stepResultFailure = stepResult.getFailure();
        Throwable rootCause = ExceptionUtils.getRootCause(stepResultFailure);
        if (rootCause instanceof InterruptedException)
        {
            throw new IllegalStateException(stepResultFailure);
        }
        else if (rootCause instanceof Error)
        {
            throw (Error) rootCause;
        }
    }
}
