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

package org.vividus.ssh.factory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SSHAgentConnector;
import com.jcraft.jsch.Session;

import org.vividus.ssh.SshSessionParameters;

public class SshSessionFactory
{
    public Session createSshSession(SshSessionParameters sshSessionParameters)
            throws JSchException, AgentProxyException
    {
        JSch jSch = createJSchInstance(sshSessionParameters);

        Session session = jSch.getSession(sshSessionParameters.getUsername(), sshSessionParameters.getHost(),
                sshSessionParameters.getPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey,password");
        session.setPassword(sshSessionParameters.getPassword());

        return session;
    }

    private JSch createJSchInstance(SshSessionParameters sshSessionParameters)
            throws AgentProxyException, JSchException
    {
        JSch jSch = new JSch();
        if (sshSessionParameters.isAgentForwarding())
        {
            IdentityRepository identityRepository = new AgentIdentityRepository(new SSHAgentConnector());
            jSch.setIdentityRepository(identityRepository);
        }
        else
        {
            boolean hasKeyFiles = sshSessionParameters.getPrivateKeyFile() != null
                    && sshSessionParameters.getPublicKeyFile() != null;
            boolean hasKeyStrings = sshSessionParameters.getPrivateKey() != null
                    && sshSessionParameters.getPublicKey() != null;

            if ((hasKeyFiles
                    && (sshSessionParameters.getPrivateKey() != null || sshSessionParameters.getPublicKey() != null))
                    || (hasKeyStrings && (sshSessionParameters.getPrivateKeyFile() != null
                            || sshSessionParameters.getPublicKeyFile() != null)))
            {
                throw new IllegalArgumentException(
                        "Please use either private-key/public-key or private-key-file/public-key-file parameters");
            }

            byte[] passphrase = Optional.ofNullable(sshSessionParameters.getPassword()).map(this::getBytes)
                    .orElse(null);
            if (hasKeyFiles)
            {
                jSch.addIdentity(
                    sshSessionParameters.getPrivateKeyFile(),
                    sshSessionParameters.getPublicKeyFile(),
                    passphrase
                );
            }
            else if (hasKeyStrings)
            {
                jSch.addIdentity(
                    "default",
                    getBytes(sshSessionParameters.getPrivateKey()),
                    getBytes(sshSessionParameters.getPublicKey()),
                    passphrase
                );
            }
        }
        return jSch;
    }

    private byte[] getBytes(String str)
    {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
