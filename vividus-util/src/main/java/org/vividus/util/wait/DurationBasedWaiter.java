/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.function.Predicate;

import org.apache.commons.lang3.function.FailableSupplier;
import org.vividus.util.Sleeper;

public final class DurationBasedWaiter extends Waiter
{
    private final long durationInMillis;

    public DurationBasedWaiter(WaitMode waitMode)
    {
        super(waitMode.calculatePollingTimeout(TimeUnit.MILLISECONDS));
        durationInMillis = waitMode.getDuration().toMillis();
    }

    public DurationBasedWaiter(Duration timeout, Duration pollingTimeout)
    {
        super(pollingTimeout.toMillis());
        durationInMillis = timeout.toMillis();
    }

    @Override
    public <T, E extends Exception> T wait(FailableSupplier<T, E> valueProvider, Predicate<T> stopCondition) throws E
    {
        long endTime = System.currentTimeMillis() + durationInMillis;

        T value;
        do
        {
            long iterationStartTime = System.currentTimeMillis();
            value = valueProvider.get();
            long iterationEndTime = System.currentTimeMillis();

            long iterationPollingTimeout = getPollingTimeoutMillis() - (iterationEndTime - iterationStartTime);
            if (iterationPollingTimeout > 0)
            {
                Sleeper.sleep(iterationPollingTimeout, TimeUnit.MILLISECONDS);
            }
        }
        while (!stopCondition.test(value) && System.currentTimeMillis() <= endTime);
        return value;
    }
}
