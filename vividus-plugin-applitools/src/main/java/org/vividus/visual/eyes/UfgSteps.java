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

package org.vividus.visual.eyes;

import java.util.List;

import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;

import org.jbehave.core.annotations.When;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.eyes.factory.ApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.UfgApplitoolsVisualCheckResult;
import org.vividus.visual.eyes.service.VisualTestingService;
import org.vividus.visual.model.VisualActionType;

public class UfgSteps extends AbstractApplitoolsSteps
{
    private final VisualTestingService<UfgApplitoolsVisualCheckResult> visualTestingService;
    private final ApplitoolsVisualCheckFactory applitoolsVisualCheckFactory;

    public UfgSteps(VisualTestingService<UfgApplitoolsVisualCheckResult> visualTestingService,
            ApplitoolsVisualCheckFactory applitoolsVisualCheckFactory, IUiContext uiContext,
            IAttachmentPublisher attachmentPublisher, ISoftAssert softAssert)
    {
        super(uiContext, attachmentPublisher, softAssert);
        this.visualTestingService = visualTestingService;
        this.applitoolsVisualCheckFactory = applitoolsVisualCheckFactory;
    }

    /**
     * Performs visual check on the <a href="https://applitools.com/platform/ultrafast-grid/">Ultrafast Grid
     * Applitools</a> visual testing plarform.
     *
     * @param actionType The action to perform, can be ESTABLISH, COMPARE_AGAINST, or CHECK_INEQUALITY_AGAINST.
     * @param testName   The name of the test.
     * @param batchName  The name of the batch. Batch acts as group of tests.
     * @param matrix     The matrix describing target platforms to run visual tests on.
     */
    @When("I $actionType baseline `$testName` in batch `$batchName` with Applitools UFG using matrix:$matrix")
    public void performCheck(VisualActionType actionType, String testName, String batchName,
            IRenderingBrowserInfo[] matrix)
    {
        ApplitoolsVisualCheck check = applitoolsVisualCheckFactory.create(batchName, testName, actionType);
        performCheck(List.of(check), matrix);
    }

    /**
     * Performs visual check on the <a href="https://applitools.com/platform/ultrafast-grid/">Ultrafast Grid
     * Applitools</a> visual testing plarform.
     * <br>
     * The custom Applitools configuration provided in the step take precendence over values specified in properties.
     * <br>
     * It's possible to perform several visual testing checks at once by passing several Applitools configuration
     * into step.
     *
     * @param applitoolsConfigurations The Applitools configuration.
     * @param matrix                   The matrix describing target platforms to run visual tests on.
     */
    @When("I run visual test with Applitools UFG using:$applitoolsConfigurations and matrix:$matrix")
    public void performCheck(List<ApplitoolsVisualCheck> applitoolsConfigurations, IRenderingBrowserInfo[] matrix)
    {
        applitoolsConfigurations.stream().forEach(check -> execute(() ->
        {
            check.getConfiguration().addBrowsers(matrix);
            return check;
        }, visualTestingService::run).ifPresent(result -> result.getTestResults().forEach(super::verifyAccessibility)));
    }

    @Override
    protected String getTemplateName()
    {
        return "ufg-applitools-visual-comparison.ftl";
    }
}
