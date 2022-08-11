/*
 * Copyright 2019-2022 the original author or authors.
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.context.VariableContext;
import org.vividus.excel.ExcelSheetWriter;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

public class ExcelFileSteps
{
    private final VariableContext variableContext;

    public ExcelFileSteps(VariableContext variableContext)
    {
        this.variableContext = variableContext;
    }

    /**
     * Creates temporary <a href="https://fileinfo.com/extension/xlsx">xlsx</a> file containing a sheet with the
     * specified content and puts the file's path to a variable with the specified name.
     * Created file will be removed while termination of the JVM.
     *
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
    public void createExcelFileContainingSheetWithContent(ExamplesTable content, Set<VariableScope> scopes,
            String variableName) throws IOException
    {
        createExcelFileContainingSheetWithNameAndContent(null, content, scopes, variableName);
    }

    /**
     * Creates temporary <a href="https://fileinfo.com/extension/xlsx">xlsx</a> file containing a sheet with the
     * specified name and content and puts the file's path to a variable with the specified name.
     * Created file will be removed while termination of the JVM.
     *
     * @param sheetName The name of the sheet
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
    @When("I create temporary excel file containing sheet with name `$sheetName` and content:$content and put its"
            + " path to $scopes variable `$variableName`")
    public void createExcelFileContainingSheetWithNameAndContent(String sheetName, ExamplesTable content,
            Set<VariableScope> scopes, String variableName) throws IOException
    {
        Path pathTemporaryFile = ResourceUtils.createTempFile("", ".xlsx", null);
        ExcelSheetWriter.createExcel(pathTemporaryFile, Optional.ofNullable(sheetName), content);
        variableContext.putVariable(scopes, variableName, pathTemporaryFile);
    }

    /**
     * Adds a new sheet with the specified name and content to the existing excel file specified by path.
     * @param sheetName The name of the sheet
     * @param content Any valid ExamplesTable that will be written to the sheet
     * @param path The path to an exiting excel file
     * @throws IOException if an I/O exception of some sort has occurred
     */
    @When("I add sheet with name `$sheetName` and content:$content to excel file at path `$path`")
    public void addSheetToExcelFile(String sheetName, ExamplesTable content, Path path) throws IOException
    {
        ExcelSheetWriter.addSheetToExcel(path, sheetName, content);
    }
}
