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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SSHAgentConnector;
import com.jcraft.jsch.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ssh.SshSessionParameters;

@ExtendWith(MockitoExtension.class)
class SshSessionFactoryTests
{
    private static final String PASSWORD = "password123";
    private static final String PASSPHRASE = "passphrase";
    private static final String PRIVATE_KEY_FILE = "/path/to/private/key";
    private static final String PUBLIC_KEY_FILE = "/path/to/public/key";
    private static final String PRIVATE_KEY_STRING = "private-key-string";
    private static final String PUBLIC_KEY_STRING = "public-key-string";
    private static final String ERROR_MESSAGE = "Please use either private-key/public-key or "
            + "private-key-file/public-key-file parameters";

    @InjectMocks
    private SshSessionFactory sshSessionFactory;

    @Test
    void shouldCreateSshSession() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = createBaseParameters();

        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(params.getUsername(), params.getHost(), params.getPort()))
                        .thenReturn(session)))
        {
            Session createdSession = sshSessionFactory.createSshSession(params);
            verifySessionCommon(createdSession);
        }
    }

    @Test
    void shouldCreateSshSessionAgentForwarding() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = createBaseParameters();
        params.setAgentForwarding(true);

        var session = mock(Session.class);
        try (
                var connector = mockConstruction(SSHAgentConnector.class);
                var jSchMock = mockConstruction(JSch.class, (mock, context) -> when(
                        mock.getSession(params.getUsername(), params.getHost(), params.getPort())).thenReturn(session)
                );
                var remoteIdentityRepositoryMock = mockConstruction(
                        AgentIdentityRepository.class, (mock, context) -> {
                            assertEquals(1, context.getCount());
                            assertEquals(List.of(connector.constructed().get(0)), context.arguments());
                        })
        )
        {
            Session createdSession = sshSessionFactory.createSshSession(params);
            var jSch = jSchMock.constructed().get(0);
            verify(jSch).setIdentityRepository(remoteIdentityRepositoryMock.constructed().get(0));
            verifySessionCommon(createdSession);
        }
    }

    @Test
    void shouldCreateSshSessionKeys() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = createBaseParameters();
        params.setPrivateKey(PRIVATE_KEY_STRING);
        params.setPublicKey(PUBLIC_KEY_STRING);
        params.setPassphrase(PASSPHRASE);

        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(params.getUsername(), params.getHost(), params.getPort()))
                        .thenReturn(session)))
        {
            Session createdSession = sshSessionFactory.createSshSession(params);
            var jSch = jSchMock.constructed().get(0);
            verify(jSch).addIdentity("default", params.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    params.getPublicKey().getBytes(StandardCharsets.UTF_8),
                    params.getPassphrase().getBytes(StandardCharsets.UTF_8));
            verifySessionCommon(createdSession);
        }
    }

    @Test
    void shouldCreateSshSessionUsingPrivateKeyFile() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = createBaseParameters();
        params.setPrivateKeyFile(PRIVATE_KEY_FILE);
        params.setPassphrase(PASSPHRASE);

        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(params.getUsername(), params.getHost(), params.getPort()))
                        .thenReturn(session)))
        {
            Session createdSession = sshSessionFactory.createSshSession(params);
            var jSch = jSchMock.constructed().get(0);
            verify(jSch).addIdentity(PRIVATE_KEY_FILE, PASSPHRASE);
            verifySessionCommon(createdSession);
        }
    }

    @Test
    void shouldCreateSshSessionKeyFiles() throws JSchException, AgentProxyException
    {
        SshSessionParameters params = createBaseParameters();
        params.setPrivateKeyFile(PRIVATE_KEY_FILE);
        params.setPublicKeyFile(PUBLIC_KEY_FILE);
        params.setPassphrase(null);

        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(params.getUsername(), params.getHost(), params.getPort()))
                        .thenReturn(session)))
        {
            Session createdSession = sshSessionFactory.createSshSession(params);
            var jSch = jSchMock.constructed().get(0);
            verify(jSch).addIdentity(params.getPrivateKeyFile(), params.getPublicKeyFile(), null);
            verifySessionCommon(createdSession);
        }
    }

    static Stream<Arguments> args()
    {
        return Stream.of(
                Arguments.arguments(PRIVATE_KEY_STRING, PUBLIC_KEY_STRING, null, PUBLIC_KEY_FILE, ERROR_MESSAGE),
                Arguments.arguments(PRIVATE_KEY_STRING, PUBLIC_KEY_STRING, PRIVATE_KEY_FILE, null, ERROR_MESSAGE),
                Arguments.arguments(null, PUBLIC_KEY_STRING, PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, ERROR_MESSAGE),
                Arguments.arguments(PRIVATE_KEY_STRING, null, PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, ERROR_MESSAGE),
                Arguments.arguments(null, PUBLIC_KEY_STRING, null, null, "The private-key must accompany public-key"),
                Arguments.arguments(null, null, null, PUBLIC_KEY_FILE,
                        "The private-key-file must accompany public-key-file")
        );
    }

    @ParameterizedTest
    @MethodSource("args")
    void shouldCreateSshSessionInvalidKeys(String prvKey, String pubKey, String prvKeyFile, String pubKeyFile,
                                           String message)
    {
        SshSessionParameters params = createBaseParameters();
        params.setPrivateKeyFile(prvKeyFile);
        params.setPublicKeyFile(pubKeyFile);
        params.setPrivateKey(prvKey);
        params.setPublicKey(pubKey);

        var session = mock(Session.class);
        try (var jSchMock = mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(params.getUsername(), params.getHost(), params.getPort()))
                        .thenReturn(session)))
        {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> sshSessionFactory.createSshSession(params));
            assertEquals(message, exception.getMessage());
        }
    }

    private void verifySessionCommon(Session session)
    {
        verify(session).setConfig("StrictHostKeyChecking", "no");
        verify(session).setConfig("PreferredAuthentications", "publickey,password");
        verify(session).setPassword(PASSWORD);
    }

    private SshSessionParameters createBaseParameters()
    {
        SshSessionParameters params = new SshSessionParameters();
        params.setUsername("testuser");
        params.setHost("localhost");
        params.setPort(22);
        params.setPassword(PASSWORD);
        return params;
    }
}
