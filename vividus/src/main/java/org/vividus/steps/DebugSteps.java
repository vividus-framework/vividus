/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps;

import java.time.Duration;

import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.Sleeper;

public class DebugSteps
{
    private final ISoftAssert softAssert;

    public DebugSteps(ISoftAssert softAssert)
    {
        this.softAssert = softAssert;
    }

    /**
     * Waits <b>period</b> and fails on purpose
     * @param period Total waiting time according to
     *               <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     */
    @When("I wait `$period` for debug")
    public void waitForDebug(Duration period)
    {
        Sleeper.sleep(period);
        softAssert.recordFailedAssertion("Use this step for debug purposes only");
    }
}
