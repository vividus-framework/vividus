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

package org.vividus.visual.eyes.factory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.UfgVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.screenshot.BaselineIndexer;

@ExtendWith(MockitoExtension.class)
class UfgApplitoolsVisualCheckFactoryTests
{
    private static final String BASELINE_NAME = "baseline-name";
    private static final String BATCH_NAME = "batch-name";

    @Mock private IRenderingBrowserInfo renderingInfo;
    @Mock private ScreenshotParameters screenshotParameters;
    @Mock private ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    @Mock private BaselineIndexer baselineIndexer;
    @InjectMocks private UfgApplitoolsVisualCheckFactory factory;

    @Test
    void shouldCreateUfgApplitoolsVisualCheck()
    {
        when(baselineIndexer.createIndexedBaseline(BASELINE_NAME)).thenReturn(BASELINE_NAME);
        when(screenshotParametersFactory.create()).thenReturn(Optional.of(screenshotParameters));

        UfgVisualCheck check = factory.create(BATCH_NAME, BASELINE_NAME, VisualActionType.ESTABLISH,
                List.of(renderingInfo));

        assertEquals(BASELINE_NAME, check.getBaselineName());
        assertEquals(BATCH_NAME, check.getBatchName());
        assertEquals(List.of(renderingInfo), check.getRenderInfos());
    }

    @Test
    void shouldCreateUfgApplitoolsVisualCheckFromBaseCheck()
    {
        ApplitoolsVisualCheck baseCheck = new ApplitoolsVisualCheck(BATCH_NAME, BASELINE_NAME,
                VisualActionType.ESTABLISH);
        baseCheck.setExecuteApiKey("execute-api-key");
        baseCheck.setReadApiKey("read-api-key");
        baseCheck.setHostApp("host-app");
        baseCheck.setHostOS("host-os");
        baseCheck.setViewportSize(new Dimension(0, 0));
        baseCheck.setMatchLevel(MatchLevel.EXACT);
        baseCheck.setServerUri(URI.create("https://example.com"));
        baseCheck.setAppName("app-name");
        baseCheck.setBaselineEnvName("baseline-env-name");
        Locator elementLocator = mock(Locator.class);
        baseCheck.setElementsToIgnore(Set.of(elementLocator));
        Locator areaLocator = mock(Locator.class);
        baseCheck.setAreasToIgnore(Set.of(areaLocator));

        UfgVisualCheck check = factory.create(baseCheck, List.of(renderingInfo));

        assertAll(
            () -> assertEquals(baseCheck.getBaselineName(), check.getBaselineName()),
            () -> assertEquals(baseCheck.getBatchName(), check.getBatchName()),
            () -> assertEquals(baseCheck.getAction(), check.getAction()),
            () -> assertEquals(baseCheck.getExecuteApiKey(), check.getExecuteApiKey()),
            () -> assertEquals(baseCheck.getReadApiKey(), check.getReadApiKey()),
            () -> assertEquals(baseCheck.getHostApp(), check.getHostApp()),
            () -> assertEquals(baseCheck.getHostOS(), check.getHostOS()),
            () -> assertEquals(baseCheck.getViewportSize(), check.getViewportSize()),
            () -> assertEquals(baseCheck.getMatchLevel(), check.getMatchLevel()),
            () -> assertEquals(baseCheck.getServerUri(), check.getServerUri()),
            () -> assertEquals(baseCheck.getAppName(), check.getAppName()),
            () -> assertEquals(baseCheck.getBaselineEnvName(), check.getBaselineEnvName()),
            () -> assertEquals(baseCheck.getElementsToIgnore(), check.getElementsToIgnore()),
            () -> assertEquals(baseCheck.getAreasToIgnore(), check.getAreasToIgnore())
        );
    }
}
