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

import java.time.Duration;
import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.CoordsProviderType;
import org.vividus.ui.screenshot.ScreenshotParameters;

public class WebScreenshotParameters extends ScreenshotParameters
{
    private int nativeHeaderToCut;
    private int nativeFooterToCut;
    private WebCutOptions webCutOptions;
    private Optional<WebElement> scrollableElement = Optional.empty();
    private CoordsProviderType coordsProvider;
    private Duration scrollTimeout;

    public WebCutOptions getWebCutOptions()
    {
        return webCutOptions;
    }

    public void setWebCutOptions(WebCutOptions webCutOptions)
    {
        this.webCutOptions = webCutOptions;
    }

    public Optional<WebElement> getScrollableElement()
    {
        return scrollableElement;
    }

    public void setScrollableElement(Optional<WebElement> scrollableElement)
    {
        this.scrollableElement = scrollableElement;
    }

    public CoordsProviderType getCoordsProvider()
    {
        return coordsProvider;
    }

    public void setCoordsProvider(CoordsProviderType coordsProvider)
    {
        this.coordsProvider = coordsProvider;
    }

    public Duration getScrollTimeout()
    {
        return scrollTimeout;
    }

    public void setScrollTimeout(Duration scrollTimeout)
    {
        this.scrollTimeout = scrollTimeout;
    }

    public int getNativeHeaderToCut()
    {
        return nativeHeaderToCut;
    }

    public void setNativeHeaderToCut(int nativeHeaderToCut)
    {
        this.nativeHeaderToCut = nativeHeaderToCut;
    }

    public int getNativeFooterToCut()
    {
        return nativeFooterToCut;
    }

    public void setNativeFooterToCut(int nativeFooterToCut)
    {
        this.nativeFooterToCut = nativeFooterToCut;
    }
}
