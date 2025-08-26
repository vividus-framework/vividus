/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.mobitru.selenium;

import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.function.FailableConsumer;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobitru.client.MobitruFacade;
import org.vividus.mobitru.client.ScreenRecording;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.TestContext;

public class MobitruRecordingListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MobitruRecordingListener.class);
    private static final String RECORDING_STARTED_FLAG = "recordingStarted";

    private final boolean videoRecordingEnabled;
    private final TestContext testContext;
    private final MobitruFacade mobitruFacade;
    private final MobitruSessionInfoStorage mobitruSessionInfoStorage;
    private final IAttachmentPublisher attachmentPublisher;

    public MobitruRecordingListener(TestContext testContext, boolean videoRecordingEnabled,
                                    MobitruFacade mobitruFacade, MobitruSessionInfoStorage mobitruSessionInfoStorage,
                                    IAttachmentPublisher attachmentPublisher)
    {
        this.testContext = testContext;
        this.videoRecordingEnabled = videoRecordingEnabled;
        this.mobitruFacade = mobitruFacade;
        this.attachmentPublisher = attachmentPublisher;
        this.mobitruSessionInfoStorage = mobitruSessionInfoStorage;
    }

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void startRecordingBeforeScenario()
    {
        // It is possible to create several video recordings within a single session
        startRecordingIfEnabled();
    }

    @Subscribe
    public void onSessionStart(WebDriverCreateEvent event)
    {
        startRecordingIfEnabled();
    }

    @Subscribe
    public void onBeforeSessionStop(BeforeWebDriverQuitEvent event)
    {
        publishRecordingIfEnabled();
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void publishRecordingAfterScenario()
    {
        publishRecordingIfEnabled();
    }

    private void startRecordingIfEnabled()
    {
        performRecordingOperation(mobitruFacade::startDeviceScreenRecording,
                "Unable to start recording on the device with UUID {}",
                true);
    }

    private void publishRecordingIfEnabled()
    {
        performRecordingOperation(deviceId -> {
            ScreenRecording recording = mobitruFacade.stopDeviceScreenRecording(deviceId);
            String attachmentName = String.format("Video Recording (%s).mp4", recording.recordingId());
            attachmentPublisher.publishAttachment(recording.content(), attachmentName);
        }, "Unable to retrieve recording from the device with UUID {}",
                false);
    }

    private void performRecordingOperation(FailableConsumer<String, MobitruOperationException> deviceIdConsumer,
            String logMessageTemplate, boolean recordingStartedState)
    {
        if (videoRecordingEnabled
                &&
                testContext.get(RECORDING_STARTED_FLAG, () -> false) != recordingStartedState)
        {
            mobitruSessionInfoStorage.getDeviceId().ifPresent(deviceId ->
            {
                try
                {
                    deviceIdConsumer.accept(deviceId);
                    testContext.put(RECORDING_STARTED_FLAG, recordingStartedState);
                }
                catch (MobitruOperationException e)
                {
                    LOGGER.atWarn()
                            .addArgument(deviceId)
                            .setCause(e)
                            .log(logMessageTemplate);
                }
            });
        }
    }
}
