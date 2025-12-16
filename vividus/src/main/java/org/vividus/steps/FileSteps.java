/*
 * Copyright 2019-2025 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

public class FileSteps
{
    private static final String FILE = "File '";

    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public FileSteps(VariableContext variableContext, ISoftAssert softAssert)
    {
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    /**
     * Creates temporary file with specified content and puts path to that file to variable with specified name.
     * Created file will be removed while termination of the JVM
     * @param name name of temporary file
     * @param content data that will be written to the file
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName name of variable
     * @throws IOException if an I/O exception of some sort has occurred
     */
    @When("I create temporary file with name `$name` and content `$content` and put path to $scopes variable"
            + " `$variableName`")
    public void createTemporaryFile(String name, DataWrapper content, Set<VariableScope> scopes, String variableName)
            throws IOException
    {
        Path temporaryFile = ResourceUtils.createTempFile(FilenameUtils.getBaseName(name),
                "." + FilenameUtils.getExtension(name));
        Files.write(temporaryFile, content.getBytes());
        variableContext.putVariable(scopes, variableName, temporaryFile.toString());
    }

    /**
     * Saves content to file with specified pathname
     * @param fileContent Content to be saved to file
     * @param filePath Fully qualified file name with parent folders and extension (e.g. temp/some_file.txt)
     * @throws IOException If an I/O error has occurred
     */
    @When("I create file with content `$fileContent` at path `$filePath`")
    public void createFile(String fileContent, String filePath) throws IOException
    {
        FileUtils.writeStringToFile(new File(filePath), fileContent, StandardCharsets.UTF_8);
    }

    /**
     * Checks if a file exists at the given path
     * @param filePath The full file path, including its name and extension (e.g. C:\Users\Public\Documents\example.txt)
     */
    @Then("file exists at path `$filePath`")
    public void assertPathExists(String filePath)
    {
        Validate.isTrue(filePath != null && !filePath.isBlank(),
                "File path must not be null, empty or blank");

        final Path path;
        try
        {
            path = Path.of(filePath);
        }
        catch (InvalidPathException ex)
        {
            softAssert.recordFailedAssertion("Invalid path '" + filePath + "': " + ex.getMessage());
            return;
        }

        if (!Files.exists(path) || !Files.isRegularFile(path))
        {
            softAssert.recordFailedAssertion(FILE + path + "' does not exist");
            return;
        }

        softAssert.recordPassedAssertion(FILE + path + "' exists");
    }
}
