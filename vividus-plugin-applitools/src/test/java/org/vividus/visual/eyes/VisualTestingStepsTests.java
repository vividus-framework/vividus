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

package org.vividus.visual.eyes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.eyes.factory.ApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheckResult;
import org.vividus.visual.eyes.service.VisualTestingService;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class VisualTestingStepsTests
{
    private static final String BATCH_NAME = "batchName";
    private static final String TEST = "test";
    private static final VisualActionType ESTABLISH = VisualActionType.ESTABLISH;

    @Mock private VisualTestingService visualTestingService;
    @Mock private ISoftAssert softAssert;
    @Mock private ApplitoolsVisualCheckFactory applitoolsVisualCheckFactory;
    private final IUiContext uiContext = mock(IUiContext.class);
    private final IAttachmentPublisher attachmentPublisher = mock(IAttachmentPublisher.class);

    @InjectMocks private final VisualTestingSteps visualTestingSteps = new VisualTestingSteps(uiContext,
            attachmentPublisher);

    @BeforeEach
    void setUp()
    {
        when(uiContext.getSearchContext()).thenReturn(mock(SearchContext.class));
    }

    @Test
    void shouldRunApplitoolsVisualCheck()
    {
        ApplitoolsVisualCheck check = mock(ApplitoolsVisualCheck.class);
        when(applitoolsVisualCheckFactory.create(BATCH_NAME, TEST, ESTABLISH)).thenReturn(check);
        ApplitoolsVisualCheckResult result = mock(ApplitoolsVisualCheckResult.class);
        when(visualTestingService.run(check)).thenReturn(result);
        visualTestingSteps.performCheck(ESTABLISH, TEST, BATCH_NAME);
        verifyVisualCheck(result, 1);
    }

    private void verifyVisualCheck(ApplitoolsVisualCheckResult result, int times)
    {
        verify(attachmentPublisher, times(times)).publishAttachment("applitools-visual-comparison.ftl",
                Map.of("result", result), "Visual comparison");
        verify(softAssert, times(times)).assertTrue("Visual check passed", false);
    }

    @Test
    void shouldRunApplitoolsVisualCheckWithCustomConfiguration()
    {
        ApplitoolsVisualCheck check = mock(ApplitoolsVisualCheck.class);
        ApplitoolsVisualCheckResult result = mock(ApplitoolsVisualCheckResult.class);
        when(visualTestingService.run(check)).thenReturn(result);
        ScreenshotConfiguration screenshotConfiguration = mock(ScreenshotConfiguration.class);
        visualTestingSteps.performCheck(List.of(check, check), screenshotConfiguration);
        verifyVisualCheck(result, 2);
        verify(check, times(2)).setScreenshotConfiguration(Optional.of(screenshotConfiguration));
    }
}
