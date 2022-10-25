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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.vividus.selenium.mobileapp.screenshot.strategies.MobileViewportShootingStrategy;
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
    void shouldCreateAshotWithMergedConfiguration() throws IllegalAccessException
    {
        mockAshotConfiguration(true);
        when(mobileAppWebDriverManager.getDpr()).thenReturn(2d);
        ashotFactory.setAppendBottomNavigationBarOnAndroid(true);
        when(mobileAppWebDriverManager.isAndroid()).thenReturn(false);
        var parameters = new ScreenshotParameters();
        parameters.setShootingStrategy(Optional.of(SIMPLE));
        var aShot = ashotFactory.create(Optional.of(parameters));
        var croppingDecorator = (ElementCroppingDecorator) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        var strategy = (ScalingDecorator) FieldUtils.readField(croppingDecorator, SHOOTING_STRATEGY, true);
        assertInstanceOf(MobileViewportShootingStrategy.class, FieldUtils.readField(strategy, SHOOTING_STRATEGY, true));
        assertCoordsProvider(aShot);
    }

    @Test
    void shouldCreateAshotWithDefaultConfigurationWithoutBottomNavigationBar() throws IllegalAccessException
    {
        mockAshotConfiguration(false);
        ashotFactory.setAppendBottomNavigationBarOnAndroid(false);
        ashotFactory.setScreenshotShootingStrategy(SIMPLE);
        var aShot = ashotFactory.create(Optional.empty());
        assertInstanceOf(MobileViewportShootingStrategy.class, FieldUtils.readField(aShot, SHOOTING_STRATEGY, true));
        assertCoordsProvider(aShot);
    }

    @Test
    void shouldCreateAshotWithDefaultConfigurationWithBottomNavigationBarOnAndroid() throws IllegalAccessException
    {
        mockAshotConfiguration(false);
        when(mobileAppWebDriverManager.getStatusBarSize()).thenReturn(2);
        ashotFactory.setAppendBottomNavigationBarOnAndroid(true);
        when(mobileAppWebDriverManager.isAndroid()).thenReturn(true);
        ashotFactory.setScreenshotShootingStrategy(SIMPLE);
        var aShot = ashotFactory.create(Optional.empty());
        var strategy = (CuttingDecorator) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        var baseStrategy = (ShootingStrategy) FieldUtils.readField(strategy, SHOOTING_STRATEGY, true);
        assertInstanceOf(SimpleShootingStrategy.class, baseStrategy);
        var cutStrategy = (CutStrategy) FieldUtils.readField(strategy, CUT_STRATEGY, true);
        assertEquals(2, cutStrategy.getHeaderHeight(null));
        assertEquals(0, cutStrategy.getFooterHeight(null));
        assertCoordsProvider(aShot);
    }

    private void mockAshotConfiguration(boolean downscale)
    {
        ashotFactory.setDownscale(downscale);
        ashotFactory.setStrategies(Map.of(SIMPLE, new SimpleScreenshotShootingStrategy(), DIMPLE, s -> {
            throw new IllegalStateException();
        }));
    }

    private void assertCoordsProvider(AShot aShot) throws IllegalAccessException
    {
        assertSame(coordsProvider, FieldUtils.readField(aShot, "coordsProvider", true));
    }
}
