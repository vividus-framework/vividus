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
import com.saucelabs.saucerest.DataCenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private static final DataCenter DATA_CENTER = DataCenter.EU_CENTRAL;

    private SauceTunnelManager sauceTunnelManager;
    private SauceConnectManager sauceConnectManager;

    private final TestContext context = new SimpleTestContext();

    @Before
    public void before()
    {
        sauceTunnelManager = mock(SauceTunnelManager.class);
        sauceConnectManager = new SauceConnectManager(USERNAME, USERKEY, DATA_CENTER, sauceTunnelManager, context);
    }

    @Test
    @PrepareForTest(SauceConnectManager.class)
    public void testStart() throws Exception
    {
        mockSocket();
        var options = mock(SauceConnectOptions.class);
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        verify(sauceTunnelManager).openConnection(USERNAME, USERKEY, DATA_CENTER, 1, null, OPTIONS, null, Boolean.TRUE,
                null);
    }

    @Test
    @PrepareForTest(SauceConnectManager.class)
    public void testStartWhenErrorAtPortAllocation() throws Exception
    {
        var options = mock(SauceConnectOptions.class);
        var ioException = new IOException();
        whenNew(ServerSocket.class).withArguments(0).thenThrow(ioException);
        var exception = assertThrows(IllegalStateException.class, () -> sauceConnectManager.start(options));
        assertEquals(ioException, exception.getCause());
    }

    @Test
    @PrepareForTest(SauceConnectManager.class)
    public void testStartTwice() throws Exception
    {
        mockSocket();
        var options = startConnection();
        var tunnelName = sauceConnectManager.start(options);
        verify(sauceTunnelManager, times(1)).openConnection(USERNAME, USERKEY, DATA_CENTER, 1, null, OPTIONS, null,
                Boolean.TRUE, null);
        assertEquals(tunnelName, sauceConnectManager.start(options));
    }

    @Test
    @PrepareForTest(SauceConnectManager.class)
    public void testStartOneMoreConnectionWithingOneThreadIsNotAllowed() throws Exception
    {
        mockSocket();
        startConnection();
        var options2 = mock(SauceConnectOptions.class);
        var exception = assertThrows(IllegalArgumentException.class, () -> sauceConnectManager.start(options2));
        assertEquals("Only one SauceConnect tunnel is allowed within one thread", exception.getMessage());
    }

    @Test
    @PrepareForTest(SauceConnectManager.class)
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
    @PrepareForTest(SauceConnectManager.class)
    public void testStopTwice() throws Exception
    {
        mockSocket();
        startConnection();
        sauceConnectManager.stop();
        sauceConnectManager.stop();
        verifyStop(times(1));
    }

    @Test
    @PrepareForTest(SauceConnectManager.class)
    public void testStartStopStart() throws Exception
    {
        mockSocket();
        var options = mock(SauceConnectOptions.class);
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        sauceConnectManager.stop();
        sauceConnectManager.start(options);
        verify(sauceTunnelManager, times(2)).openConnection(USERNAME, USERKEY, DATA_CENTER, 1, null, OPTIONS, null,
                Boolean.TRUE, null);
        verifyStop(times(1));
    }

    private SauceConnectOptions startConnection() throws IOException
    {
        var options = mock(SauceConnectOptions.class);
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        verify(sauceTunnelManager).openConnection(USERNAME, USERKEY, DATA_CENTER, 1, null, OPTIONS, null, Boolean.TRUE,
                null);
        return options;
    }

    private void verifyStop(VerificationMode mode)
    {
        verify(sauceTunnelManager, mode).closeTunnelsForPlan(USERNAME, OPTIONS, null);
    }

    private void mockSocket() throws Exception
    {
        var socket = mock(ServerSocket.class);
        whenNew(ServerSocket.class).withArguments(0).thenReturn(socket);
        when(socket.getLocalPort()).thenReturn(1);
    }
}
