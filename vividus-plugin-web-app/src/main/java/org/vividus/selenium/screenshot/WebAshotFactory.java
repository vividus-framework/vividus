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

import static ru.yandex.qatools.ashot.shooting.ShootingStrategies.cutting;

import java.util.Optional;

import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.AdjustingViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.ScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.SimpleScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.StickyHeaderCutStrategy;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;
import org.vividus.ui.web.screenshot.WebScreenshotParameters;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public class WebAshotFactory extends AbstractAshotFactory<WebScreenshotParameters>
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
    public AShot create(Optional<WebScreenshotParameters> screenshotParameters)
    {
        return screenshotParameters.map(ashotParameters -> ashotParameters.getShootingStrategy()
                                                                   .map(this::createAShot)
                                                                   .orElseGet(() -> createAShot(ashotParameters)))
                      .orElseGet(() -> createAShot(screenshotShootingStrategy));
    }

    private AShot createAShot(WebScreenshotParameters screenshotParameters)
    {
        ShootingStrategy decorated = getBaseShootingStrategy();

        decorated = configureNativePartialsToCut(screenshotParameters.getNativeHeaderToCut(),
                screenshotParameters, decorated);

        WebCutOptions webCutOptions = screenshotParameters.getWebCutOptions();
        int footerToCut = webCutOptions.getWebFooterToCut();
        int headerToCut = webCutOptions.getWebHeaderToCut();
        if (anyNotZero(footerToCut, headerToCut))
        {
            decorated = cutting(decorated, new StickyHeaderCutStrategy(headerToCut, footerToCut));
        }

        decorated = ((DebuggingViewportPastingDecorator) decorateWithViewportPasting(decorated,
                screenshotParameters))
                .withDebugger(screenshotDebugger);

        return new ScrollbarHidingAshot(screenshotParameters.getScrollableElement(), scrollbarHandler)
                .shootingStrategy(decorated)
                .coordsProvider(screenshotParameters.getCoordsProvider().create(javascriptActions));
    }

    private ShootingStrategy decorateWithViewportPasting(ShootingStrategy toDecorate,
            WebScreenshotParameters screenshotParameters)
    {
        WebCutOptions webCutOptions = screenshotParameters.getWebCutOptions();
        return ((DebuggingViewportPastingDecorator) screenshotParameters.getScrollableElement()
                       .map(e -> (ShootingStrategy) new AdjustingScrollableElementAwareViewportPastingDecorator(
                               toDecorate, e, javascriptActions, webCutOptions))
                       .orElseGet(() ->
                       new AdjustingViewportPastingDecorator(toDecorate, webCutOptions.getWebHeaderToCut(),
                               webCutOptions.getWebFooterToCut())))
                       .withScrollTimeout(((Long) screenshotParameters.getScrollTimeout().toMillis()).intValue());
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
