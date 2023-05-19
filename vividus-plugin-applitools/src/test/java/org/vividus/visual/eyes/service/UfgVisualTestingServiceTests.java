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

package org.vividus.visual.eyes.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.applitools.eyes.Padding;
import com.applitools.eyes.StepInfo;
import com.applitools.eyes.StepInfo.AppUrls;
import com.applitools.eyes.config.Configuration;
import com.applitools.eyes.fluent.GetRegion;
import com.applitools.eyes.selenium.ElementReference;
import com.applitools.eyes.selenium.TargetPathLocator;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.SimpleRegionByElement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.visual.eyes.factory.UfgEyesFactory;
import org.vividus.visual.eyes.model.ApplitoolsTestResults;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.UfgApplitoolsVisualCheckResult;
import org.vividus.visual.eyes.ufg.UfgEyes;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class UfgVisualTestingServiceTests
{
    private static final String BASELINE_NAME = "baseline-name";
    private static final String BATCH_NAME = "batch-name";
    private static final String APP_NAME = "app-name";

    @Mock private Locator elementLocator;
    @Mock private RemoteWebElement element;
    @Mock private Locator areaLocator;
    @Mock private RemoteWebElement area;

    @Captor private ArgumentCaptor<SeleniumCheckSettings> checkSettingsCaptor;
    @Mock private RemoteWebDriver remoteWebDriver;
    @Mock private ApplitoolsTestResults testResults;
    @Mock private StepInfo stepInfo;
    @Mock private AppUrls appUrls;
    @Mock private UfgEyes eyes;
    @Mock private UfgEyesFactory eyesFactory;
    @Mock private ISearchActions searchActions;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private UfgVisualTestingService service;

    @BeforeEach
    void init()
    {
        when(testResults.isPassed()).thenReturn(true);
        when(searchActions.findElements(elementLocator)).thenReturn(List.of(element));
        when(searchActions.findElements(areaLocator)).thenReturn(List.of(area));
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(remoteWebDriver);
    }

    @Test
    void shouldRunForWindow()
    {
        ApplitoolsVisualCheck applitoolsVisualCheck = createCheck();
        applitoolsVisualCheck.setSearchContext(remoteWebDriver);
        when(eyesFactory.createEyes(applitoolsVisualCheck)).thenReturn(eyes);
        when(eyes.getTestResults()).thenReturn(List.of(testResults));

        UfgApplitoolsVisualCheckResult result = service.run(applitoolsVisualCheck);
        assertTrue(result.isPassed());
        assertEquals(List.of(testResults), result.getTestResults());

        verify(eyes).open(remoteWebDriver, APP_NAME, BASELINE_NAME);
        verify(eyes).check(checkSettingsCaptor.capture());
        verify(eyes).close(false);
        SeleniumCheckSettings checkSettings = checkSettingsCaptor.getValue();
        assertNull(checkSettings.getTargetPathLocator());
        assertNull(checkSettings.getTargetRegion());
        validateIgnoreRegions(checkSettings.getIgnoreRegions());
    }

    @Test
    void shouldRunForWebElement()
    {
        ApplitoolsVisualCheck applitoolsVisualCheck = createCheck();
        RemoteWebElement webElement = mock(RemoteWebElement.class);
        applitoolsVisualCheck.setSearchContext(webElement);
        when(eyesFactory.createEyes(applitoolsVisualCheck)).thenReturn(eyes);
        when(eyes.getTestResults()).thenReturn(List.of(testResults));

        UfgApplitoolsVisualCheckResult result = service.run(applitoolsVisualCheck);
        assertTrue(result.isPassed());
        assertEquals(List.of(testResults), result.getTestResults());

        verify(eyes).open(remoteWebDriver, APP_NAME, BASELINE_NAME);
        verify(eyes).check(checkSettingsCaptor.capture());
        verify(eyes).close(false);
        SeleniumCheckSettings checkSettings = checkSettingsCaptor.getValue();
        TargetPathLocator targetPathLocator = checkSettings.getTargetPathLocator();
        ElementReference elementReference = (ElementReference) targetPathLocator.getValue();
        assertEquals(webElement, elementReference.getElement());
        validateIgnoreRegions(checkSettings.getIgnoreRegions());
    }

    private void validateIgnoreRegions(GetRegion[] ignoreRegions)
    {
        List<GetRegion> regions = List.of(ignoreRegions);
        assertThat(regions, hasSize(2));
        SimpleRegionByElement elementRegion = (SimpleRegionByElement) regions.get(0);
        assertEquals(element, elementRegion.getElement());
        validatePadding(elementRegion.getPadding(), 0, 0);
        SimpleRegionByElement areaRegion = (SimpleRegionByElement) regions.get(1);
        assertEquals(area, areaRegion.getElement());
        validatePadding(areaRegion.getPadding(), 10_000, 10_000);
    }

    private void validatePadding(Padding padding, int left, int right)
    {
        assertEquals(0, padding.getTop());
        assertEquals(0, padding.getBottom());
        assertEquals(left, padding.getLeft());
        assertEquals(right, padding.getRight());
    }

    private ApplitoolsVisualCheck createCheck()
    {
        ApplitoolsVisualCheck applitoolsVisualCheck = new ApplitoolsVisualCheck(BATCH_NAME, BASELINE_NAME,
                VisualActionType.ESTABLISH);
        applitoolsVisualCheck.setElementsToIgnore(Set.of(elementLocator));
        applitoolsVisualCheck.setAreasToIgnore(Set.of(areaLocator));
        applitoolsVisualCheck.setConfiguration(new Configuration().setAppName(APP_NAME));
        return applitoolsVisualCheck;
    }
}
