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

import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;
import org.vividus.ui.web.screenshot.WebScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.ShootingStrategies;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.CoordsProvider;

public class WebAshotFactory extends AbstractAshotFactory<WebScreenshotParameters>
{
    private static final int DEFAULT_STICKY_HEADER_HEIGHT = 100;
    private static final int DEFAULT_STICKY_FOOTER_HEIGHT = 100;
    private static final int SCROLL_TIMEOUT = 500;

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

        decorated = decorateWithViewportPasting(decorated, screenshotParameters)
                .withDebugger(screenshotDebugger)
                .withScrollTimeout(((Long) screenshotParameters.getScrollTimeout().toMillis()).intValue());

        decorated = decorateWithScrollbarHiding(decorated, screenshotParameters.getScrollableElement());

        decorated = decorateWithCropping(decorated, screenshotParameters);

        CoordsProvider coordsProvider = screenshotParameters.getCoordsProvider().create(javascriptActions);
        CoordsProvider scrollBarHidingCoordsProvider = new ScrollBarHidingCoordsProviderDecorator(coordsProvider,
                scrollbarHandler);

        return new AShot()
                .shootingStrategy(decorated)
                .coordsProvider(scrollBarHidingCoordsProvider);
    }

    private DebuggingViewportPastingDecorator decorateWithViewportPasting(ShootingStrategy toDecorate,
            WebScreenshotParameters screenshotParameters)
    {
        WebCutOptions webCutOptions = screenshotParameters.getWebCutOptions();
        return screenshotParameters.getScrollableElement().map(
                e -> (DebuggingViewportPastingDecorator) new AdjustingScrollableElementAwareViewportPastingDecorator(
                        toDecorate, e, javascriptActions, webCutOptions)
                ).orElseGet(
                () -> new DebuggingViewportPastingDecorator(toDecorate, webCutOptions.getWebHeaderToCut(),
                        webCutOptions.getWebFooterToCut())
        );
    }

    private ShootingStrategy decorateWithScrollbarHiding(ShootingStrategy strategy,
            Optional<WebElement> scrollableElement)
    {
        return new ScrollbarHidingDecorator(strategy, scrollableElement, scrollbarHandler);
    }

    private AShot createAShot(String strategyName)
    {
        ShootingStrategy baseShootingStrategy = getBaseShootingStrategy();
        ShootingStrategy shootingStrategy;
        CoordsProvider coordsProvider;
        switch (strategyName)
        {
            case "SIMPLE":
                shootingStrategy = baseShootingStrategy;
                coordsProvider = CeilingJsCoordsProvider.getSimple(javascriptActions);
                break;
            case "VIEWPORT_PASTING":
                shootingStrategy = new DebuggingViewportPastingDecorator(baseShootingStrategy,
                        DEFAULT_STICKY_HEADER_HEIGHT, DEFAULT_STICKY_FOOTER_HEIGHT)
                        .withScrollTimeout(SCROLL_TIMEOUT);
                coordsProvider = CeilingJsCoordsProvider.getScrollAdjusted(javascriptActions);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Unknown shooting strategy with the name: %s", strategyName));
        }
        shootingStrategy = decorateWithScrollbarHiding(shootingStrategy, Optional.empty());
        return new AShot().shootingStrategy(shootingStrategy)
                .coordsProvider(new ScrollBarHidingCoordsProviderDecorator(coordsProvider, scrollbarHandler));
    }

    private ShootingStrategy getBaseShootingStrategy()
    {
        return ShootingStrategies.scaling((float) javascriptActions.getDevicePixelRatio());
    }
}
