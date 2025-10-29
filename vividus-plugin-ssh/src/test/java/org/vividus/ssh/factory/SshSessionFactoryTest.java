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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ssh.SshSessionParameters;

@ExtendWith(MockitoExtension.class)
class SshSessionFactoryTest
{
    private static final String ERROR = "Please use either private-key/public-key or private-key-file/public-key-file"
            + " parameters";

    @InjectMocks
    private SshSessionFactory sshSessionFactory;

    @Test
    void shouldCreateSshSessionWithPassword() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = new SshSessionParameters();
        params.setUsername("testuser");
        params.setHost("localhost");
        params.setPort(22);
        params.setPassword("password123");

        Session session = sshSessionFactory.createSshSession(params);

        assertNotNull(session);
        assertEquals("testuser", session.getUserName());
        assertEquals("localhost", session.getHost());
        assertEquals(22, session.getPort());
    }

    @Test
    void shouldCreateSshSessionWithAgentForwarding() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = new SshSessionParameters();
        params.setUsername("testuser");
        params.setHost("localhost");
        params.setPort(22);
        params.setAgentForwarding(true);

        Session session = sshSessionFactory.createSshSession(params);

        assertNotNull(session);
    }

    @Test
    void shouldCreateSshSessionWithPrivateKeyFiles() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = new SshSessionParameters();
        params.setUsername("testuser");
        params.setHost("localhost");
        params.setPort(22);
        params.setPrivateKeyFile("/path/to/private/key");
        params.setPublicKeyFile("/path/to/public/key");
        params.setPassword("passphrase");

        assertThrows(JSchException.class, () -> sshSessionFactory.createSshSession(params));
    }

    @Test
    void shouldThrowExceptionWhenBothKeyFilesAndKeyStringsProvided()
    {
        SshSessionParameters params = new SshSessionParameters();
        params.setUsername("testuser");
        params.setHost("localhost");
        params.setPort(22);
        params.setPrivateKeyFile("/path/to/private/key");
        params.setPublicKeyFile("/path/to/public/key");
        params.setPrivateKey("private-key-string");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> sshSessionFactory.createSshSession(params));
        assertEquals(ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMixingKeyFilesAndKeyStrings()
    {
        SshSessionParameters params = new SshSessionParameters();
        params.setUsername("testuser");
        params.setHost("localhost");
        params.setPort(22);
        params.setPrivateKey("private-key-string");
        params.setPublicKey("public-key-string");
        params.setPrivateKeyFile("/path/to/private/key");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> sshSessionFactory.createSshSession(params));
        assertEquals(ERROR, exception.getMessage());
    }
}
