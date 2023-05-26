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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;
import org.vividus.ui.web.screenshot.WebScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.ElementCroppingDecorator;
import pazone.ashot.ScalingDecorator;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.CoordsProvider;
import pazone.ashot.cutter.CutStrategy;
import pazone.ashot.cutter.FixedCutStrategy;

@ExtendWith(MockitoExtension.class)
class WebAshotFactoryTests
{
    private static final String VIEWPORT_PASTING = "VIEWPORT_PASTING";
    private static final String SHOOTING_STRATEGY = "shootingStrategy";
    private static final String COORDS_PROVIDER = "coordsProvider";
    private static final int TEN = 10;
    private static final String SIMPLE = "SIMPLE";

    @Mock private WebJavascriptActions javascriptActions;
    @Mock private ScreenshotDebugger screenshotDebugger;
    @InjectMocks private WebAshotFactory factory;

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyIfThereIsNoConfigurationFound() throws IllegalAccessException
    {
        when(javascriptActions.getDevicePixelRatio()).thenReturn(2d);
        factory.setScreenshotShootingStrategy(VIEWPORT_PASTING);
        var aShot = factory.create(Optional.empty());
        validateCoordsProvider(aShot);
        var baseStrategy = (ShootingStrategy) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertThat(baseStrategy, instanceOf(ScrollbarHidingDecorator.class));
        var actualShootingStrategy = FieldUtils.readField(baseStrategy, SHOOTING_STRATEGY, true);
        assertThat(actualShootingStrategy, instanceOf(DebuggingViewportPastingDecorator.class));
    }

    @Test
    void shouldThrowAnExceptionIfThereIsNoStrategyByTheName()
    {
        var strategyName = "unknown";
        var screenshotParameters = new WebScreenshotParameters();
        screenshotParameters.setShootingStrategy(Optional.of(strategyName));
        var parameters = Optional.of(screenshotParameters);
        var exception = assertThrows(IllegalArgumentException.class, () -> factory.create(parameters));
        assertEquals("Unknown shooting strategy with the name: " + strategyName, exception.getMessage());
    }

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyUsingStrategyFromConfiguration() throws IllegalAccessException
    {
        when(javascriptActions.getDevicePixelRatio()).thenReturn(2d);
        factory.setScreenshotShootingStrategy(SIMPLE);
        var screenshotParameters = new WebScreenshotParameters();
        screenshotParameters.setShootingStrategy(Optional.of(VIEWPORT_PASTING));
        var aShot = factory.create(Optional.of(screenshotParameters));
        validateCoordsProvider(aShot);
        var baseStrategy = (ShootingStrategy) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertThat(baseStrategy, instanceOf(ScrollbarHidingDecorator.class));
        assertThat(FieldUtils.readField(baseStrategy, SHOOTING_STRATEGY, true),
                instanceOf(DebuggingViewportPastingDecorator.class));
    }

    @Test
    void shouldCreateAshotViaWithSimpleCoords() throws IllegalAccessException
    {
        when(javascriptActions.getDevicePixelRatio()).thenReturn(2d);
        factory.setScreenshotShootingStrategy(SIMPLE);
        var aShot = factory.create(Optional.empty());
        validateCoordsProvider(aShot);
        var baseStrategy = (ShootingStrategy) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertThat(baseStrategy, instanceOf(ScrollbarHidingDecorator.class));
        assertThat(FieldUtils.readField(baseStrategy, SHOOTING_STRATEGY, true), instanceOf(ScalingDecorator.class));
    }

