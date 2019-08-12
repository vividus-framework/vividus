/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.generic.steps;

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.bdd.steps.ui.web.validation.IDescriptiveSoftAssert;

public class ParameterizedChecks implements IParameterizedChecks
{
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;

    @Override
    public boolean checkIfParametersAreSet(ExamplesTable parameters)
    {
        List<Parameters> rows = parameters.getRowsAsParameters(true);
        if (!rows.isEmpty())
        {
            if (rows.size() > 1)
            {
                return descriptiveSoftAssert.recordFailedAssertion("Excess string with parameters is found");
            }
            Parameters row = rows.get(0);
            for (Entry<String, String> entry : row.values().entrySet())
            {
                if (entry.getValue().isEmpty())
                {
                    return descriptiveSoftAssert
                            .recordFailedAssertion(String.format("Parameter '%s' is empty", entry.getKey()));
                }
            }
        }
        else
        {
            return descriptiveSoftAssert.recordFailedAssertion("Parameters were not specified");
        }
        return true;
    }
}
