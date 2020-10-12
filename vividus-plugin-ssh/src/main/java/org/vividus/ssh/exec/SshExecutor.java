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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

import org.apache.commons.io.IOUtils;
import org.vividus.ssh.Commands;
import org.vividus.ssh.JSchExecutor;
import org.vividus.ssh.ServerConfiguration;
import org.vividus.util.Sleeper;

public abstract class SshExecutor<T extends Channel> extends JSchExecutor<T, SshOutput>
{
    @Override
    protected SshOutput executeCommand(ServerConfiguration serverConfiguration, Commands commands, T channel)
            throws JSchException, IOException
    {
        configureChannel(channel, serverConfiguration);
        SshOutput executionOutput = new SshOutput();
        try (ByteArrayOutputStream errorStream = new ByteArrayOutputStream())
        {
            setupCommands(channel, commands);
            channel.setExtOutputStream(errorStream);
            channel.connect();
            executionOutput.setOutputStream(readChannelInputStream(channel));
            executionOutput.setErrorStream(new String(errorStream.toByteArray(), StandardCharsets.UTF_8));
            executionOutput.setExitStatus(channel.getExitStatus());
        }
        return executionOutput;
    }

    protected abstract void configureChannel(T channel, ServerConfiguration serverConfiguration);

    protected abstract void setupCommands(T channel, Commands commands);

    private String readChannelInputStream(T channel) throws IOException
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
