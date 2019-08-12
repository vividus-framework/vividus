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

package org.vividus.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Sleeper.class, Thread.class})
public class SleeperTests
{
    @Before
    public void before()
    {
        PowerMockito.mockStatic(Thread.class);
    }

    @Test
    public void testSleepDuration() throws Exception
    {
        Sleeper.sleep(Duration.ofMillis(1));
        verifySleep(1);
    }

    @Test
    public void testSleep() throws Exception
    {
        Sleeper.sleep(1L, TimeUnit.MILLISECONDS);
        verifySleep(1);
    }

    @Test
    public void testSleepException() throws Exception
    {
        InterruptedException cause = new InterruptedException();
        PowerMockito.doThrow(cause).when(Thread.class);
        Thread.sleep(1L);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> Sleeper.sleep(1L, TimeUnit.MILLISECONDS));
        assertEquals(cause, exception.getCause());
    }

    private static void verifySleep(int times) throws InterruptedException
    {
        PowerMockito.verifyStatic(Thread.class, Mockito.times(times));
        Thread.sleep(1L);
    }
}
