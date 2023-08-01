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

package org.vividus.selenium.mobileapp.screenshot;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.strategies.MobileViewportShootingStrategy;
import org.vividus.ui.screenshot.ScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.CuttingDecorator;
import pazone.ashot.ElementCroppingDecorator;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.SimpleShootingStrategy;
import pazone.ashot.coordinates.CoordsProvider;
import pazone.ashot.cutter.CutStrategy;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class MobileAppAshotFactoryTests
{
    private static final String CUT_STRATEGY = "cutStrategy";
    private static final String SHOOTING_STRATEGY = "shootingStrategy";
    private static final String SIMPLE = "SIMPLE";
    private static final String VIEWPORT = "VIEWPORT";
    private static final String FULL_SCREEN = "FULL_SCREEN";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(MobileAppAshotFactory.class);

    @Mock private MobileAppWebDriverManager mobileAppWebDriverManager;
    @Mock private CoordsProvider coordsProvider;
    @InjectMocks private MobileAppAshotFactory ashotFactory;

    @ParameterizedTest
    @ValueSource(strings = { SIMPLE, VIEWPORT })
    void shouldCreateAshotWithMergedConfiguration(String strategyName) throws IllegalAccessException
    {
        ashotFactory.setAppendBottomNavigationBarOnAndroid(true);
        when(mobileAppWebDriverManager.isAndroid()).thenReturn(false);
        var parameters = new ScreenshotParameters();
        parameters.setShootingStrategy(Optional.of(strategyName));
        var aShot = ashotFactory.create(Optional.of(parameters));
        var croppingDecorator = (ElementCroppingDecorator) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertInstanceOf(MobileViewportShootingStrategy.class,
                FieldUtils.readField(croppingDecorator, SHOOTING_STRATEGY, true));
        assertCoordsProvider(aShot);
    }

    @Test
    void shouldCreateAshotWithDefaultConfigurationWithoutBottomNavigationBar() throws IllegalAccessException
    {
        ashotFactory.setAppendBottomNavigationBarOnAndroid(false);
        ashotFactory.setScreenshotShootingStrategy(SIMPLE);
        var aShot = ashotFactory.create(Optional.empty());
        assertInstanceOf(MobileViewportShootingStrategy.class, FieldUtils.readField(aShot, SHOOTING_STRATEGY, true));
        assertCoordsProvider(aShot);
    }

    @Test
    void shouldCreateAshotWithDefaultConfigurationWithBottomNavigationBarOnAndroid() throws IllegalAccessException
    {
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

    @Test
    void shouldCreateAshotFullScreenStrategyWithMergedConfiguration() throws IllegalAccessException
    {
        var parameters = new ScreenshotParameters();
        parameters.setShootingStrategy(Optional.of(FULL_SCREEN));
        var aShot = ashotFactory.create(Optional.of(parameters));
        var croppingDecorator = (ElementCroppingDecorator) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertInstanceOf(SimpleShootingStrategy.class,
                FieldUtils.readField(croppingDecorator, SHOOTING_STRATEGY, true));
        assertCoordsProvider(aShot);
    }

    @Test
    void shouldWarnAboutDeprecatedStrategy()
    {
        var parameters = new ScreenshotParameters();
        parameters.setShootingStrategy(Optional.of(SIMPLE));
        ashotFactory.create(Optional.of(parameters));
        assertThat(logger.getLoggingEvents(), is(List.of(
                warn("Shooting strategy '{}' is deprecated and will be removed in VIVIDUS 0.6.0. Use '{}' instead",
                        SIMPLE, VIEWPORT))));
    }

    @ParameterizedTest
    @ValueSource(strings = { FULL_SCREEN, VIEWPORT })
    void shouldNotWarnAboutDeprecatedStrategy(String strategyName)
    {
        var parameters = new ScreenshotParameters();
        parameters.setShootingStrategy(Optional.of(strategyName));
        ashotFactory.create(Optional.of(parameters));
        assertTrue(logger.getLoggingEvents().isEmpty());
    }

    @Test
    void shouldThrowAnExceptionIfThereIsNoStrategyByTheName()
    {
        var parameters = new ScreenshotParameters();
        parameters.setShootingStrategy(Optional.of("dimple"));
        var exception = assertThrows(IllegalArgumentException.class,
                () -> ashotFactory.create(Optional.of(parameters)));
        assertEquals("Unknown shooting strategy with the name: dimple", exception.getMessage());
    }

    private void assertCoordsProvider(AShot aShot) throws IllegalAccessException
    {
        assertSame(coordsProvider, FieldUtils.readField(aShot, "coordsProvider", true));
    }
}
