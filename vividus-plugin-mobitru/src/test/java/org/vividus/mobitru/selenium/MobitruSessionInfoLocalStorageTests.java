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

package org.vividus.mobitru.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.SimpleTestContext;

class MobitruSessionInfoLocalStorageTests
{
    private static final String DEVICE_ID = "device-id";

    private final MobitruSessionInfoLocalStorage storage = new MobitruSessionInfoLocalStorage(new SimpleTestContext());

    @Test
    void shouldSaveDeviceId()
    {
        storage.saveDeviceId(DEVICE_ID);
        assertEquals(Optional.of(DEVICE_ID), storage.getDeviceId());
    }

    @Test
    void shouldCleanDeviceIdOnSessionStop()
    {
        storage.saveDeviceId(DEVICE_ID);
        storage.onAfterSessionStop(mock(AfterWebDriverQuitEvent.class));
        assertEquals(Optional.empty(), storage.getDeviceId());
    }
}
