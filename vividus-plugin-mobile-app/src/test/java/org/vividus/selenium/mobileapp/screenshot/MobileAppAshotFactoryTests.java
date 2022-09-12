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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.strategies.SimpleScreenshotShootingStrategy;
import org.vividus.ui.screenshot.ScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.CuttingDecorator;
import pazone.ashot.ElementCroppingDecorator;
import pazone.ashot.ScalingDecorator;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.SimpleShootingStrategy;
import pazone.ashot.coordinates.CoordsProvider;
import pazone.ashot.cutter.CutStrategy;

@ExtendWith(MockitoExtension.class)
class MobileAppAshotFactoryTests
{
    private static final String CUT_STRATEGY = "cutStrategy";
    private static final String SHOOTING_STRATEGY = "shootingStrategy";
    private static final String DIMPLE = "dimple";
    private static final String SIMPLE = "SIMPLE";

    @Mock private MobileAppWebDriverManager mobileAppWebDriverManager;
    @Mock private CoordsProvider coordsProvider;
    @InjectMocks private MobileAppAshotFactory ashotFactory;

    @Test
    void shouldProvideDpr()
    {
        ashotFactory.getDpr();
        verify(mobileAppWebDriverManager).getDpr();
    }

    @Test
    void shouldCreateAshotWithTheMergedConfiguration() throws IllegalAccessException
    {
        mockAshotConfiguration(true);
        ScreenshotParameters parameters = new ScreenshotParameters();
        parameters.setShootingStrategy(Optional.of(SIMPLE));
        AShot aShot = ashotFactory.create(Optional.of(parameters));
        ElementCroppingDecorator croppingDecorator = (ElementCroppingDecorator) FieldUtils.readField(aShot,
                SHOOTING_STRATEGY, true);
        CuttingDecorator strategy = (CuttingDecorator) FieldUtils.readField(croppingDecorator, SHOOTING_STRATEGY, true);
        assertBaseStrategy(ScalingDecorator.class, strategy);
        assertCutStrategy(strategy, 1, 0);
        assertCoordsProvider(aShot);
    }

    @Test
    void shouldCreateAshotWithDefaultConfiguration() throws IllegalAccessException
    {
        mockAshotConfiguration(false);
        ashotFactory.setScreenshotShootingStrategy(SIMPLE);
        AShot aShot = ashotFactory.create(Optional.empty());
        CuttingDecorator strategy = (CuttingDecorator) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertBaseStrategy(SimpleShootingStrategy.class, strategy);
        assertCutStrategy(strategy, 2, 0);
        assertCoordsProvider(aShot);
    }

    private void mockAshotConfiguration(boolean downscale)
    {
        ashotFactory.setDownscale(downscale);
        ashotFactory.setStrategies(Map.of(SIMPLE, new SimpleScreenshotShootingStrategy(), DIMPLE, s ->
        {
            throw new IllegalStateException();
        }));
        when(mobileAppWebDriverManager.getDpr()).thenReturn(2d);
        when(mobileAppWebDriverManager.getStatusBarSize()).thenReturn(1);
    }

    private void assertBaseStrategy(Class<?> expectedClass, CuttingDecorator strategy) throws IllegalAccessException
    {
        ShootingStrategy baseStrategy = (ShootingStrategy) FieldUtils.readField(strategy, SHOOTING_STRATEGY, true);
        assertEquals(expectedClass, baseStrategy.getClass());
    }

    private void assertCutStrategy(CuttingDecorator strategy, int expectedHeaderHeight, int expectedFooterHeight)
            throws IllegalAccessException
    {
        CutStrategy cutStrategy = (CutStrategy) FieldUtils.readField(strategy, CUT_STRATEGY, true);
        assertEquals(expectedHeaderHeight, cutStrategy.getHeaderHeight(null));
        assertEquals(expectedFooterHeight, cutStrategy.getFooterHeight(null));
    }

    private void assertCoordsProvider(AShot aShot) throws IllegalAccessException
    {
        assertSame(coordsProvider, FieldUtils.readField(aShot, "coordsProvider", true));
    }
}
