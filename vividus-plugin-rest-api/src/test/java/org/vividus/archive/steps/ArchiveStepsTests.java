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

package org.vividus.archive.steps;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.steps.StringComparisonRule.CONTAINS;
import static org.vividus.steps.StringComparisonRule.DOES_NOT_CONTAIN;
import static org.vividus.steps.StringComparisonRule.IS_EQUAL_TO;
import static org.vividus.steps.StringComparisonRule.MATCHES;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.model.ArchiveVariable;
import org.vividus.model.NamedEntry;
import org.vividus.model.OutputFormat;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.DataWrapper;
import org.vividus.steps.StringComparisonRule;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)

class ArchiveStepsTests
{
    private static final String FILE_JSON = "file.json";
    private static final String IMAGE_PNG = "images/image.png";
    private static final String DUMMY = "dummy";
    private static final String MATHES_PATTERN = ".+\\.png";
    private static final String CONTAINS_ENTRY_WITH_NAME = "The archive contains entry with name ";
    private static final String CONTAINS_ENTRY_WITH_RULE = "The archive contains entry matching the "
            + "comparison rule '%s' with name pattern '%s'";
    @Mock private ISoftAssert softAssert;
    @Mock private VariableContext variableContext;
    @Mock private IAttachmentPublisher attachmentPublisher;

    @InjectMocks  private ArchiveSteps archiveSteps;

    @Test
    void testSaveFilesContentToVariables()
    {
        String json = "json";
        String image = "image";
        String base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABAQMAAAAl21bKAAAABlBMVEUAAAD///+l2Z/dAAAACXBIWXMAAA7EAAAOxAGVK"
                + "w4bAAAACklEQVQImWNgAAAAAgAB9HFkpgAAAABJRU5ErkJggg==";

        archiveSteps.saveArchiveEntriesToVariables(createArchiveData(),
                List.of(createVariable(IMAGE_PNG, image, OutputFormat.BASE64),
                        createVariable(FILE_JSON, json, OutputFormat.TEXT)));
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        verify(variableContext).putVariable(scopes, image, base64);
        verify(variableContext).putVariable(scopes, json,
                "{\"plugin\": \"vividus-plugin-rest-api\"}\n");
        verifyNoInteractions(softAssert);
        verifyNoMoreInteractions(variableContext);
    }

    @Test
    void testSaveFilesContentToVariablesInvalidPath()
    {
        String path = "path";
        archiveSteps.saveArchiveEntriesToVariables(createArchiveData(),
                List.of(createVariable(path, path, OutputFormat.BASE64)));
        verify(softAssert).recordFailedAssertion(
                String.format("Unable to find entry by name %s in archive", path));
        verifyNoInteractions(variableContext);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testVerifyArchiveContainsEntries()
    {
        Set<String> archiveEntries = createArchiveEntries();
        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(FILE_JSON, null),
                createEntry(IMAGE_PNG, null),
                createEntry(DUMMY, null)
        ));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + FILE_JSON), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + DUMMY), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)));
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesIsPassed()
    {
        Set<String> archiveEntries = createArchiveEntries();
        when(softAssert.assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)))).thenReturn(true);
        when(softAssert.assertThat(eq(CONTAINS_ENTRY_WITH_NAME + FILE_JSON), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)))).thenReturn(true);
        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(FILE_JSON, null),
                createEntry(IMAGE_PNG, null)
        ));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + FILE_JSON), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)));
        verifyNoMoreInteractions(softAssert);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesIsFailled()
    {
        Set<String> archiveEntries = createArchiveEntries();
        when(softAssert.assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)))).thenReturn(true);
        when(softAssert.assertThat(eq(CONTAINS_ENTRY_WITH_NAME + DUMMY), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)))).thenReturn(false);
        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(DUMMY, null),
                createEntry(IMAGE_PNG, null)
        ));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + DUMMY), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)));
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesWithUserRulesIsFailed()
    {
        Set<String> archiveEntries = createArchiveEntries();
        when(softAssert.assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, DUMMY)),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)))).thenReturn(false);
        when(softAssert.assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)))).thenReturn(true);
        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(DUMMY, MATCHES),
                createEntry(MATHES_PATTERN, MATCHES)
        ));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, DUMMY)),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)));
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesWithUserRulesIsPassed()
    {
        Set<String> archiveEntries = createArchiveEntries();
        when(softAssert.assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)))).thenReturn(true);
        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(MATHES_PATTERN, MATCHES)
        ));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)));
        verifyNoMoreInteractions(softAssert);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesWithUserRules()
    {
        String containsPattern = "file";
        Set<String> archiveEntries = createArchiveEntries();
        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(MATHES_PATTERN, MATCHES),
                createEntry(containsPattern, CONTAINS),
                createEntry(DUMMY, IS_EQUAL_TO),
                createEntry(DUMMY, DOES_NOT_CONTAIN)
        ));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, CONTAINS, containsPattern)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, IS_EQUAL_TO, DUMMY)),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, DOES_NOT_CONTAIN, DUMMY)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)));
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    private void verifyPublishAttachment(Set<String> attachmentContent)
    {
        verify(attachmentPublisher).publishAttachment("archive-entries-result-table.ftl",
                Map.of("entryNames", attachmentContent), "Archive entries");
    }

    private static NamedEntry createEntry(String name, StringComparisonRule rule)
    {
        NamedEntry entry = new NamedEntry();
        entry.setName(name);
        entry.setRule(rule);
        return entry;
    }

    private DataWrapper createArchiveData()
    {
        byte[] data = ResourceUtils.loadResourceAsByteArray(getClass(), "/org/vividus/steps/api/archive.zip");
        return new DataWrapper(data);
    }

    private  Set<String> createArchiveEntries()
    {
        Set<String> archiveEntries = new LinkedHashSet<>();
        archiveEntries.add(IMAGE_PNG);
        archiveEntries.add(FILE_JSON);
        return archiveEntries;
    }

    private static ArchiveVariable createVariable(String path, String variableName, OutputFormat outputFormat)
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        ArchiveVariable variable = new ArchiveVariable();
        variable.setPath(path);
        variable.setOutputFormat(outputFormat);
        variable.setScopes(scopes);
        variable.setVariableName(variableName);
        return variable;
    }
}
