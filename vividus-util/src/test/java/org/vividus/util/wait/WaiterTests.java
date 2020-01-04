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

import java.time.Duration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.util.CheckedSupplier;

@RunWith(PowerMockRunner.class)
@SuppressWarnings("unchecked")
public class WaiterTests
{
    @Test
    public void testOneTime() throws Exception
    {
        CheckedSupplier<Boolean, Exception> valueProvider = PowerMockito.mock(CheckedSupplier.class);
        PowerMockito.when(valueProvider.get()).thenReturn(true);
        assertTrue(Waiter.wait(new WaitMode(Duration.ZERO, 1), valueProvider, Boolean::booleanValue));
    }

    @Test
    public void testMultipleTimes() throws Exception
    {
        CheckedSupplier<Boolean, Exception> valueProvider = PowerMockito.mock(CheckedSupplier.class);
        PowerMockito.when(valueProvider.get()).thenReturn(true).thenReturn(false);
        assertFalse(Waiter.wait(new WaitMode(Duration.ofSeconds(1), 2), valueProvider, Boolean::booleanValue));
    }
}
