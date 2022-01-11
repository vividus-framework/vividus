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

package org.vividus.avro.steps;

import java.io.IOException;
import java.util.Set;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

public class AvroSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AvroSteps.class);
    private final VariableContext variableContext;

    public AvroSteps(VariableContext variableContext)
    {
        this.variableContext = variableContext;
    }

    /**
     * Converts the provided resource or file in Avro format to JSON and saves it as a text to a variable
     *
     * @param resourceNameOrFilePath The resource name or the file path.
     * @param scopes                 The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                               scopes.<br>
     *                               <i>Available scopes:</i>
     *                               <ul>
     *                               <li><b>STEP</b> - the variable will be available only within the step,
     *                               <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                               <li><b>STORY</b> - the variable will be available within the whole story,
     *                               <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                               </ul>
     * @param variableName           The variable name to save resulting JSON.
     * @throws IOException if an I/O error occurs
     */
    @When("I convert Avro data from `$resourceNameOrFilePath` to JSON and save result to $scopes variable "
            + "`$variableName`")
    public void convertAvroDataToJson(String resourceNameOrFilePath, Set<VariableScope> scopes, String variableName)
            throws IOException
    {
        byte[] data = ResourceUtils.loadResourceOrFileAsByteArray(resourceNameOrFilePath);
        DatumReader<Object> datumReader = new GenericDatumReader<>();
        try (DataFileReader<Object> dataFileReader = new DataFileReader<>(new SeekableByteArrayInput(data),
                datumReader))
        {
            LOGGER.atInfo()
                  .addArgument(() -> dataFileReader.getSchema().toString(false))
                  .log("Avro schema: {}");

            StringBuilder json = new StringBuilder("[");

            Object dataRecord = null;
            while (dataFileReader.hasNext())
            {
                dataRecord = dataFileReader.next(dataRecord);
                json.append(dataRecord);
            }
            json.append(']');
            variableContext.putVariable(scopes, variableName, json.toString());
        }
    }
}
