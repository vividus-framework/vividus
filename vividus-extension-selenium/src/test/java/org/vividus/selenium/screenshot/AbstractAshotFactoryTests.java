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

package org.vividus.selenium.screenshot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.selenium.screenshot.strategies.ScreenshotShootingStrategy;
import org.vividus.ui.screenshot.ScreenshotParameters;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.CuttingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

class AbstractAshotFactoryTests
{
    private static final String DEFAULT = "default";
    private final TestAshotFactory factory = new TestAshotFactory();

    @Test
    void shouldProvideScalingBaseStrategy() throws IllegalAccessException
    {
        ShootingStrategy scaling = factory.getBaseShootingStrategy();
        assertEquals(101f, FieldUtils.readDeclaredField(scaling, "dprX", true));
    }

    @ParameterizedTest
    @CsvSource({"0, 0, false", "1, 0, true", "0, 1, true", "-1, 1, true", "1, -1, true", "-1, -1, false"})
    void shouldCheckIfAnyNonZero(int x, int y, boolean expected)
    {
        assertEquals(expected, factory.anyNotZero(x, y));
    }

    @Test
    void shouldThrowAnExpectionIfThereIsNoStrategyByTheName()
    {
        factory.setStrategies(Map.of());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.getStrategyBy("podsas"));
        assertEquals("Unable to find the strategy with the name: podsas", exception.getMessage());
    }

    @Test
    void shouldReturnStrategyByName()
    {
        ScreenshotShootingStrategy strategy = mock(ScreenshotShootingStrategy.class);
        factory.setStrategies(Map.of(DEFAULT, strategy));
        assertSame(strategy, factory.getStrategyBy(DEFAULT));
    }

    @Test
    void shouldNotConfigurePartialsToCut()
    {
        ShootingStrategy simple = ShootingStrategies.simple();
        assertSame(simple, factory.configureNativePartialsToCut(0, new ScreenshotParameters(), simple));
    }

    @Test
    void shouldDecorateWithCutting()
    {
        assertThat(factory.configureNativePartialsToCut(10, new ScreenshotParameters(),
                ShootingStrategies.simple()), instanceOf(CuttingDecorator.class));
    }

    private static final class TestAshotFactory extends AbstractAshotFactory<ScreenshotParameters>
    {
        private static final double DPR = 101d;

        @Override
        public AShot create(Optional<ScreenshotParameters> screenshotParameters)
        {
            return new AShot();
        }

        @Override
        protected double getDpr()
        {
            return DPR;
        }
    }
}
