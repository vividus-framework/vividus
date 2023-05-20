/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.steps.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.eventbus.EventBus;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.screenshot.ScreenshotTaker;

public class ScreenshotTakingSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotTakingSteps.class);

    private final ScreenshotTaker screenshotTaker;
    private final EventBus eventBus;

    public ScreenshotTakingSteps(ScreenshotTaker screenshotTaker, EventBus eventBus)
    {
        this.screenshotTaker = screenshotTaker;
        this.eventBus = eventBus;
    }

    /**
     * Takes a screenshot and publish it to the report
     */
    @When("I take screenshot")
    public void takeScreenshot()
    {
        screenshotTaker.takeScreenshot("Step_Screenshot").ifPresent(screenshot ->
        {
            Attachment attachment = new Attachment(screenshot.getData(), screenshot.getFileName(), "image/png");
            eventBus.post(new AttachmentPublishEvent(attachment));
        });
    }

    /**
     * Takes a screenshot and saves it at the file at the specified path.
     *
     * @param screenshotFilePath The path to the file to save the screenshot, the allowed values are:
     *                           <ul>
     *                           <li>an absolute path to the file, e.g. {@code C:\Windows\screenshot.png};</li>
     *                           <li>a relative path (it is resolved from the current working directory, e.g. {@code
     *                           screenshot.png}</li>
     *                           </ul>
     * @throws IOException If an input or output exception occurred
     */
    @When("I take screenshot and save it to file at path `$screenshotFilePath`")
    public void takeScreenshotToPath(Path screenshotFilePath) throws IOException
    {
        byte[] screenshotData = screenshotTaker.takeScreenshotAsByteArray();
        FileUtils.forceMkdirParent(screenshotFilePath.toFile());
        Files.write(screenshotFilePath, screenshotData);
        LOGGER.atInfo().addArgument(screenshotFilePath::toAbsolutePath).log("Screenshot is saved at '{}'");
    }
}
