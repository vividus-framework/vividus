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

import java.nio.charset.StandardCharsets;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.ssh.OutputPublisher;

@Named
public class SshOutputPublisher implements OutputPublisher<SshOutput>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SshOutputPublisher.class);

    private final IAttachmentPublisher attachmentPublisher;

    public SshOutputPublisher(IAttachmentPublisher attachmentPublisher)
    {
        this.attachmentPublisher = attachmentPublisher;
    }

    @Override
    public void publishOutput(SshOutput sshOutput)
    {
        publishAttachment("stdout", sshOutput.getOutputStream());

        String errorStreamOutput = sshOutput.getErrorStream();
        if (StringUtils.isNotEmpty(errorStreamOutput))
        {
            publishAttachment("stderr", errorStreamOutput);
        }
    }

    private void publishAttachment(String streamName, String streamOutput)
    {
        LOGGER.debug("SSH command {} stream:{}{}", streamName, System.lineSeparator(), streamOutput);
        attachmentPublisher.publishAttachment(streamOutput.getBytes(StandardCharsets.UTF_8), streamName + ".txt");
    }
}
