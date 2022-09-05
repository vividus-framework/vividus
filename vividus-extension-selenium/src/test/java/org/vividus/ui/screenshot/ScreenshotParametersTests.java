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

package org.vividus.ui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;

class ScreenshotParametersTests
{
    @Test
    void shouldProvideDefalutValues()
    {
        ScreenshotParameters screenshotParameters = new ScreenshotParameters();
        Assertions.assertAll(() -> assertEquals(0, screenshotParameters.getCutTop()),
                             () -> assertEquals(0, screenshotParameters.getCutLeft()),
                             () -> assertEquals(0, screenshotParameters.getCutTop()),
                             () -> assertEquals(0, screenshotParameters.getCutBottom()),
                             () -> assertEquals(Optional.empty(),
                                     screenshotParameters.getShootingStrategy()));
    }

    @Test
    void shouldGetAndSetIgnoreStrategies()
    {
        ScreenshotParameters screenshotParameters = new ScreenshotParameters();
        assertNull(screenshotParameters.getIgnoreStrategies());
        Locator locator = mock(Locator.class);
        screenshotParameters.setIgnoreStrategies(Map.of(IgnoreStrategy.ELEMENT, Set.of(locator)));
        assertEquals(Map.of(IgnoreStrategy.ELEMENT, Set.of(locator)), screenshotParameters.getIgnoreStrategies());
    }
}
