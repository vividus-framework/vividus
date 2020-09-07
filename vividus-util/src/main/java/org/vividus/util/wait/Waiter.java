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

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;

public abstract class Waiter
{
    private final long pollingTimeoutMillis;

    Waiter(long pollingTimeoutMillis)
    {
        this.pollingTimeoutMillis = pollingTimeoutMillis;
    }

    public abstract  <T, E extends Exception> T wait(FailableSupplier<T, E> valueProvider, Predicate<T> stopCondition)
            throws E;

    public <E extends Exception> void wait(FailableRunnable<E> runnable, BooleanSupplier stopCondition) throws E
    {
        wait(
            () -> {
                runnable.run();
                return (Void) null;
            },
            alwaysNull -> stopCondition.getAsBoolean()
        );
    }

    protected long getPollingTimeoutMillis()
    {
        return pollingTimeoutMillis;
    }
}
