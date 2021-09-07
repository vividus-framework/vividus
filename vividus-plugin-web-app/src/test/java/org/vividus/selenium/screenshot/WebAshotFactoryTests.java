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

package org.vividus.selenium.screenshot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverFactory;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.AdjustingViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.SimpleScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.StickyHeaderCutStrategy;
import org.vividus.selenium.screenshot.strategies.ViewportPastingScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.ViewportShootingStrategy;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.util.property.PropertyMappedCollection;

import io.appium.java_client.remote.MobileCapabilityType;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.CuttingDecorator;
import ru.yandex.qatools.ashot.shooting.ScalingDecorator;
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

    @Mock private ShootingStrategy baseShootingStrategy;
    @Mock private IWebDriverFactory webDriverFactory;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private ScreenshotDebugger screenshotDebugger;
    @Mock private PropertyMappedCollection<WebScreenshotConfiguration> ashotConfigurations;
    @InjectMocks private WebAshotFactory webAshotFactory;

    @BeforeEach
    void beforeEach()
    {
        webAshotFactory.setAshotConfigurations(ashotConfigurations);
        when(javascriptActions.getDevicePixelRatio()).thenReturn(2d);
    }

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyIfThereIsNoConfigurationFound() throws IllegalAccessException
    {
        webAshotFactory.setShootingStrategy(SHOOTING_STRATEGY);
        webAshotFactory.setStrategies(Map.of(VIEWPORT_PASTING, new ViewportPastingScreenshotShootingStrategy()));
        when(ashotConfigurations.getNullable(SHOOTING_STRATEGY)).thenReturn(Optional.empty());
        mockDeviceAndOrientation();
        webAshotFactory.setScreenshotShootingStrategy(VIEWPORT_PASTING);
        AShot aShot = webAshotFactory.create(Optional.empty());
        assertThat(FieldUtils.readField(aShot, COORDS_PROVIDER, true), is(instanceOf(CeilingJsCoordsProvider.class)));
        assertThat(FieldUtils.readField(aShot, SHOOTING_STRATEGY, true),
                instanceOf(AdjustingViewportPastingDecorator.class));
    }

    private void mockDeviceAndOrientation()
    {
        String deviceName = "Google pixel 3";
        when(webDriverFactory.getCapability(MobileCapabilityType.DEVICE_NAME, false)).thenReturn(deviceName);
        when(webDriverManager.isOrientation(ScreenOrientation.LANDSCAPE)).thenReturn(true);
    }

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyUsingStrategyFromConfiguration() throws IllegalAccessException
    {
        webAshotFactory.setShootingStrategy(SHOOTING_STRATEGY);
        webAshotFactory.setStrategies(Map.of(VIEWPORT_PASTING, new ViewportPastingScreenshotShootingStrategy()));
        webAshotFactory.setScreenshotShootingStrategy(SIMPLE);
        WebScreenshotConfiguration screenshotConfiguration = new WebScreenshotConfiguration();
        screenshotConfiguration.setShootingStrategy(Optional.of(VIEWPORT_PASTING));
        when(ashotConfigurations.getNullable(SHOOTING_STRATEGY)).thenReturn(Optional.empty());
        mockDeviceAndOrientation();
        AShot aShot = webAshotFactory.create(Optional.of(screenshotConfiguration));
        assertThat(FieldUtils.readField(aShot, COORDS_PROVIDER, true), is(instanceOf(CeilingJsCoordsProvider.class)));
        assertThat(FieldUtils.readField(aShot, SHOOTING_STRATEGY, true),
                instanceOf(AdjustingViewportPastingDecorator.class));
    }

    @Test
    void shouldCreateAshotViaWithSimpleCoords() throws IllegalAccessException
    {
        webAshotFactory.setStrategies(Map.of(SIMPLE, new SimpleScreenshotShootingStrategy()));
        webAshotFactory.setScreenshotShootingStrategy(SIMPLE);
        AShot aShot = webAshotFactory.create(Optional.empty());
        assertThat(FieldUtils.readField(aShot, COORDS_PROVIDER, true), is(instanceOf(CeilingJsCoordsProvider.class)));
        assertThat(FieldUtils.readField(aShot, SHOOTING_STRATEGY, true), instanceOf(ViewportShootingStrategy.class));
    }

    @Test
    void shouldCreateAshotWithCuttingStrategiesForNativeWebHeadersFooters() throws IllegalAccessException
    {
        WebScreenshotConfiguration screenshotConfiguration = new WebScreenshotConfiguration();
        screenshotConfiguration.setNativeFooterToCut(TEN);
        screenshotConfiguration.setWebHeaderToCut(TEN);
        screenshotConfiguration.setShootingStrategy(Optional.empty());

        AShot aShot = webAshotFactory.create(Optional.of(screenshotConfiguration));

        assertThat(FieldUtils.readField(aShot, COORDS_PROVIDER, true), is(instanceOf(CeilingJsCoordsProvider.class)));
        ShootingStrategy viewportPastingDecorator = getShootingStrategy(aShot);
        assertThat(viewportPastingDecorator, is(instanceOf(AdjustingViewportPastingDecorator.class)));
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
        WebScreenshotConfiguration screenshotConfiguration = new WebScreenshotConfiguration();
        screenshotConfiguration.setScrollableElement(() -> Optional.of(webElement));
        screenshotConfiguration.setCoordsProvider("CEILING");
        screenshotConfiguration.setShootingStrategy(Optional.empty());
        AShot aShot = webAshotFactory.create(Optional.of(screenshotConfiguration));

        assertThat(FieldUtils.readField(aShot, COORDS_PROVIDER, true), is(instanceOf(CeilingJsCoordsProvider.class)));
        ShootingStrategy scrollableElementAwareDecorator = getShootingStrategy(aShot);
        assertThat(scrollableElementAwareDecorator,
                is(instanceOf(AdjustingScrollableElementAwareViewportPastingDecorator.class)));
        assertEquals(webElement, FieldUtils.readField(scrollableElementAwareDecorator, "scrollableElement", true));

        ShootingStrategy scalingDecorator = getShootingStrategy(scrollableElementAwareDecorator);
        assertThat(scalingDecorator, is(instanceOf(ScalingDecorator.class)));
        verifyDPR(scalingDecorator);
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
