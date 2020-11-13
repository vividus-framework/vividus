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

package org.vividus.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.selenium.event.WebDriverQuitEvent;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class WebDriverProviderTests
{
    private static final String SESSION_ID = "sessionId";

    @Spy
    private final TestContext testContext = new SimpleTestContext();

    @Mock
    private TestVividusDriverFactory vividusDriverFactory;

    @Mock(extraInterfaces = WrapsDriver.class)
    private RemoteWebDriver remoteWebDriver;

    @Mock
    private VividusWebDriver vividusWebDriver;

    @Mock
    private EventBus mockedEventBus;

    @InjectMocks
    private WebDriverProvider webDriverProvider;

    @Test
    void testEnd()
    {
        SessionId sessionId = mock(SessionId.class);
        testContext.put(VividusWebDriver.class, vividusWebDriver);
        when(vividusWebDriver.getWrappedDriver()).thenReturn(remoteWebDriver);
        when(remoteWebDriver.getSessionId()).thenReturn(sessionId);
        when(sessionId.toString()).thenReturn(SESSION_ID);
        webDriverProvider.end();
        verify(remoteWebDriver).quit();
        verify(mockedEventBus).post(argThat(e -> SESSION_ID.equals(((WebDriverQuitEvent) e).getSessionId())));
    }

    @Test
    void testEndWebDriveException()
    {
        SessionId sessionId = mock(SessionId.class);
        testContext.put(VividusWebDriver.class, vividusWebDriver);
        when(vividusWebDriver.getWrappedDriver()).thenReturn(remoteWebDriver);
        when(remoteWebDriver.getSessionId()).thenReturn(sessionId);
        when(sessionId.toString()).thenReturn(SESSION_ID);
        doThrow(new WebDriverException()).when(remoteWebDriver).quit();
        assertThrows(WebDriverException.class, webDriverProvider :: end);
        verify(mockedEventBus).post(argThat(e -> SESSION_ID.equals(((WebDriverQuitEvent) e).getSessionId())));
    }

    @Test
    void testEndNoWebDriver()
    {
        webDriverProvider.end();
        verify(mockedEventBus).post(argThat(e -> ((WebDriverQuitEvent) e).getSessionId() == null));
    }

    @Test
    void testDestroy()
    {
        Mockito.doNothing().when(mockedEventBus).post(any(WebDriverCreateEvent.class));
        when(vividusDriverFactory.create()).thenReturn(vividusWebDriver);
        when(vividusWebDriver.getWrappedDriver()).thenReturn(remoteWebDriver);
        webDriverProvider.get();
        webDriverProvider.destroy();
        verify(remoteWebDriver).quit();
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, false",
        "true, false, false",
        "true, true, true"
    })
    void testIsRemoteExecution(boolean initialized, boolean remote, boolean expected)
    {
        WebDriverProvider spy = spy(webDriverProvider);
        testContext.put(VividusWebDriver.class, vividusWebDriver);

        when(spy.isWebDriverInitialized()).thenReturn(initialized);
        lenient().when(vividusWebDriver.isRemote()).thenReturn(remote);

        assertEquals(expected, spy.isRemoteExecution());
    }

    @Test
    void testGetUnwrapped()
    {
        testContext.put(VividusWebDriver.class, vividusWebDriver);
        when(vividusWebDriver.getWrappedDriver()).thenReturn(remoteWebDriver);
        assertThat(webDriverProvider.getUnwrapped(WrapsDriver.class), instanceOf(WrapsDriver.class));
    }

    private static class TestVividusDriverFactory extends AbstractVividusWebDriverFactory
    {
        TestVividusDriverFactory(IBddRunContext bddRunContext, IWebDriverManagerContext webDriverManagerContext)
        {
            super(true, webDriverManagerContext, bddRunContext, Optional.empty());
        }

        @Override
        protected WebDriver createWebDriver(DesiredCapabilities desiredCapabilities)
        {
            throw new NotImplementedException();
        }
    }
}
