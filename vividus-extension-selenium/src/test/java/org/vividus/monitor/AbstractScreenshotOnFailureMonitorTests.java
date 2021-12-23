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

package org.vividus.monitor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.annotations.When;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.Screenshot;
import org.vividus.softassert.event.AssertionFailedEvent;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AbstractScreenshotOnFailureMonitorTests
{
    private static final String INNER_WHEN_MODE_STEP = "innerWhenModeStep";
    private static final String I_DO_ACTION = "I do action";
    private static final String WHEN_STEP_METHOD = "whenStep";
    private static final Meta EMPTY_META = new Meta();
    private static final String ASSERTION_FAILURE = "Assertion_Failure";
    private static final String NO_SCREENSHOT_ON_FAILURE = "noScreenshotOnFailure";

    @Mock private RunContext runContext;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private EventBus eventBus;
    @InjectMocks private TestScreenshotOnFailureMonitor monitor;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(
            AbstractPublishingAttachmentOnFailureMonitor.class);

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(eventBus, runContext, webDriverProvider);
    }

    @TestFactory
    Stream<DynamicTest> shouldProcessStepWithAnnotation() throws NoSuchMethodException
    {
        return Stream.of(getTakingScreenshotMethod(),
                TestSteps.class.getDeclaredMethod("innerWhenStep"))
                .flatMap(method ->
                {
                    reset(runContext);
                    mockScenarioAndStoryMeta(EMPTY_META);
                    return Stream.of(
                            dynamicTest("beforePerformingProcessesStepWithAnnotation",
                                () -> monitor.beforePerforming(I_DO_ACTION, false, method)),
                            dynamicTest("afterPerformingProcessesStepWithAnnotation",
                                () -> monitor.afterPerforming(I_DO_ACTION, false, method))
                            );
                });
    }

    private Method getTakingScreenshotMethod() throws NoSuchMethodException
    {
        return getClass().getDeclaredMethod(WHEN_STEP_METHOD);
    }

    @TestFactory
    Stream<DynamicTest> shouldIgnoreStepWithoutAnnotation() throws NoSuchMethodException
    {
        return Stream.of(getClass().getDeclaredMethod("anotherWhenStep"), null)
                .flatMap(method -> Stream.of(
                        dynamicTest("beforePerformingIgnoresStepWithoutAnnotation",
                            () -> monitor.beforePerforming(I_DO_ACTION, false, method)),
                        dynamicTest("afterPerformingIgnoresStepWithoutAnnotation",
                            () -> monitor.afterPerforming(I_DO_ACTION, false, method))
                        ));
    }

    @Test
    void shouldNotTakeScreenshotIfMethodAnnotatedButStoryHasNoScreenshotOnFailure()
            throws NoSuchMethodException, IOException
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, new Meta(List.of(NO_SCREENSHOT_ON_FAILURE)));
        monitor.beforePerforming(I_DO_ACTION, false, getTakingScreenshotMethod());
        monitor.onAssertionFailure(null);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldNotTakeScreenshotIfMethodAnnotatedButScenarioHasNoScreenshotOnFailure()
            throws NoSuchMethodException, IOException
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, EMPTY_META);
        mockScenarioMeta(runningStory, new Meta(List.of(NO_SCREENSHOT_ON_FAILURE)));
        monitor.beforePerforming(I_DO_ACTION, false, getTakingScreenshotMethod());
        monitor.onAssertionFailure(null);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldEnableScreenshotsIfNoRunningSceanario() throws NoSuchMethodException, IOException
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, EMPTY_META);
        monitor.beforePerforming(I_DO_ACTION, false, getTakingScreenshotMethod());
        monitor.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
    }

    @Test
    void shouldNotTakeScreenshotIfItIsNotEnabled() throws IOException
    {
        monitor.onAssertionFailure(mock(AssertionFailedEvent.class));
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldNotTakeScreenshotIfWebDriverIsNotEnabled() throws NoSuchMethodException, IOException
    {
        enableScreenshotPublishing(false);
        monitor.onAssertionFailure(mock(AssertionFailedEvent.class));
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldTakeScreenshotOfSearchContext() throws NoSuchMethodException, IOException
    {
        enableScreenshotPublishing(true);
        String title = "2019-03-07_19-11-38_898-Assertion_Failure-chrome-1440x836";
        Screenshot screenshot = new Screenshot(title + ".png", new byte[] { 1 });
        TestScreenshotOnFailureMonitor spy = spy(monitor);
        doReturn(Optional.of(screenshot)).when(spy).takeAssertionFailureScreenshot(ASSERTION_FAILURE);
        spy.onAssertionFailure(mock(AssertionFailedEvent.class));
        verify(eventBus).post(argThat((ArgumentMatcher<AttachmentPublishEvent>) event -> {
            Attachment attachment = event.getAttachment();
            return Arrays.equals(screenshot.getData(), attachment.getContent()) && title.equals(attachment.getTitle());
        }));
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldTakeScreenshotOfAssertedElementsISearchContextIsPage() throws NoSuchMethodException, IOException
    {
        enableScreenshotPublishing(true);
        TestScreenshotOnFailureMonitor spy = spy(monitor);
        doReturn(Optional.empty()).when(spy).takeAssertionFailureScreenshot(ASSERTION_FAILURE);
        spy.onAssertionFailure(mock(AssertionFailedEvent.class));
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldTakeScreenshotOfAssertedElementsWithDebugMode() throws NoSuchMethodException, IOException
    {
        mockScenarioAndStoryMeta(EMPTY_META);
        monitor.setDebugModes(Arrays.asList("mode", "ui"));
        monitor.beforePerforming(I_DO_ACTION, false, TestWithModeSteps.class.getDeclaredMethod(INNER_WHEN_MODE_STEP));
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        TestScreenshotOnFailureMonitor spy = spy(monitor);
        doReturn(Optional.empty()).when(spy).takeAssertionFailureScreenshot(ASSERTION_FAILURE);
        spy.onAssertionFailure(mock(AssertionFailedEvent.class));
        assertThat(logger.getLoggingEvents(), empty());
    }

    static Stream<Arguments> debugMode()
    {
        return Stream.of(Arguments.of(Arrays.asList("another")), null);
    }

    @ParameterizedTest
    @MethodSource("debugMode")
    void shouldNotTakeScreenshotOfAssertedElementsWithDebugModeMethod(List<String> mode)
            throws NoSuchMethodException, IOException
    {
        monitor.setDebugModes(mode);
        monitor.beforePerforming(I_DO_ACTION, false, getClass().getDeclaredMethod("whenDebugStep"));
        monitor.onAssertionFailure(null);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldTakeScreenshotOfAssertedElementsWithoutDebugModeInAnnotation() throws NoSuchMethodException, IOException
    {
        monitor.setDebugModes(Arrays.asList("anymode"));
        enableScreenshotPublishing(true);
        TestScreenshotOnFailureMonitor spy = spy(monitor);
        doReturn(Optional.empty()).when(spy).takeAssertionFailureScreenshot(ASSERTION_FAILURE);
        spy.onAssertionFailure(mock(AssertionFailedEvent.class));
        assertThat(logger.getLoggingEvents(), empty());
    }

    private void enableScreenshotPublishing(boolean webDriverInitialized) throws NoSuchMethodException
    {
        mockScenarioAndStoryMeta(EMPTY_META);
        monitor.beforePerforming(I_DO_ACTION, false, getClass().getDeclaredMethod(WHEN_STEP_METHOD));
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(webDriverInitialized);
    }

    private void mockScenarioAndStoryMeta(Meta meta)
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, meta);
        mockScenarioMeta(runningStory, meta);
    }

    private void mockStoryMeta(RunningStory runningStory, Meta meta)
    {
        when(runContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getStory()).thenReturn(new Story(null, null, meta, null, null));
    }

    private void mockScenarioMeta(RunningStory runningStory, Meta meta)
    {
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningScenario.getScenario()).thenReturn(new Scenario("test scenario", meta));
    }

    @TakeScreenshotOnFailure
    @When(I_DO_ACTION)
    void whenStep()
    {
        // nothing to do
    }

    @When(I_DO_ACTION)
    void anotherWhenStep()
    {
        // nothing to do
    }

    @TakeScreenshotOnFailure(onlyInDebugMode = "mode")
    @When(I_DO_ACTION)
    void whenDebugStep()
    {
        // nothing to do
    }

    @TakeScreenshotOnFailure
    static class TestSteps
    {
        @When(I_DO_ACTION)
        void innerWhenStep()
        {
            // nothing to do
        }
    }

    @TakeScreenshotOnFailure(onlyInDebugMode = "mode")
    static class TestWithModeSteps
    {
        @When(I_DO_ACTION)
        void innerWhenModeStep()
        {
            // nothing to do
        }
    }

    static class TestScreenshotOnFailureMonitor extends AbstractScreenshotOnFailureMonitor
    {
        TestScreenshotOnFailureMonitor(EventBus eventBus, RunContext runContext,
                IWebDriverProvider webDriverProvider)
        {
            super(eventBus, runContext, webDriverProvider);
        }

        @Override
        protected Optional<Screenshot> takeAssertionFailureScreenshot(String screenshotName)
        {
            throw new UnsupportedOperationException("The method is supposed to be mocked");
        }
    }
}
