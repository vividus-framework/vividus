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

package org.vividus.selenium.sauce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.saucerest.DataCenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.verification.VerificationMode;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

@ExtendWith(TestLoggerFactoryExtension.class)
class SauceConnectManagerTests
{
    private static final String OPTIONS = "options";
    private static final String USERNAME = "user%";
    private static final String ACCESS_KEY = "key";
    private static final DataCenter DATA_CENTER = DataCenter.EU_CENTRAL;

    private SauceTunnelManager sauceTunnelManager;
    private SauceConnectManager sauceConnectManager;

    private final TestContext context = new SimpleTestContext();

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SauceConnectManager.class);

    @BeforeEach
    void beforeEach()
    {
        sauceTunnelManager = mock(SauceTunnelManager.class);
        sauceConnectManager = new SauceConnectManager(USERNAME, ACCESS_KEY, DATA_CENTER, sauceTunnelManager, context);
    }

    @Test
    void testStart() throws IOException
    {
        var options = mock(SauceConnectOptions.class);
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        verify(sauceTunnelManager).openConnection(USERNAME, ACCESS_KEY, DATA_CENTER, null, OPTIONS, logger, System.out,
                Boolean.TRUE, null, true);
    }

    @Test
    void testStartTwice() throws IOException
    {
        var options = startConnection();
        var tunnelName = sauceConnectManager.start(options);
        verify(sauceTunnelManager, times(1)).openConnection(USERNAME, ACCESS_KEY, DATA_CENTER, null, OPTIONS, logger,
                System.out, Boolean.TRUE, null, true);
        assertEquals(tunnelName, sauceConnectManager.start(options));
    }

    @Test
    void testStartOneMoreConnectionWithingOneThreadIsNotAllowed() throws IOException
    {
        startConnection();
        var options2 = mock(SauceConnectOptions.class);
        var exception = assertThrows(IllegalArgumentException.class, () -> sauceConnectManager.start(options2));
        assertEquals("Only one SauceConnect tunnel is allowed within one thread", exception.getMessage());
    }

    @Test
    void testStop() throws IOException
    {
        startConnection();
        sauceConnectManager.stop();
        verifyStop(times(1));
    }

    @Test
    void testStopNotStarted()
    {
        sauceConnectManager.stop();
        verifyStop(never());
    }

    @Test
    void testStopTwice() throws IOException
    {
        startConnection();
        sauceConnectManager.stop();
        sauceConnectManager.stop();
        verifyStop(times(1));
    }

    @Test
    void testStartStopStart() throws IOException
    {
        var options = mock(SauceConnectOptions.class);
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        sauceConnectManager.stop();
        sauceConnectManager.start(options);
        verify(sauceTunnelManager, times(2)).openConnection(USERNAME, ACCESS_KEY, DATA_CENTER, null, OPTIONS, logger,
                System.out, Boolean.TRUE, null, true);
        verifyStop(times(1));
    }

    private SauceConnectOptions startConnection() throws IOException
    {
        var options = mock(SauceConnectOptions.class);
        when(options.build(anyString())).thenReturn(OPTIONS);
        sauceConnectManager.start(options);
        verify(sauceTunnelManager).openConnection(USERNAME, ACCESS_KEY, DATA_CENTER, null, OPTIONS, logger, System.out,
                Boolean.TRUE, null, true);
        return options;
    }

    private void verifyStop(VerificationMode mode)
    {
        verify(sauceTunnelManager, mode).closeTunnelsForPlan(USERNAME, OPTIONS, logger);
    }
}
