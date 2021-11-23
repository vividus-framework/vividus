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

import static ru.yandex.qatools.ashot.shooting.ShootingStrategies.cutting;

import java.util.Optional;

import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.AdjustingViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.ScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.SimpleScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.StickyHeaderCutStrategy;
import org.vividus.ui.web.action.WebJavascriptActions;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public class WebAshotFactory extends AbstractAshotFactory<WebScreenshotConfiguration>
{
    private final ScreenshotDebugger screenshotDebugger;
    private final IScrollbarHandler scrollbarHandler;
    private final WebJavascriptActions javascriptActions;
    private String screenshotShootingStrategy;

    protected WebAshotFactory(WebJavascriptActions javascriptActions, ScreenshotDebugger screenshotDebugger,
            IScrollbarHandler scrollbarHandler)
    {
        this.javascriptActions = javascriptActions;
        this.screenshotDebugger = screenshotDebugger;
        this.scrollbarHandler = scrollbarHandler;
    }

    @Override
    public AShot create(Optional<WebScreenshotConfiguration> screenshotConfiguration)
    {
        return getAshotConfiguration(screenshotConfiguration, (c, b) -> c)
                      .map(ashotConfiguration -> ashotConfiguration.getShootingStrategy()
                                                                   .map(this::createAShot)
                                                                   .orElseGet(() -> createAShot(ashotConfiguration)))
                      .orElseGet(() -> createAShot(screenshotShootingStrategy));
    }

    private AShot createAShot(WebScreenshotConfiguration screenshotConfiguration)
    {
        ShootingStrategy decorated = getBaseShootingStrategy();

        decorated = configureNativePartialsToCut(screenshotConfiguration.getNativeHeaderToCut(),
                screenshotConfiguration, decorated);

        int footerToCut = screenshotConfiguration.getWebFooterToCut();
        int headerToCut = screenshotConfiguration.getWebHeaderToCut();
        if (anyNotZero(footerToCut, headerToCut))
        {
            decorated = cutting(decorated, new StickyHeaderCutStrategy(headerToCut, footerToCut));
        }

        decorated = ((DebuggingViewportPastingDecorator) decorateWithViewportPasting(decorated,
                screenshotConfiguration))
                .withDebugger(screenshotDebugger);

        return new ScrollbarHidingAshot(screenshotConfiguration.getScrollableElement().get(), scrollbarHandler)
                .shootingStrategy(decorated)
                .coordsProvider(screenshotConfiguration.getCoordsProvider().create(javascriptActions));
    }

    private ShootingStrategy decorateWithViewportPasting(ShootingStrategy toDecorate,
            WebScreenshotConfiguration screenshotConfiguration)
    {
        return ((DebuggingViewportPastingDecorator) screenshotConfiguration.getScrollableElement().get()
                       .map(e -> (ShootingStrategy) new AdjustingScrollableElementAwareViewportPastingDecorator(
                               toDecorate, e, javascriptActions, screenshotConfiguration))
                       .orElseGet(() ->
                       new AdjustingViewportPastingDecorator(toDecorate, screenshotConfiguration.getWebHeaderToCut(),
                               screenshotConfiguration.getWebFooterToCut())))
                       .withScrollTimeout(((Long) screenshotConfiguration.getScrollTimeout().toMillis()).intValue());
    }

    private AShot createAShot(String screenshotShootingStrategyName)
    {
        ScreenshotShootingStrategy configured = getStrategyBy(screenshotShootingStrategyName);
        ShootingStrategy baseShootingStrategy = getBaseShootingStrategy();
        ShootingStrategy shootingStrategy = configured.getDecoratedShootingStrategy(baseShootingStrategy);
        return new ScrollbarHidingAshot(Optional.empty(), scrollbarHandler).shootingStrategy(shootingStrategy)
                .coordsProvider(configured instanceof SimpleScreenshotShootingStrategy
                        ? CeilingJsCoordsProvider.getSimple(javascriptActions)
                        : CeilingJsCoordsProvider.getScrollAdjusted(javascriptActions));
    }

    @Override
    protected double getDpr()
    {
        return javascriptActions.getDevicePixelRatio();
    }

    public void setScreenshotShootingStrategy(String screenshotShootingStrategy)
    {
        this.screenshotShootingStrategy = screenshotShootingStrategy;
    }
}
