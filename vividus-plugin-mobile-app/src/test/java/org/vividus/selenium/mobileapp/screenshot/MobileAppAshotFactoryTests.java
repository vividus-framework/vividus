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

package org.vividus.selenium.mobileapp.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.strategies.SimpleScreenshotShootingStrategy;
import org.vividus.ui.screenshot.ScreenshotParameters;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.shooting.CuttingDecorator;
import ru.yandex.qatools.ashot.shooting.ElementCroppingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;

@ExtendWith(MockitoExtension.class)
class MobileAppAshotFactoryTests
{
    private static final String CUT_STRATEGY = "cutStrategy";
    private static final String SHOOTING_STRATEGY = "shootingStrategy";
    private static final String DIMPLE = "dimple";
    private static final String SIMPLE = "SIMPLE";

    @Mock
    private MobileAppWebDriverManager mobileAppWebDriverManager;
    @Mock
    private CoordsProvider coordsProvider;
    @InjectMocks
    private MobileAppAshotFactory ashotFactory;

    @Test
    void shouldProvideDpr()
    {
        ashotFactory.getDpr();
        verify(mobileAppWebDriverManager).getDpr();
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @CsvSource({ "true,  1, ru.yandex.qatools.ashot.shooting.ScalingDecorator",
            "false, 2, ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy" })
    void shouldCreateAshotWithTheMergedConfiguration(boolean downscale, int headerToCut, Class<?> strategyType)
            throws IllegalAccessException
    {
        mockAshotConfiguration(DIMPLE, downscale);
        AShot aShot = ashotFactory.create(createConfigurationWith(10, Optional.of(SIMPLE)));
        ElementCroppingDecorator croppingDecorator = (ElementCroppingDecorator) FieldUtils.readField(aShot,
                SHOOTING_STRATEGY, true);
        CuttingDecorator strategy = (CuttingDecorator) FieldUtils.readField(croppingDecorator, SHOOTING_STRATEGY, true);
        ShootingStrategy baseStrategy = (ShootingStrategy) FieldUtils.readField(strategy, SHOOTING_STRATEGY, true);
        CutStrategy cutStrategy = (CutStrategy) FieldUtils.readField(strategy, CUT_STRATEGY, true);
        assertEquals(strategyType, baseStrategy.getClass());
        assertEquals(10, cutStrategy.getFooterHeight(null));
        assertEquals(headerToCut, cutStrategy.getHeaderHeight(null));
        assertSame(coordsProvider, FieldUtils.readField(aShot, "coordsProvider", true));
    }

    @SuppressWarnings("unchecked")
    private void mockAshotConfiguration(String defaultStrategy, boolean downscale)
    {
        ashotFactory.setDownscale(downscale);
        ashotFactory.setStrategies(Map.of(SIMPLE, new SimpleScreenshotShootingStrategy(), DIMPLE, s ->
        {
            throw new IllegalStateException();
        }));
        when(mobileAppWebDriverManager.getDpr()).thenReturn(2d);
        when(mobileAppWebDriverManager.getStatusBarSize()).thenReturn(1);
    }

    private Optional<ScreenshotParameters> createConfigurationWith(int nativeFooterToCut, Optional<String> strategy)
    {
        ScreenshotParameters screenshotParameters = new ScreenshotParameters();
        screenshotParameters.setNativeFooterToCut(nativeFooterToCut);
        screenshotParameters.setShootingStrategy(strategy);
        return Optional.of(screenshotParameters);
    }
}
