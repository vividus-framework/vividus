/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.mobitru.recording;

import com.google.common.eventbus.Subscribe;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobitru.client.MobitruFacade;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.mobitru.client.model.ScreenRecording;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.TestContext;

public class MobitruRecordingListener
{
    private static final String KEY = "mobitruDeviceId";
    private static final Logger LOGGER = LoggerFactory.getLogger(MobitruRecordingListener.class);

    private final MobitruFacade mobitruFacade;
    private final IAttachmentPublisher attachmentPublisher;
    private final IWebDriverProvider webDriverProvider;
    private boolean enableRecording;
    private final TestContext testContext;

    public MobitruRecordingListener(MobitruFacade mobitruFacade, IAttachmentPublisher attachmentPublisher,
                                    TestContext testContext, IWebDriverProvider webDriverProvider)
    {
        this.mobitruFacade = mobitruFacade;
        this.attachmentPublisher = attachmentPublisher;
        this.testContext = testContext;
        this.webDriverProvider = webDriverProvider;
    }

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void startRecordingBeforeScenario()
    {
        //perform start recording if a web driver session is already exist,
        //which means onSessionStart will be not executed
        if (webDriverProvider.isWebDriverInitialized())
        {
            startRecordingIfEnabled();
        }
    }

    @Subscribe
    public void onSessionStart(WebDriverCreateEvent event)
    {
        startRecordingIfEnabled();
    }

    @Subscribe
    public void onSessionStop(BeforeWebDriverQuitEvent event)
    {
        publishRecordingIfEnabled();
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void publishRecordingAfterScenario()
    {
        //perform publish if a web driver session is still exist,
        //which means onSessionStop method was not executed
        if (webDriverProvider.isWebDriverInitialized())
        {
            publishRecordingIfEnabled();
        }
    }

    private void startRecordingIfEnabled()
    {
        if (enableRecording)
        {
            String deviceId = getDeviceId();
            try
            {
                mobitruFacade.startDeviceScreenRecording(deviceId);
            }
            catch (MobitruOperationException e)
            {
                LOGGER.atWarn()
                        .addArgument(deviceId)
                        .setCause(e)
                        .log("Unable to start recording on device with UUID {}");
            }
        }
    }

    private void publishRecordingIfEnabled()
    {
        if (enableRecording)
        {
            String deviceId = getDeviceId();
            try
            {
                ScreenRecording recording = mobitruFacade.stopDeviceScreenRecording(deviceId);
                String attachmentName = String.format("mobitru_session_recording_%s.mp4", recording.recordingId());
                attachmentPublisher.publishAttachment(recording.content(), attachmentName);
            }
            catch (MobitruOperationException e)
            {
                LOGGER.atWarn()
                        .addArgument(deviceId)
                        .setCause(e)
                        .log("Unable to stop or download recording on device with UUID {}");
            }
        }
    }

    public void setEnableRecording(boolean enableRecording)
    {
        this.enableRecording = enableRecording;
    }

    private String getDeviceId()
    {
        return testContext.get(KEY);
    }
}
