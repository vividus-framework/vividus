/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class WebDriverProviderTests
{
    private static final String SESSION_ID = "sessionId";

    private final TestContext testContext = new SimpleTestContext();
    @Mock private VividusWebDriverFactory vividusDriverFactory;
    @Mock private EventBus mockedEventBus;
    @InjectMocks private final WebDriverProvider webDriverProvider = new WebDriverProvider(testContext);

    @Mock(extraInterfaces = WrapsDriver.class)
    private RemoteWebDriver remoteWebDriver;

    @Test
    void testEnd()
    {
        SessionId sessionId = mock();
        testContext.put(WebDriver.class, remoteWebDriver);
        when(remoteWebDriver.getSessionId()).thenReturn(sessionId);
        when(sessionId.toString()).thenReturn(SESSION_ID);
        webDriverProvider.end();
        verify(remoteWebDriver).quit();
        verifyEventsPosting();
    }

    @Test
    void testEndWebDriveException()
    {
        SessionId sessionId = mock();
        testContext.put(WebDriver.class, remoteWebDriver);
        when(remoteWebDriver.getSessionId()).thenReturn(sessionId);
        when(sessionId.toString()).thenReturn(SESSION_ID);
        doThrow(new WebDriverException()).when(remoteWebDriver).quit();
        assertThrows(WebDriverException.class, webDriverProvider :: end);
        verifyEventsPosting();
    }

    @Test
    void testEndNoWebDriver()
    {
        webDriverProvider.end();
        verifyNoInteractions(mockedEventBus);
    }

    @Test
    void testDestroy()
    {
        Mockito.doNothing().when(mockedEventBus).post(any(WebDriverCreateEvent.class));
        when(vividusDriverFactory.createWebDriver()).thenReturn(remoteWebDriver);
        webDriverProvider.get();
        webDriverProvider.destroy();
        verify(remoteWebDriver).quit();
    }

    @Test
    void testGetUnwrapped()
    {
        testContext.put(WebDriver.class, remoteWebDriver);
        assertThat(webDriverProvider.getUnwrapped(WrapsDriver.class), instanceOf(WrapsDriver.class));
    }

    private void verifyEventsPosting()
    {
        var ordered = inOrder(mockedEventBus);
        var beforeWebDriverQuitEventArgumentCaptor = ArgumentCaptor.forClass(BeforeWebDriverQuitEvent.class);
        var afterWebDriverQuitEventArgumentCaptor = ArgumentCaptor.forClass(AfterWebDriverQuitEvent.class);
        ordered.verify(mockedEventBus).post(beforeWebDriverQuitEventArgumentCaptor.capture());
        ordered.verify(mockedEventBus).post(afterWebDriverQuitEventArgumentCaptor.capture());
        ordered.verifyNoMoreInteractions();
        assertEquals(SESSION_ID, beforeWebDriverQuitEventArgumentCaptor.getValue().sessionId());
        assertEquals(SESSION_ID, afterWebDriverQuitEventArgumentCaptor.getValue().sessionId());
    }
}
