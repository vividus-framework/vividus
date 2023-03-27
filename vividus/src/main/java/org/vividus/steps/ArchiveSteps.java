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

import static org.hamcrest.Matchers.hasItem;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.zip.ZipUtils;
import org.vividus.variable.VariableScope;

public class ArchiveSteps
{
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;
    private final IAttachmentPublisher attachmentPublisher;

    public ArchiveSteps(VariableContext variableContext, ISoftAssert softAssert,
            IAttachmentPublisher attachmentPublisher)
    {
        this.variableContext = variableContext;
        this.softAssert = softAssert;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Saves content of a file from archive to context variable in specified format
     * Example:
     * <p>
     * <code>
     * When I save content of `${response-as-bytes}` archive entries to variables:<br>
     * |path                        |variableName|scopes   |outputFormat|
     * |files/2011-11-11/skyrim.json|entityJson  |SCENARIO |TEXT        |
     * </code>
     * </p>
     * Currently, available types are TEXT and BASE64
     *
     * @param archiveData The data of archive to verify
     * @param parameters  describes saving parameters
     */
    @When("I save content of `$archiveData` archive entries to variables:$parameters")
    public void saveArchiveEntriesToVariables(DataWrapper archiveData, List<ArchiveVariable> parameters)
    {
        List<String> expectedEntries = parameters.stream().map(ArchiveVariable::getPath).collect(Collectors.toList());
        Map<String, byte[]> zipEntries = ZipUtils.readZipEntriesFromBytes(archiveData.getBytes(),
                expectedEntries::contains);
        parameters.forEach(arcVar ->
        {
            String path = arcVar.getPath();
            Optional.ofNullable(zipEntries.get(path)).ifPresentOrElse(
                    data -> variableContext.putVariable(arcVar.getScopes(), arcVar.getVariableName(),
                            arcVar.getOutputFormat().convert(data)),
                    () -> softAssert.recordFailedAssertion(
                            String.format("Unable to find entry by name %s in archive", path)));
        });
    }

    /**
     * Verifies that at least one (or no one) entry in an archive matches the specified string comparison rule.
     * If comparison rule column does not exist,
     * the verification that archive entries have the specified names is performed.
     * <p>
     * Usage example:
     * </p>
     * <p>
     * <code>
     * Then `${response-as-bytes}` archive contains entries with names:$parameters<br>
     * |rule      |name                                    |<br>
     * |contains  |2011-11-11/skyrim.json                  |<br>
     * |matches   |files/2011-11-11/logs/papyrus\.\d+\.log |<br>
     * </code>
     * </p>
     *
     * @param archiveData The data of archive to verify
     * @param parameters  The ExampleTable that contains specified string comparison <b>rule</b> and entry <b>name</b>
     *                    pattern that should be found using current <b>rule</b>. Available columns:
     *                    <ul>
     *                    <li>rule - String comparison rule: "is equal to", "contains", "does not contain", "matches"
     *                    .</li>
     *                    <li>name - Desired entry name pattern used with current <b>rule</b>.</li>
     *                    </ul>
     */
    @Then("`$archiveData` archive contains entries with names:$parameters")
    public void verifyArchiveContainsEntries(DataWrapper archiveData, List<NamedEntry> parameters)
    {
        Set<String> entryNames = ZipUtils.readZipEntryNamesFromBytes(archiveData.getBytes());
        MutableBoolean reportNotPublished = new MutableBoolean(true);

        parameters.forEach(entry -> assertArchiveEntry(entry, entryNames, reportNotPublished));
    }

    private void assertArchiveEntry(NamedEntry expectedEntry, Set<String> entryNames,
            MutableBoolean reportNotPublished)
    {
        String expectedName = expectedEntry.getName();
        StringComparisonRule comparisonRule = expectedEntry.getRule();

        String assertionDescription;
        Matcher<Iterable<? super String>> entryMatcher;
        if (comparisonRule != null)
        {
            assertionDescription = String.format(
                    "The archive contains entry matching the comparison " + "rule '%s' with name pattern '%s'",
                    comparisonRule, expectedName);
            entryMatcher = hasItem(comparisonRule.createMatcher(expectedName));
        }
        else
        {
            assertionDescription = "The archive contains entry with name " + expectedName;
            entryMatcher = hasItem(expectedName);
        }

        softAssert.assertThat(assertionDescription, entryNames, entryMatcher,
                publishAttachmentOnFailure(entryNames, reportNotPublished));
    }

    private Consumer<Boolean> publishAttachmentOnFailure(Set<String> entryNames, MutableBoolean reportNotPublished)
    {
        return passed ->
        {
            if (!passed && reportNotPublished.isTrue())
            {
                attachmentPublisher.publishAttachment("templates/archive-entries-result-table.ftl",
                        Map.of("entryNames", entryNames), "Archive entries");
                reportNotPublished.setFalse();
            }
        };
    }

    @AsParameters
    public static class ArchiveVariable
    {
        private String path;
        private String variableName;
        private Set<VariableScope> scopes;
        private OutputFormat outputFormat;

        public String getPath()
        {
            return path;
        }

        public void setPath(String path)
        {
            this.path = path;
        }

        public String getVariableName()
        {
            return variableName;
        }

        public void setVariableName(String variableName)
        {
            this.variableName = variableName;
        }

        public Set<VariableScope> getScopes()
        {
            return scopes;
        }

        public void setScopes(Set<VariableScope> scopes)
        {
            this.scopes = scopes;
        }

        public OutputFormat getOutputFormat()
        {
            return outputFormat;
        }

        public void setOutputFormat(OutputFormat outputFormat)
        {
            this.outputFormat = outputFormat;
        }
    }

    @AsParameters
    public static class NamedEntry
    {
        private StringComparisonRule rule;
        private String name;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public StringComparisonRule getRule()
        {
            return rule;
        }

        public void setRule(StringComparisonRule rule)
        {
            this.rule = rule;
        }
    }

    public enum OutputFormat
    {
        TEXT
        {
            @Override
            public String convert(byte[] data)
            {
                return new String(data, StandardCharsets.UTF_8);
            }
        },
        BASE64
        {
            @Override
            public String convert(byte[] data)
            {
                return Base64.getEncoder().encodeToString(data);
            }
        };

        public abstract String convert(byte[] data);
    }
}
