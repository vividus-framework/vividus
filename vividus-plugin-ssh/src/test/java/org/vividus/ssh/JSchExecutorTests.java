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

package org.vividus.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
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
        ServerConfiguration server = getDefaultServerConfiguration();
        Session session = mock(Session.class);
        try (MockedConstruction<JSch> jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(server.getUsername(), server.getHost(), server.getPort()))
                        .thenReturn(session)))
        {
            ChannelExec channelExec = mockChannelOpening(session);
            SshOutput actual = new TestJSchExecutor().execute(server, COMMANDS);
            assertEquals(SSH_OUTPUT, actual);
            JSch jSch = jSchMock.constructed().get(0);
            InOrder ordered = inOrder(jSch, session, channelExec);
            ordered.verify(jSch).addIdentity(IDENTITY_NAME, server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    server.getPublicKey().getBytes(StandardCharsets.UTF_8),
                    server.getPassphrase().getBytes(StandardCharsets.UTF_8));
            verifyFullConnection(ordered, server, session, channelExec);
        }
    }

    @Test
    void shouldExecuteSuccessfullyWithAgentForwarding()
            throws AgentProxyException, JSchException, CommandExecutionException
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setAgentForwarding(true);
        Session session = mock(Session.class);
        Connector connector = mock(Connector.class);
        try (MockedStatic<ConnectorFactory> connectorFactoryMock = mockStatic(ConnectorFactory.class);
                MockedConstruction<JSch> jSchMock = mockConstruction(JSch.class, (mock, context) -> when(
                        mock.getSession(server.getUsername(), server.getHost(), server.getPort())).thenReturn(session));
                MockedConstruction<RemoteIdentityRepository> remoteIdentityRepositoryMock = mockConstruction(
                        RemoteIdentityRepository.class, (mock, context) -> {
                            assertEquals(1, context.getCount());
                            assertEquals(List.of(connector), context.arguments());
                        })
        )
        {
            ConnectorFactory connectorFactory = mock(ConnectorFactory.class);
            connectorFactoryMock.when(ConnectorFactory::getDefault).thenReturn(connectorFactory);
            when(connectorFactory.createConnector()).thenReturn(connector);
            ChannelExec channelExec = mockChannelOpening(session);
            SshOutput actual = new TestJSchExecutor().execute(server, COMMANDS);
            assertEquals(SSH_OUTPUT, actual);
            JSch jSch = jSchMock.constructed().get(0);
            InOrder ordered = inOrder(jSch, session, channelExec);
            ordered.verify(jSch).setIdentityRepository(remoteIdentityRepositoryMock.constructed().get(0));
            verifyFullConnection(ordered, server, session, channelExec);
        }
    }

    @Test
    void shouldFailOnCommandExecutionError() throws JSchException
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setPublicKey(null);
        Session session = mock(Session.class);
        try (MockedConstruction<JSch> jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(server.getUsername(), server.getHost(), server.getPort()))
                        .thenReturn(session)))
        {
            ChannelExec channelExec = mockChannelOpening(session);
            JSchException jSchException = new JSchException();
            CommandExecutionException exception = assertThrows(CommandExecutionException.class,
                    () -> new TestJSchExecutor()
                    {
                        @Override
                        protected SshOutput executeCommand(ServerConfiguration serverConfig, Commands commands,
                                ChannelExec channel) throws JSchException
                        {
                            throw jSchException;
                        }
                    }.execute(server, COMMANDS));
            assertEquals(jSchException, exception.getCause());
            JSch jSch = jSchMock.constructed().get(0);
            InOrder ordered = inOrder(jSch, session, channelExec);
            verifyFullConnection(ordered, server, session, channelExec);
        }
    }

    @Test
    void shouldFailOnChannelOpeningError() throws JSchException
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setPrivateKey(null);
        Session session = mock(Session.class);
        try (MockedConstruction<JSch> jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(server.getUsername(), server.getHost(), server.getPort()))
                        .thenReturn(session)))
        {
            JSchException jSchException = new JSchException();
            when(session.openChannel(EXEC)).thenThrow(jSchException);
            CommandExecutionException exception = assertThrows(CommandExecutionException.class,
                    () -> new TestJSchExecutor().execute(server, COMMANDS));
            assertEquals(jSchException, exception.getCause());
            JSch jSch = jSchMock.constructed().get(0);
            InOrder ordered = inOrder(jSch, session);
            verifySessionConnection(ordered, server, session);
            ordered.verify(session).openChannel(EXEC);
            ordered.verify(session).disconnect();
        }
    }

    @Test
    void shouldFailOnJSchConfigurationError()
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setPassphrase(null);
        JSchException jSchException = new JSchException();
        try (MockedConstruction<JSch> ignored = mockConstruction(JSch.class,
                (mock, context) -> doThrow(jSchException).when(mock).addIdentity(IDENTITY_NAME,
                        server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                        server.getPublicKey().getBytes(StandardCharsets.UTF_8), null)))
        {
            CommandExecutionException exception = assertThrows(CommandExecutionException.class,
                    () -> new TestJSchExecutor().execute(server, COMMANDS));
            assertEquals(jSchException, exception.getCause());
        }
    }

    private ServerConfiguration getDefaultServerConfiguration()
    {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAgentForwarding(false);
        serverConfiguration.setPassphrase("passphrase");
        serverConfiguration.setPrivateKey("privatekey");
        serverConfiguration.setPublicKey("publickey");
        serverConfiguration.setUsername("username");
        serverConfiguration.setHost("host");
        serverConfiguration.setPort(22);
        serverConfiguration.setPassword("password");
        return serverConfiguration;
    }

    private ChannelExec mockChannelOpening(Session session) throws JSchException
    {
        ChannelExec channelExec = mock(ChannelExec.class);
        when(session.openChannel(EXEC)).thenReturn(channelExec);
        return channelExec;
    }

    private void verifyFullConnection(InOrder ordered, ServerConfiguration server, Session session,
            ChannelExec channelExec) throws JSchException
    {
        verifySessionConnection(ordered, server, session);
        ordered.verify(session).openChannel(EXEC);
        ordered.verify(channelExec).disconnect();
        ordered.verify(session).disconnect();
    }

    private void verifySessionConnection(InOrder ordered, ServerConfiguration server, Session session)
            throws JSchException
    {
        ordered.verify(session).setConfig("StrictHostKeyChecking", "no");
        ordered.verify(session).setConfig("PreferredAuthentications", "publickey,password");
        ordered.verify(session).setPassword(server.getPassword());
        ordered.verify(session).connect(30_000);
    }

    private static class TestJSchExecutor extends JSchExecutor<ChannelExec, SshOutput>
    {
        @Override
        @SuppressWarnings("checkstyle:SimpleAccessorNameNotation")
        public String getChannelType()
        {
            return EXEC;
        }

        @Override
        protected SshOutput executeCommand(ServerConfiguration serverConfig, Commands commands, ChannelExec channel)
                throws JSchException
        {
            return SSH_OUTPUT;
        }
    }
}
