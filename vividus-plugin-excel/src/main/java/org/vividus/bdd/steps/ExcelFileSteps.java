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

package org.vividus.bdd.steps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.excel.ExcelSheetWriter;
import org.vividus.util.ResourceUtils;

public class ExcelFileSteps
{
    private final IBddVariableContext bddVariableContext;

    public ExcelFileSteps(IBddVariableContext bddVariableContext)
    {
        this.bddVariableContext = bddVariableContext;
    }

    /**
     * Creates temporary xlsx file with specified content and puts path to that file to variable with specified name.
     * Created file will be removed while termination of the JVM
     * @param content Any valid ExamplesTable that will be written to the file
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName name of path variable
     * @throws IOException if an I/O exception of some sort has occurred
     */
    @When("I create temporary excel file with content:$content and put path to $scopes variable `$variableName`")
    public void initVariableUsingExcelFilePath(ExamplesTable content,
                 Set<VariableScope> scopes, String variableName) throws IOException
    {
        Path pathTemporaryFile = ResourceUtils.createTempFile("", ".xlsx", null);
        ExcelSheetWriter.createExcel(pathTemporaryFile, content);
        bddVariableContext.putVariable(scopes, variableName, pathTemporaryFile);
    }
}
