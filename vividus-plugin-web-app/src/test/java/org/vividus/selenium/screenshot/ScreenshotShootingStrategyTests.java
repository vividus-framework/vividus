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

package org.vividus.selenium.screenshot;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ru.yandex.qatools.ashot.shooting.CuttingDecorator;
import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;

class ScreenshotShootingStrategyTests
{
    private static final String SAMSUNG_GALAXY_S4_EMULATOR = "Samsung Galaxy S4 Emulator";
    private static final String GOOGLE_NEXUS_7_HD_EMULATOR = "Google Nexus 7 HD Emulator";
    private static final String IPHONE_SIMULATOR = "iPhone Simulator";
    private static final String IPAD_SIMULATOR = "iPad Simulator";

    //CHECKSTYLE:OFF
    static Stream<Arguments> data()
    {
        return Stream.of(
            Arguments.of(ScreenshotShootingStrategy.SIMPLE,                  SimpleShootingStrategy.class,             false, false,  null                      ),
            Arguments.of(ScreenshotShootingStrategy.SIMPLE,                  ViewportShootingStrategy.class,           true,  false,  null                      ),
            Arguments.of(ScreenshotShootingStrategy.VIEWPORT_PASTING,        ViewportPastingDecorator.class,          false, false,  null                      ),
            Arguments.of(ScreenshotShootingStrategy.VIEWPORT_PASTING,        SimpleShootingStrategy.class,            true,  false,  null                      ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, false,  IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  false,  IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, true,   IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  true,   IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, true,   IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true, true,   IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, false,  IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  false,  IPAD_SIMULATOR            ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, false,  IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  false,  IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, true,   IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  true,   IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, true,   IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  true,   IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, false,  IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  false,  IPHONE_SIMULATOR          ),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, false,  GOOGLE_NEXUS_7_HD_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  false,  SAMSUNG_GALAXY_S4_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, true,   GOOGLE_NEXUS_7_HD_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        AdjustingRotatingDecorator.class,        true,  true,   SAMSUNG_GALAXY_S4_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, true,   GOOGLE_NEXUS_7_HD_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        AdjustingRotatingDecorator.class,        true,  true,   SAMSUNG_GALAXY_S4_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        DebuggingViewportPastingDecorator.class, false, false,  GOOGLE_NEXUS_7_HD_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.DEVICE_DEPENDENT,        CuttingDecorator.class,                  true,  false,  SAMSUNG_GALAXY_S4_EMULATOR),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_PORTRAIT,  ViewportPastingDecorator.class,         false, false,  null                      ),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_PORTRAIT,  ShootingTargetAreaStrategy.class,       true,  false,  null                      ),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_PORTRAIT,  ViewportPastingDecorator.class,         false, true,   null                      ),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_PORTRAIT,  ShootingTargetAreaStrategy.class,       true,  true,   null                      ),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_LANDSCAPE, ViewportPastingDecorator.class,         false, true,   null                      ),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_LANDSCAPE, ShootingTargetAreaStrategy.class,       true,  true,   null                      ),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_LANDSCAPE, ViewportPastingDecorator.class,         false, false,  null                      ),
            Arguments.of(ScreenshotShootingStrategy.PHONE_ANDROID_LANDSCAPE, ShootingTargetAreaStrategy.class,       true,  false,  null                      )
        );
    }
    //CHECKSTYLE:ON

    @ParameterizedTest
    @MethodSource("data")
    void test(ScreenshotShootingStrategy strategy, Class<? extends ShootingStrategy> clazz,
        boolean viewportScreenshots, boolean isLandscape, String deviceName)
    {
        assertThat(strategy.getDecoratedShootingStrategy(new SimpleShootingStrategy(), viewportScreenshots,
                isLandscape, deviceName), instanceOf(clazz));
    }
}
