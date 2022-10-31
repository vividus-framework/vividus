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

package org.vividus.mobitru.mobileapp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.selenium.event.WebDriverCreateEvent;

@ExtendWith(MockitoExtension.class)
class MobitruApplicationActivatorTests
{
    @Mock private ApplicationActions applicationActions;
    @InjectMocks private MobitruApplicationActivator mobitruApplicationActivator;

    @Test
    void shouldActivateApplication()
    {
        var event = mock(WebDriverCreateEvent.class);
        var bundleId = "dev.vividus.starter";
        mobitruApplicationActivator.setBundleId(bundleId);
        mobitruApplicationActivator.onSessionStart(event);
        verify(applicationActions).activateApp(bundleId);
        verifyNoInteractions(event);
    }
}
