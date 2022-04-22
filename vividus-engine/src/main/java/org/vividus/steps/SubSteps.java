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

package org.vividus.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.embedder.PerformableTree.State;
import org.jbehave.core.failures.IgnoringStepsFailure;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCreator.AbstractStep;
import org.jbehave.core.steps.StepResult;

public class SubSteps
{
    private final StoryReporter storyReporter;
    private final Embedder embedder;
    private final List<Step> steps;

    public SubSteps(StoryReporter storyReporter, Embedder embedder, List<Step> steps)
    {
        this.storyReporter = storyReporter;
        this.embedder = embedder;
        this.steps = steps;
    }

    public void execute(Optional<Supplier<String>> stepContextInfoProvider)
    {
        RunContext context = embedder.storyManager().getContext();

        UUIDExceptionWrapper failure = null;
        for (Step step : steps)
        {
            DecoratingResultStep decoratingResultStep = new DecoratingResultStep(step,
                    context.configuration().keywords(), stepContextInfoProvider);

            StepResult stepResult = executeStep(decoratingResultStep, context);
            if (failure == null)
            {
                failure = stepResult.getFailure();
            }
        }
        if (context.state().getFailure() instanceof IgnoringStepsFailure)
        {
            throw context.state().getFailure();
        }
        if (failure != null)
        {
            throw failure;
        }
    }

    private StepResult executeStep(Step step, RunContext context)
    {
        List<StepResult> results = new ArrayList<>();
        State state = context.state().run(step, results, context.configuration().keywords(), storyReporter);
        context.stateIs(state);

        // The last one is the result of the deepest composed step
        StepResult stepResult = results.get(results.size() - 1);

        Throwable rootCause = ExceptionUtils.getRootCause(stepResult.getFailure());
        if (rootCause instanceof InterruptedException)
        {
            throw new IllegalStateException(stepResult.getFailure());
        }
        else if (rootCause instanceof Error)
        {
            throw (Error) rootCause;
        }
        else if (rootCause instanceof PendingStepFound)
        {
            throw new UUIDExceptionWrapper(rootCause);
        }
        return stepResult;
    }

    static class DecoratingResultStep extends AbstractStep
    {
        private final Step step;
        private final Keywords keywords;
        private final Optional<Supplier<String>> stepContextInfoProvider;

        DecoratingResultStep(Step step, Keywords keywords, Optional<Supplier<String>> stepContextInfoProvider)
        {
            this.step = step;
            this.keywords = keywords;
            this.stepContextInfoProvider = stepContextInfoProvider;
        }

        @Override
        public StepResult perform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened)
        {
            return withStepContextInfo(() -> step.perform(storyReporter, storyFailureIfItHappened));
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened)
        {
            return withStepContextInfo(() -> step.doNotPerform(storyReporter, storyFailureIfItHappened));
        }

        @Override
        public String asString(Keywords keywords)
        {
            return step.asString(keywords);
        }

        @Override
        public List<Step> getComposedSteps()
        {
            return step.getComposedSteps();
        }

        @SuppressWarnings({ "checkstyle:IllegalCatchExtended", "PMD.AvoidCatchingGenericException" })
        public StepResult withStepContextInfo(Supplier<StepResult> stepResultSupplier)
        {
            Optional<String> stepContextInfo = stepContextInfoProvider.map(Supplier::get);
            StepResult stepResult = stepResultSupplier.get();
            stepContextInfo.map(contextInfo ->
            {
                try
                {
                    return this.asString(keywords) + " [" + contextInfo + "]";
                }
                catch (Exception e)
                {
                    return null;
                }
            }).ifPresent(stepResult::withParameterValues);
            return stepResult;
        }
    }
}
