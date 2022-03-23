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

package org.vividus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunContext;
import org.vividus.model.Failure;
import org.vividus.model.Node;
import org.vividus.model.NodeContext;
import org.vividus.model.NodeType;
import org.vividus.model.Statistic;
import org.vividus.report.allure.event.StatusProvider;
import org.vividus.report.allure.model.StatusPriority;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.testcontext.TestContext;
import org.vividus.util.json.JsonUtils;

@SuppressWarnings({ "PMD.GodClass", "PMD.ExcessiveImports" })
public class StatisticsStoryReporter extends AbstractReportControlStoryReporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsStoryReporter.class);

    private static final Map<NodeType, Statistic> AGGREGATOR = new EnumMap<>(NodeType.class);
    private static final FailuresBox FAILURES = new FailuresBox();

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

    private boolean collectFailures;
    private File statisticsFolder;

    public StatisticsStoryReporter(ReportControlContext reportControlContext, RunContext runContext,
            EventBus eventBus, TestContext testContext, JsonUtils jsonUtils)
    {
        super(reportControlContext, runContext);
        eventBus.register(this);
        this.testContext = testContext;
        this.jsonUtils = jsonUtils;
        Stream.of(NodeType.values()).forEach(t -> AGGREGATOR.put(t, new Statistic()));
    }

    public void init()
    {
        if (collectFailures)
        {
            FAILURES.enable();
        }
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        addFailure(() -> event.getSoftAssertionError().getError().getMessage());
        updateStepStatus(Status.from(StatusPriority.from(event)));
    }

    private void addFailure(Supplier<String> message)
    {
        if (collectFailures)
        {
            FAILURES.add(Failure.from(getRunContext().getRunningStory(), message.get()));
        }
    }

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        if (givenStory)
        {
            startNode(NodeType.GIVEN_STORY);
        }
        else
        {
            testContext.put(NodeContext.class, new NodeContext(new Node(NodeType.STORY)));
        }
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        Node node = startNode(NodeType.SCENARIO);
        if (!scenario.getSteps().isEmpty())
        {
            node.setHasChildren(true);
        }
    }

    @Override
    public void afterScenario(Timing timing)
    {
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
                startNode(NodeType.STEP);
            }
        });
    }

    @Override
    public void successful(String step)
    {
        perform(() -> endStep(Status.PASSED));
    }

    @Override
    public void ignorable(String step)
    {
        endStep(Status.SKIPPED);
    }

    @Override
    public void pending(String step)
    {
        endStep(Status.PENDING);
    }

    @Override
    public void notPerformed(String step)
    {
        endStep(Status.SKIPPED);
    }

    @Override
    public void failed(String step, Throwable throwable)
    {
        perform(() ->
        {
            Status status = Status.from(StatusPriority.from(StatusProvider.getStatus(throwable)));
            if (status == Status.BROKEN)
            {
                addFailure(() -> throwable.getCause().toString());
            }
            endStep(status);
        });
    }

    @Override
    public void afterStoriesSteps(Stage stage)
    {
        if (stage == Stage.AFTER)
        {
            try
            {
                Files.createDirectories(statisticsFolder.toPath());
                Path statistics = statisticsFolder.toPath().resolve("statistics.json");
                String results = jsonUtils.toPrettyJson(AGGREGATOR);
                Files.write(statistics, results.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
            catch (IOException e)
            {
                LOGGER.atDebug()
                      .addArgument(statisticsFolder::getAbsoluteFile)
                      .setCause(e)
                      .log("Unable to write statistics.json into folder: {}");
            }
        }
    }

    private Node startNode(NodeType type)
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
                          .ifPresent(status -> updateNodeStatus(currentNode, status));

        // Skip statistics increment for "wrapping" steps: composite steps and steps executing nested steps are
        // considered as containers (no business logic), thus they are not counted in the overall statistics
        if (currentNode.getType() != NodeType.STEP || currentNode.getChildren().isEmpty())
        {
            Statistic targetStatisticsCategory = AGGREGATOR.get(currentNode.getType());
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

    public static Map<NodeType, Statistic> getStatistics()
    {
        return Map.copyOf(AGGREGATOR);
    }

    public static List<Failure> getFailures()
    {
        return FAILURES.getFailures();
    }

    public void setStatisticsFolder(File statisticsFolder)
    {
        this.statisticsFolder = statisticsFolder;
    }

    public void setCollectFailures(boolean collectFailures)
    {
        this.collectFailures = collectFailures;
    }

    private static final class FailuresBox
    {
        private static final List<Failure> FAILURES = new ArrayList<>();

        private boolean enabled;

        private void add(Failure failure)
        {
            FAILURES.add(failure);
        }

        @SuppressWarnings("NoNullForCollectionReturn")
        private List<Failure> getFailures()
        {
            return enabled ? new ArrayList<>(FAILURES) : null;
        }

        private void enable()
        {
            enabled = true;
        }
    }
}
