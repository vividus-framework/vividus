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
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.BrowserWindowSize;
import org.vividus.selenium.IBrowserWindowSizeManager;
import org.vividus.softassert.ISoftAssert;

@TakeScreenshotOnFailure
public class WindowSteps
{
    private final IBrowserWindowSizeManager browserWindowSizeManager;
    private final ISoftAssert softAssert;

    public WindowSteps(IBrowserWindowSizeManager browserWindowSizeManager, ISoftAssert softAssert)
    {
        this.browserWindowSizeManager = browserWindowSizeManager;
        this.softAssert = softAssert;
    }

    /**
     * Change current browser window size to the specified one.
     * @param targetSize Any browser window size in pixels, e.g. 800x600.
     *                   Note: browser window size should be smaller than the current screen resolution.
     */
    @When("I change window size to `$targetSize`")
    public void resizeCurrentWindow(BrowserWindowSize targetSize)
    {
        browserWindowSizeManager.resizeBrowserWindow(targetSize, screenSize ->
        {
            boolean meetScreenBorders = targetSize.getWidth() <= screenSize.getWidth()
                    && targetSize.getHeight() <= screenSize.getHeight();
            return softAssert.assertTrue(
                    String.format("The desired browser window size %s fits the screen size (%dx%d)",
                            targetSize, screenSize.getWidth(), screenSize.getHeight()), meetScreenBorders);
        }, window -> window.setSize(targetSize.toDimension()));
    }
}
