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

package org.vividus.bdd.steps.csv;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.csv.CsvReader;

public class CsvSteps
{
    private final CsvReader csvReader = new CsvReader();

    @Inject private IBddVariableContext bddVariableContext;

    /**
     * Save CSV string to indexed <i>zero</i>-based variable, e.g. var[0], var[1] and etc.
     * @param csv CSV string
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from the next batch
     * </ul>
     * @param variableName A name of variable to save CSV string
     * @throws IOException If an exception during reading CSV string occurred
     */
    @When("I save CSV `$csv` to $scopes variable `$variableName`")
    public void saveCsvStringIntoVariable(String csv, Set<VariableScope> scopes, String variableName) throws IOException
    {
        List<Map<String, String>> result = csvReader.readCsvString(csv);
        bddVariableContext.putVariable(scopes, variableName, result);
    }
}
