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
import static org.mockito.Mockito.verifyNoInteractions;

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
import org.vividus.mobitru.client.model.ScreenRecording;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.TestContext;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class MobitruRecordingListenerTests
{
    private static final byte[] BINARY_RESPONSE = {1, 0, 1, 0};
    private static final String RECORDING_ID = "15a02180-16f6-4eb9-b5e8-9e945f5e85fe";
    private static final String RECORDING_REPORT_NAME = String.format("mobitru_session_recording_%s.mp4",
            RECORDING_ID);
    private static final String KEY = "mobitruDeviceId";
    private static final String UDID = "Z3CV103D2DO";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(MobitruRecordingListener.class);

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private MobitruFacade mobitruFacade;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private TestContext testContext;
    @InjectMocks private MobitruRecordingListener mobitruRecordingListener;

    @Test
    void shouldStartRecording() throws MobitruOperationException
    {
        mobitruRecordingListener.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        var ordered = Mockito.inOrder(testContext, mobitruFacade);
        var event = mock(WebDriverCreateEvent.class);
        mobitruRecordingListener.onSessionStart(event);
        ordered.verify(testContext).get(KEY);
        ordered.verify(mobitruFacade).startDeviceScreenRecording(UDID);
    }

    @Test
    void shouldNotStartRecordingIfDisabled() {
        mobitruRecordingListener.setEnableRecording(false);
        var event = mock(WebDriverCreateEvent.class);
        mobitruRecordingListener.onSessionStart(event);
        verifyNoInteractions(mobitruFacade);
        verifyNoInteractions(testContext);
    }

    @Test
    void shouldStartRecordingBeforeScenario() throws MobitruOperationException
    {
        mobitruRecordingListener.setEnableRecording(true);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        var ordered = Mockito.inOrder(testContext, mobitruFacade);
        mobitruRecordingListener.startRecordingBeforeScenario();
        ordered.verify(testContext).get(KEY);
        ordered.verify(mobitruFacade).startDeviceScreenRecording(UDID);
    }

    @Test
    void shouldNotStartRecordingIfDriverIsNotInitialized() {
        mobitruRecordingListener.setEnableRecording(true);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        mobitruRecordingListener.startRecordingBeforeScenario();
        verifyNoInteractions(mobitruFacade);
        verifyNoInteractions(testContext);
    }

    @Test
    void shouldStopRecordingAndDownload() throws MobitruOperationException
    {
        mobitruRecordingListener.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        ScreenRecording recording = mock(ScreenRecording.class);
        when(recording.recordingId()).thenReturn(RECORDING_ID);
        when(recording.content()).thenReturn(BINARY_RESPONSE);
        when(mobitruFacade.stopDeviceScreenRecording(UDID)).thenReturn(recording);
        var ordered = Mockito.inOrder(testContext, mobitruFacade, attachmentPublisher);
        var event = mock(BeforeWebDriverQuitEvent.class);
        mobitruRecordingListener.onSessionStop(event);
        ordered.verify(testContext).get(KEY);
        ordered.verify(mobitruFacade).stopDeviceScreenRecording(UDID);
        ordered.verify(attachmentPublisher).publishAttachment(BINARY_RESPONSE,
                RECORDING_REPORT_NAME);
    }

    @Test
    void shouldNotStopRecordingIfDisabled() {
        mobitruRecordingListener.setEnableRecording(false);
        var event = mock(BeforeWebDriverQuitEvent.class);
        mobitruRecordingListener.onSessionStop(event);
        verifyNoInteractions(mobitruFacade);
        verifyNoInteractions(testContext);
    }

    @Test
    void shouldPublishRecordingAfterScenario() throws MobitruOperationException
    {
        mobitruRecordingListener.setEnableRecording(true);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        ScreenRecording recording = mock(ScreenRecording.class);
        when(recording.recordingId()).thenReturn(RECORDING_ID);
        when(recording.content()).thenReturn(BINARY_RESPONSE);
        when(mobitruFacade.stopDeviceScreenRecording(UDID)).thenReturn(recording);
        var ordered = Mockito.inOrder(testContext, mobitruFacade, attachmentPublisher);
        mobitruRecordingListener.publishRecordingAfterScenario();
        ordered.verify(testContext).get(KEY);
        ordered.verify(mobitruFacade).stopDeviceScreenRecording(UDID);
        ordered.verify(attachmentPublisher).publishAttachment(BINARY_RESPONSE,
                RECORDING_REPORT_NAME);
    }

    @Test
    void shouldNotPublishRecordingIfDriverIsNotInitialized() {
        mobitruRecordingListener.setEnableRecording(true);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        mobitruRecordingListener.publishRecordingAfterScenario();
        verifyNoInteractions(mobitruFacade);
        verifyNoInteractions(testContext);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void shouldLogWarnOnStartWithoutThrow() throws MobitruOperationException
    {
        mobitruRecordingListener.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        var exception = new MobitruOperationException(UDID);
        doThrow(exception).when(mobitruFacade).startDeviceScreenRecording(UDID);
        var event = mock(WebDriverCreateEvent.class);
        assertDoesNotThrow(() -> mobitruRecordingListener.onSessionStart(event));
        assertEquals(List.of(
                        LoggingEvent.warn(exception, "Unable to start recording on device with UUID {}", UDID)),
                LOGGER.getLoggingEvents());
    }

    @Test
    void shouldLogWarnOnStopWithoutThrow() throws MobitruOperationException
    {
        mobitruRecordingListener.setEnableRecording(true);
        when(testContext.get(KEY)).thenReturn(UDID);
        var exception = new MobitruOperationException(UDID);
        doThrow(exception).when(mobitruFacade).stopDeviceScreenRecording(UDID);
        var event = mock(BeforeWebDriverQuitEvent.class);
        assertDoesNotThrow(() -> mobitruRecordingListener.onSessionStop(event));
        assertEquals(List.of(
                        LoggingEvent.warn(exception,
                                "Unable to stop or download recording on device with UUID {}", UDID)),
                LOGGER.getLoggingEvents());
    }
}
