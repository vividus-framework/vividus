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

package org.vividus.reportportal.jbehave;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import com.epam.reportportal.jbehave.ReportPortalStoryReporter;
import com.epam.reportportal.listeners.LogLevel;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.tree.TestItemTree.TestItemLeaf;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.ThreadSafeReporter;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.vividus.softassert.event.AssertionFailedEvent;

public class AdaptedDelegatingReportPortalStoryReporter extends DelegatingStoryReporter implements ThreadSafeReporter
{
    private final ReportPortalStoryReporter reporter;
    private final List<TestItemLeaf> failedSteps = new ArrayList<>();

    public AdaptedDelegatingReportPortalStoryReporter(EventBus eventBus, ReportPortalStoryReporter reporter)
    {
        super(reporter);
        this.reporter = reporter;
        eventBus.register(this);
    }

    @Override
    public void beforeStep(Step step)
    {
        if (step.getExecutionType() == StepExecutionType.COMMENT)
        {
            return;
        }
        runIfNotSystem(step.getStepAsString(), reporter::beforeStep);
    }

    @Override
    public void successful(String step)
    {
        reporter.getLastStep()
                .map(failedSteps::remove)
                .filter(Boolean.TRUE::equals)
                .ifPresentOrElse(e -> reporter.failed(step, null),
                    () -> runIfNotSystem(step, reporter::successful));
    }

    private void runIfNotSystem(String step, Consumer<String> toRun)
    {
        if (!step.contains("afterStories"))
        {
            toRun.accept(step);
        }
    }

    @Override
    public void afterScenario(Timing timing)
    {
        reporter.afterScenario();
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter)
    {
        reporter.scenarioNotAllowed(scenario, filter);
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        reporter.getLastStep().ifPresent(s -> {
            failedSteps.add(s);
            ReportPortal.emitLog(s.getItemId(), itemUuid -> {
                SaveLogRQ rq = new SaveLogRQ();
                rq.setItemUuid(itemUuid);
                rq.setLevel(LogLevel.ERROR.name());
                rq.setLogTime(new Date());
                rq.setMessage(ExceptionUtils.getStackTrace(event.getSoftAssertionError().getError()));
                return rq;
            });
        });
    }
}
