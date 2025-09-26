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

package org.vividus.selenium;

import java.time.Duration;
import java.util.function.Consumer;

import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutConfigurer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutConfigurer.class);

    private Duration pageLoadTimeout;
    private Duration asyncScriptTimeout;

    public void configure(Timeouts timeouts)
    {
        setTimeout(timeouts, t -> t.pageLoadTimeout(pageLoadTimeout));
        setTimeout(timeouts, t -> t.scriptTimeout(asyncScriptTimeout));
    }

    public void configurePageLoadTimeout(Duration duration, Timeouts timeouts)
    {
        setTimeout(timeouts, t -> t.pageLoadTimeout(duration));
    }

    private static void setTimeout(Timeouts timeouts, Consumer<Timeouts> consumer)
    {
        try
        {
            consumer.accept(timeouts);
        }
        catch (WebDriverException e)
        {
            LOGGER.atWarn().addArgument(e::getMessage).log("{}");
        }
    }

    public void setPageLoadTimeout(Duration pageLoadTimeout)
    {
        this.pageLoadTimeout = pageLoadTimeout;
    }

    public void setAsyncScriptTimeout(Duration asyncScriptTimeout)
    {
        this.asyncScriptTimeout = asyncScriptTimeout;
    }
}
