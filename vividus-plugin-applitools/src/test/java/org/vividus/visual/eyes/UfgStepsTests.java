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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.applitools.eyes.AccessibilityGuidelinesVersion;
import com.applitools.eyes.AccessibilityLevel;
import com.applitools.eyes.AccessibilityStatus;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.SessionAccessibilityStatus;
import com.applitools.eyes.StepInfo;
import com.applitools.eyes.StepInfo.AppUrls;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.config.Configuration;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;
import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceName;
import com.applitools.eyes.visualgrid.model.RenderBrowserInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.eyes.factory.ApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsTestResults;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.UfgApplitoolsVisualCheckResult;
import org.vividus.visual.eyes.service.VisualTestingService;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class UfgStepsTests
{
    private static final String BATCH_NAME = "batch-name";
    private static final String BASELINE_NAME = "baseline-name";
    private static final String CHECK_PASSED = "Visual check passed";

    @Mock private Configuration configuration;
    @Mock private IRenderingBrowserInfo renderInfo;
    @Mock private VisualTestingService<UfgApplitoolsVisualCheckResult> visualTestingService;
    @Mock private ApplitoolsVisualCheckFactory factory;
    @Mock private IUiContext uiContext;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;
    @Mock private SearchContext searchContext;
    @Mock private UfgApplitoolsVisualCheckResult visualCheckResult;
    @InjectMocks private UfgSteps steps;

    private ApplitoolsVisualCheck applitoolsVisualCheck;

    @BeforeEach
    void beforeEach()
    {
        applitoolsVisualCheck = new ApplitoolsVisualCheck(BATCH_NAME, BASELINE_NAME, VisualActionType.ESTABLISH);
        applitoolsVisualCheck.setConfiguration(configuration);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
        when(visualTestingService.run(applitoolsVisualCheck)).thenReturn(visualCheckResult);
        when(visualCheckResult.isPassed()).thenReturn(true);
    }

    @Test
    void shouldCreateApplitoolsConfigAndPerformVisualCheckUsingUltrafastGrid()
    {
        when(factory.create(BATCH_NAME, BASELINE_NAME, VisualActionType.ESTABLISH))
            .thenReturn(applitoolsVisualCheck);

        steps.performCheck(VisualActionType.ESTABLISH, BASELINE_NAME, BATCH_NAME,
                new IRenderingBrowserInfo[] { renderInfo });

        verify(configuration).addBrowsers(new IRenderingBrowserInfo[] { renderInfo });
        verify(softAssert).assertTrue(CHECK_PASSED, true);
    }

    @Test
    void shouldPerformVisualCheckUsingUltrafastGridWithExistingApplitoolsConfig()
    {
        steps.performCheck(List.of(applitoolsVisualCheck), new IRenderingBrowserInfo[] { renderInfo });

        verify(configuration).addBrowsers(new IRenderingBrowserInfo[] { renderInfo });
        verify(softAssert).assertTrue(CHECK_PASSED, true);
    }

    @Test
    void shouldPerformVisualCheckAndAccessibilityCheck()
    {
        ApplitoolsTestResults desktopResults = createResults("Linux", "Chrome", new RectangleSize(1920, 1080),
                new DesktopBrowserInfo(new RectangleSize(1920, 1080), BrowserType.CHROME).getRenderBrowserInfo(),
                AccessibilityStatus.Failed);
        ApplitoolsTestResults iosResults = createResults("iOS 16.1", "Safari", new RectangleSize(375, 821),
                new RenderBrowserInfo(new IosDeviceInfo(IosDeviceName.iPhone_X)), AccessibilityStatus.Passed);
        when(visualCheckResult.getTestResults()).thenReturn(List.of(desktopResults, iosResults));

        steps.performCheck(List.of(applitoolsVisualCheck), new IRenderingBrowserInfo[] { renderInfo, renderInfo });

        verify(softAssert).assertTrue(
                "WCAG 2.1 - AA accessibility check for test: baseline-name Chrome 1920x1080", false);
        verify(softAssert).assertTrue(
                "WCAG 2.1 - AA accessibility check for test: baseline-name iPhone X iOS 16.1 Safari", true);
    }

    private ApplitoolsTestResults createResults(String os, String browser, RectangleSize viewport,
            RenderBrowserInfo deviceInfo, AccessibilityStatus status)
    {
        TestResults results = new TestResults();
        results.setName(BASELINE_NAME);
        results.setHostOS(os);
        results.setHostApp(browser);
        results.setHostDisplaySize(viewport);

        StepInfo stepInfo = new StepInfo();
        AppUrls appUrls = stepInfo.new AppUrls();
        stepInfo.setAppUrls(appUrls);
        appUrls.setStepEditor("http://example.com");
        results.setStepsInfo(new StepInfo[] { stepInfo });

        SessionAccessibilityStatus accessibilityStatus = mock(SessionAccessibilityStatus.class);
        when(accessibilityStatus.getLevel()).thenReturn(AccessibilityLevel.AA);
        when(accessibilityStatus.getStatus()).thenReturn(status);
        when(accessibilityStatus.getVersion()).thenReturn(AccessibilityGuidelinesVersion.WCAG_2_1);
        results.setAccessibilityStatus(accessibilityStatus);

        TestResultContainer container = new TestResultContainer(results, deviceInfo, null);

        return new ApplitoolsTestResults(container);
    }
}
