/*
 * Copyright 2019-2025 the original author or authors.
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
import static org.mockito.Mockito.when;

import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ssh.exec.SshOutput;
import org.vividus.ssh.factory.SshSessionFactory;

@ExtendWith(MockitoExtension.class)
class JSchExecutorTests
{
    private static final String EXEC = "exec";
    private static final SshOutput SSH_OUTPUT = new SshOutput();
    private static final Commands COMMANDS = new Commands("ssh-command");

    @Mock private SshSessionFactory sshSessionFactory;
    @Mock private SshConnectionParameters server;
    @InjectMocks private TestJSchExecutor executor;

    @Test
    void shouldExecuteSuccessfully() throws JSchException, CommandExecutionException, AgentProxyException
    {
        var session = mock(Session.class);
        when(sshSessionFactory.createSshSession(server)).thenReturn(session);

        var channelExec = mockChannelOpening(session);
        var actual = executor.execute(server, COMMANDS);
        assertEquals(SSH_OUTPUT, actual);
        var ordered = inOrder(session, channelExec);
        verifyFullConnection(ordered, session, channelExec);
    }

    @Test
    void shouldFailOnCommandExecutionError() throws JSchException, AgentProxyException
    {
        var session = mock(Session.class);
        when(sshSessionFactory.createSshSession(server)).thenReturn(session);
        var channelExec = mockChannelOpening(session);
        var jSchException = new JSchException();
        var exception = assertThrows(CommandExecutionException.class,
                () -> new TestJSchExecutor(sshSessionFactory)
                {
                    @Override
                    protected SshOutput executeCommand(SshConnectionParameters serverConfig, Commands commands,
                                                       ChannelExec channel) throws JSchException
                    {
                        throw jSchException;
                    }
                }.execute(server, COMMANDS));
        assertEquals(jSchException, exception.getCause());
        var ordered = inOrder(session, channelExec);
        verifyFullConnection(ordered, session, channelExec);
    }

    @Test
    void shouldFailOnChannelOpeningError() throws JSchException, AgentProxyException
    {
        var session = mock(Session.class);
        when(sshSessionFactory.createSshSession(server)).thenReturn(session);
        var jSchException = new JSchException();
        when(session.openChannel(EXEC)).thenThrow(jSchException);
        var exception = assertThrows(CommandExecutionException.class,
                () -> executor.execute(server, COMMANDS));
        assertEquals(jSchException, exception.getCause());
        var ordered = inOrder(session);
        verifySessionConnection(ordered, session);
        ordered.verify(session).openChannel(EXEC);
        ordered.verify(session).disconnect();
    }

    @Test
    void shouldFailOnInvalidSessionCreation() throws JSchException, AgentProxyException
    {
        var jSchException = new JSchException();
        doThrow(jSchException).when(sshSessionFactory).createSshSession(server);
        var exception = assertThrows(CommandExecutionException.class,
                () -> executor.execute(server, COMMANDS));
        assertEquals(jSchException, exception.getCause());
    }

    private ChannelExec mockChannelOpening(Session session) throws JSchException
    {
        var channelExec = mock(ChannelExec.class);
        when(session.openChannel(EXEC)).thenReturn(channelExec);
        return channelExec;
    }

    private void verifyFullConnection(InOrder ordered, Session session, ChannelExec channelExec) throws JSchException
    {
        verifySessionConnection(ordered, session);
        ordered.verify(session).openChannel(EXEC);
        ordered.verify(channelExec).disconnect();
        ordered.verify(session).disconnect();
    }

    private void verifySessionConnection(InOrder ordered, Session session)
            throws JSchException
    {
        ordered.verify(session).connect(30_000);
    }

    @SuppressWarnings("checkstyle:FinalClass")
    private static class TestJSchExecutor extends JSchExecutor<ChannelExec, SshOutput>
    {
        protected TestJSchExecutor(SshSessionFactory sshSessionFactory)
        {
            super(sshSessionFactory);
        }

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
