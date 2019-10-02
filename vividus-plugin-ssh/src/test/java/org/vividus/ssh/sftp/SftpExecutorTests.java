/*
 * Copyright 2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ssh.Commands;
import org.vividus.ssh.ServerConfiguration;

@ExtendWith(MockitoExtension.class)
class SftpExecutorTests
{
    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private SftpExecutor sftpExecutor;

    @Test
    void shouldReturnExecChannelType()
    {
        assertEquals("sftp", sftpExecutor.getChannelType());
    }

    @Test
    void testExecuteCommandsViaSftpSuccessfully() throws Exception
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String pwd = "/Users";
        when(channel.pwd()).thenReturn(pwd).thenReturn(pwd);
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAgentForwarding(true);
        Commands commands = new Commands("pwd; cd ~; pwd");
        SftpOutput sftpOutput = sftpExecutor.executeCommand(serverConfiguration, commands, channel);
        assertEquals(pwd + System.lineSeparator() + pwd, sftpOutput.getResult());
        InOrder ordered = inOrder(channel);
        ordered.verify(channel).setAgentForwarding(serverConfiguration.isAgentForwarding());
        ordered.verify(channel).connect();
        ordered.verify(channel).pwd();
        ordered.verify(channel).cd("~");
        ordered.verify(channel).pwd();
        ordered.verifyNoMoreInteractions();
        verifyNoInteractions(softAssert);
    }

    @Test
    void testPopulateErrorStreamOnSftpError() throws Exception
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        SftpException sftpException = new SftpException(0, "error");
        doThrow(sftpException).when(channel).pwd();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAgentForwarding(true);
        SftpOutput sftpOutput = sftpExecutor.executeCommand(serverConfiguration, new Commands("pwd"), channel);
        assertEquals("", sftpOutput.getResult());
        InOrder ordered = inOrder(channel, softAssert);
        ordered.verify(channel).setAgentForwarding(serverConfiguration.isAgentForwarding());
        ordered.verify(channel).connect();
        ordered.verify(channel).pwd();
        ordered.verify(softAssert).recordFailedAssertion("SFTP command error", sftpException);
        ordered.verifyNoMoreInteractions();
    }
}
