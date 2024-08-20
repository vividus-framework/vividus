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

package org.vividus.mobitru.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobitru.client.MobitruFacade;
import org.vividus.mobitru.client.ScreenRecording;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class MobitruRecordingListenerTests
{
    private static final byte[] BINARY_RESPONSE = {1, 0, 1, 0};
    private static final String RECORDING_ID = "15a02180-16f6-4eb9-b5e8-9e945f5e85fe";
    private static final String RECORDING_REPORT_NAME = String.format("Video Recording (%s).mp4",
            RECORDING_ID);
    private static final String UDID = "Z3CV103D2DO";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(MobitruRecordingListener.class);

    @Mock private MobitruFacade mobitruFacade;
    @Mock private MobitruSessionInfoStorage mobitruSessionInfoStorage;
    @Mock private IAttachmentPublisher attachmentPublisher;

    @Test
    void shouldStartRecording() throws MobitruOperationException
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.of(UDID));
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.onSessionStart(mock(WebDriverCreateEvent.class));
        verify(mobitruFacade).startDeviceScreenRecording(UDID);
    }

    @Test
    void shouldNotStartRecordingIfDisabled()
    {
        var mobitruRecordingListener = new MobitruRecordingListener(false, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.onSessionStart(mock(WebDriverCreateEvent.class));
        verifyNoInteractions(mobitruSessionInfoStorage, mobitruFacade);
    }

    @Test
    void shouldStartRecordingBeforeScenario() throws MobitruOperationException
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.of(UDID));
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.startRecordingBeforeScenario();
        verify(mobitruFacade).startDeviceScreenRecording(UDID);
    }

    @Test
    void shouldNotStartRecordingIfDriverIsNotInitialized()
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.empty());
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.startRecordingBeforeScenario();
        verifyNoInteractions(mobitruFacade);
    }

    @Test
    void shouldAttachRecordingOnSessionFinish() throws MobitruOperationException
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.of(UDID));
        var recording = new ScreenRecording(RECORDING_ID, BINARY_RESPONSE);
        when(mobitruFacade.stopDeviceScreenRecording(UDID)).thenReturn(recording);
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.onBeforeSessionStop(mock(BeforeWebDriverQuitEvent.class));
        verify(attachmentPublisher).publishAttachment(BINARY_RESPONSE, RECORDING_REPORT_NAME);
    }

    @Test
    void shouldNotStopRecordingIfDisabled()
    {
        var mobitruRecordingListener = new MobitruRecordingListener(false, mobitruFacade,
                mobitruSessionInfoStorage, attachmentPublisher);
        mobitruRecordingListener.onBeforeSessionStop(mock(BeforeWebDriverQuitEvent.class));
        verifyNoInteractions(mobitruSessionInfoStorage, mobitruFacade, attachmentPublisher);
    }

    @Test
    void shouldPublishRecordingAfterScenario() throws MobitruOperationException
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.of(UDID));
        var recording = new ScreenRecording(RECORDING_ID, BINARY_RESPONSE);
        when(mobitruFacade.stopDeviceScreenRecording(UDID)).thenReturn(recording);
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.publishRecordingAfterScenario();
        verify(attachmentPublisher).publishAttachment(BINARY_RESPONSE, RECORDING_REPORT_NAME);
    }

    @Test
    void shouldNotPublishRecordingIfDriverIsNotInitialized()
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.empty());
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.publishRecordingAfterScenario();
        verifyNoInteractions(mobitruFacade, attachmentPublisher);
    }

    @Test
    void shouldLogWarnOnStartWithoutThrow() throws MobitruOperationException
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.of(UDID));
        var exception = new MobitruOperationException(UDID);
        doThrow(exception).when(mobitruFacade).startDeviceScreenRecording(UDID);
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.onSessionStart(mock(WebDriverCreateEvent.class));
        assertEquals(List.of(
                LoggingEvent.warn(exception, "Unable to start recording on the device with UUID {}", UDID)),
                logger.getLoggingEvents());
    }

    @Test
    void shouldLogWarnOnStopWithoutThrow() throws MobitruOperationException
    {
        when(mobitruSessionInfoStorage.getDeviceId()).thenReturn(Optional.of(UDID));
        var exception = new MobitruOperationException(UDID);
        doThrow(exception).when(mobitruFacade).stopDeviceScreenRecording(UDID);
        var mobitruRecordingListener = new MobitruRecordingListener(true, mobitruFacade, mobitruSessionInfoStorage,
                attachmentPublisher);
        mobitruRecordingListener.onBeforeSessionStop(mock(BeforeWebDriverQuitEvent.class));
        assertEquals(List.of(
                        LoggingEvent.warn(exception,
                                "Unable to retrieve recording from the device with UUID {}", UDID)),
                logger.getLoggingEvents());
    }
}
