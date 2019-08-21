/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.ui.web.action;

import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.vividus.ui.web.action.AlertActions.Action;

public interface IMouseActions
{
    ClickResult click(WebElement element);

    ClickResult click(WrapsElement element);

    void moveToAndClick(WebElement element);

    ClickResult clickViaJavascript(WebElement element);

    /**
     * Clicks on element and process alert with specified action if it occurs
     * @param element web element to click on
     * @param defaultAlertAction action to process alert by default
     * @return ClickResult
     */
    ClickResult click(WebElement element, Optional<Action> defaultAlertAction);

    void moveToElement(WebElement element);

    void contextClick(WebElement element);
}
