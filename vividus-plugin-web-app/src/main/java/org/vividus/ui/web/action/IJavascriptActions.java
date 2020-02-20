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

package org.vividus.ui.web.action;

import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

public interface IJavascriptActions
{
    <T> T executeScript(String script, Object... args);

    /**
     * Loads script from resource, executes it and returns result
     * @param <T> the type of the returned result
     * @param clazz Class to search resource relatively
     * @param jsResourceName script input parameters
     * @param args script input parameters
     * @return result of the script execution
     */
    <T> T executeScriptFromResource(Class<?> clazz, String jsResourceName, Object... args);

    <T> T executeAsyncScript(String script, Object... args);

    /**
     * Loads an asynchronous script from resource, executes it and returns result
     * @param <T> the type of returned result
     * @param clazz Class to search resource relatively
     * @param jsResourceName script input parameters
     * @param args script input parameters
     * @return result of the script execution
     */
    <T> T executeAsyncScriptFromResource(Class<?> clazz, String jsResourceName, Object... args);

    void scrollIntoView(WebElement webElement, boolean alignedToTheTop);

    void scrollElementIntoViewportCenter(WebElement webElement);

    /**
     * Scrolls to the end of the page with dynamically loading content upon scrolling
     */
    void scrollToEndOfPage();

    /**
     * Scrolls page to the top (0, 0)
     */
    void scrollToStartOfPage();

    void scrollToEndOf(WebElement element);

    void scrollToStartOf(WebElement element);

    void scrollToLeftOf(WebElement element);

    void scrollToRightOf(WebElement element);

    /**
     * Opens page URL in a new window
     * @param pageUrl An absolute URL of the page
     */
    void openPageUrlInNewWindow(String pageUrl);

    void triggerMouseEvents(WebElement webElement, String... eventTypes);

    String getPageText();

    String getElementText(WebElement webElement);

    String getElementValue(WebElement webElement);

    String getUserAgent();

    double getDevicePixelRatio();

    Map<String, String> getElementAttributes(WebElement webElement);

    /**
     * Sets the top position of a positioned element.
     * @param top specifies the top position of the element including padding, scrollbar, border and margin
     *  for a given WebElement
     * @param webElement WebElement to set top for
     * @return origin top position of webElement
     */
    int setElementTopPosition(WebElement webElement, int top);

    Dimension getViewportSize();
}
