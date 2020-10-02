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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.vividus.selenium.IWebDriverProvider;

public abstract class AbstractScreenshotTaker implements ScreenshotTaker
{
    private final IWebDriverProvider webDriverProvider;

    public AbstractScreenshotTaker(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    @Override
    public BufferedImage takeViewportScreenshot() throws IOException
    {
        try (InputStream inputStream = new ByteArrayInputStream(getScreenshotBytes()))
        {
            return ImageIO.read(inputStream);
        }
    }

    protected byte[] getScreenshotBytes()
    {
        return webDriverProvider.getUnwrapped(TakesScreenshot.class).getScreenshotAs(OutputType.BYTES);
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }
}
