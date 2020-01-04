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

package org.vividus.bdd.steps.ui;

import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.jbehave.core.annotations.When;
import org.vividus.selenium.screenshot.IScreenshotTaker;

public class ScreenshotTakingSteps
{
    @Inject private IScreenshotTaker screenshotTaker;

    /**
     * Takes a screenshot and saves it to the default location
     * @throws IOException If an input or output exception occurred
     */
    @When("I take a screenshot")
    public void whenITakeScreenshot() throws IOException
    {
        screenshotTaker.takeScreenshotAsFile("Step_Screenshot");
    }

    /**
     * Takes a screenshot and saves it by the specified <b>path</b>
     * <br>
     * <i>Possible values:</i>
     * <ul>
     * <li>An <b>absolute</b> path contains the root directory and all other
     * subdirectories that contain a file or folder.
     * (<i>C:\Windows\screenshot.bmp</i>)
     * <li>A <b>relative</b> path starts from some given working directory. (<i>screenshot.bmp</i>)
     * </ul>
     * @param path Path to the location for saving the screenshot
     * @throws IOException If an input or output exception occurred
     */
    @When("I take a screenshot to '$path'")
    public void whenITakeScreenshotToPath(String path) throws IOException
    {
        screenshotTaker.takeScreenshot(Paths.get(path));
    }
}
