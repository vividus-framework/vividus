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

package org.vividus.report.allure;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.Timing;
import org.vividus.AbstractReportControlStoryReporter;
import org.vividus.JBehaveFailureUnwrapper;
import org.vividus.batch.BatchStorage;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.report.allure.adapter.IVerificationErrorAdapter;
import org.vividus.report.allure.model.ScenarioExecutionStage;
import org.vividus.report.allure.model.Status;
import org.vividus.report.allure.model.StoryExecutionStage;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.testcontext.TestContext;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.model.ExecutableItem;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.util.ResultsUtils;

@SuppressWarnings("checkstyle:MethodCount")
public class AllureStoryReporter extends AbstractReportControlStoryReporter
{
    private static final String CURRENT_STEP_KEY = "allureCurrentLinkedStep";
    private static final String PENDING_STEP_MARKER = "The step is not implemented";

    private final AllureLifecycle lifecycle;
    private final IAllureReportGenerator allureReportGenerator;
    private final BatchStorage batchStorage;
    private final TestContext testContext;
    private final IAllureRunContext allureRunContext;
    private final IVerificationErrorAdapter verificationErrorAdapter;

    public AllureStoryReporter(ReportControlContext reportControlContext, RunContext runContext,
            IAllureReportGenerator allureReportGenerator, BatchStorage batchStorage, TestContext testContext,
            IAllureRunContext allureRunContext, IVerificationErrorAdapter verificationErrorAdapter)
    {
        super(reportControlContext, runContext);
        this.lifecycle = Allure.getLifecycle();
        this.allureReportGenerator = allureReportGenerator;
        this.batchStorage = batchStorage;
        this.testContext = testContext;
        this.allureRunContext = allureRunContext;
        this.verificationErrorAdapter = verificationErrorAdapter;
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration)
    {
        super.storyCancelled(story, storyDuration);
        StatusDetails statusDetails = new StatusDetails()
                .setMessage("Story timed out after " + storyDuration.getDurationInSecs() + "s");
        if (!getLinkedStep().isRootItem())
        {
            stopStep(Status.BROKEN, statusDetails);
        }
        else
        {
            updateTestCaseStatus(Status.BROKEN, statusDetails);
        }
        stopTestCase();
    }

    @Override
    public void beforeStorySteps(Stage stage, Lifecycle.ExecutionType type)
    {
        if (Stage.AFTER == stage && Lifecycle.ExecutionType.SYSTEM == type)
        {
            allureRunContext.setStoryExecutionStage(StoryExecutionStage.SYSTEM_AFTER_STORY_STEPS);
        }
        super.beforeStorySteps(stage, type);
    }

    @Override
    public void beforeStoriesSteps(Stage stage)
    {
        if (stage == Stage.BEFORE)
        {
            allureReportGenerator.start();
        }
        super.beforeStoriesSteps(stage);
    }

