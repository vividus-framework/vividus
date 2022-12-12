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

package org.vividus.results;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.AbstractReportControlStoryReporter;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunContext;
import org.vividus.report.allure.model.Status;
import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.ExitCode;
import org.vividus.results.model.Failure;
import org.vividus.results.model.Node;
import org.vividus.results.model.NodeContext;
import org.vividus.results.model.Statistic;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.testcontext.TestContext;
import org.vividus.util.json.JsonUtils;

@SuppressWarnings({ "PMD.GodClass", "PMD.ExcessiveImports" })
public class CollectingStatisticsStoryReporter extends AbstractReportControlStoryReporter implements ResultsProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectingStatisticsStoryReporter.class);

    private final AtomicReference<Optional<Status>> status = new AtomicReference<>(Optional.empty());
    private final AtomicBoolean recordStatus = new AtomicBoolean(true);
    private final Map<ExecutableEntity, Statistic> statistics = new EnumMap<>(ExecutableEntity.class);
    private final Optional<List<Failure>> failures;
    private final Stopwatch stopwatch;

    private final Map<Status, Consumer<Statistic>> mapper = Map.of(
            Status.PASSED, Statistic::incrementPassed,
            Status.FAILED, Statistic::incrementFailed,
            Status.BROKEN, Statistic::incrementBroken,
            Status.PENDING, Statistic::incrementPending,
            Status.KNOWN_ISSUES_ONLY, Statistic::incrementKnownIssue,
            Status.SKIPPED, Statistic::incrementSkipped
    );

    private final TestContext testContext;
    private final JsonUtils jsonUtils;

    private final File statisticsFolder;

    public CollectingStatisticsStoryReporter(boolean collectFailures, File statisticsFolder,
            ReportControlContext reportControlContext, RunContext runContext, TestContext testContext,
            JsonUtils jsonUtils)
    {
        super(reportControlContext, runContext);
        this.testContext = testContext;
        this.jsonUtils = jsonUtils;
        this.statisticsFolder = statisticsFolder;
        Stream.of(ExecutableEntity.values()).forEach(t -> statistics.put(t, new Statistic()));
        failures = collectFailures ? Optional.of(new ArrayList<>()) : Optional.empty();
        stopwatch = Stopwatch.createUnstarted();
    }

    @Override
    public Map<ExecutableEntity, Statistic> getStatistics()
    {
        return Map.copyOf(statistics);
    }

    @Override
    public Optional<List<Failure>> getFailures()
    {
        return failures.map(ArrayList::new);
    }

    @Override
    public ExitCode calculateExitCode()
    {
        return status.get().map(s ->
        {
            switch (s)
            {
                case PASSED:
                    return ExitCode.PASSED;
                case KNOWN_ISSUES_ONLY:
                    return ExitCode.KNOWN_ISSUES;
                default:
                    return ExitCode.FAILED;
            }
        }).orElse(ExitCode.FAILED);
    }

    @Override
    public Duration getDuration()
    {
        return stopwatch.elapsed();
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        addFailure(() -> event.getSoftAssertionError().getError().getMessage());
        updateStepStatus(Status.from(event));
    }

    private void addFailure(Supplier<String> message)
    {
        failures.ifPresent(
                f -> f.add(Failure.from(getRunContext().getRunningStory(), message.get()))
        );
    }

    @Override
    public void beforeStoriesSteps(Stage stage)
    {
        if (stage == Stage.AFTER)
        {
            stopwatch.stop();
        }

        recordStatus.set(false);
        super.beforeStoriesSteps(stage);
    }

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        if (givenStory)
        {
            startNode(ExecutableEntity.GIVEN_STORY);
        }
        else
        {
            testContext.put(NodeContext.class, new NodeContext(new Node(ExecutableEntity.STORY)));
        }
        super.beforeStory(story, givenStory);
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        Node node = startNode(ExecutableEntity.SCENARIO);
        if (!scenario.getSteps().isEmpty())
        {
            node.setHasChildren(true);
        }
        super.beforeScenario(scenario);
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter)
    {
        changeStatus(Status.SKIPPED);
        super.scenarioExcluded(scenario, filter);
    }

    @Override
    public void afterScenario(Timing timing)
    {
        super.afterScenario(timing);
        endNode();
    }

    @Override
    public void afterStory(boolean givenStory)
    {
        super.afterStory(givenStory);
        endNode();
        if (!givenStory)
        {
            testContext.remove(NodeContext.class);
        }
    }

    @Override
    public void beforeStep(Step step)
    {
        perform(() ->
        {
            if (step.getExecutionType() != StepExecutionType.COMMENT)
            {
                startNode(ExecutableEntity.STEP);
            }
        });
        super.beforeStep(step);
    }

    @Override
    public void successful(String step)
    {
        perform(() -> endStep(Status.PASSED));
        changeStatus(Status.PASSED);
        super.successful(step);
    }

    @Override
    public void ignorable(String step)
    {
        endStep(Status.SKIPPED);
        changeStatus(Status.SKIPPED);
        super.ignorable(step);
    }

    @Override
    public void pending(PendingStep step)
    {
        endStep(Status.PENDING);
        changeStatus(Status.PENDING);
        super.pending(step);
    }

    @Override
    public void notPerformed(String step)
    {
        endStep(Status.SKIPPED);
        changeStatus(Status.SKIPPED);
        super.notPerformed(step);
    }

    @Override
    public void failed(String step, Throwable throwable)
    {
        Status stepStatus = Status.from(throwable);
        perform(() ->
        {
            if (stepStatus == Status.BROKEN)
            {
                addFailure(() -> throwable.getCause().toString());
            }
            endStep(stepStatus);
        });
        changeStatus(stepStatus);
        super.failed(step, throwable);
    }

    @Override
    public void afterStoriesSteps(Stage stage)
    {
        super.afterStoriesSteps(stage);
        recordStatus.set(true);

        if (stage == Stage.BEFORE)
        {
            stopwatch.start();
        }
        if (stage == Stage.AFTER)
        {
            String targetFileName = "statistics.json";
            try
            {
                Files.createDirectories(statisticsFolder.toPath());
                Path statisticsFilePath = statisticsFolder.toPath().resolve(targetFileName);
                String results = jsonUtils.toPrettyJson(this.statistics);
                Files.writeString(statisticsFilePath, results, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
            catch (IOException e)
            {
                LOGGER.atDebug()
                       .addArgument(targetFileName)
                       .addArgument(statisticsFolder::getAbsoluteFile)
                       .setCause(e)
                       .log("Unable to write {} into folder: {}");
            }
        }
    }

    private Node startNode(ExecutableEntity type)
    {
        Node node = new Node(type);
        context().addToTail(node);
        return node;
    }

    private void updateStepStatus(Status status)
    {
        updateNodeStatus(context().getTail(), status);
    }

    private void endStep(Status status)
    {
        updateStepStatus(status);
        endNode();
    }

    private void endNode()
    {
        Node currentNode = context().getTail();
        if (currentNode.getChildren().isEmpty() && currentNode.isHasChildren())
        {
            currentNode.withStatus(Status.SKIPPED);
        }
        else if (currentNode.getStatus() == null)
        {
            currentNode.withStatus(Status.PASSED);
        }
        currentNode.getChildren().stream()
                          .reduce((l, r) -> l.getStatus().getPriority() < r.getStatus().getPriority() ? l : r)
                          .map(Node::getStatus)
                          .ifPresent(newStatus -> updateNodeStatus(currentNode, newStatus));

        // Skip statistics increment for "wrapping" steps: composite steps and steps executing nested steps are
        // considered as containers (no business logic), thus they are not counted in the overall statistics
        if (currentNode.getType() != ExecutableEntity.STEP || currentNode.getChildren().isEmpty())
        {
            Statistic targetStatisticsCategory = statistics.get(currentNode.getType());
            mapper.get(currentNode.getStatus()).accept(targetStatisticsCategory);
        }

        context().removeFromTail();
    }

    private void updateNodeStatus(Node node, Status status)
    {
        if (node.getStatus() == null || node.getStatus().getPriority() > status.getPriority())
        {
            node.withStatus(status);
        }
    }

    private NodeContext context()
    {
        return testContext.get(NodeContext.class);
    }

    private void changeStatus(Status newStatus)
    {
        if (recordStatus.get())
        {
            status.updateAndGet(
                    currentStatus -> currentStatus.filter(s -> s.getPriority() <= newStatus.getPriority())
                                                  .or(() -> Optional.of(newStatus))
            );
        }
    }
}
