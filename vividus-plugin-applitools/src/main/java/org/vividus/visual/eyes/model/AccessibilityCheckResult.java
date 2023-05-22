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

import java.net.URI;

import com.applitools.eyes.AccessibilityStatus;
import com.applitools.eyes.SessionAccessibilityStatus;

import org.apache.hc.core5.net.URIBuilder;

public class AccessibilityCheckResult
{
    private static final String GUIDELINE_FORMAT = "%s - %s";

    private final String url;
    private final AccessibilityStatus status;
    private final String guideline;

    public AccessibilityCheckResult(String url, SessionAccessibilityStatus status)
    {
        this.url = new URIBuilder(URI.create(url)).addParameter("accessibility", "true").toString();
        this.status = status.getStatus();

        switch (status.getVersion())
        {
            case WCAG_2_0:
                this.guideline = String.format(GUIDELINE_FORMAT, "WCAG 2.0", status.getLevel());
                break;
            case WCAG_2_1:
                this.guideline = String.format(GUIDELINE_FORMAT, "WCAG 2.1", status.getLevel());
                break;
            default:
                throw new IllegalArgumentException("Unsupported accessibility standard: " + status.getVersion());
        }
    }

    public String getUrl()
    {
        return url;
    }

    public String getStatus()
    {
        return status.name().toLowerCase();
    }

    public boolean isPassed()
    {
        return status == AccessibilityStatus.Passed;
    }

    public String getGuideline()
    {
        return guideline;
    }
}
