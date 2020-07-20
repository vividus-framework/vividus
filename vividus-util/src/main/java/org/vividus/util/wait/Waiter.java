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

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.vividus.util.Sleeper;
import org.vividus.util.function.CheckedRunnable;
import org.vividus.util.function.CheckedSupplier;

public final class Waiter
{
    private final long durationInMillis;
    private final long pollingTimeoutMillis;

    public Waiter(WaitMode waitMode)
    {
        durationInMillis = waitMode.getDuration().toMillis();
        pollingTimeoutMillis = waitMode.calculatePollingTimeout(TimeUnit.MILLISECONDS);
    }

    public <T, E extends Exception> T wait(CheckedSupplier<T, E> valueProvider, Predicate<T> stopCondition) throws E
    {
        long endTime = System.currentTimeMillis() + durationInMillis;

        T value;
        do
        {
            value = valueProvider.get();
            Sleeper.sleep(pollingTimeoutMillis, TimeUnit.MILLISECONDS);
        }
        while (!stopCondition.test(value) && System.currentTimeMillis() <= endTime);
        return value;
    }

    public <E extends Exception> void wait(CheckedRunnable<E> runnable, BooleanSupplier stopCondition) throws E
    {
        wait(
            () -> {
                runnable.run();
                return (Void) null;
            },
            alwaysNull -> stopCondition.getAsBoolean()
        );
    }
}
