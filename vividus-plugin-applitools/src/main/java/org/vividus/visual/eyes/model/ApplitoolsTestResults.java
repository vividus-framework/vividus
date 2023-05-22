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

package org.vividus.visual.eyes.model;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.SessionAccessibilityStatus;
import com.applitools.eyes.StepInfo;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsStatus;
import com.applitools.eyes.visualgrid.model.RenderBrowserInfo;

import org.apache.commons.lang3.StringUtils;

public class ApplitoolsTestResults
{
    private final boolean passed;
    private final TestResultsStatus status;
    private final String name;
    private final String os;
    private final String browser;
    private final RectangleSize viewport;
    private Optional<String> device = Optional.empty();
    private final String url;
    private AccessibilityCheckResult accessibilityCheckResult;

    public ApplitoolsTestResults(TestResults testResults)
    {
        this.passed = testResults.isPassed();
        this.status = testResults.getStatus();
        this.name = testResults.getName();
        this.os = testResults.getHostOS();
        this.browser = testResults.getHostApp();
        this.viewport = testResults.getHostDisplaySize();
        this.url = testResults.getUrl();

        if (testResults.getAccessibilityStatus() != null)
        {
            SessionAccessibilityStatus accessibilityStatus = testResults.getAccessibilityStatus();
            StepInfo stepInfo = testResults.getStepsInfo()[0];
            this.accessibilityCheckResult = new AccessibilityCheckResult(stepInfo.getAppUrls().getStepEditor(),
                    accessibilityStatus);
        }
    }

    public ApplitoolsTestResults(TestResultContainer container)
    {
        this(container.getTestResults());

        RenderBrowserInfo renderInfo = container.getBrowserInfo();

        if (renderInfo.getIosDeviceInfo() != null)
        {
            this.device = Optional.of(renderInfo.getIosDeviceInfo().getDeviceName());
        }
        else if (renderInfo.getEmulationInfo() != null)
        {
            this.device = Optional.of(renderInfo.getEmulationInfo().getDeviceName());
        }
    }

    public String getTestIdentifier()
    {
        Stream<String> parameters = device.isEmpty() ? Stream.of(name, browser, viewport.toString())
                : Stream.of(name, getDevice(), os, browser);
        return parameters.filter(StringUtils::isNoneEmpty).collect(Collectors.joining(" "));
    }

    public boolean isPassed()
    {
        return passed;
    }

    public String getStatus()
    {
        return status.name().toLowerCase();
    }

    public String getName()
    {
        return name;
    }

    public String getOs()
    {
        return os;
    }

    public String getBrowser()
    {
        return browser;
    }

    public RectangleSize getViewport()
    {
        return viewport;
    }

    public String getDevice()
    {
        return device.orElse("");
    }

    public String getUrl()
    {
        return url;
    }

    public AccessibilityCheckResult getAccessibilityCheckResult()
    {
        return accessibilityCheckResult;
    }
}
