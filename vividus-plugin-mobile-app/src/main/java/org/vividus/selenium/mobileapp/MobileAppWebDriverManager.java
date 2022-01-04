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
import org.openqa.selenium.remote.Response;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.ui.action.JavascriptActions;

import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.android.HasAndroidDeviceDetails;

public class MobileAppWebDriverManager extends GenericWebDriverManager
{
    private static final String HEIGHT = "height";

    private final JavascriptActions javascriptActions;
    private final Supplier<Float> dpr = Suppliers.memoize(this::calculateDpr);

    public MobileAppWebDriverManager(IWebDriverProvider webDriverProvider,
            IWebDriverManagerContext webDriverManagerContext, JavascriptActions javascriptActions)
    {
        super(webDriverProvider, webDriverManagerContext);
        this.javascriptActions = javascriptActions;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSessionDetail(String detail)
    {
        Response response = getUnwrappedDriver(ExecutesMethod.class).execute("getSession");
        Map<String, Object> sessionDetails = (Map<String, Object>) response.getValue();
        return (T) sessionDetails.get(detail);
    }

    @SuppressWarnings("unchecked")
    public int getStatusBarSize()
    {
        if (isTvOS())
        {
            return 0;
        }
        if (isIOSNativeApp())
        {
            Number statBarHeight;
            try
            {
                statBarHeight = getStatBarHeightUnsafely();
            }
            catch (WebDriverException e)
            {
                // Handling SauceLabs error:
                // org.openqa.selenium.WebDriverException:
                // failed serving request GET https://production-sushiboat.default/wd/hub/session/XXXX
                statBarHeight = null;
            }
            if (null == statBarHeight)
            {
                // Appium 1.21.0 or higher is required
                Map<String, ?> deviceScreenInfo = javascriptActions.executeScript("mobile:deviceScreenInfo");
                Map<String, ?> statusBarSize = (Map<String, ?>) deviceScreenInfo.get("statusBarSize");
                statBarHeight = (Number) statusBarSize.get(HEIGHT);
            }
            return statBarHeight.intValue();
        }
        return getAndroidStatusBar();
    }

    private int getAndroidStatusBar()
    {
        try
        {
            HasAndroidDeviceDetails details = getUnwrappedDriver(HasAndroidDeviceDetails.class);
            return Optional.ofNullable(details.getSystemBars())
                           .map(b -> b.get("statusBar"))
                           .map(sb -> sb.get(HEIGHT))
                           .map(Long.class::cast)
                           .map(Long::intValue)
                           .orElse(0);
        }
        catch (WebDriverException e)
        {
            // The workaround is for Android TV. It is not clear if any of the Android TVs could have a status bar
            // at all, but the session capabilities contains `statBarHeight`, and to be on the safe side fall-back code
            // was added to get the status bar height in case of exception.
            return getStatBarHeightSafely();
        }
    }

    public double getDpr()
    {
        return dpr.get();
    }

    private float calculateDpr()
    {
        byte[] imageBytes = getUnwrappedDriver(TakesScreenshot.class).getScreenshotAs(OutputType.BYTES);
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

    private int getStatBarHeightSafely()
    {
        Long statBarHeight = getStatBarHeightUnsafely();
        if (statBarHeight == null)
        {
            throw new IllegalStateException("Unable to receive status bar height. Received value is null.");
        }
        return statBarHeight.intValue();
    }

    private Long getStatBarHeightUnsafely()
    {
        return getSessionDetail("statBarHeight");
    }
}
