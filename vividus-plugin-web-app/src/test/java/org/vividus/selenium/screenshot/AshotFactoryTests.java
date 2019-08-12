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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.powermock.reflect.Whitebox;
import org.vividus.selenium.IWebDriverFactory;
import org.vividus.selenium.SauceLabsCapabilityType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.action.IJavascriptActions;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.CuttingDecorator;
import ru.yandex.qatools.ashot.shooting.ScalingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.FixedCutStrategy;

@ExtendWith(MockitoExtension.class)
class AshotFactoryTests
{
    private static final String SHOOTING_STRATEGY = "shootingStrategy";
    private static final String COORDS_PROVIDER = "coordsProvider";
    private static final int TEN = 10;

    @Mock
    private Map<String, ScreenshotConfiguration> ashotConfigurations;
    @Mock
    private ShootingStrategy baseShootingStrategy;
    @Mock
    private IWebDriverFactory webDriverFactory;
    @Mock
    private IWebDriverManager webDriverManager;
    @Mock
    private IJavascriptActions javascriptActions;
    @Mock
    private IScrollbarHandler scrollbarHandler;
    @Mock
    private ScreenshotDebugger screenshotDebugger;

    @InjectMocks
    private AshotFactory ashotFactory;

    @BeforeEach
    void callInit()
    {
        when(webDriverManager.getDevicePixelRatio()).thenReturn(2d);
        ashotFactory.init();
    }

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyIfThereIfConfigurationNotFound()
    {
        mockDevice();
        ashotFactory.setScreenshotShootingStrategy(ScreenshotShootingStrategy.VIEWPORT_PASTING);
        AShot aShot = ashotFactory.create(false, Optional.empty());
        assertThat(Whitebox.getInternalState(aShot, COORDS_PROVIDER), is(instanceOf(CeilingJsCoordsProvider.class)));
        assertThat(Whitebox.getInternalState(aShot, SHOOTING_STRATEGY), instanceOf(ViewportPastingDecorator.class));
    }

    private void mockDevice()
    {
        DesiredCapabilities capabilities = mock(DesiredCapabilities.class);
        when(webDriverFactory.getSeleniumGridDesiredCapabilities()).thenReturn(capabilities);
        String deviceName = "Google pixel 3";
        when(capabilities.getCapability(SauceLabsCapabilityType.DEVICE_NAME)).thenReturn(deviceName);
        when(webDriverManager.isOrientation(ScreenOrientation.LANDSCAPE)).thenReturn(true);
    }

    @Test
    void shouldCreateAshotViaScreenshotShootingStrategyUsingStrategyFromConfiguration()
    {
        mockDevice();
        ScreenshotConfiguration screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setScreenshotShootingStrategy(Optional.of(ScreenshotShootingStrategy.SIMPLE));
        AShot aShot = ashotFactory.create(false, Optional.of(screenshotConfiguration));
        assertThat(Whitebox.getInternalState(aShot, COORDS_PROVIDER), is(instanceOf(CeilingJsCoordsProvider.class)));
        assertThat(Whitebox.getInternalState(aShot, SHOOTING_STRATEGY), instanceOf(ScalingDecorator.class));
    }

    @Test
    void shouldCreateAshotViaWithSimpleCoords()
    {
        DesiredCapabilities capabilities = mock(DesiredCapabilities.class);
        when(webDriverFactory.getSeleniumGridDesiredCapabilities()).thenReturn(capabilities);
        ashotFactory.setScreenshotShootingStrategy(ScreenshotShootingStrategy.SIMPLE);
        ShootingStrategy strategy = ScreenshotShootingStrategy.SIMPLE
                .getDecoratedShootingStrategy(baseShootingStrategy, false, false, null);
        AShot aShot = ashotFactory.create(false, Optional.empty());
        assertThat(Whitebox.getInternalState(aShot, COORDS_PROVIDER), is(instanceOf(CeilingJsCoordsProvider.class)));
        assertEquals(strategy, baseShootingStrategy);
    }

    @Test
    void shouldCreateAshotWithCuttingStrategiesForNativeWebHeadersFooters()
    {
        ScreenshotConfiguration screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setNativeFooterToCut(TEN);
        screenshotConfiguration.setWebHeaderToCut(TEN);
        screenshotConfiguration.setScreenshotShootingStrategy(Optional.empty());

        AShot aShot = ashotFactory.create(false, Optional.of(screenshotConfiguration));

        assertThat(Whitebox.getInternalState(aShot, COORDS_PROVIDER), is(instanceOf(CeilingJsCoordsProvider.class)));
        ShootingStrategy viewportPastingDecorator = getShootingStrategy(aShot);
        assertThat(viewportPastingDecorator, is(instanceOf(AdjustingViewportPastingDecorator.class)));
        assertEquals(500, (int) Whitebox.getInternalState(viewportPastingDecorator, "scrollTimeout"));
        assertEquals(screenshotDebugger, Whitebox.getInternalState(viewportPastingDecorator, "screenshotDebugger"));

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

    private void verifyDPR(ShootingStrategy scalingDecorator)
    {
        assertEquals(2f, (float) Whitebox.getInternalState(scalingDecorator, "dprX"));
        assertEquals(2f, (float) Whitebox.getInternalState(scalingDecorator, "dprY"));
    }

    @Test
    void shouldCreateAshotUsingScrollableElement()
    {
        WebElement webElement = mock(WebElement.class);
        ScreenshotConfiguration screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setScrollableElement(() -> Optional.of(webElement));
        screenshotConfiguration.setCoordsProvider("CEILING");
        screenshotConfiguration.setScreenshotShootingStrategy(Optional.empty());
        AShot aShot = ashotFactory.create(false, Optional.of(screenshotConfiguration));

        assertThat(Whitebox.getInternalState(aShot, COORDS_PROVIDER), is(instanceOf(CeilingJsCoordsProvider.class)));
        ShootingStrategy scrollableElementAwareDecorator = getShootingStrategy(aShot);
        assertThat(scrollableElementAwareDecorator,
                is(instanceOf(AdjustingScrollableElementAwareViewportPastingDecorator.class)));
        assertEquals(webElement,
                Whitebox.getInternalState(scrollableElementAwareDecorator, "scrollableElement"));

        ShootingStrategy scalingDecorator = getShootingStrategy(scrollableElementAwareDecorator);
        assertThat(scalingDecorator, is(instanceOf(ScalingDecorator.class)));
        verifyDPR(scalingDecorator);
    }

    private CutStrategy getCutStrategy(Object hasCutStrategy)
    {
        return Whitebox.getInternalState(hasCutStrategy, "cutStrategy");
    }

    private void validateCutStrategy(int footer, int header, CutStrategy toValidate)
    {
        assertEquals(footer, toValidate.getFooterHeight(null));
        assertEquals(header, toValidate.getHeaderHeight(null));
    }

    private ShootingStrategy getShootingStrategy(Object hasShootingStrategy)
    {
        return Whitebox.getInternalState(hasShootingStrategy, SHOOTING_STRATEGY);
    }

}
