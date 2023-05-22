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

package org.vividus.visual.eyes.ufg;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsStatus;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.visualgrid.model.ChromeEmulationInfo;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.IosDeviceInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceName;
import com.applitools.eyes.visualgrid.model.RenderBrowserInfo;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.visual.eyes.model.ApplitoolsTestResults;

class UfgEyesTests
{
    static Stream<Arguments> platforms()
    {
        return Stream.of(
            arguments(
                new RenderBrowserInfo(new IosDeviceInfo(IosDeviceName.iPhone_14)),
                IosDeviceName.iPhone_14.getName()
            ),
            arguments(
                new RenderBrowserInfo(new ChromeEmulationInfo(DeviceName.Galaxy_S20, ScreenOrientation.PORTRAIT)),
                DeviceName.Galaxy_S20.getName()
            ),
            arguments(
                new DesktopBrowserInfo(new RectangleSize(1, 1), BrowserType.CHROME).getRenderBrowserInfo(),
                ""
            )
        );
    }

    @ParameterizedTest
    @MethodSource("platforms")
    void shouldReturnResultsForTest(RenderBrowserInfo info, String deviceName)
    {
        EyesRunner eyesRunner = mock(EyesRunner.class);
        TestResultsSummary testResultsSummary = mock(TestResultsSummary.class);
        when(eyesRunner.getAllTestResults(false)).thenReturn(testResultsSummary);

        TestResults testResults = new TestResults();
        testResults.setStatus(TestResultsStatus.Passed);
        testResults.setName("test-name");
        testResults.setHostOS("host-os");
        testResults.setHostApp("host-app");
        testResults.setHostDisplaySize(new RectangleSize(1, 1));
        testResults.setUrl("url");

        TestResultContainer testResultContainer = new TestResultContainer(testResults, info, null);
        when(testResultsSummary.getAllResults()).thenReturn(new TestResultContainer[] { testResultContainer });

        UfgEyes eyes = new UfgEyes(eyesRunner);

        List<ApplitoolsTestResults> results = eyes.getTestResults();
        assertThat(results, hasSize(1));
        ApplitoolsTestResults result = results.get(0);
        assertTrue(result.isPassed());
        assertEquals(testResults.getName(), result.getName());
        assertEquals("passed", result.getStatus());
        assertEquals(testResults.getHostOS(), result.getOs());
        assertEquals(testResults.getHostApp(), result.getBrowser());
        assertEquals(testResults.getHostDisplaySize(), result.getViewport());
        assertEquals(testResults.getUrl(), result.getUrl());
        assertEquals(deviceName, result.getDevice());
    }
}
