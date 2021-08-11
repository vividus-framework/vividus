/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.selenium.screenshot.strategies;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;

class DeviceDependentScreenshotShootingStrategyTests
{
    private static final SimpleShootingStrategy SHOOTING_STRATEGY = new SimpleShootingStrategy();
    private static final String IPHONE_SIMULATOR = "iPhone Simulator";
    private static final String IPAD_SIMULATOR = "iPad Simulator";

    private final DeviceDependentScreenshotShootingStrategy strategy = new DeviceDependentScreenshotShootingStrategy();

    //CHECKSTYLE:OFF
    static Stream<Arguments> data()
    {
        return Stream.of(
            Arguments.of(false,  IPAD_SIMULATOR   ),
            Arguments.of(true,   IPAD_SIMULATOR   ),
            Arguments.of(false,  IPHONE_SIMULATOR ),
            Arguments.of(true,   IPHONE_SIMULATOR )
        );
    }
    //CHECKSTYLE:ON

    @ParameterizedTest
    @MethodSource("data")
    void shouldCreateCorrectStrategy(boolean isLandscape, String deviceName)
    {
        assertThat(strategy.getDecoratedShootingStrategy(SHOOTING_STRATEGY, isLandscape, deviceName),
                instanceOf(DebuggingViewportPastingDecorator.class));
    }

    @Test
    void shoudFailInCaseOfUnknownDevice()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                strategy.getDecoratedShootingStrategy(SHOOTING_STRATEGY, false, "iPear"));
        assertEquals("No device is found for name: iPear", exception.getMessage());
    }
}
