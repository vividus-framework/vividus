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

import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.AdjustingViewportPastingDecorator;
import org.vividus.selenium.screenshot.strategies.ScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.SimpleScreenshotShootingStrategy;
import org.vividus.selenium.screenshot.strategies.StickyHeaderCutStrategy;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;
import org.vividus.ui.web.screenshot.WebScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.DebuggingViewportPastingDecorator;
import pazone.ashot.ScrollbarHidingDecorator;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.CoordsProvider;

public class WebAshotFactory extends AbstractAshotFactory<WebScreenshotParameters>
{
    private final ScreenshotDebugger screenshotDebugger;
    private final IScrollbarHandler scrollbarHandler;
    private final WebJavascriptActions javascriptActions;

    protected WebAshotFactory(ScreenshotCropper screenshotCropper, WebJavascriptActions javascriptActions,
            ScreenshotDebugger screenshotDebugger, IScrollbarHandler scrollbarHandler)
    {
        super(screenshotCropper);
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
                      .orElseGet(() -> createAShot(getScreenshotShootingStrategy()));
    }

    private AShot createAShot(WebScreenshotParameters screenshotParameters)
    {
        ShootingStrategy decorated = getBaseShootingStrategy();

        decorated = decorateWithFixedCutStrategy(decorated, screenshotParameters.getNativeHeaderToCut(),
                screenshotParameters.getNativeFooterToCut());

        WebCutOptions webCutOptions = screenshotParameters.getWebCutOptions();
        decorated = decorateWithCutStrategy(decorated, webCutOptions.getWebHeaderToCut(),
                webCutOptions.getWebFooterToCut(), StickyHeaderCutStrategy::new);

        decorated = ((DebuggingViewportPastingDecorator) decorateWithViewportPasting(decorated,
                screenshotParameters))
                .withDebugger(screenshotDebugger);

        decorated = decorateWithScrollbarHiding(decorated, screenshotParameters.getScrollableElement());

        decorated = decorateWithCropping(decorated, screenshotParameters);

        CoordsProvider coordsProvider = screenshotParameters.getCoordsProvider().create(javascriptActions);
        CoordsProvider scrollBarHidingCoordsProvider = new ScrollBarHidingCoordsProviderDecorator(coordsProvider,
                scrollbarHandler);

        return new AShot()
                .shootingStrategy(decorated)
                .coordsProvider(scrollBarHidingCoordsProvider);
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

    private ShootingStrategy decorateWithScrollbarHiding(ShootingStrategy strategy,
            Optional<WebElement> scrollableElement)
    {
        return new ScrollbarHidingDecorator(strategy, scrollableElement, scrollbarHandler);
    }

    private AShot createAShot(String screenshotShootingStrategyName)
    {
        ScreenshotShootingStrategy configured = getStrategyBy(screenshotShootingStrategyName);
        ShootingStrategy baseShootingStrategy = getBaseShootingStrategy();
        ShootingStrategy shootingStrategy = configured.getDecoratedShootingStrategy(baseShootingStrategy);
        shootingStrategy = decorateWithScrollbarHiding(shootingStrategy, Optional.empty());
        CoordsProvider coordsProvider = configured instanceof SimpleScreenshotShootingStrategy
                ? CeilingJsCoordsProvider.getSimple(javascriptActions)
                : CeilingJsCoordsProvider.getScrollAdjusted(javascriptActions);
        return new AShot().shootingStrategy(shootingStrategy)
                .coordsProvider(new ScrollBarHidingCoordsProviderDecorator(coordsProvider, scrollbarHandler));
    }

    @Override
    protected double getDpr()
    {
        return javascriptActions.getDevicePixelRatio();
    }
}
