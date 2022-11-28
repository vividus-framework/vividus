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

package org.vividus.selenium.mobileapp.screenshot.strategies;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;

import pazone.ashot.ImageReadException;
import pazone.ashot.SimpleShootingStrategy;
import pazone.ashot.util.ImageTool;

@SuppressWarnings("serial")
public class MobileViewportShootingStrategy extends SimpleShootingStrategy
{
    @Override
    public BufferedImage getScreenshot(WebDriver wd)
    {
        // Get screenshot without status bar
        String base64Png = (String) ((JavascriptExecutor) wd).executeScript("mobile:viewportScreenshot");
        // https://github.com/SeleniumHQ/selenium/issues/11168
        String rfc4648Base64 = base64Png.replaceAll("\\r?\\n", "");
        byte[] bytes = OutputType.BYTES.convertFromBase64Png(rfc4648Base64);
        try
        {
            return ImageTool.toBufferedImage(bytes);
        }
        catch (IOException e)
        {
            throw new ImageReadException("Can not parse screenshot data", e);
        }
    }
}
