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

package org.vividus.selenium.driver;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.interactions.TouchScreen;

public class DelegatingWebDriver implements WebDriver, JavascriptExecutor, TakesScreenshot,
        WrapsDriver, HasInputDevices, HasTouchScreen, HasCapabilities, Interactive
{
    private static final String JAVASCRIPT_NOT_SUPPORTED = "Underlying driver instance does not support executing"
            + " javascript";
    private static final String SCREENSHOTS_NOT_SUPPORTED = "Underlying driver instance does not support taking"
            + " screenshots";
    private static final String ADVANCED_INTERACTION_NOT_SUPPORTED = "Underlying driver does not implement advanced"
            + " user interactions yet.";
    private static final String DRIVER_NOT_IMPLEMENT_HAS_CAPABILITIES = "Wrapped WebDriver doesn't implement"
            + " HasCapabilities interface";

    private final WebDriver wrappedDriver;

    public DelegatingWebDriver(WebDriver webDriver)
    {
        wrappedDriver = webDriver;
    }

    @Override
    public void get(String url)
    {
        wrappedDriver.get(url);
    }

    @Override
    public String getCurrentUrl()
    {
        return wrappedDriver.getCurrentUrl();
    }

    @Override
    public String getTitle()
    {
        return wrappedDriver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by)
    {
        return wrappedDriver.findElements(by);
    }

    @Override
    public WebElement findElement(By by)
    {
        return wrappedDriver.findElement(by);
    }

    @Override
    public String getPageSource()
    {
        return wrappedDriver.getPageSource();
    }

    @Override
    public void close()
    {
        wrappedDriver.close();
    }

    @Override
    public void quit()
    {
        wrappedDriver.quit();
    }

    @Override
    public Set<String> getWindowHandles()
    {
        return wrappedDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle()
    {
        return wrappedDriver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo()
    {
        return wrappedDriver.switchTo();
    }

    @Override
    public Navigation navigate()
    {
        return wrappedDriver.navigate();
    }

    @Override
    public Options manage()
    {
        return wrappedDriver.manage();
    }

    @Override
    public WebDriver getWrappedDriver()
    {
        return wrappedDriver;
    }

    @Override
    public TouchScreen getTouch()
    {
        if (wrappedDriver instanceof HasTouchScreen)
        {
            return ((HasTouchScreen) wrappedDriver).getTouch();
        }
        throw new UnsupportedOperationException(ADVANCED_INTERACTION_NOT_SUPPORTED);
    }

    @Override
    public Keyboard getKeyboard()
    {
        if (wrappedDriver instanceof HasInputDevices)
        {
            return ((HasInputDevices) wrappedDriver).getKeyboard();
        }
        throw new UnsupportedOperationException(ADVANCED_INTERACTION_NOT_SUPPORTED);
    }

    @Override
    public Mouse getMouse()
    {
        if (wrappedDriver instanceof HasInputDevices)
        {
            return ((HasInputDevices) wrappedDriver).getMouse();
        }
        throw new UnsupportedOperationException(ADVANCED_INTERACTION_NOT_SUPPORTED);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException
    {
        if (wrappedDriver instanceof TakesScreenshot)
        {
            return ((TakesScreenshot) wrappedDriver).getScreenshotAs(target);
        }
        throw new UnsupportedOperationException(SCREENSHOTS_NOT_SUPPORTED);
    }

    @Override
    public Object executeScript(String script, Object... args)
    {
        if (wrappedDriver instanceof JavascriptExecutor)
        {
            return ((JavascriptExecutor) wrappedDriver).executeScript(script, args);
        }
        throw new UnsupportedOperationException(JAVASCRIPT_NOT_SUPPORTED);
    }

    @Override
    public Object executeAsyncScript(String script, Object... args)
    {
        if (wrappedDriver instanceof JavascriptExecutor)
        {
            return ((JavascriptExecutor) wrappedDriver).executeAsyncScript(script, args);
        }
        throw new UnsupportedOperationException(JAVASCRIPT_NOT_SUPPORTED);
    }

    @Override
    public Capabilities getCapabilities()
    {
        if (wrappedDriver instanceof HasCapabilities)
        {
            return ((HasCapabilities) wrappedDriver).getCapabilities();
        }
        throw new IllegalStateException(DRIVER_NOT_IMPLEMENT_HAS_CAPABILITIES);
    }

    @Override
    public void perform(Collection<Sequence> actions)
    {
        if (wrappedDriver instanceof Interactive)
        {
            ((Interactive) wrappedDriver).perform(actions);
            return;
        }
        throw new UnsupportedOperationException(ADVANCED_INTERACTION_NOT_SUPPORTED);
    }

    @Override
    public void resetInputState()
    {
        if (wrappedDriver instanceof Interactive)
        {
            ((Interactive) wrappedDriver).resetInputState();
            return;
        }
        throw new UnsupportedOperationException(ADVANCED_INTERACTION_NOT_SUPPORTED);
    }
}
