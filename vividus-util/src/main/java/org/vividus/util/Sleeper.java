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

package org.vividus.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class Sleeper
{
    private Sleeper()
    {
    }

    public static void sleep(Duration duration)
    {
        sleep(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static void sleep(long time, TimeUnit timeUnit)
    {
        try
        {
            Thread.sleep(TimeUnit.MILLISECONDS.convert(time, timeUnit));
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
