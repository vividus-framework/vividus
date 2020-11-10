/*
 * Copyright 2019-2020 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

public class FileSteps
{
    @Inject private IBddVariableContext bddVariableContext;

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
     * @param pathVariable name of variable
     * @throws IOException if an I/O exception of some sort has occurred
     */
    @When("I create temporary file with name '$name' and content '$content' and put path to '$scopes' variable"
            + " with name '$pathVariable'")
    public void saveResponseBodyToFile(String name, String content, Set<VariableScope> scopes, String pathVariable)
            throws IOException
    {
        File temporaryFile = File.createTempFile(FilenameUtils.getBaseName(name),
                "." + FilenameUtils.getExtension(name));
        temporaryFile.deleteOnExit();
        FileUtils.writeStringToFile(temporaryFile, content, StandardCharsets.UTF_8);
        bddVariableContext.putVariable(scopes, pathVariable, temporaryFile.getAbsolutePath());
    }

    /**
     * Saves content to file with specified pathname
     * @param pathname Fully qualified file name with parent folders and extension (e.g. temp/some_file.txt)
     * @param fileContent Content to be saved to file
     * @throws IOException If an I/O error has occurred
     */
    @When("I create a file with the pathname `$pathname` and the content `$fileContent`")
    public void createFile(String pathname, String fileContent) throws IOException
    {
        FileUtils.writeStringToFile(new File(pathname), fileContent, StandardCharsets.UTF_8);
    }
}
