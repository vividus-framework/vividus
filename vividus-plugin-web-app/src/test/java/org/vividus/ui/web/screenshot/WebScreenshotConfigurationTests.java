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

package org.vividus.ui.web.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.vividus.selenium.screenshot.CoordsProviderType;

class WebScreenshotConfigurationTests
{
    private static final WebScreenshotConfiguration CONFIGURATION = new WebScreenshotConfiguration();

    @Test
    void shouldReturnWebDriverCoordsProviderAsDefaultOne()
    {
        assertEquals(CoordsProviderType.CEILING, CONFIGURATION.getCoordsProvider());
    }

    @Test
    void shouldConvertTimeoutToDuration()
    {
        assertEquals(Duration.ofMillis(500), CONFIGURATION.getScrollTimeout());
        CONFIGURATION.setScrollTimeout("PT1S");
        assertEquals(Duration.ofMillis(1000), CONFIGURATION.getScrollTimeout());
    }
}
