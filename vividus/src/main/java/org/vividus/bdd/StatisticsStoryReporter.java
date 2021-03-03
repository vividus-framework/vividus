/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.bdd;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.Timing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.model.Node;
import org.vividus.bdd.model.NodeContext;
import org.vividus.bdd.model.NodeType;
import org.vividus.bdd.model.Statistic;
import org.vividus.bdd.report.allure.event.StatusProvider;
import org.vividus.bdd.report.allure.model.StatusPriority;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.testcontext.TestContext;
import org.vividus.util.json.JsonUtils;

public class StatisticsStoryReporter extends NullStoryReporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsStoryReporter.class);

    private static final Map<NodeType, Statistic> AGGREGATOR = new EnumMap<>(NodeType.class);

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

    private File statisticsFolder;

    public StatisticsStoryReporter(EventBus eventBus, TestContext testContext, JsonUtils jsonUtils)
    {
        eventBus.register(this);
        this.testContext = testContext;
        this.jsonUtils = jsonUtils;
        Stream.of(NodeType.values()).forEach(t -> AGGREGATOR.put(t, new Statistic()));
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        updateStepStatus(Status.from(StatusPriority.from(event)));
    }

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        if (givenStory)
        {
            appendContainer(NodeType.GIVEN_STORY);
        }
        else
        {
            testContext.put(NodeContext.class, new NodeContext().withRoot(new Node(NodeType.STORY)));
        }
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        Node node = appendContainer(NodeType.SCENARIO);
        if (!scenario.getSteps().isEmpty())
        {
            node.setHasChildrens(true);
        }
    }

    private Node appendContainer(NodeType type)
    {
        NodeContext context = context();
        Node current = context.getRoot();
        if (context.getTail() != null)
        {
            current = context.getTail();
        }
        Node node = new Node(type);
        current.addChild(node);
        context.setTail(node);
        return node;
    }

    @Override
    public void afterScenario(Timing timing)
    {
        completeNode();
    }

    @Override
    public void afterStory(boolean givenStory)
    {
        if (context() != null && !givenStory)
        {
            calculateStatus(context().getRoot());
            testContext.remove(NodeContext.class);
        }
        else if (givenStory)
        {
            completeNode();
        }
        super.afterStory(givenStory);
    }

    private void completeNode()
    {
        NodeContext context = context();
        Node node = context.getTail();
        calculateStatus(node);
        context.setTail(node.getParent());
    }

    @Override
    public void beforeStep(String step)
    {
        Node node = new Node(NodeType.STEP);
        if (isRoot())
        {
            context().getRoot().addChild(node);
            return;
        }
        context().getTail().addChild(node);
    }

    @Override
    public void successful(String step)
    {
        updateStepStatus(Status.PASSED);
    }

    @Override
    public void ignorable(String step)
    {
        updateStepStatus(Status.SKIPPED);
    }

    @Override
    public void comment(String step)
    {
        updateStepStatus(Status.SKIPPED);
    }

    @Override
    public void pending(String step)
    {
        updateStepStatus(Status.PENDING);
    }

    @Override
    public void notPerformed(String step)
    {
        updateStepStatus(Status.SKIPPED);
    }

    @Override
    public void failed(String step, Throwable throwable)
    {
        if (!StringUtils.contains(step, "verifyIfAssertionsPassed"))
        {
            Status status = Status.from(StatusPriority.from(StatusProvider.getStatus(throwable)));
            updateStepStatus(status);
        }
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

    private void calculateStatus(Node node)
    {
        if (hasNoExecutedChildrens(node))
        {
            node.withStatus(Status.SKIPPED);
            aggregate(node);
            return;
        }
        node.getChildren().stream()
                          .map(n ->
                          {
                              if (n.getType() == NodeType.STEP)
                              {
                                  aggregate(n);
                              }
                              return n;
                          })
                          .reduce((l, r) -> l.getStatus().getPriority() < r.getStatus().getPriority() ? l : r)
                          .map(Node::getStatus)
                          .map(node::withStatus)
                          .ifPresentOrElse(this::aggregate, () -> {
                              node.withStatus(Status.PASSED);
                              aggregate(node);
                          });
    }

    private boolean hasNoExecutedChildrens(Node node)
    {
        return node.getChildren().isEmpty() && node.isHasChildrens();
    }

    private void aggregate(Node node)
    {
        Statistic stat = AGGREGATOR.get(node.getType());
        mapper.get(node.getStatus()).accept(stat);
    }

    private void updateStepStatus(Status status)
    {
        NodeContext context = context();
        Node currentNode = isRoot() ? context.getRoot() : context.getTail();
        Iterator<Node> iterator = currentNode.getChildren().descendingIterator();
        while (iterator.hasNext())
        {
            Node node = iterator.next();
            if (node.getStatus() == null)
            {
                node.withStatus(status);
                return;
            }
        }
    }

    private NodeContext context()
    {
        return testContext.get(NodeContext.class);
    }

    private boolean isRoot()
    {
        return context().getTail() == null;
    }

    public static Map<NodeType, Statistic> getStatistics()
    {
        return Map.copyOf(AGGREGATOR);
    }

    public void setStatisticsFolder(File statisticsFolder)
    {
        this.statisticsFolder = statisticsFolder;
    }
}
