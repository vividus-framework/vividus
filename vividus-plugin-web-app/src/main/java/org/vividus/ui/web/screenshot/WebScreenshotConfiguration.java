/*
 * Copyright 2019-2024 the original author or authors.
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

import static org.vividus.selenium.screenshot.WebAshotFactory.DEFAULT_STICKY_FOOTER_HEIGHT;
import static org.vividus.selenium.screenshot.WebAshotFactory.DEFAULT_STICKY_HEADER_HEIGHT;

import java.time.Duration;
import java.util.Optional;

import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.screenshot.CoordsProviderType;
import org.vividus.ui.screenshot.ScreenshotConfiguration;

public class WebScreenshotConfiguration extends ScreenshotConfiguration
{
    private int maxHeight;
    private int nativeHeaderToCut;
    private int nativeFooterToCut;
    private int webHeaderToCut = DEFAULT_STICKY_HEADER_HEIGHT;
    private int webFooterToCut = DEFAULT_STICKY_FOOTER_HEIGHT;
    private Optional<Locator> scrollableElement = Optional.empty();
    private CoordsProviderType coordsProvider = CoordsProviderType.CEILING;
    @SuppressWarnings("MagicNumber")
    private Duration scrollTimeout = Duration.ofMillis(500);
    private boolean hideScrollbars = true;

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    public int getWebHeaderToCut()
    {
        return webHeaderToCut;
    }

    public void setWebHeaderToCut(int webHeaderToCut)
    {
        this.webHeaderToCut = webHeaderToCut;
    }

    public int getNativeFooterToCut()
    {
        return nativeFooterToCut;
    }

    public void setNativeFooterToCut(int nativeFooterToCut)
    {
        this.nativeFooterToCut = nativeFooterToCut;
    }

    public int getWebFooterToCut()
    {
        return webFooterToCut;
    }

    public void setWebFooterToCut(int webFooterToCut)
    {
        this.webFooterToCut = webFooterToCut;
    }

    public Optional<Locator> getScrollableElement()
    {
        return scrollableElement;
    }

    public void setScrollableElement(Optional<Locator> scrollableElement)
    {
        this.scrollableElement = scrollableElement;
    }

    public CoordsProviderType getCoordsProvider()
    {
        return coordsProvider;
    }

    public void setCoordsProvider(String coordsProvider)
    {
        this.coordsProvider = CoordsProviderType.valueOf(coordsProvider);
    }

    public Duration getScrollTimeout()
    {
        return scrollTimeout;
    }

    public void setScrollTimeout(String scrollTimeout)
    {
        this.scrollTimeout = Duration.parse(scrollTimeout);
    }

    public int getNativeHeaderToCut()
    {
        return nativeHeaderToCut;
    }

    protected void setNativeHeaderToCut(int nativeHeaderToCut)
    {
        this.nativeHeaderToCut = nativeHeaderToCut;
    }

    public boolean isHideScrollbars()
    {
        return hideScrollbars;
    }

    public void setHideScrollbars(boolean hideScrollbars)
    {
        this.hideScrollbars = hideScrollbars;
    }
}
