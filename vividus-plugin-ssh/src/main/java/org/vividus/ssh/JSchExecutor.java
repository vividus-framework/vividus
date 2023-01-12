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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SSHAgentConnector;
import com.jcraft.jsch.Session;

public abstract class JSchExecutor<T extends Channel, R> implements CommandExecutor<R>
{
    private static final int CONNECT_TIMEOUT = 30_000;

    @Override
    public R execute(SshConnectionParameters sshConnectionParameters, Commands commands)
            throws CommandExecutionException
    {
        try
        {
            JSch jSch = createJSchInstance(sshConnectionParameters);
            return execute(jSch, sshConnectionParameters, commands);
        }
        catch (JSchException | AgentProxyException e)
        {
            throw new CommandExecutionException(e);
        }
    }

    private R execute(JSch jSch, SshConnectionParameters sshConnectionParameters, Commands commands)
            throws JSchException, CommandExecutionException
    {
        Session session = jSch.getSession(sshConnectionParameters.getUsername(),
                sshConnectionParameters.getHost(), sshConnectionParameters.getPort());
        try
        {
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,password");
            session.setPassword(sshConnectionParameters.getPassword());
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

    private JSch createJSchInstance(SshConnectionParameters sshConnectionParameters)
            throws AgentProxyException, JSchException
    {
        JSch jSch = new JSch();
        if (sshConnectionParameters.isAgentForwarding())
        {
            IdentityRepository identityRepository = new AgentIdentityRepository(new SSHAgentConnector());
            jSch.setIdentityRepository(identityRepository);
        }
        else if (sshConnectionParameters.getPrivateKey() != null && sshConnectionParameters.getPublicKey() != null)
        {
            String passphrase = sshConnectionParameters.getPassphrase();
            jSch.addIdentity("default", getBytes(sshConnectionParameters.getPrivateKey()),
                    getBytes(sshConnectionParameters.getPublicKey()), passphrase != null ? getBytes(passphrase) : null);
        }
        return jSch;
    }

    private byte[] getBytes(String str)
    {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
