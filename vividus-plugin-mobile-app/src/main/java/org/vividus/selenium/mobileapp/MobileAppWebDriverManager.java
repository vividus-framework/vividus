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

package org.vividus.selenium.mobileapp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import com.google.common.base.Suppliers;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.selenium.manager.IWebDriverManagerContext;

import io.appium.java_client.CommandExecutionHelper;
import io.appium.java_client.HasSessionDetails;
import io.appium.java_client.android.AndroidMobileCommandHelper;
import io.appium.java_client.android.HasAndroidDeviceDetails;

public class MobileAppWebDriverManager extends GenericWebDriverManager
{
    private final Supplier<Float> dpr = Suppliers.memoize(this::calculateDpr);

    public MobileAppWebDriverManager(IWebDriverProvider webDriverProvider,
            IWebDriverManagerContext webDriverManagerContext)
    {
        super(webDriverProvider, webDriverManagerContext);
    }

    public int getStatusBarSize()
    {
        if (super.isTvOS())
        {
            return 0;
        }
        return  super.isIOSNativeApp() ? getStatBarHeight() : getAndroidStatusBar();
    }

    private int getAndroidStatusBar()
    {
        try
        {
            HasAndroidDeviceDetails details = getWebDriverProvider().getUnwrapped(HasAndroidDeviceDetails.class);
            // https://github.com/appium/java-client/commit/9020174c578bed5e03c24b43c2c5ba590f663201
            Map<String, Map<String, Object>>  systemBars = CommandExecutionHelper.execute(details,
                    AndroidMobileCommandHelper.getSystemBarsCommand());
            return Optional.ofNullable(systemBars)
                           .map(b -> b.get("statusBar"))
                           .map(sb -> sb.get("height"))
                           .map(Long.class::cast)
                           .map(Long::intValue)
                           .orElse(0);
        }
        catch (WebDriverException e)
        {
            // The workaround is for Android TV. It is not clear if any of the Android TVs could have a status bar
            // at all, but the session capabilities contains `statBarHeight`, and to be on the safe side fall-back code
            // was added to get the status bar height in case of exception.
            return getStatBarHeight();
        }
    }

    public double getDpr()
    {
        return dpr.get();
    }

    private float calculateDpr()
    {
        byte[] imageBytes = getWebDriverProvider().getUnwrapped(TakesScreenshot.class)
                .getScreenshotAs(OutputType.BYTES);
        try (InputStream is = new ByteArrayInputStream(imageBytes))
        {
            BufferedImage image = ImageIO.read(is);
            return image.getHeight() / (float) getSize().getHeight();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private int getStatBarHeight()
    {
        HasSessionDetails details = getWebDriverProvider().getUnwrapped(HasSessionDetails.class);
        return ((Long) details.getSessionDetail("statBarHeight")).intValue();
    }
}
