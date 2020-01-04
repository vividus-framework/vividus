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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;

public abstract class JSchExecutor<T extends Channel, R> implements CommandExecutor<R>
{
    private static final int CONNECT_TIMEOUT = 30_000;

    @Override
    public R execute(ServerConfiguration serverConfiguration, Commands commands) throws CommandExecutionException
    {
        try
        {
            JSch jSch = createJSchInstance(serverConfiguration);
            return execute(jSch, serverConfiguration, commands);
        }
        catch (JSchException | AgentProxyException e)
        {
            throw new CommandExecutionException(e);
        }
    }

    private R execute(JSch jSch, ServerConfiguration serverConfiguration, Commands commands)
            throws JSchException, CommandExecutionException
    {
        Session session = jSch.getSession(serverConfiguration.getUsername(), serverConfiguration.getHost(),
                serverConfiguration.getPort());
        try
        {
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,password");
            session.setPassword(serverConfiguration.getPassword());
            session.connect(CONNECT_TIMEOUT);
            @SuppressWarnings("unchecked")
            T channel = (T) session.openChannel(getChannelType());
            try
            {
                return executeCommand(serverConfiguration, commands, channel);
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

    protected abstract String getChannelType();

    protected abstract R executeCommand(ServerConfiguration serverConfig, Commands commands, T channel)
            throws JSchException, IOException;

    private JSch createJSchInstance(ServerConfiguration server) throws AgentProxyException, JSchException
    {
        JSch jSch = new JSch();
        if (server.isAgentForwarding())
        {
            Connector connector = ConnectorFactory.getDefault().createConnector();
            jSch.setIdentityRepository(new RemoteIdentityRepository(connector));
        }
        else if (server.getPrivateKey() != null && server.getPublicKey() != null)
        {
            String passphrase = server.getPassphrase();
            jSch.addIdentity("default", getBytes(server.getPrivateKey()), getBytes(server.getPublicKey()),
                    passphrase != null ? getBytes(passphrase) : null);
        }
        return jSch;
    }

    private byte[] getBytes(String str)
    {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
