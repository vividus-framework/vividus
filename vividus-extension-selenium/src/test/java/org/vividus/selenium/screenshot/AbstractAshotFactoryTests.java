/*
 * Copyright 2019-2023 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.screenshot.ScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.CuttingDecorator;
import pazone.ashot.ElementCroppingDecorator;
import pazone.ashot.ShootingStrategies;
import pazone.ashot.ShootingStrategy;

@ExtendWith(MockitoExtension.class)
class AbstractAshotFactoryTests
{
    @Mock private ScreenshotCropper screenshotCropper;
    @InjectMocks private TestAshotFactory factory;

    @Test
    void shouldStoreStrategyName()
    {
        var simple = "simple";
        factory.setScreenshotShootingStrategy(simple);
        assertSame(simple, factory.getScreenshotShootingStrategy());
    }

    @ParameterizedTest
    @CsvSource({
            " 0,  0",
            "-1, -1"
    })
    void shouldNotConfigurePartialsToCut()
    {
        var simple = ShootingStrategies.simple();
        assertSame(simple, factory.decorateWithFixedCutStrategy(simple, 0, 0));
    }

    @ParameterizedTest
    @CsvSource({
            "10,   0",
            " 0,  10",
            "-1,   1",
            " 1,  -1"
    })
    void shouldDecorateWithCutting(int headerToCut, int footerToCut)
    {
        assertThat(factory.decorateWithFixedCutStrategy(ShootingStrategies.simple(), headerToCut, footerToCut),
                instanceOf(CuttingDecorator.class));
    }

    @Test
    void shouldDecorateWithCropping()
    {
        var strategy = mock(ShootingStrategy.class);
        var screenshotParameters = mock(ScreenshotParameters.class);
        assertThat(factory.decorateWithCropping(strategy, screenshotParameters),
                instanceOf(ElementCroppingDecorator.class));
        verify(screenshotParameters).getIgnoreStrategies();
    }

    private static final class TestAshotFactory extends AbstractAshotFactory<ScreenshotParameters>
    {
        TestAshotFactory(ScreenshotCropper screenshotCropper)
        {
            super(screenshotCropper);
        }

        @Override
        public AShot create(Optional<ScreenshotParameters> screenshotParameters)
        {
            throw new UnsupportedOperationException();
        }
    }
}
