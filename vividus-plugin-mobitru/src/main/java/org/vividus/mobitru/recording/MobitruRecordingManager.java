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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobitru.client.MobitruFacade;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.TestContext;

public class MobitruRecordingManager
{
    private static final String KEY = "mobitruDeviceId";
    private static final Logger LOGGER = LoggerFactory.getLogger(MobitruRecordingManager.class);

    private final MobitruFacade mobitruFacade;
    private final IAttachmentPublisher attachmentPublisher;
    private boolean enableRecording;
    private final TestContext testContext;

    public MobitruRecordingManager(MobitruFacade mobitruFacade, IAttachmentPublisher attachmentPublisher,
                                   TestContext testContext)
    {
        this.mobitruFacade = mobitruFacade;
        this.attachmentPublisher = attachmentPublisher;
        this.testContext = testContext;
    }

    @Subscribe
    public void onSessionStart(WebDriverCreateEvent event)
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

    @Subscribe
    public void onSessionStop(BeforeWebDriverQuitEvent event)
    {
        if (enableRecording)
        {
            String deviceId = getDeviceId();
            try
            {
                String recordingId = mobitruFacade.stopDeviceScreenRecording(deviceId);
                byte[] recording = mobitruFacade.downloadDeviceScreenRecording(recordingId);
                attachmentPublisher.publishAttachment(recording, "Mobitru device session recording");
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
