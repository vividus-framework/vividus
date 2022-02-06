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

package org.vividus.steps.ui.web;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.Dimension;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;

@TakeScreenshotOnFailure
public class WindowSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManager webDriverManager;
    private final ISoftAssert softAssert;

    public WindowSteps(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager, ISoftAssert softAssert)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
        this.softAssert = softAssert;
    }

    /**
     * Changes the current browser window size to the specified one.
     * NOTE: The specified browser window size should be smaller than the current screen resolution.
     *
     * @param targetSize The desired browser window size in pixels, e.g. `800x600`,
     *                   where the first measure is window width, the last one is window height.
     */
    @When("I change window size to `$targetSize`")
    public void resizeCurrentWindow(Dimension targetSize)
    {
        if (canTryToResize(targetSize))
        {
            webDriverProvider.get().manage().window().setSize(targetSize);
        }
    }

    private boolean canTryToResize(Dimension targetSize)
    {
        return webDriverManager.checkWindowFitsScreen(targetSize, (fitsScreen, size) -> {
            String assertionDescription = String.format(
                    "The desired browser window size %dx%d fits the screen size (%dx%d)", targetSize.getWidth(),
                    targetSize.getHeight(), size.getWidth(), size.getHeight());
            softAssert.assertTrue(assertionDescription, fitsScreen);
        }).orElse(true);
    }
}
