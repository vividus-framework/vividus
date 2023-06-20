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

package org.vividus.replacement;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.model.Step;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.vividus.ChainedStoryReporter;
import org.vividus.testcontext.TestContext;

public class DeprecatedCompositeStepsReporter extends ChainedStoryReporter
{
    public static final Pattern DEPRECATED_COMPOSITE_STEP_COMMENT_PATTERN = Pattern
            .compile("^!--\\s+DEPRECATED:\\s+(.*),\\s+(.*)$");
    private static final Object DEPRECATED_STEP_NOTIFICATION_KEY = DeprecatedCompositeStepsReporter.class;

    private final TestContext testContext;
    private final DeprecatedStepNotificationFactory deprecatedStepNotificationFactory;

    public DeprecatedCompositeStepsReporter(TestContext testContext,
            DeprecatedStepNotificationFactory deprecatedStepNotificationFactory)
    {
        this.testContext = testContext;
        this.deprecatedStepNotificationFactory = deprecatedStepNotificationFactory;
    }

    @Override
    public void beforeStep(Step step)
    {
        Step stepToCheck = step;
        if (step.getExecutionType() == StepExecutionType.COMMENT)
        {
            Matcher matcher = DEPRECATED_COMPOSITE_STEP_COMMENT_PATTERN.matcher(step.getStepAsString());
            if (matcher.matches())
            {
                String deprecatedStepNotification = deprecatedStepNotificationFactory
                        .createDeprecatedStepNotification(matcher.group(1), matcher.group(2));
                stepToCheck = new Step(StepExecutionType.COMMENT, deprecatedStepNotification);
                testContext.put(DEPRECATED_STEP_NOTIFICATION_KEY, deprecatedStepNotification);
            }
        }
        super.beforeStep(stepToCheck);
    }

    @Override
    public void comment(String step)
    {
        String comment = (String) Optional.ofNullable(testContext.remove(DEPRECATED_STEP_NOTIFICATION_KEY))
                .orElse(step);
        super.comment(comment);
    }
}
