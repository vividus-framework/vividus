/*
 * Copyright 2019-2026 the original author or authors.
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

import org.vividus.selenium.screenshot.strategies.AdjustingScrollableElementAwareViewportPastingDecorator;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;
import org.vividus.ui.web.screenshot.WebScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.CdpShootingStrategy;
import pazone.ashot.ShootingStrategies;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.CoordsProvider;

public class WebAshotFactory extends AbstractAshotFactory<WebScreenshotParameters>
{
    public static final int DEFAULT_STICKY_HEADER_HEIGHT = 100;
    public static final int DEFAULT_STICKY_FOOTER_HEIGHT = 100;
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
        return screenshotParameters
                .map(ashotParameters -> ashotParameters.getShootingStrategy()
                        .map(strategy -> createAShot(strategy, ashotParameters))
                        .orElseGet(() -> createAShot(ashotParameters)))
                .orElseGet(() -> createAShot(getScreenshotShootingStrategy(), null));
    }

    private AShot createAShot(WebScreenshotParameters screenshotParameters)
    {
        ShootingStrategy decorated = getBaseShootingStrategy(screenshotParameters);

        decorated = decorateWithViewportPasting(decorated, screenshotParameters)
                .withDebugger(screenshotDebugger)
                .withScrollTimeout(((Long) screenshotParameters.getScrollTimeout().toMillis()).intValue());

        decorated = decorateWithScrollbarHiding(decorated, screenshotParameters);

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
                        toDecorate, e, javascriptActions, webCutOptions, screenshotParameters.getMaxHeight())
                ).orElseGet(
                () -> new DebuggingViewportPastingDecorator(toDecorate, webCutOptions.webHeaderToCut(),
                        webCutOptions.webFooterToCut(), screenshotParameters.getMaxHeight())
        );
    }

    private ShootingStrategy decorateWithScrollbarHiding(ShootingStrategy strategy, WebScreenshotParameters params)
    {
        if (params == null)
        {
            return new ScrollbarHidingDecorator(strategy, Optional.empty(), scrollbarHandler);
        }
        return params.isHideScrollbars()
                ? new ScrollbarHidingDecorator(strategy, params.getScrollableElement(), scrollbarHandler)
                : strategy;
    }

    private AShot createAShot(String strategyName, WebScreenshotParameters screenshotParameters)
    {
        if ("CDP".equals(strategyName))
        {
            ShootingStrategy shootingStrategy = decorateBaseShootingStrategy(new CdpShootingStrategy(),
                    screenshotParameters);
            shootingStrategy = decorateStrategyWithCropping(shootingStrategy, screenshotParameters);

            return new AShot().shootingStrategy(shootingStrategy);
        }

        ShootingStrategy baseShootingStrategy = getBaseShootingStrategy(screenshotParameters);
        ShootingStrategy shootingStrategy;
        @SuppressWarnings("checkstyle:Indentation")
        CoordsProvider coordsProvider = switch (strategyName)
        {
            case "SIMPLE" ->
            {
                shootingStrategy = baseShootingStrategy;
                yield CeilingJsCoordsProvider.getSimple(javascriptActions);
            }
            case "VIEWPORT_PASTING" ->
            {
                shootingStrategy = new DebuggingViewportPastingDecorator(baseShootingStrategy,
                        DEFAULT_STICKY_HEADER_HEIGHT, DEFAULT_STICKY_FOOTER_HEIGHT, 0)
                                .withScrollTimeout(SCROLL_TIMEOUT);
                yield CeilingJsCoordsProvider.getScrollAdjusted(javascriptActions);
            }
            default -> throw new IllegalArgumentException(
                    String.format("Unknown shooting strategy with the name: %s", strategyName));
        };
        shootingStrategy = decorateWithScrollbarHiding(shootingStrategy, screenshotParameters);
        shootingStrategy = decorateStrategyWithCropping(shootingStrategy, screenshotParameters);

        return new AShot().shootingStrategy(shootingStrategy)
                .coordsProvider(new ScrollBarHidingCoordsProviderDecorator(coordsProvider, scrollbarHandler));
    }

    private ShootingStrategy getBaseShootingStrategy(WebScreenshotParameters screenshotParameters)
    {
        return decorateBaseShootingStrategy(ShootingStrategies.simple(), screenshotParameters);
    }

    private ShootingStrategy decorateBaseShootingStrategy(ShootingStrategy baseShootingStrategy,
            WebScreenshotParameters screenshotParameters)
    {
        ShootingStrategy shootingStrategy = ShootingStrategies.scaling(baseShootingStrategy,
                (float) javascriptActions.getDevicePixelRatio());
        if (screenshotParameters != null)
        {
            shootingStrategy = decorateWithFixedCutStrategy(shootingStrategy,
                    screenshotParameters.getNativeHeaderToCut(), screenshotParameters.getNativeFooterToCut());
        }
        return shootingStrategy;
    }

    private ShootingStrategy decorateStrategyWithCropping(ShootingStrategy shootingStrategy,
            WebScreenshotParameters screenshotParameters)
    {
        return screenshotParameters == null ? shootingStrategy
                : decorateWithCropping(shootingStrategy, screenshotParameters);
    }
}
