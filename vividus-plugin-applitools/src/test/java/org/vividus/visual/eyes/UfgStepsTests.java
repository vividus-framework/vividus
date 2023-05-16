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

import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;

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
import org.vividus.visual.eyes.factory.UfgApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.UfgApplitoolsVisualCheckResult;
import org.vividus.visual.eyes.model.UfgVisualCheck;
import org.vividus.visual.eyes.service.VisualTestingService;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class UfgStepsTests
{
    private static final String BATCH_NAME = "batch-name";
    private static final String BASELINE_NAME = "baseline-name";
    private static final String CHECK_PASSED = "Visual check passed";

    @Mock private IRenderingBrowserInfo renderInfo;
    @Mock private VisualTestingService<UfgApplitoolsVisualCheckResult, UfgVisualCheck> visualTestingService;
    @Mock private UfgApplitoolsVisualCheckFactory factory;
    @Mock private IUiContext uiContext;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;
    @Mock private SearchContext searchContext;
    @Mock private UfgApplitoolsVisualCheckResult visualCheckResult;
    @InjectMocks private UfgSteps steps;

    private UfgVisualCheck ufgVisualCheck;

    @BeforeEach
    void beforeEach()
    {
        ufgVisualCheck = new UfgVisualCheck(BATCH_NAME, BASELINE_NAME, VisualActionType.ESTABLISH, List.of(renderInfo));
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
        when(visualTestingService.run(ufgVisualCheck)).thenReturn(visualCheckResult);
        when(visualCheckResult.isPassed()).thenReturn(true);
    }

    @Test
    void shouldCreateApplitoolsConfigAndPerformVisualCheckUsingUltrafastGrid()
    {
        when(factory.create(BATCH_NAME, BASELINE_NAME, VisualActionType.ESTABLISH, List.of(renderInfo)))
            .thenReturn(ufgVisualCheck);

        steps.performCheck(VisualActionType.ESTABLISH, BASELINE_NAME, BATCH_NAME, List.of(renderInfo));

        verify(softAssert).assertTrue(CHECK_PASSED, true);
    }

    @Test
    void shouldPerformVisualCheckUsingUltrafastGridWithExistingApplitoolsConfig()
    {
        ApplitoolsVisualCheck baseCheck = mock(ApplitoolsVisualCheck.class);
        when(factory.create(baseCheck, List.of(renderInfo))).thenReturn(ufgVisualCheck);

        steps.performCheck(List.of(baseCheck), List.of(renderInfo));

        verify(softAssert).assertTrue(CHECK_PASSED, true);
    }
}
