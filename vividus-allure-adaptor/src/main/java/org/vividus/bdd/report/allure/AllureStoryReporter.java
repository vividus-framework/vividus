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

package org.vividus.bdd.report.allure;

import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.StepCollector.Stage;
import org.vividus.bdd.ChainedStoryReporter;
import org.vividus.bdd.JBehaveFailureUnwrapper;
import org.vividus.bdd.batch.BatchStorage;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.report.allure.adapter.IVerificationErrorAdapter;
import org.vividus.bdd.report.allure.event.StatusProvider;
import org.vividus.bdd.report.allure.model.ScenarioExecutionStage;
import org.vividus.bdd.report.allure.model.StatusPriority;
import org.vividus.bdd.report.allure.model.StoryExecutionStage;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.testcontext.TestContext;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.WithStatus;
import io.qameta.allure.util.ResultsUtils;

@SuppressWarnings("checkstyle:MethodCount")
public class AllureStoryReporter extends ChainedStoryReporter implements IAllureStepReporter
{
    private static final String CURRENT_STEP_KEY = "allureCurrentLinkedStep";

    private final AllureLifecycle lifecycle = Allure.getLifecycle();
    private IAllureReportGenerator allureReportGenerator;
    private IBddRunContext bddRunContext;
    private BatchStorage batchStorage;
    private TestContext testContext;
    private IAllureRunContext allureRunContext;
    private IVerificationErrorAdapter verificationErrorAdapter;

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration)
    {
        super.storyCancelled(story, storyDuration);
        StatusDetails statusDetails = new StatusDetails()
                .setMessage("Story timed out after " + storyDuration.getDurationInSecs() + "s");
        if (!getLinkedStep().isRootItem())
        {
            stopBddStep(Status.BROKEN, statusDetails);
        }
        else
        {
            updateTestCaseStatus(Status.BROKEN, statusDetails);
        }
        stopTestCase();
    }

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        RunningStory runningStory = bddRunContext.getRunningStory();
        if (runningStory.isAllowed())
        {
            allureRunContext.initExecutionStages();
            String storyName = runningStory.getName();

            switch (storyName)
            {
                case "BeforeStories":
                    allureReportGenerator.start();
                    break;
                case "AfterStories":
                    allureReportGenerator.end();
                    break;
                default:
                    List<Label> storyLabels = allureRunContext.createNewStoryLabels(givenStory);
                    String title = givenStory ? "Given Story: " + storyName : storyName;
                    if (!bddRunContext.isDryRun() && getLinkedStep() != null)
                    {
                        startStep(title, false);
                        break;
                    }
                    collectLabels(runningStory, givenStory, title).forEach(
                        (name, value) -> storyLabels.add(ResultsUtils.createLabel(name.value(), value)));
                    break;
            }
        }
        super.beforeStory(story, givenStory);
    }

    private Map<LabelName, String> collectLabels(RunningStory runningStory, boolean givenStory, String allureStoryTitle)
    {
        Map<LabelName, String> labels = new EnumMap<>(LabelName.class);
        labels.put(LabelName.HOST, ResultsUtils.getHostName());
        labels.put(LabelName.THREAD, ResultsUtils.getThreadName());
        labels.put(LabelName.STORY, runningStory.getName());
        labels.put(LabelName.FRAMEWORK, "Vividus");
        labels.put(LabelName.PARENT_SUITE, getBatchName());

        if (givenStory)
        {
            labels.put(LabelName.SUITE, getParentSuiteKey());
            labels.put(LabelName.SUB_SUITE, allureStoryTitle);
        }
        else
        {
            labels.put(LabelName.SUITE, allureStoryTitle);
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
        String runningBatchKey = bddRunContext.getRunningBatchKey();
        return batchStorage.getBatchExecutionConfiguration(runningBatchKey).getName();
    }

    @Override
    public void afterStorySteps(Stage stage)
    {
        super.afterStorySteps(stage);
        if (stage == Stage.BEFORE
                && allureRunContext.getStoryExecutionStage() == StoryExecutionStage.LIFECYCLE_BEFORE_STORY_STEPS)
        {
            stopTestCase();
        }
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        RunningStory runningStory = bddRunContext.getRunningStory();
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
        RunningStory runningStory = bddRunContext.getRunningStory();
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
    public void beforeScenarioSteps(Stage stage)
    {
        if (stage == Stage.BEFORE)
        {
            allureRunContext.setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
        }
        else if (stage == Stage.AFTER)
        {
            allureRunContext.setScenarioExecutionStage(ScenarioExecutionStage.AFTER_STEPS);
        }
        super.beforeScenarioSteps(stage);
    }

    @Override
    public void afterScenarioSteps(Stage stage)
    {
        super.afterScenarioSteps(stage);
        allureRunContext.resetScenarioExecutionStage();
    }

    @Override
    public void beforeStep(String step)
    {
        startBddStep(step);
        super.beforeStep(step);
    }

    @Override
    public void successful(String step)
    {
        super.successful(step);
        modifyStepTitle(step);
        stopBddStep(Status.PASSED);
    }

    @Override
    public void ignorable(String step)
    {
        startBddStep(step);
        super.ignorable(step);
        stopBddStep(Status.SKIPPED, new StatusDetails().setMessage("Step is commented"));
    }

    @Override
    public void comment(String step)
    {
        startStep(step);
        super.comment(step);
        updateStepStatus(Status.SKIPPED);
        stopStep();
    }

    @Override
    public void pending(String step)
    {
        startBddStep(step);
        super.pending(step);
        stopBddStep(Status.SKIPPED, new StatusDetails().setMessage("Step is not implemented"));
    }

    @Override
    public void notPerformed(String step)
    {
        startBddStep(step);
        super.notPerformed(step);
        stopBddStep(Status.SKIPPED, new StatusDetails().setMessage("Step is not performed"));
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
            for (KnownIssue knownIssue: ((VerificationError) cause).getKnownIssues())
            {
                if (!knownIssue.isPotentiallyKnown())
                {
                    links.add(ResultsUtils.createIssueLink(knownIssue.getIdentifier()));
                }
            }
            lifecycle.updateTestCase(getRootStepId(), result -> result.getLinks().addAll(links));
        }
        stopBddStep(StatusProvider.getStatus(cause), getStatusDetailsFromThrowable(cause));
    }

    @Override
    public void afterScenario()
    {
        super.afterScenario();
        stopTestCase();
        allureRunContext.setStoryExecutionStage(StoryExecutionStage.AFTER_SCENARIO);
    }

    @Override
    public void afterStory(boolean givenStory)
    {
        super.afterStory(givenStory);
        if (bddRunContext.getRunningStory().isAllowed())
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
                    log.setStatus(Status.FAILED);
                    break;
                default:
                    log.setStatus(Status.PASSED);
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
                startBddStep("@BeforeScenario");
            }
            else if (ScenarioExecutionStage.AFTER_STEPS == scenarioExecutionStage && getLinkedStep().isRootItem())
            {
                startBddStep("@AfterScenario");
                updateStepStatus(Status.PASSED);
            }
        }
    }

    @Override
    public void updateStepStatus(Status status)
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
                stepResult.setStatus(status);
            }
        });
    }

    private void updateTestCaseStatus(Status status, StatusDetails statusDetails)
    {
        lifecycle.updateTestCase(getRootStepId(), scenarioResult ->
        {
            if (isStatusUpdateNeeded(scenarioResult, status))
            {
                scenarioResult.setStatus(status).setStatusDetails(statusDetails);
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
                                              .noneMatch(l -> l.getUrl().equals(url) && l.getName().equals(name));

            if (notExists)
            {
                Link link = new Link().setName(name).setUrl(url);
                result.getLinks().add(link);
            }
        });
    }

    private void startBddStep(String stepTitle)
    {
        startStep(stepTitle);
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
        StepResult stepResult = new StepResult().setName(stepTitle).setStatus(
                StatusPriority.getLowest().getStatusModel());
        String parentStepId = getCurrentStepId();
        String childStepId = parentStepId + "-" + Thread.currentThread().getId();
        lifecycle.startStep(parentStepId, childStepId, stepResult);
        putCurrentStepId(childStepId);
        allureRunContext.startStep();
    }

    private void stopBddStep(Status status)
    {
        stopBddStep(status, new StatusDetails());
    }

    private void stopBddStep(Status status, StatusDetails statusDetails)
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

    private boolean isStatusUpdateNeeded(WithStatus item, Status overrideStatus)
    {
        return StatusPriority.fromStatus(item.getStatus()).getPriority() > StatusPriority.fromStatus(overrideStatus)
                .getPriority();
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
            RunningStory runningStory = bddRunContext.getRunningStory();
            Meta storyMeta = runningStory.getStory().getMeta();
            Meta scenarioMeta = runningScenario.getScenario().getMeta();

            Map<VividusLabel, Set<String>> metaLabels = Stream.of(VividusLabel.values())
                    .collect(Collectors.toMap(Function.identity(),
                        label -> label.extractMetaValues(storyMeta, scenarioMeta), (l, r) -> l, LinkedHashMap::new));

            List<Link> links = metaLabels.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(v -> e.getKey().createLink(v)))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());

            List<Label> labels = metaLabels.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(v -> e.getKey().createLabel(v)))
                    .collect(Collectors.toList());
            labels.addAll(allureRunContext.getCurrentStoryLabels());

            int index = runningScenario.getIndex();
            String scenarioId = runningScenario.getUuid() + (index != -1 ? "[" + index + "]" : "");
            lifecycle.scheduleTestCase(new TestResult()
                    .setHistoryId(getHistoryId(runningStory, runningScenario))
                    .setUuid(scenarioId)
                    .setName(runningScenario.getTitle())
                    .setLabels(labels)
                    .setLinks(links)
                    .setStatus(StatusPriority.getLowest().getStatusModel()));
            lifecycle.startTestCase(scenarioId);
            putCurrentStepId(scenarioId);
        }
        else
        {
            startStep(runningScenario.getTitle());
        }
        allureRunContext.setStoryExecutionStage(storyExecutionStage);
    }

    private String getHistoryId(RunningStory runningStory, RunningScenario runningScenario)
    {
        Deque<RunningStory> chain = bddRunContext.getStoriesChain();
        String chainedStories = StreamSupport.stream(
                Spliterators.spliterator(chain.descendingIterator(), chain.size(), Spliterator.ORDERED), false)
                .map(RunningStory::getStory)
                .map(Story::getPath)
                .collect(Collectors.joining(" > "));

        List<Scenario> scenariosWithTitle = runningStory.getStory().getScenarios().stream()
                .filter(scenario -> scenario.getTitle().equals(runningScenario.getScenario().getTitle()))
                .collect(Collectors.toList());
        StringBuilder scenario = new StringBuilder(runningScenario.getTitle());
        if (scenariosWithTitle.size() > 1)
        {
            scenario.append('-').append(scenariosWithTitle.indexOf(runningScenario.getScenario()));
        }
        return String.format("[batch: %s][stories-chain: %s][scenario: %s]", getBatchName(), chainedStories, scenario);
    }

    private void stopTestCase()
    {
        if (allureRunContext.isStepInProgress())
        {
            // Stop @AfterScenario step
            stopBddStep(StatusPriority.getLowest().getStatusModel());
        }

        LinkedQueueItem<String> step = getLinkedStep();
        if (!step.isRootItem())
        {
            stopStep();
            return;
        }

        String id = step.getValue();
        String testRunId = getTestRunId();
        if (testRunId != null)
        {
            Link testRunLink = ResultsUtils.createTmsLink(testRunId).setName("Test run ID");
            lifecycle.updateTestCase(id, result -> result.getLinks().add(testRunLink));
        }

        lifecycle.stopTestCase(id);
        lifecycle.writeTestCase(id);
        switchToParent();
    }

    private String getTestRunId()
    {
        return null;
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

    public void setAllureReportGenerator(IAllureReportGenerator allureReportGenerator)
    {
        this.allureReportGenerator = allureReportGenerator;
    }

    public void setBddRunContext(IBddRunContext bddRunContext)
    {
        this.bddRunContext = bddRunContext;
    }

    public void setBatchStorage(BatchStorage batchStorage)
    {
        this.batchStorage = batchStorage;
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void setAllureRunContext(IAllureRunContext allureRunContext)
    {
        this.allureRunContext = allureRunContext;
    }

    public void setVerificationErrorAdapter(IVerificationErrorAdapter verificationErrorAdapter)
    {
        this.verificationErrorAdapter = verificationErrorAdapter;
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
