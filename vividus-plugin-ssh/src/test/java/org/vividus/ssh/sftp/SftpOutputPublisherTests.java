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

package org.vividus.ssh.sftp;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
class SftpOutputPublisherTests
{
    @Mock
    private IAttachmentPublisher attachmentPublisher;

    @InjectMocks
    private SftpOutputPublisher sftpOutputPublisher;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SftpOutputPublisher.class);

    @Test
    void publishOutput()
    {
        String result = "result";
        SftpOutput sftpOutput = new SftpOutput();
        sftpOutput.setResult(result);
        sftpOutputPublisher.publishOutput(sftpOutput);
        verify(attachmentPublisher).publishAttachment(result.getBytes(StandardCharsets.UTF_8), "result.txt");
        verifyNoMoreInteractions(attachmentPublisher);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(debug("SFTP command result:{}{}", System.lineSeparator(), result))));
    }
}