    @Test
    void shouldCreateAshotWithCuttingStrategiesForNativeWebHeadersFooters() throws IllegalAccessException
    {
        var screenshotParameters = new WebScreenshotParameters();
        screenshotParameters.setNativeFooterToCut(TEN);
        var webCutOptions = new WebCutOptions(TEN, TEN);
        screenshotParameters.setWebCutOptions(webCutOptions);
        screenshotParameters.setShootingStrategy(Optional.empty());
        screenshotParameters.setWebCutOptions(new WebCutOptions(TEN, 0));
        screenshotParameters.setScrollTimeout(Duration.ofMillis(500));
        screenshotParameters.setCoordsProvider(CoordsProviderType.CEILING);

        when(javascriptActions.getDevicePixelRatio()).thenReturn(2d);
        var aShot = factory.create(Optional.of(screenshotParameters));

        validateCoordsProvider(aShot);
        var baseStrategy = getShootingStrategy(aShot);
        assertThat(baseStrategy, is(instanceOf(ElementCroppingDecorator.class)));

        var scrollbarHidingDecorator = (ShootingStrategy) FieldUtils.readField(baseStrategy,
                SHOOTING_STRATEGY, true);
        assertThat(scrollbarHidingDecorator, is(instanceOf(ScrollbarHidingDecorator.class)));

        var viewportPastingDecorator = (ShootingStrategy) FieldUtils.readField(scrollbarHidingDecorator,
                SHOOTING_STRATEGY, true);
        assertEquals(500, (int) FieldUtils.readField(viewportPastingDecorator, "scrollTimeout", true));
        assertEquals(screenshotDebugger, FieldUtils.readField(viewportPastingDecorator, "screenshotDebugger", true));

        var nativeCuttingDecorator = getShootingStrategy(viewportPastingDecorator);
        var nativeCutStrategy = getCutStrategy(nativeCuttingDecorator);
        assertThat(nativeCutStrategy, is(instanceOf(FixedCutStrategy.class)));
        assertEquals(10, nativeCutStrategy.getFooterHeight(null));
        assertEquals(0, nativeCutStrategy.getHeaderHeight(null));

        var scalingDecorator = getShootingStrategy(nativeCuttingDecorator);
        assertThat(scalingDecorator, is(instanceOf(ScalingDecorator.class)));
        verifyDPR(scalingDecorator);
    }

    private void verifyDPR(ShootingStrategy scalingDecorator) throws IllegalAccessException
    {
        assertEquals(2f, (float) FieldUtils.readField(scalingDecorator, "dprX", true));
        assertEquals(2f, (float) FieldUtils.readField(scalingDecorator, "dprY", true));
    }

    @Test
    void shouldCreateAshotUsingScrollableElement() throws IllegalAccessException
    {
        var webElement = mock(WebElement.class);
        var screenshotParameters = new WebScreenshotParameters();
        screenshotParameters.setScrollableElement(Optional.of(webElement));
        screenshotParameters.setCoordsProvider(CoordsProviderType.CEILING);
        screenshotParameters.setShootingStrategy(Optional.empty());
        screenshotParameters.setWebCutOptions(new WebCutOptions(0, 0));
        screenshotParameters.setScrollTimeout(Duration.ofMillis(TEN));

        when(javascriptActions.getDevicePixelRatio()).thenReturn(2d);
        var aShot = factory.create(Optional.of(screenshotParameters));

        validateCoordsProvider(aShot);
        var decorator = getShootingStrategy(aShot);
        assertThat(decorator, is(instanceOf(ElementCroppingDecorator.class)));

        var scrollbarHidingDecorator = (ShootingStrategy) FieldUtils.readField(decorator,
                SHOOTING_STRATEGY, true);
        assertThat(scrollbarHidingDecorator, is(instanceOf(ScrollbarHidingDecorator.class)));

        var scrollableElementAwareDecorator = (ShootingStrategy) FieldUtils
                .readField(scrollbarHidingDecorator, SHOOTING_STRATEGY, true);
        assertThat(scrollableElementAwareDecorator,
                is(instanceOf(AdjustingScrollableElementAwareViewportPastingDecorator.class)));

        assertEquals(webElement, FieldUtils.readField(scrollableElementAwareDecorator, "scrollableElement", true));

        var scalingDecorator = getShootingStrategy(scrollableElementAwareDecorator);
        assertThat(scalingDecorator, is(instanceOf(ScalingDecorator.class)));
        verifyDPR(scalingDecorator);
    }

    private void validateCoordsProvider(AShot aShot) throws IllegalAccessException
    {
        var coordsProvider = (CoordsProvider) FieldUtils.readField(aShot, COORDS_PROVIDER, true);
        assertThat(coordsProvider, is(instanceOf(ScrollBarHidingCoordsProviderDecorator.class)));
        coordsProvider = (CoordsProvider) FieldUtils.readField(coordsProvider, COORDS_PROVIDER, true);
        assertThat(coordsProvider, is(instanceOf(CeilingJsCoordsProvider.class)));
    }

    private CutStrategy getCutStrategy(Object hasCutStrategy) throws IllegalAccessException
    {
        return (CutStrategy) FieldUtils.readField(hasCutStrategy, "cutStrategy", true);
    }

    private ShootingStrategy getShootingStrategy(Object hasShootingStrategy) throws IllegalAccessException
    {
        return (ShootingStrategy) FieldUtils.readField(hasShootingStrategy, SHOOTING_STRATEGY, true);
    }
}
