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

package org.vividus.bdd.steps.api;

import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.api.IApiTestContext;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.model.ArchiveVariable;
import org.vividus.bdd.model.NamedEntry;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.zip.ZipUtils;

public class HttpResponseSteps
{
    @Inject private IApiTestContext apiTestContext;
    @Inject private ISoftAssert softAssert;
    @Inject private IBddVariableContext bddVariableContext;

    /**
     * Saves content of a file from archive in response to context variable in specified format
     * Example:
     * <p>
     * <code>
     * When I save files content from the response archive into variables with parameters:<br>
     * |path                        |variableName|scope   |outputFormat|
     * |files/2011-11-11/skyrim.json|entityJson  |SCENARIO|TEXT        |
     * </code>
     * </p>
     * Currently available types are TEXT and BASE64
     * @param parameters describes saving parameters
     */
    @When("I save content of the response archive entries to the variables:$parameters")
    public void saveFilesContentToVariables(List<ArchiveVariable> parameters)
    {
        List<String> expectedEntries = parameters.stream().map(ArchiveVariable::getPath).collect(Collectors.toList());
        Map<String, byte[]> zipEntries = ZipUtils.readZipEntriesFromBytes(getResponseBody(), expectedEntries::contains);
        parameters.forEach(arcVar ->
        {
            String path = arcVar.getPath();
            Optional.ofNullable(zipEntries.get(path)).ifPresentOrElse(
                data -> bddVariableContext.putVariable(arcVar.getScopes(), arcVar.getVariableName(),
                            arcVar.getOutputFormat().convert(data)),
                () -> softAssert.recordFailedAssertion(
                            String.format("Unable to find entry by name %s in response archive", path)));
        });
    }

    /**
     * Verifies that one of specified entries in the response archive has one of specified names
     * Example:
     * <p>
     * <code>
     * Then the response archive contains entries with the names:$parameters<br>
     * |name                        |
     * |files/2011-11-11/skyrim.json|
     * </code>
     * </p>
     * @param parameters contains names
     */
    @Then("the response archive contains entries with the names:$parameters")
    public void verifyArhiveContainsEntries(List<NamedEntry> parameters)
    {
        Set<String> entryNames = ZipUtils.readZipEntryNamesFromBytes(getResponseBody());
        parameters.stream().map(NamedEntry::getName).forEach(expectedName ->
                softAssert.assertThat("The response archive contains entry with name " + expectedName, entryNames,
                        hasItem(expectedName)));
    }

    private byte[] getResponseBody()
    {
        return apiTestContext.getResponse().getResponseBody();
    }
}
