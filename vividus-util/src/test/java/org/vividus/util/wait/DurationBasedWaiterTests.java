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

package org.vividus.util.wait;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;

import org.apache.commons.lang3.function.FailableBooleanSupplier;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;
import org.junit.jupiter.api.Test;
import org.vividus.util.Sleeper;

class DurationBasedWaiterTests
{
    @Test
    void shouldReturnValueAfterReachingTimeout() throws IOException
    {
        FailableBooleanSupplier<IOException> valueProvider = mock();
        when(valueProvider.getAsBoolean()).thenReturn(false);
        int durationInMillis = 500;
        var waiter = new DurationBasedWaiter(new WaitMode(Duration.ofMillis(durationInMillis), 2));
        assertAll(
                () -> assertFalse(waiter.wait(valueProvider)),
                () -> assertEquals(durationInMillis, waiter.getDurationInMillis())
        );
    }

    @Test
    void shouldReturnValueAfterReachingStopCondition() throws IOException
    {
        FailableSupplier<Boolean, IOException> valueProvider = mock();
        when(valueProvider.get()).thenReturn(false).thenReturn(true).thenReturn(false);
        DurationBasedWaiter waiter = new DurationBasedWaiter(new WaitMode(Duration.ofSeconds(1), 3));
        DurationBasedWaiter waiterSpy = spy(waiter);
        assertTrue(waiterSpy.wait(valueProvider, Boolean::booleanValue));
        verify(waiterSpy).getPollingTimeoutMillis();
    }

    @Test
    void shouldInvokeRunnableOnceBeforeReachingStopCondition() throws IOException
    {
        FailableRunnable<IOException> failableRunnable = mock();
        new DurationBasedWaiter(Duration.ZERO, Duration.ZERO).wait(failableRunnable, Boolean.TRUE::booleanValue);
        verify(failableRunnable, times(1)).run();
    }

    @Test
    void shouldDecreasePollingTimeoutAccordinglyAfterLongRunningRunnable() throws IOException
    {
        FailableSupplier<Boolean, IOException> valueProvider = mock();
        when(valueProvider.get())
                .thenAnswer(invocation -> {
                    Sleeper.sleep(Duration.ofSeconds(5));
                    return false;
                })
                .thenReturn(true)
                .thenReturn(false);
        assertTrue(new DurationBasedWaiter(new WaitMode(Duration.ofSeconds(10), 10))
                .wait(valueProvider, Boolean::booleanValue));
    }
}
