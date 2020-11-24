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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;

import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class DurationBasedWaiterTests
{
    @Test
    void shouldReturnValueAfterReachingTimeout() throws IOException
    {
        FailableSupplier<Boolean, IOException> valueProvider = mock(FailableSupplier.class);
        when(valueProvider.get()).thenReturn(false);
        assertFalse(new DurationBasedWaiter(new WaitMode(Duration.ofMillis(500), 2))
                .wait(valueProvider, Boolean::booleanValue));
    }

    @Test
    void shouldReturnValueAfterReachingStopCondition() throws IOException
    {
        FailableSupplier<Boolean, IOException> valueProvider = mock(FailableSupplier.class);
        when(valueProvider.get()).thenReturn(false).thenReturn(true).thenReturn(false);
        assertTrue(new DurationBasedWaiter(new WaitMode(Duration.ofSeconds(1), 3))
                .wait(valueProvider, Boolean::booleanValue));
    }

    @Test
    void shouldInvokeRunnableOnceBeforeReachingStopCondition() throws IOException
    {
        FailableRunnable<IOException> failableRunnable = mock(FailableRunnable.class);
        new DurationBasedWaiter(Duration.ZERO, Duration.ZERO).wait(failableRunnable, Boolean.TRUE::booleanValue);
        verify(failableRunnable, times(1)).run();
    }
}
