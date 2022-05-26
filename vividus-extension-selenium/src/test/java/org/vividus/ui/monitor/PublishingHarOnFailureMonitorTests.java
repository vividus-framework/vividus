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

package org.vividus.ui.monitor;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarLog;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.annotations.When;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.proxy.har.HarOnFailureManager;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class PublishingHarOnFailureMonitorTests
{
    private static final String I_DO_ACTION = "I do action";
    private static final String NO_HAR_ON_FAILURE_META_NAME = "noHarOnFailure";
    private static final String ERROR_MESSAGE = "Unable to capture HAR";
    private static final Meta EMPTY_META = new Meta();

    @Mock private EventBus eventBus;
    @Mock private RunContext runContext;
    @Mock private HarOnFailureManager harOnFailureManager;
    @Mock private IWebDriverProvider webDriverProvider;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(
            AbstractPublishingAttachmentOnFailureMonitor.class);

    private PublishingHarOnFailureMonitor createMonitor(boolean publishHarOnFailure)
    {
        return new PublishingHarOnFailureMonitor(publishHarOnFailure, harOnFailureManager, eventBus, runContext,
                webDriverProvider);
    }

    private static Method getCapturingHarMethod() throws NoSuchMethodException
    {
        return PublishingHarOnFailureMonitorTests.class.getDeclaredMethod("whenStep");
    }

    @TestFactory
    Stream<DynamicTest> getProcessStepWithAnnotation() throws NoSuchMethodException
    {
        var monitor = createMonitor(true);
        return Stream.of(getCapturingHarMethod(), TestSteps.class.getDeclaredMethod("innerWhenStep"))
                .flatMap(method -> Stream.of(
                            dynamicTest("beforePerformingProcessesStepWithAnnotation",
                                    () -> {
                                        mockScenarioAndStoryMeta();
                                        monitor.beforePerforming(I_DO_ACTION, false, method);
                                    }),
                            dynamicTest("afterPerformingProcessesStepWithAnnotation",
                                    () -> monitor.afterPerforming(I_DO_ACTION, false, method))
                        )
                );
    }

    @TestFactory
    Stream<DynamicTest> getIgnoreStepWithoutAnnotation() throws NoSuchMethodException
    {
        var monitor = createMonitor(false);
        return Stream.of(getClass().getDeclaredMethod("anotherWhenStep"), null)
                .flatMap(method -> Stream.of(
                            dynamicTest("beforePerformingIgnoresStepWithoutAnnotation",
                                    () -> monitor.beforePerforming(I_DO_ACTION, false, method)),
                            dynamicTest("afterPerformingIgnoresStepWithoutAnnotation",
                                    () -> monitor.afterPerforming(I_DO_ACTION, false, method))
                        )
                );
    }

    @Test
    void shouldEnableHarIfNoRunningScenario() throws NoSuchMethodException
    {
        mockStoryMeta(new RunningStory(), EMPTY_META);
        var monitor = createMonitor(true);
        monitor.beforePerforming(I_DO_ACTION, false, getCapturingHarMethod());
        monitor.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
    }

    @Test
    void shouldNotTakeHarIfMethodAnnotatedButStoryHasNoHarOnFailure()
    {
        mockStoryMeta(new RunningStory(), new Meta(List.of(NO_HAR_ON_FAILURE_META_NAME)));
        var monitor = createMonitor(true);
        monitor.beforePerforming(I_DO_ACTION, false, null);
        monitor.onAssertionFailure(null);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldNotTakeHarIfMethodAnnotatedButScenarioHasNoHarOnFailure() throws NoSuchMethodException
    {
        var runningStory = new RunningStory();
        mockStoryMeta(runningStory, EMPTY_META);
        mockScenarioMeta(runningStory, new Meta(List.of(NO_HAR_ON_FAILURE_META_NAME)));
        var monitor = createMonitor(false);
        monitor.beforePerforming(I_DO_ACTION, false, getCapturingHarMethod());
        monitor.onAssertionFailure(null);
        verifyNoInteractions(webDriverProvider);
    }

    static Stream<Arguments> capturingHarData() throws NoSuchMethodException
    {
        return Stream.of(
                arguments(true, null),
                arguments(false, getCapturingHarMethod())
        );
    }

    @ParameterizedTest
    @MethodSource("capturingHarData")
    void shouldCaptureHarOnAssertionFailure(boolean publishHarOnFailure, Method method)
    {
        mockScenarioAndStoryMeta();
        var monitor = createMonitor(publishHarOnFailure);
        var har = new Har();
        har.setLog(new HarLog());
        when(harOnFailureManager.takeHar()).thenReturn(Optional.of(har));
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        monitor.beforePerforming(I_DO_ACTION, false, method);
        monitor.onAssertionFailure(null);
        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventBus).post(eventCaptor.capture());
        var event = eventCaptor.getValue();
        assertThat(event, instanceOf(AttachmentPublishEvent.class));
        var attachment = ((AttachmentPublishEvent) event).getAttachment();
        assertEquals("har-on-failure", attachment.getTitle());
        assertEquals("{\"log\":{\"version\":\"1.1\",\"creator\":{\"name\":\"\",\"version\":\"\"},\"pages\":[],"
                + "\"entries\":[]}}", new String(attachment.getContent(), StandardCharsets.UTF_8));
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldDoNothingIfNoHarIsCaptured()
    {
        mockScenarioAndStoryMeta();
        var monitor = createMonitor(true);
        when(harOnFailureManager.takeHar()).thenReturn(Optional.empty());
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        monitor.beforePerforming(I_DO_ACTION, false, null);
        monitor.onAssertionFailure(null);
        verifyNoInteractions(eventBus);
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldLogErrorIfHarPublishingIsFailed()
    {
        mockScenarioAndStoryMeta();
        var monitor = createMonitor(true);
        var exception = new IllegalStateException();
        when(harOnFailureManager.takeHar()).thenThrow(exception);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        monitor.beforePerforming(I_DO_ACTION, false, null);
        monitor.onAssertionFailure(null);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(error(exception, ERROR_MESSAGE))));
    }

    private void mockScenarioAndStoryMeta()
    {
        var runningStory = new RunningStory();
        mockStoryMeta(runningStory, EMPTY_META);
        mockScenarioMeta(runningStory, EMPTY_META);
    }

    private void mockStoryMeta(RunningStory runningStory, Meta meta)
    {
        when(runContext.getRunningStory()).thenReturn(runningStory);
        runningStory.setStory(new Story(null, null, meta, null, null));
    }

    private void mockScenarioMeta(RunningStory runningStory, Meta meta)
    {
        var runningScenario = new RunningScenario();
        runningScenario.setScenario(new Scenario("test scenario", meta));
        runningStory.setRunningScenario(runningScenario);
    }

    @PublishHarOnFailure
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

    @PublishHarOnFailure
    static class TestSteps
    {
        @When(I_DO_ACTION)
        void innerWhenStep()
        {
            // nothing to do
        }
    }
}
