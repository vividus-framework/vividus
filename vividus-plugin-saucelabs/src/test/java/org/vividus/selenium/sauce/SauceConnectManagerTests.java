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

package org.vividus.selenium.sauce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.net.ServerSocket;

import com.saucelabs.ci.sauceconnect.SauceTunnelManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

@RunWith(PowerMockRunner.class)
public class SauceConnectManagerTests
{
    private static final String OPTIONS = "options";
    private static final String USERNAME = "user%";
    private static final String USERKEY = "key";

    @Mock
    private SauceTunnelManager sauceTunnelManager;

    @Mock
    private SauceConnectOptions options;

    @InjectMocks
    private SauceConnectManager sauceConnectManager;

    private final TestContext context = new SimpleTestContext();

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        sauceConnectManager.setSauceLabsUsername(USERNAME);
        sauceConnectManager.setSauceLabsAccessKey(USERKEY);
        sauceConnectManager.setTestContext(context);
    }

    @Test
    @PrepareForTest({ SauceConnectManager.class, ISauceConnectManager.class })
    public void testStart() throws Exception
    {
        mockSocket();
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        verify(sauceTunnelManager).openConnection(USERNAME, USERKEY, 1, null, OPTIONS, null, Boolean.TRUE, null);
    }

    @Test
    @PrepareForTest({ SauceConnectManager.class, ISauceConnectManager.class })
    public void testStartWhenErrorAtPortAllocation() throws Exception
    {
        IOException ioException = new IOException();
        whenNew(ServerSocket.class).withArguments(0).thenThrow(ioException);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> sauceConnectManager.start(options));
        assertEquals(ioException, exception.getCause());
    }

    @Test
    @PrepareForTest({ SauceConnectManager.class, ISauceConnectManager.class })
    public void testStartTwice() throws Exception
    {
        mockSocket();
        startConnection();
        String tunnelId = sauceConnectManager.start(options);
        verify(sauceTunnelManager, times(1)).openConnection(USERNAME, USERKEY, 1, null, OPTIONS, null, Boolean.TRUE,
                null);
        assertEquals(tunnelId, sauceConnectManager.start(options));
    }

    @Test
    @PrepareForTest({ SauceConnectManager.class, ISauceConnectManager.class })
    public void testStartOneMoreConnectionWithingOneThreadIsNotAllowed() throws Exception
    {
        mockSocket();
        startConnection();
        SauceConnectOptions options2 = mock(SauceConnectOptions.class);
        when(options2.build(anyString())).thenReturn(OPTIONS);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sauceConnectManager.start(options2));
        assertEquals("Only one SauceConnect tunnel is allowed within one thread", exception.getMessage());
    }

    @Test
    @PrepareForTest({ SauceConnectManager.class, ISauceConnectManager.class })
    public void testStop() throws Exception
    {
        mockSocket();
        startConnection();
        sauceConnectManager.stop();
        verifyStop(times(1));
    }

    @Test
    public void testStopNotStarted()
    {
        sauceConnectManager.stop();
        verifyStop(never());
    }

    @Test
    @PrepareForTest({ SauceConnectManager.class, ISauceConnectManager.class })
    public void testStopTwice() throws Exception
    {
        mockSocket();
        startConnection();
        sauceConnectManager.stop();
        sauceConnectManager.stop();
        verifyStop(times(1));
    }

    @Test
    @PrepareForTest({ SauceConnectManager.class, ISauceConnectManager.class })
    public void testStartStopStart() throws Exception
    {
        mockSocket();
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        sauceConnectManager.stop();
        sauceConnectManager.start(options);
        verify(sauceTunnelManager, times(2)).openConnection(USERNAME, USERKEY, 1, null, OPTIONS, null, Boolean.TRUE,
                null);
        verifyStop(times(1));
    }

    private void startConnection() throws IOException
    {
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        verify(sauceTunnelManager).openConnection(USERNAME, USERKEY, 1, null, OPTIONS, null, Boolean.TRUE, null);
    }

    private void verifyStop(VerificationMode mode)
    {
        verify(sauceTunnelManager, mode).closeTunnelsForPlan(USERNAME, OPTIONS, null);
    }

    private void mockSocket() throws Exception
    {
        ServerSocket socket = mock(ServerSocket.class);
        whenNew(ServerSocket.class).withArguments(0).thenReturn(socket);
        when(socket.getLocalPort()).thenReturn(1);
    }
}
