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

package org.vividus.selenium;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.openqa.selenium.WebDriver.Window;

public interface IBrowserWindowSizeManager
{
    /**
     * Establishes the possible screen size and resizes the browser window to target size
     * if it suits the screen size, or performs provided actions on window.
     * @param targetSize Target browser window size;
     * @param screenSizeCheck Conditions to check expected size suits actual screen size (if graphical screen exists);
     * @param actionsIfNoScreen Actions to perform on browser window if actual screen size can not be established.
     */
    void resizeBrowserWindow(BrowserWindowSize targetSize, Predicate<BrowserWindowSize> screenSizeCheck,
                             Consumer<Window> actionsIfNoScreen);

    /**
     * Resizes the browser window to target size or performs provided actions on window.
     * @param targetSize Target browser window size;
     * @param actionsIfEmpty Actions to perform on browser window if target size is null.
     */
    void resizeBrowserWindow(BrowserWindowSize targetSize, Consumer<Window> actionsIfEmpty);
}
