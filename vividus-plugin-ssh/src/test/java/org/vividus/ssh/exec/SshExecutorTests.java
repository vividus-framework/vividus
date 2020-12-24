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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.ChannelExec;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.vividus.ssh.Commands;
import org.vividus.ssh.ServerConfiguration;

class SshExecutorTests
{
    private final SshExecutor<ChannelExec> sshExecutor = new SshExecutor<ChannelExec>()
    {
        @Override
        public String getChannelType()
        {
            return "any";
        }

        @Override
        protected void configureChannel(ChannelExec channel, ServerConfiguration serverConfiguration)
        {
            channel.setPty(true);
        }

        @Override
        protected void setupCommands(ChannelExec channel, Commands commands)
        {
            channel.setCommand(commands.getJoinedCommands());
        }
    };

    @Test
    void testExecuteCommandsViaSshSuccessfully() throws Exception
    {
        ChannelExec channel = mock(ChannelExec.class);
        String errorOutput = "error-output";
        mockErrorStream(errorOutput, channel);
        String commandOutput = "command-output";
        @SuppressWarnings("PMD.CloseResource")
        InputStream channelInputStream = mockChannelInputStream(commandOutput, channel);
        when(channel.isClosed()).thenReturn(Boolean.FALSE).then(invocation ->
        {
            channelInputStream.reset();
            return Boolean.TRUE;
        }).thenReturn(Boolean.TRUE);
        int exitStatus = 1;
        when(channel.getExitStatus()).thenReturn(exitStatus);
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        String commands = "ssh-command";
        SshOutput sshOutput = sshExecutor.executeCommand(serverConfiguration, new Commands(commands), channel);
        assertEquals(commandOutput + commandOutput, sshOutput.getOutputStream());
        assertEquals(errorOutput, sshOutput.getErrorStream());
        assertEquals(exitStatus, sshOutput.getExitStatus());
        InOrder ordered = inOrder(channel);
        ordered.verify(channel).setPty(true);
        ordered.verify(channel).setCommand(commands);
        ordered.verify(channel).connect();
        ordered.verify(channel, times(3)).isClosed();
        ordered.verify(channel).getExitStatus();
        ordered.verifyNoMoreInteractions();
    }

    private InputStream mockChannelInputStream(String commandOutput, ChannelExec channel) throws IOException
    {
        InputStream channelInputStream = IOUtils.toInputStream(commandOutput, StandardCharsets.UTF_8);
        when(channel.getInputStream()).thenReturn(channelInputStream);
        return channelInputStream;
    }

    private void mockErrorStream(String errorOutput, ChannelExec channel)
    {
        doNothing().when(channel).setExtOutputStream(argThat(out -> {
            try
            {
                out.write(errorOutput.getBytes(StandardCharsets.UTF_8));
            }
            catch (@SuppressWarnings("unused") IOException e)
            {
                return false;
            }
            return true;
        }));
    }
}
