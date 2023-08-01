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

package org.vividus.ui.web.screenshot;

import java.time.Duration;
import java.util.Optional;

import org.vividus.selenium.screenshot.CoordsProviderType;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.ScreenshotConfiguration;

public class WebScreenshotConfiguration extends ScreenshotConfiguration
{
    private int nativeHeaderToCut;
    private int nativeFooterToCut;
    private int webHeaderToCut;
    private int webFooterToCut;
    private Optional<Locator> scrollableElement = Optional.empty();
    private CoordsProviderType coordsProvider = CoordsProviderType.CEILING;
    @SuppressWarnings("MagicNumber")
    private Duration scrollTimeout = Duration.ofMillis(500);

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
}
