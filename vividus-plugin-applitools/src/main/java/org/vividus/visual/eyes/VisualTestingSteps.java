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

package org.vividus.visual.eyes;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.jbehave.core.annotations.When;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.bdd.AbstractVisualSteps;
import org.vividus.visual.eyes.factory.ApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.service.VisualTestingService;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheckResult;

public class VisualTestingSteps extends AbstractVisualSteps
{
    @Inject private VisualTestingService visualTestingService;
    @Inject private ISoftAssert softAssert;
    @Inject private ApplitoolsVisualCheckFactory applitoolsVisualCheckFactory;

    public VisualTestingSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher)
    {
        super(uiContext, attachmentPublisher);
    }

    /**
     * Performs visual check on the Applitools service;
     * @param actionType ESTABLISH or COMPARE_AGAINST
     * @param testName name of the test.
     * @param batchName name of the batch. Batch acts as group of tests.
     */
    @When("I $actionType baseline `$testName` in batch `$batchName` with Applitools")
    public void performCheck(VisualActionType actionType, String testName, String batchName)
    {
        ApplitoolsVisualCheck visualCheck = applitoolsVisualCheckFactory.create(batchName, testName, actionType);
        runApplitoolsTest(visualCheck);
    }

    /**
     * Perform visual check on the Applitools service and provides possibility to use custom configuration eyes;<br>
     * <table border="1">
     *    <caption>Example of eyes configuration:</caption>
     *    <thead>
     *      <tr>
     *        <th>readApiKey</th>
     *        <th>hostApp</th>
     *        <th>hostOS</th>
     *        <th>viewportSize</th>
     *        <th>matchLevel</th>
     *        <th>serverUri</th>
     *        <th>appName</th>
     *        <th>batchName</th>
     *        <th>baselineEnvName</th>
     *        <th>baselineName</th>
     *        <th>action</th>
     *      </tr>
     *    </thead>
     *    <tbody>
     *      <tr>
     *        <td>key</td>
     *        <td>Chrome</td>
     *        <td>Windows 95</td>
     *        <td>1x1</td>
     *        <td>EXACT</td>
     *        <td>https://eyesapi.applitools.com</td>
     *        <td>AUT</td>
     *        <td>UAT</td>
     *        <td>PREDEV</td>
     *        <td>someName</td>
     *        <td>ESTABLISH</td>
     *      </tr>
     *    </tbody>
     *  </table>
     *  <br> Mind that: batchName, baselineName, action are mandatory parameters;
     * @param applitoolsConfigurations custom applitools configuration
     */
    @When("I run visual test with Applitools using:$applitoolsConfigurations")
    public void performCheck(List<ApplitoolsVisualCheck> applitoolsConfigurations)
    {
        applitoolsConfigurations.forEach(this::runApplitoolsTest);
    }

    /**
     * Perform visual check on the Applitools service and provides possibility to use custom configurations for
     * the checks and screenshot strategy;<br>
     * <table border="1">
     *    <caption>Example of eyes configuration:</caption>
     *    <thead>
     *      <tr>
     *        <th>readApiKey</th>
     *        <th>hostApp</th>
     *        <th>hostOS</th>
     *        <th>viewportSize</th>
     *        <th>matchLevel</th>
     *        <th>serverUri</th>
     *        <th>appName</th>
     *        <th>batchName</th>
     *        <th>baselineEnvName</th>
     *        <th>baselineName</th>
     *        <th>action</th>
     *      </tr>
     *    </thead>
     *    <tbody>
     *      <tr>
     *        <td>key</td>
     *        <td>Chrome</td>
     *        <td>Windows 95</td>
     *        <td>1x1</td>
     *        <td>EXACT</td>
     *        <td>https://eyesapi.applitools.com</td>
     *        <td>AUT</td>
     *        <td>UAT</td>
     *        <td>PREDEV</td>
     *        <td>someName</td>
     *        <td>ESTABLISH</td>
     *      </tr>
     *    </tbody>
     *  </table>
     *  <br> Mind that: batchName, baselineName, action are mandatory parameters;<br>
     * <table border="1">
     *    <caption>Example of screenshot configuration:</caption>
     *    <thead>
     *      <tr>
     *        <th>webHeaderToCut</th>
     *        <th>webFooterToCut</th>
     *        <th>nativeHeaderToCut</th>
     *        <th>nativeFooterToCut</th>
     *        <th>scrollableElement</th>
     *        <th>coordsProvider</th>
     *        <th>scrollTimeout</th>
     *        <th>screenshotShootingStrategy</th>
     *      </tr>
     *    </thead>
     *    <tbody>
     *      <tr>
     *        <td>100</td>
     *        <td>10</td>
     *        <td>20</td>
     *        <td>20</td>
     *        <td>By.id(scrollable)</td>
     *        <td>WEB_DRIVER</td>
     *        <td>500</td>
     *        <td>SIMPLE</td>
     *      </tr>
     *    </tbody>
     *  </table>
     * @param applitoolsConfigurations custom applitools configuration
     * @param screenshotConfiguration custom shooting strategy configuration
     */
    @When(value = "I run visual test with Applitools using:$applitoolsConfigurations and screenshot"
            + " config:$screenshotConfiguration", priority = 1)
    public void performCheck(List<ApplitoolsVisualCheck> applitoolsConfigurations,
            ScreenshotConfiguration screenshotConfiguration)
    {
        applitoolsConfigurations.forEach(check -> {
            check.setScreenshotConfiguration(Optional.of(screenshotConfiguration));
            runApplitoolsTest(check);
        });
    }

    void runApplitoolsTest(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        VisualCheckResult visualCheckResult = execute(visualTestingService::run, () -> applitoolsVisualCheck,
                "applitools-visual-comparison.ftl");
        softAssert.assertTrue("Visual check passed", visualCheckResult.isPassed());
    }
}
