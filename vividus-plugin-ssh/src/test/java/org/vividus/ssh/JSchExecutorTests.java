/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SSHAgentConnector;
import com.jcraft.jsch.Session;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.vividus.ssh.exec.SshOutput;

class JSchExecutorTests
{
    private static final String EXEC = "exec";
    private static final SshOutput SSH_OUTPUT = new SshOutput();
    private static final Commands COMMANDS = new Commands("ssh-command");
    private static final String IDENTITY_NAME = "default";

    @Test
    void shouldExecuteSuccessfullyWithoutAgentForwarding() throws JSchException, CommandExecutionException
    {
        var server = getDefaultServerConfiguration();
        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(server.getUsername(), server.getHost(), server.getPort()))
                        .thenReturn(session)))
        {
            var channelExec = mockChannelOpening(session);
            var actual = new TestJSchExecutor().execute(server, COMMANDS);
            assertEquals(SSH_OUTPUT, actual);
            var jSch = jSchMock.constructed().get(0);
            var ordered = inOrder(jSch, session, channelExec);
            ordered.verify(jSch).addIdentity(IDENTITY_NAME, server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    server.getPublicKey().getBytes(StandardCharsets.UTF_8),
                    server.getPassphrase().getBytes(StandardCharsets.UTF_8));
            verifyFullConnection(ordered, server, session, channelExec);
        }
    }

    @Test
    void shouldExecuteSuccessfullyWithAgentForwarding()
            throws JSchException, CommandExecutionException
    {
        var server = getDefaultServerConfiguration();
        server.setAgentForwarding(true);
        var session = mock(Session.class);
        try (
            var connector = mockConstruction(SSHAgentConnector.class);
            var jSchMock = mockConstruction(JSch.class, (mock, context) -> when(
                mock.getSession(server.getUsername(), server.getHost(), server.getPort())).thenReturn(session)
            );
            var remoteIdentityRepositoryMock = mockConstruction(
                    AgentIdentityRepository.class, (mock, context) -> {
                        assertEquals(1, context.getCount());
                        assertEquals(List.of(connector.constructed().get(0)), context.arguments());
                })
        )
        {
            var channelExec = mockChannelOpening(session);
            var actual = new TestJSchExecutor().execute(server, COMMANDS);
            assertEquals(SSH_OUTPUT, actual);
            var jSch = jSchMock.constructed().get(0);
            var ordered = inOrder(jSch, session, channelExec);
            ordered.verify(jSch).setIdentityRepository(remoteIdentityRepositoryMock.constructed().get(0));
            verifyFullConnection(ordered, server, session, channelExec);
        }
    }

    @Test
    void shouldFailOnCommandExecutionError() throws JSchException
    {
        var server = getDefaultServerConfiguration();
        server.setPublicKey(null);
        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(server.getUsername(), server.getHost(), server.getPort()))
                        .thenReturn(session)))
        {
            var channelExec = mockChannelOpening(session);
            var jSchException = new JSchException();
            var exception = assertThrows(CommandExecutionException.class,
                    () -> new TestJSchExecutor()
                    {
                        @Override
                        protected SshOutput executeCommand(SshConnectionParameters serverConfig, Commands commands,
                                ChannelExec channel) throws JSchException
                        {
                            throw jSchException;
                        }
                    }.execute(server, COMMANDS));
            assertEquals(jSchException, exception.getCause());
            var jSch = jSchMock.constructed().get(0);
            var ordered = inOrder(jSch, session, channelExec);
            verifyFullConnection(ordered, server, session, channelExec);
        }
    }

    @Test
    void shouldFailOnChannelOpeningError() throws JSchException
    {
        var server = getDefaultServerConfiguration();
        server.setPrivateKey(null);
        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(server.getUsername(), server.getHost(), server.getPort()))
                        .thenReturn(session)))
        {
            var jSchException = new JSchException();
            when(session.openChannel(EXEC)).thenThrow(jSchException);
            var exception = assertThrows(CommandExecutionException.class,
                    () -> new TestJSchExecutor().execute(server, COMMANDS));
            assertEquals(jSchException, exception.getCause());
            var jSch = jSchMock.constructed().get(0);
            var ordered = inOrder(jSch, session);
            verifySessionConnection(ordered, server, session);
            ordered.verify(session).openChannel(EXEC);
            ordered.verify(session).disconnect();
        }
    }

    @SuppressWarnings("try")
    @Test
    void shouldFailOnJSchConfigurationError()
    {
        var server = getDefaultServerConfiguration();
        server.setPassphrase(null);
        var jSchException = new JSchException();
        try (var ignored = mockConstruction(JSch.class,
                (mock, context) -> doThrow(jSchException).when(mock).addIdentity(IDENTITY_NAME,
                        server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                        server.getPublicKey().getBytes(StandardCharsets.UTF_8), null)))
        {
            var exception = assertThrows(CommandExecutionException.class,
                    () -> new TestJSchExecutor().execute(server, COMMANDS));
            assertEquals(jSchException, exception.getCause());
        }
    }

    private SshConnectionParameters getDefaultServerConfiguration()
    {
        var sshConnectionParameters = new SshConnectionParameters();
        sshConnectionParameters.setAgentForwarding(false);
        sshConnectionParameters.setPassphrase("passphrase");
        sshConnectionParameters.setPrivateKey("privatekey");
        sshConnectionParameters.setPublicKey("publickey");
        sshConnectionParameters.setUsername("username");
        sshConnectionParameters.setHost("host");
        sshConnectionParameters.setPort(22);
        sshConnectionParameters.setPassword("password");
        return sshConnectionParameters;
    }

    private ChannelExec mockChannelOpening(Session session) throws JSchException
    {
        var channelExec = mock(ChannelExec.class);
        when(session.openChannel(EXEC)).thenReturn(channelExec);
        return channelExec;
    }

    private void verifyFullConnection(InOrder ordered, SshConnectionParameters server, Session session,
            ChannelExec channelExec) throws JSchException
    {
        verifySessionConnection(ordered, server, session);
        ordered.verify(session).openChannel(EXEC);
        ordered.verify(channelExec).disconnect();
        ordered.verify(session).disconnect();
    }

    private void verifySessionConnection(InOrder ordered, SshConnectionParameters server, Session session)
            throws JSchException
    {
        ordered.verify(session).setConfig("StrictHostKeyChecking", "no");
        ordered.verify(session).setConfig("PreferredAuthentications", "publickey,password");
        ordered.verify(session).setPassword(server.getPassword());
        ordered.verify(session).connect(30_000);
    }

    @SuppressWarnings("checkstyle:FinalClass")
    private static class TestJSchExecutor extends JSchExecutor<ChannelExec, SshOutput>
    {
        @Override
        @SuppressWarnings("checkstyle:SimpleAccessorNameNotation")
        public String getChannelType()
        {
            return EXEC;
        }

        @Override
        protected SshOutput executeCommand(SshConnectionParameters serverConfig, Commands commands, ChannelExec channel)
                throws JSchException
        {
            return SSH_OUTPUT;
        }
    }
}
