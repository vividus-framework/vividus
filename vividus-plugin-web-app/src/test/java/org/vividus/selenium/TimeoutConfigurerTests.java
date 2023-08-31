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

package org.vividus.selenium;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver.Timeouts;

class TimeoutConfigurerTests
{
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofMinutes(1);
    private static final Duration ASYNC_SCRIPT_TIMEOUT = Duration.ofSeconds(30);

    private TimeoutConfigurer timeoutConfigurer;

    @BeforeEach
    void beforeEach()
    {
        timeoutConfigurer = new TimeoutConfigurer();
        timeoutConfigurer.setPageLoadTimeout(PAGE_LOAD_TIMEOUT);
        timeoutConfigurer.setAsyncScriptTimeout(ASYNC_SCRIPT_TIMEOUT);
    }

    @Test
    void testSetTimeouts()
    {
        Timeouts timeouts = mock(Timeouts.class);
        timeoutConfigurer.configure(timeouts);
        verify(timeouts).pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        verify(timeouts).scriptTimeout(ASYNC_SCRIPT_TIMEOUT);
    }

    @Test
    void testSetPageLoadTimeout()
    {
        Duration customPageLoadTimeout = Duration.ofSeconds(30L);
        Timeouts timeouts = mock(Timeouts.class);
        timeoutConfigurer.configurePageLoadTimeout(customPageLoadTimeout, timeouts);
        verify(timeouts).pageLoadTimeout(customPageLoadTimeout);
    }

    @Test
    void testSetTimeoutsWithExceptionAtPageLoadTimeoutSetting()
    {
        Timeouts timeouts = mock(Timeouts.class);
        UnsupportedCommandException exception = new UnsupportedCommandException("pagetimeout");
        when(timeouts.pageLoadTimeout(PAGE_LOAD_TIMEOUT)).thenThrow(exception);
        timeoutConfigurer.configure(timeouts);
        verify(timeouts).scriptTimeout(ASYNC_SCRIPT_TIMEOUT);
    }

    @Test
    void testSetTimeoutsWithExceptionAtScriptTimeoutSetting()
    {
        Timeouts timeouts = mock(Timeouts.class);
        UnsupportedCommandException exception = new UnsupportedCommandException("asynctimeout");
        when(timeouts.scriptTimeout(ASYNC_SCRIPT_TIMEOUT)).thenThrow(exception);
        timeoutConfigurer.configure(timeouts);
        verify(timeouts).pageLoadTimeout(PAGE_LOAD_TIMEOUT);
    }
}
