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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.google.zxing.NotFoundException;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.SearchContext;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.screenshot.AshotScreenshotTaker;
import org.vividus.selenium.screenshot.ScreenshotTaker;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.BarcodeActions;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.variable.VariableScope;

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

public class BarcodeSteps
{
    private static final String NOT_FOUND_MESSAGE = "There is no barcode on the screen";
    private static final String NOT_FOUND_FROM_CONTEXT_MESSAGE =
            "There is no barcode on the selected context, page or screen";

    private final IUiContext uiContext;
    private final ScreenshotTaker screenshotTaker;
    private final AshotScreenshotTaker<ScreenshotParameters> ashotScreenshotTaker;
    private final BarcodeActions barcodeActions;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;
    private final EventBus eventBus;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public BarcodeSteps(IUiContext uiContext, ScreenshotTaker screenshotTaker,
            AshotScreenshotTaker ashotScreenshotTaker, BarcodeActions barcodeActions, VariableContext variableContext,
            ISoftAssert softAssert, EventBus eventBus)
    {
        this.uiContext = uiContext;
        this.screenshotTaker = screenshotTaker;
        this.ashotScreenshotTaker = ashotScreenshotTaker;
        this.barcodeActions = barcodeActions;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
        this.eventBus = eventBus;
    }

    /**
     * Scan a barcode and save its value to the <b>variable</b> with the specified
     * <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Takes a viewport screenshot
     * <li>Scans a barcode from the screenshot and save its value to the <i>variable name</i>
     * </ul>
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario
     * <li><b>STORY</b> - the variable will be available within the whole story
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     * @throws IOException If an input or output exception occurred
     */
    @When("I scan barcode from screen and save result to $scopes variable `$variableName`")
    public void scanBarcode(Set<VariableScope> scopes, String variableName) throws IOException
    {
        BufferedImage viewportScreenshot = screenshotTaker.takeViewportScreenshot();

        scanScreenshot(viewportScreenshot, scopes, variableName, NOT_FOUND_MESSAGE);
    }

    /**
     * Scan a barcode from context and save its value to the <b>variable</b> with the specified
     * <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Takes a screenshot the specified context. If it's not set - takes a screenshot of the entire page
     * <li>Scans a barcode from the screenshot and save its value to the <i>variable name</i>
     * </ul>
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario
     * <li><b>STORY</b> - the variable will be available within the whole story
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     * @throws IOException If an input or output exception occurred
     */
    @When("I scan barcode from context and save result to $scopes variable `$variableName`")
    public void scanBarcodeFromContext(Set<VariableScope> scopes, String variableName) throws IOException
    {
        Optional<SearchContext> searchContext = uiContext.getOptionalSearchContext();

        if (searchContext.isPresent())
        {
            Screenshot screenshot = ashotScreenshotTaker.takeAshotScreenshot(searchContext.get(), Optional.empty());
            BufferedImage contextScreenshot = screenshot.getImage();

            scanScreenshot(contextScreenshot, scopes, variableName, NOT_FOUND_FROM_CONTEXT_MESSAGE);
        }
    }

    private void scanScreenshot(BufferedImage screenshot, Set<VariableScope> scopes, String variableName,
            String notFoundMessage) throws IOException
    {
        try
        {
            String result = barcodeActions.scanBarcode(screenshot);
            variableContext.putVariable(scopes, variableName, result);
        }
        catch (NotFoundException e)
        {
            Attachment attachment = new Attachment(ImageTool.toByteArray(screenshot), "Screenshot", "image/png");
            eventBus.post(new AttachmentPublishEvent(attachment));
            softAssert.recordFailedAssertion(notFoundMessage, e);
        }
    }
}
