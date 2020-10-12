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

import com.jcraft.jsch.ChannelExec;

import org.junit.jupiter.api.Test;
import org.vividus.ssh.Commands;
import org.vividus.ssh.ServerConfiguration;

class SshExecExecutorTests
{
    private final SshExecExecutor sshExecExecutor = new SshExecExecutor();

    @Test
    void shouldReturnExecChannelType()
    {
        assertEquals("exec", sshExecExecutor.getChannelType());
    }

    @Test
    void shouldConfigureChannel()
    {
        ChannelExec channel = mock(ChannelExec.class);
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAgentForwarding(true);
        serverConfiguration.setPseudoTerminalEnabled(true);
        sshExecExecutor.configureChannel(channel, serverConfiguration);
        verify(channel).setAgentForwarding(true);
        verify(channel).setPty(true);
    }

    @Test
    void shouldSetupCommands()
    {
        ChannelExec channel = mock(ChannelExec.class);
        String joinedCommands = "commands";
        sshExecExecutor.setupCommands(channel, new Commands(joinedCommands));
        verify(channel).setCommand(joinedCommands);
    }
}
