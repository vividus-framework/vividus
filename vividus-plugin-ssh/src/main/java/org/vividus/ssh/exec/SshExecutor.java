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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import org.apache.commons.io.IOUtils;
import org.vividus.ssh.Commands;
import org.vividus.ssh.JSchExecutor;
import org.vividus.ssh.ServerConfiguration;
import org.vividus.util.Sleeper;

public class SshExecutor extends JSchExecutor<ChannelExec, SshOutput>
{
    @Override
    protected String getChannelType()
    {
        return "exec";
    }

    @Override
    protected SshOutput executeCommand(ServerConfiguration serverConfiguration, Commands commands, ChannelExec channel)
            throws JSchException, IOException
    {
        channel.setAgentForwarding(serverConfiguration.isAgentForwarding());
        channel.setPty(serverConfiguration.isPseudoTerminalEnabled());
        SshOutput executionOutput = new SshOutput();
        try (ByteArrayOutputStream errorStream = new ByteArrayOutputStream())
        {
            channel.setCommand(commands.getJoinedCommands());
            channel.setErrStream(errorStream);
            channel.connect();
            executionOutput.setOutputStream(readChannelInputStream(channel));
            executionOutput.setErrorStream(new String(errorStream.toByteArray(), StandardCharsets.UTF_8));
            executionOutput.setExitStatus(channel.getExitStatus());
        }
        return executionOutput;
    }

    private String readChannelInputStream(ChannelExec channel) throws IOException
    {
        try (InputStream in = channel.getInputStream();
                InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                StringWriter stringWriter = new StringWriter())
        {
            while (true)
            {
                IOUtils.copy(reader, stringWriter);
                if (channel.isClosed())
                {
                    if (in.available() > 0)
                    {
                        continue;
                    }
                    return stringWriter.toString();
                }
                Sleeper.sleep(Duration.ofSeconds(1));
            }
        }
    }
}
