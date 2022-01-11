/*
 * Copyright 2019-2021 the original author or authors.
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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver.Timeouts;

class TimeoutConfigurerTests
{
    private static final int PAGE_LOAD_TIMEOUT = 1;
    private static final TimeUnit PAGE_LOAD_TIMEOUT_TIMEUNIT = TimeUnit.MINUTES;

    private static final int ASYNC_SCRIPT_TIMEOUT = 30;
    private static final TimeUnit ASYNC_SCRIPT_TIMEOUT_TIMEUNIT = TimeUnit.SECONDS;

    private static final Duration DURATION_PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30L);

    private TimeoutConfigurer timeoutConfigurer;

    @BeforeEach
    void beforeEach()
    {
        timeoutConfigurer = new TimeoutConfigurer();
        timeoutConfigurer.setPageLoadTimeout(PAGE_LOAD_TIMEOUT);
        timeoutConfigurer.setPageLoadTimeoutTimeUnit(PAGE_LOAD_TIMEOUT_TIMEUNIT);
        timeoutConfigurer.setAsyncScriptTimeout(ASYNC_SCRIPT_TIMEOUT);
        timeoutConfigurer.setAsyncScriptTimeoutTimeUnit(ASYNC_SCRIPT_TIMEOUT_TIMEUNIT);
    }

    @Test
    void testSetTimeouts()
    {
        Timeouts timeouts = mock(Timeouts.class);
        timeoutConfigurer.configure(timeouts);
        verify(timeouts).pageLoadTimeout(PAGE_LOAD_TIMEOUT, PAGE_LOAD_TIMEOUT_TIMEUNIT);
        verify(timeouts).setScriptTimeout(ASYNC_SCRIPT_TIMEOUT, ASYNC_SCRIPT_TIMEOUT_TIMEUNIT);
    }

    @Test
    void testSetPageLoadTimeout()
    {
        Timeouts timeouts = mock(Timeouts.class);
        timeoutConfigurer.configurePageLoadTimeout(DURATION_PAGE_LOAD_TIMEOUT, timeouts);
        verify(timeouts).pageLoadTimeout(DURATION_PAGE_LOAD_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    }

    @Test
    void testSetTimeoutsWithExceptionAtPageLoadTimeoutSetting()
    {
        Timeouts timeouts = mock(Timeouts.class);
        UnsupportedCommandException exception = new UnsupportedCommandException("pagetimeout");
        when(timeouts.pageLoadTimeout(PAGE_LOAD_TIMEOUT, PAGE_LOAD_TIMEOUT_TIMEUNIT)).thenThrow(exception);
        timeoutConfigurer.configure(timeouts);
        verify(timeouts).setScriptTimeout(ASYNC_SCRIPT_TIMEOUT, ASYNC_SCRIPT_TIMEOUT_TIMEUNIT);
    }

    @Test
    void testSetTimeoutsWithExceptionAtScriptTimeoutSetting()
    {
        Timeouts timeouts = mock(Timeouts.class);
        UnsupportedCommandException exception = new UnsupportedCommandException("asynctimeout");
        when(timeouts.setScriptTimeout(ASYNC_SCRIPT_TIMEOUT, ASYNC_SCRIPT_TIMEOUT_TIMEUNIT)).thenThrow(exception);
        timeoutConfigurer.configure(timeouts);
        verify(timeouts).pageLoadTimeout(PAGE_LOAD_TIMEOUT, PAGE_LOAD_TIMEOUT_TIMEUNIT);
    }
}
