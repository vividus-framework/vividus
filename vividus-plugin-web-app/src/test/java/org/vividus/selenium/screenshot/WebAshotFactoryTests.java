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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.AdjustingViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.SimpleScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.StickyHeaderCutStrategy;
import org.vividus.selenium.screenshot.strategies.ViewportPastingScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.ViewportShootingStrategy;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;
import org.vividus.ui.web.screenshot.WebScreenshotParameters;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.shooting.CuttingDecorator;
import ru.yandex.qatools.ashot.shooting.ElementCroppingDecorator;
import ru.yandex.qatools.ashot.shooting.ScalingDecorator;
import ru.yandex.qatools.ashot.shooting.ScrollbarHidingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.FixedCutStrategy;

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
    @InjectMocks private WebAshotFactory webAshotFactory;

    @BeforeEach
    void beforeEach()
    {
        when(javascriptActions.getDevicePixelRatio()).thenReturn(2d);
    }

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyIfThereIsNoConfigurationFound() throws IllegalAccessException
    {
        webAshotFactory.setStrategies(Map.of(VIEWPORT_PASTING, new ViewportPastingScreenshotShootingStrategy()));
        webAshotFactory.setScreenshotShootingStrategy(VIEWPORT_PASTING);
        AShot aShot = webAshotFactory.create(Optional.empty());
        validateCoordsProvider(aShot);
        ShootingStrategy baseStrategy = (ShootingStrategy) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertThat(baseStrategy, instanceOf(ScrollbarHidingDecorator.class));
        assertThat(FieldUtils.readField(baseStrategy, SHOOTING_STRATEGY, true),
                instanceOf(AdjustingViewportPastingDecorator.class));
    }

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyUsingStrategyFromConfiguration() throws IllegalAccessException
    {
        webAshotFactory.setStrategies(Map.of(VIEWPORT_PASTING, new ViewportPastingScreenshotShootingStrategy()));
        webAshotFactory.setScreenshotShootingStrategy(SIMPLE);
        WebScreenshotParameters screenshotParameters = new WebScreenshotParameters();
        screenshotParameters.setShootingStrategy(Optional.of(VIEWPORT_PASTING));
        AShot aShot = webAshotFactory.create(Optional.of(screenshotParameters));
        validateCoordsProvider(aShot);
        ShootingStrategy baseStrategy = (ShootingStrategy) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertThat(baseStrategy, instanceOf(ScrollbarHidingDecorator.class));
        assertThat(FieldUtils.readField(baseStrategy, SHOOTING_STRATEGY, true),
                instanceOf(AdjustingViewportPastingDecorator.class));
    }

    @Test
    void shouldCreateAshotViaWithSimpleCoords() throws IllegalAccessException
    {
        webAshotFactory.setStrategies(Map.of(SIMPLE, new SimpleScreenshotShootingStrategy()));
        webAshotFactory.setScreenshotShootingStrategy(SIMPLE);
        AShot aShot = webAshotFactory.create(Optional.empty());
        validateCoordsProvider(aShot);
        ShootingStrategy baseStrategy = (ShootingStrategy) FieldUtils.readField(aShot, SHOOTING_STRATEGY, true);
        assertThat(baseStrategy, instanceOf(ScrollbarHidingDecorator.class));
        assertThat(FieldUtils.readField(baseStrategy, SHOOTING_STRATEGY, true),
                instanceOf(ViewportShootingStrategy.class));
    }

    @Test
    void shouldCreateAshotWithCuttingStrategiesForNativeWebHeadersFooters() throws IllegalAccessException
    {
        WebScreenshotParameters screenshotParameters = new WebScreenshotParameters();
        screenshotParameters.setNativeFooterToCut(TEN);
        WebCutOptions webCutOptions = new WebCutOptions(TEN, TEN);
        screenshotParameters.setWebCutOptions(webCutOptions);
        screenshotParameters.setShootingStrategy(Optional.empty());
        screenshotParameters.setWebCutOptions(new WebCutOptions(TEN, 0));
        screenshotParameters.setScrollTimeout(Duration.ofMillis(500));
        screenshotParameters.setCoordsProvider(CoordsProviderType.CEILING);

        AShot aShot = webAshotFactory.create(Optional.of(screenshotParameters));

        validateCoordsProvider(aShot);
        ShootingStrategy baseStrategy = getShootingStrategy(aShot);
        assertThat(baseStrategy, is(instanceOf(ElementCroppingDecorator.class)));

        ShootingStrategy scrollbarHidingDecorator = (ShootingStrategy) FieldUtils.readField(baseStrategy,
                SHOOTING_STRATEGY, true);
        assertThat(scrollbarHidingDecorator, is(instanceOf(ScrollbarHidingDecorator.class)));

        ShootingStrategy viewportPastingDecorator = (ShootingStrategy) FieldUtils.readField(scrollbarHidingDecorator,
                SHOOTING_STRATEGY, true);
        assertEquals(500, (int) FieldUtils.readField(viewportPastingDecorator, "scrollTimeout", true));
        assertEquals(screenshotDebugger, FieldUtils.readField(viewportPastingDecorator, "screenshotDebugger", true));

        ShootingStrategy webCuttingDecorator = getShootingStrategy(viewportPastingDecorator);
        assertThat(webCuttingDecorator, is(instanceOf(CuttingDecorator.class)));
        CutStrategy webCutStrategy = getCutStrategy(webCuttingDecorator);
        assertThat(webCutStrategy, is(instanceOf(StickyHeaderCutStrategy.class)));
        webCutStrategy.getHeaderHeight(null);
        validateCutStrategy(0, 10, webCutStrategy);

        ShootingStrategy nativeCuttingDecorator = getShootingStrategy(webCuttingDecorator);
        CutStrategy nativeCutStrategy = getCutStrategy(nativeCuttingDecorator);
        assertThat(nativeCutStrategy, is(instanceOf(FixedCutStrategy.class)));
        validateCutStrategy(10, 0, nativeCutStrategy);

        ShootingStrategy scalingDecorator = getShootingStrategy(nativeCuttingDecorator);
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
        WebElement webElement = mock(WebElement.class);
        WebScreenshotParameters screenshotParameters = new WebScreenshotParameters();
        screenshotParameters.setScrollableElement(Optional.of(webElement));
        screenshotParameters.setCoordsProvider(CoordsProviderType.CEILING);
        screenshotParameters.setShootingStrategy(Optional.empty());
        screenshotParameters.setWebCutOptions(new WebCutOptions(0, 0));
        screenshotParameters.setScrollTimeout(Duration.ofMillis(TEN));
        AShot aShot = webAshotFactory.create(Optional.of(screenshotParameters));

        validateCoordsProvider(aShot);
        ShootingStrategy decorator = getShootingStrategy(aShot);
        assertThat(decorator, is(instanceOf(ElementCroppingDecorator.class)));

        ShootingStrategy scrollbarHidingDecorator = (ShootingStrategy) FieldUtils.readField(decorator,
                SHOOTING_STRATEGY, true);
        assertThat(scrollbarHidingDecorator, is(instanceOf(ScrollbarHidingDecorator.class)));

        ShootingStrategy scrollableElementAwareDecorator = (ShootingStrategy) FieldUtils
                .readField(scrollbarHidingDecorator, SHOOTING_STRATEGY, true);
        assertThat(scrollableElementAwareDecorator,
                is(instanceOf(AdjustingScrollableElementAwareViewportPastingDecorator.class)));

        assertEquals(webElement, FieldUtils.readField(scrollableElementAwareDecorator, "scrollableElement", true));

        ShootingStrategy scalingDecorator = getShootingStrategy(scrollableElementAwareDecorator);
        assertThat(scalingDecorator, is(instanceOf(ScalingDecorator.class)));
        verifyDPR(scalingDecorator);
    }

    private void validateCoordsProvider(AShot aShot) throws IllegalAccessException
    {
        CoordsProvider coordsProvider = (CoordsProvider) FieldUtils.readField(aShot, COORDS_PROVIDER, true);
        assertThat(coordsProvider, is(instanceOf(ScrollBarHidingCoordsProviderDecorator.class)));
        coordsProvider = (CoordsProvider) FieldUtils.readField(coordsProvider, COORDS_PROVIDER, true);
        assertThat(coordsProvider, is(instanceOf(CeilingJsCoordsProvider.class)));
    }

    private CutStrategy getCutStrategy(Object hasCutStrategy) throws IllegalAccessException
    {
        return (CutStrategy) FieldUtils.readField(hasCutStrategy, "cutStrategy", true);
    }

    private void validateCutStrategy(int footer, int header, CutStrategy toValidate)
    {
        assertEquals(footer, toValidate.getFooterHeight(null));
        assertEquals(header, toValidate.getHeaderHeight(null));
    }

    private ShootingStrategy getShootingStrategy(Object hasShootingStrategy) throws IllegalAccessException
    {
        return (ShootingStrategy) FieldUtils.readField(hasShootingStrategy, SHOOTING_STRATEGY, true);
    }
}
