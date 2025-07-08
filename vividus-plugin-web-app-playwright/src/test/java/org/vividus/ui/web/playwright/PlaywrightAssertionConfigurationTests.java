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

package org.vividus.ui.web.playwright;

import static org.mockito.Mockito.times;

import java.time.Duration;

import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaywrightAssertionConfigurationTests
{
    @InjectMocks
    private PlaywrightAssertionConfiguration config;

    @Test
    void shouldSetDefaultAssertionTimeout()
    {
        Duration duration = Duration.ofMillis(500);
        config.setAssertionTimeout(duration);

        try (var assertionsMock = Mockito.mockStatic(PlaywrightAssertions.class))
        {
            config.init();
            assertionsMock.verify(() -> PlaywrightAssertions.setDefaultAssertionTimeout(duration.toMillis()), times(1));
        }
    }
}
