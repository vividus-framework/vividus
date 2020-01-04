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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.vividus.bdd.ChainedStoryReporter;
import org.vividus.bdd.JBehaveFailureUnwrapper;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.MetaWrapper;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.report.allure.adapter.IVerificationErrorAdapter;
import org.vividus.bdd.report.allure.event.StatusProvider;
import org.vividus.bdd.report.allure.model.ScenarioExecutionStage;
import org.vividus.bdd.report.allure.model.StatusPriority;
import org.vividus.bdd.report.allure.model.StoryExecutionStage;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.event.SubStepsPublishingFinishEvent;
import org.vividus.reporter.event.SubStepsPublishingStartEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.testcontext.TestContext;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.SeverityLevel;
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
    private static final char TEST_CASE_META_SEPARATOR = ';';
    private static final String CURRENT_STEP_KEY = "allureCurrentLinkedStep";
    private static final String TEST_TIER = "testTier";
    private static final String GROUP_BY_META_TAG = "group";
    private static final String TEST_CASE_ID = "testCaseId";

    private final AllureLifecycle lifecycle = Allure.getLifecycle();
    private IAllureReportGenerator allureReportGenerator;
    private IBddRunContext bddRunContext;
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
            String name = runningStory.getName();

            switch (name)
            {
                case "BeforeStories":
                    allureReportGenerator.start();
                    break;
                case "AfterStories":
                    allureReportGenerator.end();
                    break;
                default:
                    List<Label> storyLabels = allureRunContext.createNewStoryLabels(givenStory);
                    String title = givenStory ? "Given Story: " + name : name;
                    if (!bddRunContext.isDryRun() && getLinkedStep() != null)
                    {
                        startStep(title, false);
                        break;
                    }
                    storyLabels.add(new Label().setName(LabelName.SUITE.value()).setValue(title));
                    storyLabels.add(new Label().setName(LabelName.HOST.value()).setValue(ResultsUtils.getHostName()));
                    storyLabels.add(new Label().setName(LabelName.THREAD.value())
                            .setValue(ResultsUtils.getThreadName()));

                    String groupByMetaTag = new MetaWrapper(story.getMeta()).getOptionalPropertyValue(GROUP_BY_META_TAG)
                            .orElse("Ungrouped");
                    storyLabels.add(new Label().setName(LabelName.STORY.value()).setValue(name));
                    storyLabels.add(new Label().setName(LabelName.FEATURE.value()).setValue(groupByMetaTag));
                    storyLabels.add(new Label().setName(LabelName.FRAMEWORK.value()).setValue("Vividus"));
                    if (givenStory)
                    {
                        allureRunContext.getRootStoryLabels().stream()
                                .filter(l -> LabelName.SUITE.value().equals(l.getName()))
                                .findFirst()
                                .map(Label::getValue)
                                .map(v -> new Label().setName(LabelName.PARENT_SUITE.value()).setValue(v))
                                .ifPresent(storyLabels::add);
                    }
                    break;
            }
        }
        super.beforeStory(story, givenStory);
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        if (allureRunContext.getStoryExecutionStage() == StoryExecutionStage.LIFECYCLE_BEFORE_STORY_STEPS)
        {
            stopTestCase();
        }
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
        tableRow.forEach((parameterName, parameterValue) ->
        {
            if (!storyMeta.hasProperty(parameterName) && !scenarioMeta.hasProperty(parameterName))
            {
                Parameter parameter = new Parameter().setName(parameterName).setValue(parameterValue);
                String id = getCurrentStepId();
                if (step == null)
                {
                    lifecycle.updateTestCase(id, result -> result.setParameters(List.of(parameter)));
                }
                else
                {
                    lifecycle.updateStep(id, result -> result.setParameters(List.of(parameter)));
                }
            }
        });

        super.example(tableRow, exampleIndex);
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

    private List<Label> getTestCaseMetaLabels(String key, Meta... metas)
    {
        return Stream.of(metas)
                .map(MetaWrapper::new)
                .map(meta -> meta.getOptionalPropertyValue(key))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(propertyValue -> Stream.of(StringUtils.split(propertyValue, TEST_CASE_META_SEPARATOR)))
                .map(StringUtils::trim).distinct().map(v -> new Label().setName(key).setValue(v))
                .collect(Collectors.toList());
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
    @AllowConcurrentEvents
    public void onSubStepsPublishingStart(SubStepsPublishingStartEvent event)
    {
        startBddStep(null);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onSubStepsPublishingFinish(SubStepsPublishingFinishEvent event)
    {
        modifyStepTitle(event.getSubStepTitle());
        Throwable throwable = event.getSubStepThrowable();
        if (throwable == null)
        {
            updateStepStatus(Status.PASSED);
            stopStep();
        }
        else
        {
            stopStep(StatusProvider.getStatus(throwable), getStatusDetailsFromThrowable(throwable));
        }
    }

    private void startBddStep(String stepTitle)
    {
        startStep(stepTitle);
        allureRunContext.setScenarioExecutionStage(ScenarioExecutionStage.IN_PROGRESS);
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
    }

    private void stopBddStep(Status status)
    {
        stopBddStep(status, new StatusDetails());
    }

    private void stopBddStep(Status status, StatusDetails statusDetails)
    {
        stopStep(status, statusDetails);
        allureRunContext.setScenarioExecutionStage(ScenarioExecutionStage.AFTER_STEPS);
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
            List<Label> labels = new ArrayList<>(allureRunContext.getCurrentStoryLabels());

            Story story = bddRunContext.getRunningStory().getStory();
            Meta scenarioMeta = runningScenario.getScenario().getMeta();
            new MetaWrapper(scenarioMeta).getOptionalPropertyValue(TEST_TIER)
                    .map(testTier -> SeverityLevel.values()[Integer.parseInt(testTier)])
                    .map(SeverityLevel::value)
                    .ifPresent(severityLevel -> labels.add(new Label().setName("severity").setValue(severityLevel)));

            List<Label> testCaseMetaLabels = Stream
                    .of("testCaseGroup", TEST_CASE_ID, "requirementId")
                    .flatMap(label -> getTestCaseMetaLabels(label, scenarioMeta, story.getMeta()).stream()).distinct()
                    .collect(Collectors.toList());
            labels.addAll(testCaseMetaLabels);

            int index = runningScenario.getIndex();
            String scenarioId = runningScenario.getUuid() + (index != -1 ? "[" + index + "]" : "");
            lifecycle.scheduleTestCase(new TestResult()
                    .setUuid(scenarioId)
                    .setName(runningScenario.getTitle())
                    .setLabels(labels)
                    .setLinks(convertLabelsToLinks(testCaseMetaLabels))
                    .setStatus(StatusPriority.getLowest().getStatusModel()));
            lifecycle.startTestCase(scenarioId);
            putCurrentStepId(scenarioId);
        }
        else
        {
            startStep(runningScenario.getTitle());
        }
        allureRunContext.setStoryExecutionStage(storyExecutionStage);
        allureRunContext.setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
    }

    private List<Link> convertLabelsToLinks(List<Label> labels)
    {
        List<Link> links = new ArrayList<>();
        for (Label label : labels)
        {
            if (label.getName().equals(TEST_CASE_ID))
            {
                String identifier = label.getValue();
                Link link = ResultsUtils.createIssueLink(identifier);
                if (!identifier.equals(link.getUrl()))
                {
                    links.add(link.setType(ResultsUtils.TMS_LINK_TYPE));
                }
            }
        }
        return links;
    }

    private void updateLabels(List<Label> labels, LabelName labelToUpdate, String newValue)
    {
        labels.stream().filter(label -> labelToUpdate.value().equals(label.getName()))
                .forEach(label -> label.setValue(newValue));
    }

    private void stopTestCase()
    {
        if (allureRunContext.getScenarioExecutionStage() == ScenarioExecutionStage.IN_PROGRESS)
        {
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
        allureRunContext.resetScenarioExecutionStage();
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
