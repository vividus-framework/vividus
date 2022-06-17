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

package org.vividus.ui.web.screenshot;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.AbstractScreenshotParametersFactory;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotPrecondtionMismatchException;

public class WebScreenshotParametersFactory
        extends AbstractScreenshotParametersFactory<WebScreenshotConfiguration, WebScreenshotParameters>
{
    private final ISearchActions searchActions;

    public WebScreenshotParametersFactory(ISearchActions searchActions)
    {
        this.searchActions = searchActions;
    }

    @Override
    public Optional<ScreenshotParameters> create(Optional<WebScreenshotConfiguration> screenshotConfiguration)
    {
        return getScreenshotConfiguration(screenshotConfiguration, (c, b) -> c).map(
                cfg -> configure(cfg, createWithBaseConfiguration(cfg)));
    }

    @Override
    public Optional<ScreenshotParameters> create(Map<IgnoreStrategy, Set<Locator>> ignores)
    {
        WebScreenshotConfiguration configuration = getDefaultConfiguration().orElseGet(WebScreenshotConfiguration::new);
        return Optional.of(configure(configuration, createWithBaseConfiguration(configuration, ignores)));
    }

    @Override
    protected WebScreenshotParameters createScreenshotParameters()
    {
        return new WebScreenshotParameters();
    }

    private ScreenshotParameters configure(WebScreenshotConfiguration config, WebScreenshotParameters parameters)
    {
        parameters.setNativeHeaderToCut(ensureValidCutSize(config.getNativeHeaderToCut(), "native header"));

        WebCutOptions webCutOptions = new WebCutOptions(
                ensureValidCutSize(config.getWebHeaderToCut(), "web header"),
                ensureValidCutSize(config.getWebFooterToCut(), "web footer")
        );
        parameters.setWebCutOptions(webCutOptions);

        config.getScrollableElement().ifPresent(locator ->
        {
            WebElement scrollableElement = searchActions.findElement(locator).orElseThrow(
                () -> new ScreenshotPrecondtionMismatchException("Scrollable element does not exist"));
            parameters.setScrollableElement(Optional.of(scrollableElement));
        });

        parameters.setCoordsProvider(config.getCoordsProvider());
        parameters.setScrollTimeout(config.getScrollTimeout());

        return parameters;
    }
}
