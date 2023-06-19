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

package org.vividus.steps;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.vividus.annotation.Replacement;

public class DeprecatedSteps
{
    @Replacement(versionToRemoveStep = "0.10.0",
            replacementFormatPattern = "When actual code step with parameters `%3$s`-`%2$s`-`%1$s` and SubSteps:%4$s")
    @Given("deprecated code step with parameters $parameter1-`$parameter2`-`$parameter3` and SubSteps:$subSteps")
    @Alias("deprecated code step with parameters $parameter1-'$parameter2'-'$parameter3' and SubSteps:$subSteps")
    public void deprecatedStep(String parameter1, String parameter2, String parameter3, SubSteps subSteps)
    {
        // nothing to do
    }

    @Deprecated(since = "0.4.8", forRemoval = true)
    @Given("deprecated code without replace")
    public void deprecatedStepWithoutReplace()
    {
        // nothing to do
    }
}
