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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.vividus.steps.StringComparisonRule.CONTAINS;
import static org.vividus.steps.StringComparisonRule.DOES_NOT_CONTAIN;
import static org.vividus.steps.StringComparisonRule.IS_EQUAL_TO;
import static org.vividus.steps.StringComparisonRule.MATCHES;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ArchiveSteps.ArchiveVariable;
import org.vividus.steps.ArchiveSteps.NamedEntry;
import org.vividus.steps.ArchiveSteps.OutputFormat;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)

class ArchiveStepsTests
{
    private static final String FILE_JSON = "file.json";
    private static final String IMAGE_PNG = "images/image.png";
    private static final String DUMMY = "dummy";
    private static final String MATCHES_PATTERN = ".+\\.png";
    private static final String CONTAINS_ENTRY_WITH_NAME = "The archive contains entry with name ";
    private static final String CONTAINS_ENTRY_WITH_RULE = "The archive contains entry matching the "
            + "comparison rule '%s' with name pattern '%s'";

    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks  private ArchiveSteps archiveSteps;

    @Test
    void testSaveFilesContentToVariables()
    {
        var json = "json";
        var image = "image";
        var base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABAQMAAAAl21bKAAAABlBMVEUAAAD///+l2Z/dAAAACXBIWXMAAA7EAAAOxAGVK"
                + "w4bAAAACklEQVQImWNgAAAAAgAB9HFkpgAAAABJRU5ErkJggg==";

        archiveSteps.saveArchiveEntriesToVariables(createArchiveData(),
                List.of(createVariable(IMAGE_PNG, image, OutputFormat.BASE64),
                        createVariable(FILE_JSON, json, OutputFormat.TEXT)));
        var scopes = Set.of(VariableScope.SCENARIO);
        verify(variableContext).putVariable(scopes, image, base64);
        verify(variableContext).putVariable(scopes, json,
                "{\"hello\": \"world\"}\n");
        verifyNoInteractions(softAssert);
        verifyNoMoreInteractions(variableContext);
    }

    @Test
    void testSaveFilesContentToVariablesInvalidPath()
    {
        var path = "path";
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
        var archiveEntries = createArchiveEntries();
        lenient().doAnswer(getAssertionAnswer(false)).when(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + DUMMY),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)), any());

        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(FILE_JSON, null),
                createEntry(IMAGE_PNG, null),
                createEntry(DUMMY, null)
        ));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + FILE_JSON), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + DUMMY), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)), any());
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesIsPassed()
    {
        var archiveEntries = createArchiveEntries();
        doAnswer(getAssertionAnswer(true)).when(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)), any());
        doAnswer(getAssertionAnswer(true)).when(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + FILE_JSON),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)), any());

        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(FILE_JSON, null),
                createEntry(IMAGE_PNG, null)
        ));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + FILE_JSON), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());
        verifyNoMoreInteractions(softAssert);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesIsFailed()
    {
        var archiveEntries = createArchiveEntries();
        doAnswer(getAssertionAnswer(false)).when(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + DUMMY),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)), any());
        doAnswer(getAssertionAnswer(true)).when(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)), any());

        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(DUMMY, null),
                createEntry(IMAGE_PNG, null)
        ));
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + DUMMY), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(CONTAINS_ENTRY_WITH_NAME + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesWithUserRulesIsFailed()
    {
        var archiveEntries = createArchiveEntries();
        doAnswer(getAssertionAnswer(false)).when(softAssert).assertThat(
                eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, DUMMY)), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)), any());
        doAnswer(getAssertionAnswer(true)).when(softAssert).assertThat(
                eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATCHES_PATTERN)), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());

        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(DUMMY, MATCHES),
                createEntry(MATCHES_PATTERN, MATCHES)
        ));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, DUMMY)),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATCHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)), any());
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesWithUserRulesIsPassed()
    {
        var archiveEntries = createArchiveEntries();
        doAnswer(getAssertionAnswer(true)).when(softAssert).assertThat(
                eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATCHES_PATTERN)), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());

        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(createEntry(MATCHES_PATTERN, MATCHES)
        ));
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATCHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)), any());
        verifyNoMoreInteractions(softAssert);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void testVerifyArchiveContainsEntriesWithUserRules()
    {
        var archiveEntries = createArchiveEntries();
        doAnswer(getAssertionAnswer(true)).when(softAssert).assertThat(
                eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATCHES_PATTERN)), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());
        doAnswer(getAssertionAnswer(false)).when(softAssert).assertThat(
                eq(String.format(CONTAINS_ENTRY_WITH_RULE, CONTAINS, DUMMY)), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)), any());
        doAnswer(getAssertionAnswer(false)).when(softAssert).assertThat(
                eq(String.format(CONTAINS_ENTRY_WITH_RULE, IS_EQUAL_TO, DUMMY)), eq(archiveEntries),
                argThat(e -> !e.matches(archiveEntries)), any());
        doAnswer(getAssertionAnswer(true)).when(softAssert).assertThat(
                eq(String.format(CONTAINS_ENTRY_WITH_RULE, DOES_NOT_CONTAIN, DUMMY)), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)), any());

        archiveSteps.verifyArchiveContainsEntries(createArchiveData(), List.of(
                createEntry(MATCHES_PATTERN, MATCHES),
                createEntry(DUMMY, CONTAINS),
                createEntry(DUMMY, IS_EQUAL_TO),
                createEntry(DUMMY, DOES_NOT_CONTAIN)
        ));

        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, MATCHES, MATCHES_PATTERN)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, CONTAINS, DUMMY)),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, IS_EQUAL_TO, DUMMY)),
                eq(archiveEntries), argThat(e -> !e.matches(archiveEntries)), any());
        verify(softAssert).assertThat(eq(String.format(CONTAINS_ENTRY_WITH_RULE, DOES_NOT_CONTAIN, DUMMY)),
                eq(archiveEntries), argThat(e -> e.matches(archiveEntries)), any());
        verifyNoMoreInteractions(softAssert);
        verifyPublishAttachment(archiveEntries);
        verifyNoMoreInteractions(attachmentPublisher);
    }

    private void verifyPublishAttachment(Set<String> attachmentContent)
    {
        verify(attachmentPublisher).publishAttachment("templates/archive-entries-result-table.ftl",
                Map.of("entryNames", attachmentContent), "Archive entries");
    }

    private static NamedEntry createEntry(String name, StringComparisonRule rule)
    {
        var entry = new NamedEntry();
        entry.setName(name);
        entry.setRule(rule);
        return entry;
    }

    private DataWrapper createArchiveData()
    {
        var data = ResourceUtils.loadResourceAsByteArray(getClass(), "archive.zip");
        return new DataWrapper(data);
    }

    private  Set<String> createArchiveEntries()
    {
        Set<String> archiveEntries = new LinkedHashSet<>();
        archiveEntries.add(IMAGE_PNG);
        archiveEntries.add(FILE_JSON);
        return archiveEntries;
    }

    private Answer getAssertionAnswer(boolean assertionPassed)
    {
        return a ->
        {
            Consumer<Boolean> consumer = a.getArgument(3);
            consumer.accept(assertionPassed);
            return null;
        };
    }

    private static ArchiveVariable createVariable(String path, String variableName, OutputFormat outputFormat)
    {
        var scopes = Set.of(VariableScope.SCENARIO);
        var variable = new ArchiveVariable();
        variable.setPath(path);
        variable.setOutputFormat(outputFormat);
        variable.setScopes(scopes);
        variable.setVariableName(variableName);
        return variable;
    }
}
