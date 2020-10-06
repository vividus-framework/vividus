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

import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;

import io.appium.java_client.HidesKeyboard;

public class KeyboardActions
{
    private final IWebDriverProvider webDriverProvider;

    public KeyboardActions(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Type <b>text</b> into the <b>element</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>type text into the element</li>
     * <li>hide keyboard</li>
     * </ol>
     * @param element element to type text, must not be {@code null}
     * @param text text to type into the element, must not be {@code null}
     */
    public void typeText(WebElement element, String text)
    {
        element.sendKeys(text);
        webDriverProvider.getUnwrapped(HidesKeyboard.class).hideKeyboard();
    }
}
