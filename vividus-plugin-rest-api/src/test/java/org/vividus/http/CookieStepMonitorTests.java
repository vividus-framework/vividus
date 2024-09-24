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

package org.vividus.http;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookieStepMonitorTests
{
    @Mock private CookieStoreProvider cookieStoreProvider;
    @InjectMocks private CookieStepMonitor cookieStepMonitor;

    @Test
    void shouldClearCookiesAfterStep()
    {
        cookieStepMonitor.afterPerforming(null, false, null);
        verify(cookieStoreProvider).resetStepCookies();
    }

    @Test
    void shouldNotClearCookiesAfterStep()
    {
        cookieStepMonitor.afterPerforming(null, true, null);
        verifyNoInteractions(cookieStoreProvider);
    }
}
