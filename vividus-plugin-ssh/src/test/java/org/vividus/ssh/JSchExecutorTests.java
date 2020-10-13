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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.ssh.exec.SshOutput;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JSchExecutor.class, ConnectorFactory.class})
public class JSchExecutorTests
{
    private static final String EXEC = "exec";
    private static final SshOutput SSH_OUTPUT = new SshOutput();
    private static final Commands COMMANDS = new Commands("ssh-command");
    private static final String IDENTITY_NAME = "default";

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void shouldExecuteSuccessfullyWithoutAgentForwarding() throws Exception
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        JSch jSch = mock(JSch.class);
        whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        Session session = mock(Session.class);
        when(jSch.getSession(server.getUsername(), server.getHost(), server.getPort())).thenReturn(session);
        ChannelExec channelExec = mockChannelOpening(session);
        SshOutput actual = new TestJSchExecutor().execute(server, COMMANDS);
        assertEquals(SSH_OUTPUT, actual);
        InOrder ordered = inOrder(jSch, session, channelExec);
        ordered.verify(jSch).addIdentity(IDENTITY_NAME, server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                server.getPublicKey().getBytes(StandardCharsets.UTF_8),
                server.getPassphrase().getBytes(StandardCharsets.UTF_8));
        verifyFullConnection(ordered, server, session, channelExec);
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void shouldExecuteSuccessfullyWithAgentForwarding() throws Exception
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setAgentForwarding(true);
        JSch jSch = mock(JSch.class);
        whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        mockStatic(ConnectorFactory.class);
        ConnectorFactory connectorFactory = mock(ConnectorFactory.class);
        when(ConnectorFactory.getDefault()).thenReturn(connectorFactory);
        Connector connector = mock(Connector.class);
        when(connectorFactory.createConnector()).thenReturn(connector);
        RemoteIdentityRepository remoteIdentityRepository = mock(RemoteIdentityRepository.class);
        whenNew(RemoteIdentityRepository.class).withArguments(connector).thenReturn(remoteIdentityRepository);
        doNothing().when(jSch).setIdentityRepository(remoteIdentityRepository);
        Session session = mock(Session.class);
        when(jSch.getSession(server.getUsername(), server.getHost(), server.getPort())).thenReturn(session);
        ChannelExec channelExec = mockChannelOpening(session);
        SshOutput actual = new TestJSchExecutor().execute(server, COMMANDS);
        assertEquals(SSH_OUTPUT, actual);
        InOrder ordered = inOrder(jSch, session, channelExec);
        ordered.verify(jSch).setIdentityRepository(remoteIdentityRepository);
        verifyFullConnection(ordered, server, session, channelExec);
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void shouldFailOnCommandExecutionError() throws Exception
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setPublicKey(null);
        JSch jSch = mock(JSch.class);
        whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        Session session = mock(Session.class);
        when(jSch.getSession(server.getUsername(), server.getHost(), server.getPort())).thenReturn(session);
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
        InOrder ordered = inOrder(jSch, session, channelExec);
        verifyFullConnection(ordered, server, session, channelExec);
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void shouldFailOnChannelOpeningError() throws Exception
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setPrivateKey(null);
        JSch jSch = mock(JSch.class);
        whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        Session session = mock(Session.class);
        when(jSch.getSession(server.getUsername(), server.getHost(), server.getPort())).thenReturn(session);
        JSchException jSchException = new JSchException();
        when(session.openChannel(EXEC)).thenThrow(jSchException);
        CommandExecutionException exception = assertThrows(CommandExecutionException.class,
            () -> new TestJSchExecutor().execute(server, COMMANDS));
        assertEquals(jSchException, exception.getCause());
        InOrder ordered = inOrder(jSch, session);
        verifySessionConnection(ordered, server, session);
        ordered.verify(session).openChannel(EXEC);
        ordered.verify(session).disconnect();
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void shouldFailOnJSchConfigurationError() throws Exception
    {
        ServerConfiguration server = getDefaultServerConfiguration();
        server.setPassphrase(null);
        JSch jSch = mock(JSch.class);
        whenNew(JSch.class).withNoArguments().thenReturn(jSch);
        JSchException jSchException = new JSchException();
        doThrow(jSchException).when(jSch).addIdentity(IDENTITY_NAME,
                server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                server.getPublicKey().getBytes(StandardCharsets.UTF_8), null);
        CommandExecutionException exception = assertThrows(CommandExecutionException.class,
            () -> new TestJSchExecutor().execute(server, COMMANDS));
        assertEquals(jSchException, exception.getCause());
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
