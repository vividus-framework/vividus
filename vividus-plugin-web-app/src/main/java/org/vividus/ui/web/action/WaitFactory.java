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

package org.vividus.ui.web.action;

import java.time.Duration;

import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

public class WaitFactory implements IWaitFactory
{
    private Duration timeout;
    private Duration pollingPeriod;

    @Override
    public <T> Wait<T> createWait(T input)
    {
        return createWait(input, timeout);
    }

    @Override
    public <T> Wait<T> createWait(T input, Duration timeout)
    {
        return createWait(input, timeout, pollingPeriod);
    }

    @Override
    public <T> Wait<T> createWait(T input, Duration timeout, Duration pollingPeriod)
    {
        FluentWait<T> fluentWait = new FluentWait<>(input).pollingEvery(pollingPeriod);
        DescriptiveWait<T> wait = new DescriptiveWait<>(fluentWait);
        wait.setTimeout(timeout);
        return wait;
    }

    public void setTimeout(Duration timeout)
    {
        this.timeout = timeout;
    }

    public void setPollingPeriod(Duration pollingPeriod)
    {
        this.pollingPeriod = pollingPeriod;
    }
}
