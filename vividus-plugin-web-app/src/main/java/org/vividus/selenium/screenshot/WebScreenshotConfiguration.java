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

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import org.openqa.selenium.WebElement;

public class WebScreenshotConfiguration extends ScreenshotConfiguration
{
    public static final int SCROLL_TIMEOUT = 500;
    private int nativeHeaderToCut;
    private int webHeaderToCut;
    private int webFooterToCut;
    private Supplier<Optional<WebElement>> scrollableElement = Optional::empty;
    private CoordsProviderType coordsProvider = CoordsProviderType.CEILING;
    private Duration scrollTimeout = Duration.ofMillis(SCROLL_TIMEOUT);

    public int getWebHeaderToCut()
    {
        return webHeaderToCut;
    }

    public void setWebHeaderToCut(int webHeaderToCut)
    {
        this.webHeaderToCut = webHeaderToCut;
    }

    public int getWebFooterToCut()
    {
        return webFooterToCut;
    }

    public void setWebFooterToCut(int webFooterToCut)
    {
        this.webFooterToCut = webFooterToCut;
    }

    public Supplier<Optional<WebElement>> getScrollableElement()
    {
        return scrollableElement;
    }

    public void setScrollableElement(Supplier<Optional<WebElement>> scrollableElement)
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
