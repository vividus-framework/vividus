/*
 * Copyright 2019 the original author or authors.
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

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.vividus.util.CheckedSupplier;
import org.vividus.util.Sleeper;

public final class Waiter
{
    private Waiter()
    {
    }

    public static <T, E extends Exception> T wait(WaitMode waitMode,
            CheckedSupplier<T, E> valueProvider, Predicate<T> repeatCondition) throws E
    {
        long durationInMillis = waitMode.getDuration().toMillis();
        long expectedTime = System.currentTimeMillis() + durationInMillis;
        long pollingTimeoutMillis = durationInMillis / waitMode.getRetryTimes();
        T value;
        do
        {
            value = valueProvider.get();
            Sleeper.sleep(pollingTimeoutMillis, TimeUnit.MILLISECONDS);
        }
        while (repeatCondition.test(value) && System.currentTimeMillis() <= expectedTime);
        return value;
    }
}
