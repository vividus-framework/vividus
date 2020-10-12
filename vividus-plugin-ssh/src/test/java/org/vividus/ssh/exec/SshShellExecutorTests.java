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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.ChannelShell;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.vividus.ssh.Commands;
import org.vividus.ssh.ServerConfiguration;

class SshShellExecutorTests
{
    private final SshShellExecutor sshShellExecutor = new SshShellExecutor();

    @Test
    void shouldReturnExecChannelType()
    {
        assertEquals("shell", sshShellExecutor.getChannelType());
    }

    @Test
    void shouldConfigureChannel()
    {
        ChannelShell channel = mock(ChannelShell.class);
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAgentForwarding(true);
        serverConfiguration.setPseudoTerminalEnabled(true);
        sshShellExecutor.configureChannel(channel, serverConfiguration);
        verify(channel).setAgentForwarding(true);
        verify(channel).setPty(true);
    }

    @Test
    void shouldSetupCommands() throws IOException
    {
        ChannelShell channel = mock(ChannelShell.class);
        String joinedCommands = "commands";
        sshShellExecutor.setupCommands(channel, new Commands(joinedCommands));
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(channel).setInputStream(inputStreamCaptor.capture());
        String actual = IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8);
        assertEquals(joinedCommands, actual);
    }
}
