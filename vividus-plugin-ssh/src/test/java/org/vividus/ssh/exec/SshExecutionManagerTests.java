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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.channels.Channel;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.ssh.CommandExecutionException;
import org.vividus.ssh.Commands;
import org.vividus.ssh.ServerConfiguration;

class SshExecutionManagerTests
{
    private static final Commands COMMANDS = new Commands("ssh-command");

    @ParameterizedTest
    @CsvSource({
            ",      org.vividus.ssh.exec.SshExecExecutor",
            "exec,  org.vividus.ssh.exec.SshExecExecutor",
            "shell, org.vividus.ssh.exec.SshShellExecutor"
    })
    void shouldRunExecution(String channelType, Class<SshExecutor<? extends Channel>> executorClass)
            throws CommandExecutionException
    {
        SshExecutor<? extends Channel> executor = mock(executorClass);
        when(executor.getChannelType()).thenCallRealMethod();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setChannelType(Optional.ofNullable(channelType));

        SshOutput sshOutput = new SshOutput();
        when(executor.execute(serverConfiguration, COMMANDS)).thenReturn(sshOutput);
        SshOutputPublisher outputPublisher = mock(SshOutputPublisher.class);
        SshExecutionManager executionManager = new SshExecutionManager(List.of(executor), outputPublisher);
        SshOutput actual = executionManager.run(serverConfiguration, COMMANDS);
        assertEquals(sshOutput, actual);
        verify(outputPublisher).publishOutput(sshOutput);
    }

    @Test
    void shouldThrowErrorWhenChannelTypeIsNotSupported() throws CommandExecutionException
    {
        SshShellExecutor executor = mock(SshShellExecutor.class);
        when(executor.getChannelType()).thenReturn("invalid");
        ServerConfiguration serverConfiguration = new ServerConfiguration();

        SshOutput sshOutput = new SshOutput();
        when(executor.execute(serverConfiguration, COMMANDS)).thenReturn(sshOutput);
        SshOutputPublisher outputPublisher = mock(SshOutputPublisher.class);
        SshExecutionManager executionManager = new SshExecutionManager(List.of(executor), outputPublisher);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executionManager.run(serverConfiguration, COMMANDS));
        assertEquals("Unsupported channel type: exec", exception.getMessage());
        verifyNoInteractions(outputPublisher);
    }
}
