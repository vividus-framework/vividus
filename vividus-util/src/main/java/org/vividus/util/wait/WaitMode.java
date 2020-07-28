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

package org.vividus.util.wait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WaitMode
{
    private final Duration duration;
    private final int retryTimes;

    public WaitMode(Duration duration, int retryTimes)
    {
        this.duration = duration;
        this.retryTimes = retryTimes;
    }

    public Duration getDuration()
    {
        return duration;
    }

    public int getRetryTimes()
    {
        return retryTimes;
    }

    public long calculatePollingTimeout(TimeUnit timeUnit)
    {
        return timeUnit.convert(duration) / retryTimes;
    }
}
