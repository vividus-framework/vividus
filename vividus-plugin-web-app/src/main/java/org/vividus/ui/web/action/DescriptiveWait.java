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
import java.util.function.Function;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

public class DescriptiveWait<F> implements Wait<F>
{
    private final FluentWait<F> wait;
    private Duration timeout;
    private Function<? super F, ?> isTrue;

    public DescriptiveWait(FluentWait<F> wait)
    {
        this.wait = wait;
    }

    public void setTimeout(Duration timeout)
    {
        this.timeout = timeout;
        this.wait.withTimeout(timeout);
    }

    @Override
    public <T> T until(Function<? super F, T> isTrue)
    {
        this.isTrue = isTrue;
        return wait.until(isTrue);
    }

    @Override
    public String toString()
    {
        return "Wait with timeout of " + DurationFormatUtils.formatDurationWords(timeout.toMillis(), true, true)
                + ". Condition: " + isTrue;
    }

    public Duration getTimeout()
    {
        return timeout;
    }
}
