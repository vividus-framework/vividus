/*
 * Copyright 2019-2021 the original author or authors.
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import com.google.zxing.NotFoundException;

import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.selenium.screenshot.ScreenshotTaker;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.BarcodeActions;

public class BarcodeSteps
{
    private final ScreenshotTaker screenshotTaker;
    private final BarcodeActions barcodeActions;
    private final IBddVariableContext bddVariableContext;
    private final ISoftAssert softAssert;

    public BarcodeSteps(ScreenshotTaker screenshotTaker, BarcodeActions barcodeActions,
                        IBddVariableContext bddVariableContext, ISoftAssert softAssert)
    {
        this.screenshotTaker = screenshotTaker;
        this.barcodeActions = barcodeActions;
        this.bddVariableContext = bddVariableContext;
        this.softAssert = softAssert;
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
    @When("I scan a barcode from screen and save result to $scopes variable `$variableName`")
    public void scanBarcode(Set<VariableScope> scopes, String variableName) throws IOException
    {
        BufferedImage screenshotPath = screenshotTaker.takeViewportScreenshot();
        try
        {
            String result = barcodeActions.scanBarcode(screenshotPath);
            bddVariableContext.putVariable(scopes, variableName, result);
        }
        catch (NotFoundException e)
        {
            softAssert.recordFailedAssertion("There is no barcode on the screen", e);
        }
    }
}
