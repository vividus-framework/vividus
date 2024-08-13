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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobitru.client.MobitruFacade;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.TestContext;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class MobitruRecordingManagerTests
{
    private static final byte[] BINARY_RESPONSE = {1, 0, 1, 0};
    private static final String RECORDING_ID = "15a02180-16f6-4eb9-b5e8-9e945f5e85fe";
    private static final String KEY = "mobitruDeviceId";
    private static final String UDID = "Z3CV103D2DO";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(MobitruRecordingManager.class);

    @Mock private MobitruFacade mobitruFacade;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private TestContext testContext;
    @InjectMocks private MobitruRecordingManager mobitruRecordingManager;

    @Test
    void shouldStartRecording() throws MobitruOperationException
    {
        mobitruRecordingManager.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        var ordered = Mockito.inOrder(testContext, mobitruFacade);
        var event = mock(WebDriverCreateEvent.class);
        mobitruRecordingManager.onSessionStart(event);
        ordered.verify(testContext).get(KEY);
        ordered.verify(mobitruFacade).startDeviceScreenRecording(UDID);
    }

    @Test
    void shouldStartRecordingAndDownload() throws MobitruOperationException
    {
        mobitruRecordingManager.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        when(mobitruFacade.stopDeviceScreenRecording(UDID)).thenReturn(RECORDING_ID);
        when(mobitruFacade.downloadDeviceScreenRecording(RECORDING_ID)).thenReturn(BINARY_RESPONSE);
        var ordered = Mockito.inOrder(testContext, mobitruFacade, attachmentPublisher);
        var event = mock(BeforeWebDriverQuitEvent.class);
        mobitruRecordingManager.onSessionStop(event);
        ordered.verify(testContext).get(KEY);
        ordered.verify(mobitruFacade).stopDeviceScreenRecording(UDID);
        ordered.verify(mobitruFacade).downloadDeviceScreenRecording(RECORDING_ID);
        ordered.verify(attachmentPublisher).publishAttachment(BINARY_RESPONSE,
                "Mobitru device session recording");
    }

    @Test
    void shouldLogWarnOnStartWithoutThrow() throws MobitruOperationException
    {
        mobitruRecordingManager.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        var exception = new MobitruOperationException(UDID);
        doThrow(exception).when(mobitruFacade).startDeviceScreenRecording(UDID);
        var event = mock(WebDriverCreateEvent.class);
        assertDoesNotThrow(() -> mobitruRecordingManager.onSessionStart(event));
        assertEquals(List.of(
                        LoggingEvent.warn(exception, "Unable to start recording on device with UUID {}", UDID)),
                LOGGER.getLoggingEvents());
    }

    @Test
    void shouldLogWarnOnStopWithoutThrow() throws MobitruOperationException
    {
        mobitruRecordingManager.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        var exception = new MobitruOperationException(UDID);
        doThrow(exception).when(mobitruFacade).stopDeviceScreenRecording(UDID);
        var event = mock(BeforeWebDriverQuitEvent.class);
        assertDoesNotThrow(() -> mobitruRecordingManager.onSessionStop(event));
        assertEquals(List.of(
                        LoggingEvent.warn(exception,
                                "Unable to stop or download recording on device with UUID {}", UDID)),
                LOGGER.getLoggingEvents());
    }
}
