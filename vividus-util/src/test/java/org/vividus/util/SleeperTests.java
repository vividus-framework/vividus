/*
 * Copyright 2019-2024 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class SleeperTests
{
    @Test
    @PrepareForTest(Sleeper.class)
    public void testSleepDuration() throws Exception
    {
        PowerMockito.mockStatic(Sleeper.class);
        PowerMockito.doCallRealMethod().when(Sleeper.class, "sleep", Duration.ofMillis(1));

        Sleeper.sleep(Duration.ofMillis(1));

        PowerMockito.verifyStatic(Sleeper.class);
        Sleeper.sleep(1, TimeUnit.MILLISECONDS);
    }

    @Test
    public void shouldSleepForGivenTimeout() throws InterruptedException
    {
        TimeUnit timeUnit = mock();
        var timeout = 1L;
        Sleeper.sleep(timeout, timeUnit);
        verify(timeUnit).sleep(timeout);
    }

    @Test
    public void shouldWrapInterruptedExceptionAtSleep() throws InterruptedException
    {
        TimeUnit timeUnit = mock();
        var timeout = 1L;
        var cause = new InterruptedException();
        doThrow(cause).when(timeUnit).sleep(timeout);
        var exception = assertThrows(IllegalStateException.class, () -> Sleeper.sleep(timeout, timeUnit));
        assertEquals(cause, exception.getCause());
    }
}
