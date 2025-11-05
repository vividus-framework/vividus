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

import java.io.IOException;

import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.vividus.ssh.factory.SshSessionFactory;

public abstract class JSchExecutor<T extends Channel, R> implements CommandExecutor<R>
{
    private static final int CONNECT_TIMEOUT = 30_000;

    private final SshSessionFactory sshSessionFactory;

    protected JSchExecutor(SshSessionFactory sshSessionFactory)
    {
        this.sshSessionFactory = sshSessionFactory;
    }

    @Override
    public R execute(SshConnectionParameters sshConnectionParameters, Commands commands)
            throws CommandExecutionException
    {
        try
        {
            Session session = sshSessionFactory.createSshSession(sshConnectionParameters);
            return execute(session, sshConnectionParameters, commands);
        }
        catch (JSchException | AgentProxyException e)
        {
            throw new CommandExecutionException(e);
        }
    }

    private R execute(Session session, SshConnectionParameters sshConnectionParameters, Commands commands)
            throws JSchException, CommandExecutionException
    {
        try
        {
            session.connect(CONNECT_TIMEOUT);
            @SuppressWarnings("unchecked")
            T channel = (T) session.openChannel(getChannelType());
            try
            {
                return executeCommand(sshConnectionParameters, commands, channel);
            }
            catch (JSchException | IOException e)
            {
                throw new CommandExecutionException(e);
            }
            finally
            {
                channel.disconnect();
            }
        }
        catch (JSchException e)
        {
            throw new CommandExecutionException(e);
        }
        finally
        {
            session.disconnect();
        }
    }

    public abstract String getChannelType();

    protected abstract R executeCommand(SshConnectionParameters serverConfig, Commands commands, T channel)
            throws JSchException, IOException;
}