    @Override
    public void afterStoriesSteps(Stage stage)
    {
        super.afterStoriesSteps(stage);
        if (stage == Stage.AFTER)
        {
            allureReportGenerator.end();
        }
    }

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        RunningStory runningStory = getRunContext().getRunningStory();
        if (runningStory.isNotExcluded())
        {
            allureRunContext.initExecutionStages();
            String storyName = runningStory.getName();

            List<Label> storyLabels = allureRunContext.createNewStoryLabels(givenStory);
            String title = givenStory ? "Given Story: " + storyName : storyName;
            if (!getRunContext().isDryRun() && getLinkedStep() != null)
            {
                startStep(title, false);
            }
            else
            {
                storyLabels.addAll(collectLabels(runningStory, givenStory, title));
            }
        }
        super.beforeStory(story, givenStory);
    }

    private List<Label> collectLabels(RunningStory runningStory, boolean givenStory, String allureStoryTitle)
    {
        List<Label> labels = new ArrayList<>();
        labels.add(ResultsUtils.createFrameworkLabel("VIVIDUS"));
        labels.add(ResultsUtils.createHostLabel());
        labels.add(ResultsUtils.createThreadLabel());
        labels.add(ResultsUtils.createStoryLabel(runningStory.getName()));
        labels.add(ResultsUtils.createParentSuiteLabel(getBatchName()));

        if (givenStory)
        {
            labels.add(ResultsUtils.createSuiteLabel(getParentSuiteKey()));
            labels.add(ResultsUtils.createSubSuiteLabel(allureStoryTitle));
        }
        else
        {
            labels.add(ResultsUtils.createSuiteLabel(allureStoryTitle));
        }
        return labels;
    }

    private String getParentSuiteKey()
    {
        return allureRunContext.getRootStoryLabels().stream()
                .filter(l -> LabelName.SUITE.value().equals(l.getName()))
                .findFirst()
                .map(Label::getValue)
                .orElseThrow(() -> new IllegalStateException("No running root story found"));
    }

    private String getBatchName()
    {
        String runningBatchKey = getRunContext().getRunningBatchKey();
        return batchStorage.getBatchConfiguration(runningBatchKey).getName();
    }

    @Override
    public void afterStorySteps(Stage stage, Lifecycle.ExecutionType type)
    {
        super.afterStorySteps(stage, type);
        if (stage == Stage.BEFORE
                && allureRunContext.getStoryExecutionStage() == StoryExecutionStage.LIFECYCLE_BEFORE_STORY_STEPS)
        {
            stopTestCase();
        }
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        RunningStory runningStory = getRunContext().getRunningStory();
        if ((scenario.getExamplesTable().getRowCount() == 0 || scenario.getGivenStories().requireParameters())
                && runningStory.getStory().getLifecycle().getExamplesTable().getRowCount() == 0)
        {
            startTestCase(runningStory.getRunningScenario(), StoryExecutionStage.BEFORE_SCENARIO);
        }
        super.beforeScenario(scenario);
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex)
    {
        RunningStory runningStory = getRunContext().getRunningStory();
        RunningScenario runningScenario = runningStory.getRunningScenario();

        if (exampleIndex > 0)
        {
            stopTestCase();
        }

        LinkedQueueItem<String> step = getLinkedStep();
        startTestCase(runningScenario, StoryExecutionStage.BEFORE_SCENARIO);

        Meta storyMeta = runningStory.getStory().getMeta();
        Meta scenarioMeta = runningScenario.getScenario().getMeta();
        List<Parameter> parameters = tableRow.entrySet().stream()
                .filter(e -> !storyMeta.hasProperty(e.getKey()) && !scenarioMeta.hasProperty(e.getKey()))
                .map(e -> new Parameter().setName(e.getKey()).setValue(e.getValue()))
                .collect(Collectors.toList());

        String id = getCurrentStepId();
        if (step == null)
        {
            lifecycle.updateTestCase(id, result -> result.setParameters(parameters));
        }
        else
        {
            lifecycle.updateStep(id, result -> result.setParameters(parameters));
        }

        super.example(tableRow, exampleIndex);
    }

    @Override
    public void beforeScenarioSteps(Stage stage, Lifecycle.ExecutionType type)
    {
        if (stage == Stage.BEFORE)
        {
            allureRunContext.setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
        }
        else if (stage == Stage.AFTER)
        {
            allureRunContext.setScenarioExecutionStage(ScenarioExecutionStage.AFTER_STEPS);
        }
        super.beforeScenarioSteps(stage, type);
    }

    @Override
    public void afterScenarioSteps(Stage stage, Lifecycle.ExecutionType type)
    {
        super.afterScenarioSteps(stage, type);
        allureRunContext.resetScenarioExecutionStage();
    }

    @Override
    public void beforeStep(Step step)
    {
        perform(() -> startStep(step.getStepAsString()));
        super.beforeStep(step);
    }

    @Override
    public void successful(String step)
    {
        super.successful(step);
        perform(() ->
        {
            modifyStepTitle(step);
            stopStep(Status.PASSED);
        });
    }

    @Override
    public void ignorable(String step)
    {
        super.ignorable(step);
        stopStep(Status.SKIPPED, new StatusDetails().setMessage("The step is commented"));
    }

    @Override
    public void comment(String step)
    {
        super.comment(step);
        updateStepStatus(getLinkedStep().getValue(), Status.SKIPPED);
        stopStep();
    }

    @Override
    public void pending(PendingStep step)
    {
        super.pending(step);
        stopStep(Status.SKIPPED, new StatusDetails().setMessage(PENDING_STEP_MARKER));
    }

    @Override
    public void notPerformed(String step)
    {
        super.notPerformed(step);
        stopStep(Status.SKIPPED, new StatusDetails().setMessage("The step is not performed"));
    }

    @Override
    public void failed(String step, Throwable throwable)
    {
        super.failed(step, throwable);

        checkForBeforeAfterScenarioSteps();

        if (!(throwable instanceof UUIDExceptionWrapper) || !(throwable.getCause() instanceof BeforeOrAfterFailed))
        {
            modifyStepTitle(step);
        }
        Throwable cause = JBehaveFailureUnwrapper.unwrapCause(throwable);
        boolean isVerificationError = cause instanceof VerificationError;
        if (isVerificationError)
        {
            cause = verificationErrorAdapter.adapt((VerificationError) cause);

            List<Link> links = new ArrayList<>();
            for (KnownIssue knownIssue : ((VerificationError) cause).getKnownIssues())
            {
                if (!knownIssue.isPotentiallyKnown())
                {
                    links.add(ResultsUtils.createIssueLink(knownIssue.getIdentifier()));
                }
            }
            lifecycle.updateTestCase(getRootStepId(), result -> result.getLinks().addAll(links));
        }

        if (allureRunContext.getStoryExecutionStage() == StoryExecutionStage.SYSTEM_AFTER_STORY_STEPS)
        {
            startTestCase("System After Story step: " + step, StoryExecutionStage.SYSTEM_AFTER_STORY_STEPS);
            startStep(step);
            stopStep(Status.from(cause), getStatusDetailsFromThrowable(cause));
            stopTestCase();
        }
        else
        {
            stopStep(Status.from(cause), getStatusDetailsFromThrowable(cause));
        }
    }

    @Override
    public void afterScenario(Timing timing)
    {
        super.afterScenario(timing);
        stopTestCase();
        allureRunContext.setStoryExecutionStage(StoryExecutionStage.AFTER_SCENARIO);
    }

    @Override
    public void afterStory(boolean givenStory)
    {
        super.afterStory(givenStory);
        RunningStory runningStory = getRunContext().getRunningStory();
        if (runningStory.isNotExcluded())
        {
            if (allureRunContext.getStoryExecutionStage() == StoryExecutionStage.LIFECYCLE_AFTER_STORY_STEPS)
            {
                stopTestCase();
            }
            if (getLinkedStep() != null)
            {
                updateStepStatus(Status.PASSED);
                stopStep();
            }
            allureRunContext.resetCurrentStoryLabels(givenStory);
            allureRunContext.resetExecutionStages();
        }
    }

    public void addLogStep(String logLevel, String logEntry)
    {
        checkForBeforeAfterScenarioSteps();

        String stepId = getCurrentStepId();
        if (stepId != null)
        {
            StepResult log = new StepResult();
            switch (logLevel)
            {
                case "DEBUG":
                    log.setStatusDetails(new StatusDetails().setMuted(true));
                    break;
                case "ERROR":
                    log.setStatus(io.qameta.allure.model.Status.FAILED);
                    break;
                default:
                    log.setStatus(io.qameta.allure.model.Status.PASSED);
                    break;
            }
            log.setName(logEntry);
            String logUid = UUID.randomUUID().toString();
            lifecycle.startStep(stepId, logUid, log);
            lifecycle.stopStep(logUid);
        }
    }

    private void checkForBeforeAfterScenarioSteps()
    {
        if (!allureRunContext.isStepInProgress())
        {
            ScenarioExecutionStage scenarioExecutionStage = allureRunContext.getScenarioExecutionStage();
            if (ScenarioExecutionStage.BEFORE_STEPS == scenarioExecutionStage)
            {
                startStep("@BeforeScenario");
            }
            else if (ScenarioExecutionStage.AFTER_STEPS == scenarioExecutionStage && getLinkedStep().isRootItem())
            {
                startStep("@AfterScenario");
                updateStepStatus(Status.PASSED);
            }
        }
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        updateStepStatus(Status.from(event));
    }

    private void updateStepStatus(Status status)
    {
        LinkedQueueItem<String> step = getLinkedStep();
        while (!step.isRootItem())
        {
            updateStepStatus(step.getValue(), status);
            step = step.getPreviousItem();
        }
    }

    private void updateStepStatus(String stepId, Status status)
    {
        lifecycle.updateStep(stepId, stepResult ->
        {
            if (isStatusUpdateNeeded(stepResult, status))
            {
                stepResult.setStatus(status.getAllureStatus());
            }
        });
    }

    private void updateTestCaseStatus(Status status, StatusDetails statusDetails)
    {
        lifecycle.updateTestCase(getRootStepId(), scenarioResult ->
        {
            if (isStatusUpdateNeeded(scenarioResult, status))
            {
                scenarioResult.setStatus(status.getAllureStatus()).setStatusDetails(statusDetails);
            }
        });
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onAttachmentPublish(AttachmentPublishEvent event)
    {
        Attachment attachment = event.getAttachment();
        lifecycle.addAttachment(attachment.getTitle(), attachment.getContentType(), null, attachment.getContent());
    }

    @Subscribe
    public void onLinkPublish(LinkPublishEvent event)
    {
        lifecycle.updateTestCase(getRootStepId(), result ->
        {
            String name = event.getName();
            String url = event.getUrl();

            boolean notExists = result.getLinks().stream()
                    .noneMatch(l -> Objects.equals(l.getUrl(), url) && Objects.equals(l.getName(), name));

            if (notExists)
            {
                Link link = new Link().setName(name).setUrl(url);
                result.getLinks().add(link);
            }
        });
    }

    private void startStep(String stepTitle)
    {
        startStep(stepTitle, true);
    }

    private void startStep(String stepTitle, boolean checkForLifecycleSteps)
    {
        if (checkForLifecycleSteps)
        {
            StoryExecutionStage storyExecutionStage = allureRunContext.getStoryExecutionStage();
            if (storyExecutionStage == null)
            {
                startTestCase("Lifecycle: Before story", StoryExecutionStage.LIFECYCLE_BEFORE_STORY_STEPS);
            }
            else if (storyExecutionStage == StoryExecutionStage.AFTER_SCENARIO)
            {
                startTestCase("Lifecycle: After story", StoryExecutionStage.LIFECYCLE_AFTER_STORY_STEPS);
            }
        }
        StepResult stepResult = new StepResult().setName(stepTitle).setStatus(Status.getLowest().getAllureStatus());
        String parentStepId = getCurrentStepId();
        String childStepId = parentStepId + "-" + Thread.currentThread().getId();
        lifecycle.startStep(parentStepId, childStepId, stepResult);
        putCurrentStepId(childStepId);
        allureRunContext.startStep();
    }

    private void stopStep(Status status)
    {
        stopStep(status, new StatusDetails());
    }

    private void stopStep(Status status, StatusDetails statusDetails)
    {
        LinkedQueueItem<String> step = getLinkedStep();
        while (step != null && !step.isRootItem())
        {
            updateStepStatus(step.getValue(), status);
            step = step.getPreviousItem();
        }
        stopStep();
        updateTestCaseStatus(status, statusDetails);
    }

    private void stopStep()
    {
        lifecycle.stopStep(getCurrentStepId());
        switchToParent();
        allureRunContext.stopStep();
    }

    private void modifyStepTitle(String stepTitle)
    {
        lifecycle.updateStep(getCurrentStepId(), stepResult -> stepResult.setName(stepTitle));
    }

    private boolean isStatusUpdateNeeded(ExecutableItem item, Status newStatus)
    {
        Status currentStatus;
        StatusDetails currentStatusDetails = item.getStatusDetails();
        if (currentStatusDetails != null && PENDING_STEP_MARKER.equals(currentStatusDetails.getMessage()))
        {
            currentStatus = Status.PENDING;
        }
        else
        {
            currentStatus = Stream.of(Status.values())
                    .filter(s -> s != Status.PENDING && s.getAllureStatus() == item.getStatus())
                    .findFirst()
                    .get();
        }
        return currentStatus.getPriority() > newStatus.getPriority();
    }

    private void startTestCase(String scenarioTitle, StoryExecutionStage storyExecutionStage)
    {
        RunningScenario scenario = new RunningScenario();
        scenario.setScenario(new Scenario(scenarioTitle, Meta.EMPTY));
        startTestCase(scenario, storyExecutionStage);
    }

    private void startTestCase(RunningScenario runningScenario, StoryExecutionStage storyExecutionStage)
    {
        allureRunContext.setStoryExecutionStage(storyExecutionStage);
        if (getLinkedStep() == null)
        {
            RunningStory runningStory = getRunContext().getRunningStory();
            Meta storyMeta = runningStory.getStory().getMeta();
            Scenario scenario = runningScenario.getScenario();
            Meta scenarioMeta = scenario.getMeta();

            Map<VividusLabel, Set<Entry<String, String>>> metaLabels = Stream.of(VividusLabel.values())
                    .collect(Collectors.toMap(Function.identity(),
                        label -> label.extractMetaValues(storyMeta, scenarioMeta), (l, r) -> l, LinkedHashMap::new));

            int index = runningScenario.getIndex();
            String scenarioId = scenario.getId() + (index != -1 ? "[" + index + "]" : "");
            lifecycle.scheduleTestCase(new TestResult()
                    .setHistoryId(getHistoryId(runningStory, runningScenario))
                    .setUuid(scenarioId)
                    .setName(runningScenario.getTitle())
                    .setLabels(createLabels(storyMeta, scenarioMeta, metaLabels))
                    .setLinks(createLinks(metaLabels))
                    .setStatus(Status.getLowest().getAllureStatus()));
            lifecycle.startTestCase(scenarioId);
            putCurrentStepId(scenarioId);
        }
        else
        {
            startStep(runningScenario.getTitle());
        }
        allureRunContext.setStoryExecutionStage(storyExecutionStage);
    }

    private static List<Link> createLinks(Map<VividusLabel, Set<Entry<String, String>>> metaLabels)
    {
        return metaLabels.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> e.getKey().createLink(v)))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private List<Label> createLabels(Meta storyMeta, Meta scenarioMeta,
            Map<VividusLabel, Set<Entry<String, String>>> metaLabels)
    {
        List<Label> labels = metaLabels.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> e.getKey().createLabel(v)))
                .collect(Collectors.toList());
        labels.addAll(allureRunContext.getCurrentStoryLabels());

        Set<String> metaLabelsNames = metaLabels.values().stream()
                .flatMap(Set::stream)
                .map(Entry::getKey)
                .collect(Collectors.toSet());

        Meta mergedMeta = scenarioMeta.inheritFrom(storyMeta);
        Set<String> tagNames = mergedMeta.getPropertyNames();
        tagNames.removeAll(metaLabelsNames);

        tagNames.stream().map(name -> {
            String value = mergedMeta.getProperty(name);
            StringBuilder tag = new StringBuilder("@").append(name);
            if (!value.isEmpty())
            {
                tag.append(' ').append(value);
            }
            return tag.toString();
        }).map(ResultsUtils::createTagLabel).forEach(labels::add);

        return labels;
    }

    private String getHistoryId(RunningStory runningStory, RunningScenario runningScenario)
    {
        Deque<RunningStory> chain = getRunContext().getStoriesChain();
        String chainedStories = StreamSupport.stream(
                Spliterators.spliterator(chain.descendingIterator(), chain.size(), Spliterator.ORDERED), false)
                .map(RunningStory::getStory)
                .map(Story::getPath)
                .collect(Collectors.joining(" > "));

        String currentScenarioTitle = runningScenario.getScenario().getTitle();
        long scenarioWithSameTitle = runningStory.getRanScenarios()
                                                 .stream()
                                                 .map(Scenario::getTitle)
                                                 .filter(currentScenarioTitle::equals)
                                                 .count();
        StringBuilder scenario = new StringBuilder(runningScenario.getTitle());
        if (scenarioWithSameTitle > 0)
        {
            scenario.append('-').append(scenarioWithSameTitle - 1);
        }
        return String.format("[batch: %s][stories-chain: %s][scenario: %s]", getBatchName(), chainedStories, scenario);
    }

    private void stopTestCase()
    {
        if (allureRunContext.isStepInProgress())
        {
            // Stop @AfterScenario step
            stopStep(Status.getLowest());
        }

        LinkedQueueItem<String> step = getLinkedStep();
        if (!step.isRootItem())
        {
            stopStep();
            return;
        }

        String id = step.getValue();
        lifecycle.stopTestCase(id);
        lifecycle.writeTestCase(id);
        switchToParent();
    }

    private StatusDetails getStatusDetailsFromThrowable(Throwable throwable)
    {
        String message = throwable.getMessage();
        return new StatusDetails()
                .setMessage(message == null ? throwable.getClass().getSimpleName() : message.trim())
                .setTrace(ExceptionUtils.getStackTrace(throwable));
    }

    private LinkedQueueItem<String> getLinkedStep()
    {
        return testContext.get(CURRENT_STEP_KEY);
    }

    private void putLinkedStep(LinkedQueueItem<String> step)
    {
        testContext.put(CURRENT_STEP_KEY, step);
    }

    private String getCurrentStepId()
    {
        LinkedQueueItem<String> step = getLinkedStep();
        return step != null ? step.getValue() : null;
    }

    private String getRootStepId()
    {
        LinkedQueueItem<String> step = getLinkedStep();
        return step != null ? step.getRootItem().getValue() : null;
    }

    private void putCurrentStepId(String stepId)
    {
        LinkedQueueItem<String> step = getLinkedStep();
        LinkedQueueItem<String> attachStep;
        if (step == null)
        {
            attachStep = new LinkedQueueItem<>(stepId);
        }
        else
        {
            attachStep = step.attachItem(stepId);
        }
        putLinkedStep(attachStep);
    }

    private void switchToParent()
    {
        putLinkedStep(getLinkedStep().getPreviousItem());
    }

    static class LinkedQueueItem<E>
    {
        private final E value;
        private LinkedQueueItem<E> previousItem;

        LinkedQueueItem(E value)
        {
            this.value = value;
        }

        boolean isRootItem()
        {
            return previousItem == null;
        }

        LinkedQueueItem<E> getRootItem()
        {
            LinkedQueueItem<E> item = this;
            while (item != null && !item.isRootItem())
            {
                item = item.getPreviousItem();
            }
            return item;
        }

        LinkedQueueItem<E> getPreviousItem()
        {
            return previousItem;
        }

        LinkedQueueItem<E> attachItem(E value)
        {
            LinkedQueueItem<E> attachItem = new LinkedQueueItem<>(value);
            attachItem.previousItem = this;
            return attachItem;
        }

        E getValue()
        {
            return value;
        }
    }
}
