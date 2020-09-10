/*
 * Copyright 2019-2020 the original author or authors.
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
import java.util.function.Supplier;

import javax.inject.Inject;

import com.google.common.base.Suppliers;

import org.openqa.selenium.ScreenOrientation;
import org.vividus.selenium.IWebDriverFactory;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.action.IJavascriptActions;

import io.appium.java_client.remote.MobileCapabilityType;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.FixedCutStrategy;

public class AshotFactory implements IAshotFactory
{
    private ScreenshotShootingStrategy screenshotShootingStrategy;

    @Inject private IWebDriverFactory webDriverFactory;
    @Inject private IWebDriverManager webDriverManager;
    @Inject private IJavascriptActions javascriptActions;
    @Inject private ScreenshotDebugger screenshotDebugger;

    private final Supplier<ShootingStrategy> baseShootingStrategy = Suppliers.memoize(
        () -> ShootingStrategies.scaling((float) javascriptActions.getDevicePixelRatio()));

    @Override
    public AShot create(boolean viewportScreenshot, Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        return screenshotConfiguration.map(
            ashotConfiguration -> ashotConfiguration.getScreenshotShootingStrategy()
                    .map(sss -> createAShot(baseShootingStrategy.get(), sss, viewportScreenshot))
                    .orElseGet(() -> createAshot(ashotConfiguration)))
                .orElseGet(() -> createAShot(baseShootingStrategy.get(), viewportScreenshot));
    }

    private AShot createAshot(ScreenshotConfiguration screenshotConfiguration)
    {
        ShootingStrategy decorated = baseShootingStrategy.get();

        int nativeFooterToCut = screenshotConfiguration.getNativeFooterToCut();
        int nativeHeaderToCut = screenshotConfiguration.getNativeHeaderToCut();
        if (anyNotZero(nativeFooterToCut, nativeHeaderToCut))
        {
            decorated = cutting(baseShootingStrategy.get(), new FixedCutStrategy(nativeHeaderToCut, nativeFooterToCut));
        }

        int footerToCut = screenshotConfiguration.getWebFooterToCut();
        int headerToCut = screenshotConfiguration.getWebHeaderToCut();
        if (anyNotZero(footerToCut, headerToCut))
        {
            decorated = cutting(decorated, new StickyHeaderCutStrategy(headerToCut, footerToCut));
        }

        decorated = ((DebuggingViewportPastingDecorator) decorateWithViewportPasting(decorated,
                screenshotConfiguration))
                .withDebugger(screenshotDebugger);

        return new AShot().shootingStrategy(decorated)
                .coordsProvider(screenshotConfiguration.getCoordsProvider().create(javascriptActions));
    }

    private ShootingStrategy decorateWithViewportPasting(ShootingStrategy toDecorate,
            ScreenshotConfiguration screenshotConfiguration)
    {
        return ((DebuggingViewportPastingDecorator) screenshotConfiguration.getScrollableElement().get()
                       .map(e -> (ShootingStrategy) new AdjustingScrollableElementAwareViewportPastingDecorator(
                               toDecorate, e, javascriptActions, screenshotConfiguration))
                       .orElseGet(() ->
                       new AdjustingViewportPastingDecorator(toDecorate, screenshotConfiguration.getWebHeaderToCut(),
                               screenshotConfiguration.getWebFooterToCut())))
                       .withScrollTimeout(((Long) screenshotConfiguration.getScrollTimeout().toMillis()).intValue());
    }

    private AShot createAShot(ShootingStrategy baseShootingStrategy, boolean viewportScreenshot)
    {
        return createAShot(baseShootingStrategy, screenshotShootingStrategy, viewportScreenshot);
    }

    private AShot createAShot(ShootingStrategy baseShootingStrategy,
            ScreenshotShootingStrategy screenshotShootingStrategy, boolean viewportScreenshot)
    {
        String deviceName = webDriverFactory.getCapability(MobileCapabilityType.DEVICE_NAME, false);
        boolean landscapeOrientation = webDriverManager.isOrientation(ScreenOrientation.LANDSCAPE);
        ShootingStrategy shootingStrategy = screenshotShootingStrategy.getDecoratedShootingStrategy(
                baseShootingStrategy, viewportScreenshot, landscapeOrientation, deviceName);
        return new AShot().shootingStrategy(shootingStrategy)
                .coordsProvider(screenshotShootingStrategy == ScreenshotShootingStrategy.SIMPLE
                        ? CeilingJsCoordsProvider.getSimple(javascriptActions)
                        : CeilingJsCoordsProvider.getScrollAdjusted(javascriptActions));
    }

    private boolean anyNotZero(int first, int second)
    {
        return first > 0 || second > 0;
    }

    public void setScreenshotShootingStrategy(ScreenshotShootingStrategy screenshotShootingStrategy)
    {
        this.screenshotShootingStrategy = screenshotShootingStrategy;
    }
}
