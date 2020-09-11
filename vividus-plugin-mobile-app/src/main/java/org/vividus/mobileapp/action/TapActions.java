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

package org.vividus.mobileapp.action;

import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.ElementOption.element;

import java.time.Duration;
import java.util.function.UnaryOperator;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverUtil;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.ElementOption;

public class TapActions
{
    private final IWebDriverProvider webDriverProvider;

    public TapActions(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Taps on the <b>element</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>press on the element</li>
     * <li>release</li>
     * </ol>
     * @param element element to tap, must not be {@code null}
     */
    public void tap(WebElement element)
    {
        performAction(b -> b.tap(elementOption(element)));
    }

    /**
     * Taps on the <b>element</b> with specified <b>duration</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>press on the element</li>
     * <li>wait for the duration</li>
     * <li>release</li>
     * </ol>
     * @param element element to tap, must not be {@code null}
     * @param duration between an element is pressed and released in
     * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format, must not be {@code null}
     */
    public void tap(WebElement element, Duration duration)
    {
        performAction(b -> b.press(elementOption(element))
                            .waitAction(waitOptions(duration))
                            .release());
    }

    private void performAction(UnaryOperator<TouchAction<?>> actionBuilder)
    {
        PerformsTouchActions performsTouchActions = webDriverProvider.getUnwrapped(PerformsTouchActions.class);
        TouchAction<?> touchAction = new TouchAction<>(performsTouchActions);
        actionBuilder.apply(touchAction).perform();
    }

    private static ElementOption elementOption(WebElement webElement)
    {
        return element(WebDriverUtil.unwrap(webElement, RemoteWebElement.class));
    }
}
