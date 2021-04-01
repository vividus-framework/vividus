/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.azure.storage.blob;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.vividus.azure.storage.blob.service.BlobStorageService;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;

public class BlobSteps
{
    private final BddVariableContext bddVariableContext;
    private final BlobStorageService blobStorageService;

    public BlobSteps(BddVariableContext bddVariableContext, BlobStorageService blobStorageService)
    {
        this.bddVariableContext = bddVariableContext;
        this.blobStorageService = blobStorageService;
    }

    /**
     * Reads a blob by the path and saves its content as a text to a variable
     * @param path         The path to a blob
     * @param containerId  The container key
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>scopes
     * @param variableName he variable name to store the result.
     */
    @When("I read blob `$path` from `$containerId` blob container and save its content to $scopes variable"
            + " `$variableName`")
    public void readBlob(String path, String containerId, Set<VariableScope> scopes, String variableName)
    {
        byte[] blob = blobStorageService.readBlob(containerId, path);
        bddVariableContext.putVariable(scopes, variableName, new String(blob, StandardCharsets.UTF_8));
    }

    /**
     * Reads a blob by the path and saves its content to a temporary file with the specified name
     * and saves path to the temporary file to a variable.
     * @param path         The path to a blob
     * @param containerId  The container key
     * @param fileSiffix   The suffix of the file
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>scopes
     * @param variableName The variable name to store the result.
     * @throws IOException In case of failed temporary file creation
     */
    @When("I read blob `$path` from `$containerId` blob container to $filename temp file and save path to"
            + " $scopes variable `$variableName`")
    public void readBlobToFile(String path, String containerId, String fileSiffix, Set<VariableScope> scopes,
            String variableName) throws IOException
    {
        byte[] blob = blobStorageService.readBlob(containerId, path);
        Path temp = Files.createTempFile(null, fileSiffix);
        Files.write(temp, blob);
        bddVariableContext.putVariable(scopes, variableName, temp);
    }

    /**
     * Deletes a blob from the container
     * @param path        The path to a file
     * @param containerId The key of the container
     */
    @When("I delete blob `$path` from `$blobContainerKey` blob container")
    public void deleteAFile(String path, String containerId)
    {
        blobStorageService.delete(containerId, path);
    }

    /**
     * Finds all blob in container according to specified rule
     * @param rule         The rule to verify string<br>
     *                     <i>Available rules:</i>
     *                     <ul>
     *                     <li><b>MATCHES</b> - uses the regexp to validate the string,
     *                     <li><b>CONTAINES</b> - checks if data under test contains string
     *                     <li><b>DOES_NOT_CONTAIN</b> - checks if data under test does not contains string
     *                     <li><b>IS_EQUAL_TO</b> - checks if string equal to expected value
     *                     </ul>
     * @param expectedPath The path to a blob
     * @param containerId  The container id
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>scopes
     * @param variableName The variable name to store the result.
     */
    @When("I find all blobs with path $comparisonRule `$expectedPath` from `$containerId` and save result"
            + " to $scopes variable `$variableName`")
    public void findFiles(StringComparisonRule rule, String expectedPath, String containerId,
            Set<VariableScope> scopes, String variableName)
    {
        List<Path> files = blobStorageService.findFiles(rule.createMatcher(expectedPath), containerId);
        bddVariableContext.putVariable(scopes, variableName, files);
    }
}
