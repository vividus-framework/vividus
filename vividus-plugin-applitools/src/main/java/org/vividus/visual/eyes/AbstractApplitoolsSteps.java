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

import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.eyes.model.AccessibilityCheckResult;
import org.vividus.visual.eyes.model.ApplitoolsTestResults;
import org.vividus.visual.steps.AbstractVisualSteps;

public abstract class AbstractApplitoolsSteps extends AbstractVisualSteps
{
    protected AbstractApplitoolsSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher,
            ISoftAssert softAssert)
    {
        super(uiContext, attachmentPublisher, softAssert);
    }

    protected void verifyAccessibility(ApplitoolsTestResults results)
    {
        AccessibilityCheckResult accessibilityStatus = results.getAccessibilityCheckResult();
        if (accessibilityStatus != null)
        {
            getSoftAssert().assertTrue(String.format("%s accessibility check for test: %s",
                    accessibilityStatus.getGuideline(), results.getTestIdentifier()), accessibilityStatus.isPassed());
        }
    }
}
