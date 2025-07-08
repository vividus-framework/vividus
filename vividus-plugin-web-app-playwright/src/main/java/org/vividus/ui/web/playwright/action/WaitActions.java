/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.action;

import java.util.function.Supplier;

import com.microsoft.playwright.TimeoutError;

import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;

public class WaitActions
{
    private static final String PASSED_CONDITION = "Passed wait condition: ";
    private static final String FAILED_CONDITION = "Failed wait condition: ";

    private final ISoftAssert softAssert;
    private final PlaywrightSoftAssert playwrightSoftAssert;

    public WaitActions(ISoftAssert softAssert, PlaywrightSoftAssert playwrightSoftAssert)
    {
        this.softAssert = softAssert;
        this.playwrightSoftAssert = playwrightSoftAssert;
    }

    public void runWithTimeoutAssertion(String conditionDescription, Runnable timeoutOperation)
    {
        runWithTimeoutAssertion(() -> conditionDescription, timeoutOperation);
    }

    public void runWithTimeoutAssertion(Supplier<String> conditionDescription, Runnable timeoutOperation)
    {
        try
        {
            timeoutOperation.run();
            softAssert.recordPassedAssertion(PASSED_CONDITION + conditionDescription.get());
        }
        catch (TimeoutError e)
        {
            softAssert.recordFailedAssertion(FAILED_CONDITION + conditionDescription.get() + ". " + e.getMessage(), e);
        }
    }

    public void runTimeoutPlaywrightAssertion(Supplier<String> conditionDescription, Runnable timeoutAssertOperation)
    {
        playwrightSoftAssert.runAssertion(() -> FAILED_CONDITION + conditionDescription.get(), () -> {
            timeoutAssertOperation.run();
            softAssert.recordPassedAssertion(PASSED_CONDITION + conditionDescription.get());
        });
    }
}
