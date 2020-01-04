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

package org.vividus.ssh.exec;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.IAttachmentPublisher;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SshOutputPublisherTests
{
    @Mock
    private IAttachmentPublisher attachmentPublisher;

    @InjectMocks
    private SshOutputPublisher sshOutputPublisher;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SshOutputPublisher.class);

    @Test
    void publishOutputWithoutError()
    {
        String output = "success";
        SshOutput sshOutput = new SshOutput();
        sshOutput.setOutputStream(output);
        sshOutputPublisher.publishOutput(sshOutput);
        LoggingEvent loggingEvent = verifyStdoutAttachmentPublishing(output);
        verifyNoMoreInteractions(attachmentPublisher);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(loggingEvent)));
    }

    @Test
    void publishOutputWithError()
    {
        String output = "fail";
        String error = "error";
        SshOutput sshOutput = new SshOutput();
        sshOutput.setOutputStream(output);
        sshOutput.setErrorStream(error);
        sshOutputPublisher.publishOutput(sshOutput);
        LoggingEvent outputLoggingEvent = verifyStdoutAttachmentPublishing(output);
        LoggingEvent errorLoggingEvent = verifyStderrAttachmentPublishing(error);
        verifyNoMoreInteractions(attachmentPublisher);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(outputLoggingEvent, errorLoggingEvent)));
    }

    private LoggingEvent verifyStdoutAttachmentPublishing(String output)
    {
        return verifyStdoutAttachmentPublishing("stdout", output, "stdout.txt");
    }

    private LoggingEvent verifyStderrAttachmentPublishing(String output)
    {
        return verifyStdoutAttachmentPublishing("stderr", output, "stderr.txt");
    }

    private LoggingEvent verifyStdoutAttachmentPublishing(String stream, String output, String fileName)
    {
        verify(attachmentPublisher).publishAttachment(output.getBytes(StandardCharsets.UTF_8), fileName);
        return debug("SSH command {} stream:{}{}", stream, System.lineSeparator(), output);
    }
}
