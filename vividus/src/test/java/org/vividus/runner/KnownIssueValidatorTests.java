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

package org.vividus.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.issue.VividusKnownIssueIdentifier;
import org.vividus.softassert.issue.IKnownIssueProvider;
import org.vividus.softassert.issue.KnownIssueChecker;
import org.vividus.softassert.model.KnownIssue;

@ExtendWith(MockitoExtension.class)
class KnownIssueValidatorTests
{
    @Mock private IKnownIssueProvider knownIssueProvider;
    @Mock private KnownIssueChecker knownIssueChecker;

    @Test
    @StdIo
    void testNoOptionsAndNoKnownIssues(StdOut stdOut) throws IOException, ParseException
    {
        when(knownIssueProvider.getKnownIssueIdentifiers()).thenReturn(new HashMap<>());
        testValidator();
        assertThat(stdOut.capturedLines(), arrayContaining(""));
    }

    @Test
    @StdIo
    void testNoInputFileAndNonEmptyKnownIssues(StdOut stdOut) throws IOException, ParseException
    {
        Map<String, VividusKnownIssueIdentifier> knownIssues = new HashMap<>();
        var key = "VVD-1";
        knownIssues.put(key, null);
        doReturn(knownIssues).when(knownIssueProvider).getKnownIssueIdentifiers();
        testValidator();
        assertThat(stdOut.capturedLines(), arrayContaining("Known issues found:", key));
    }

    @Test
    @StdIo
    void testFilePassedAsOption(StdOut stdOut, @TempDir Path tempDir) throws IOException, ParseException
    {
        var failuresList = tempDir.resolve("testFile.txt").toFile();
        var matchedFailure = "Elements to click found by locator By.xpath";
        var unmatchedFailure = "There is exactly one element with locator By.xpath";
        FileUtils.writeLines(failuresList, List.of(matchedFailure, unmatchedFailure));

        var knownIssue = mock(KnownIssue.class);
        when(knownIssueChecker.getKnownIssue(matchedFailure)).thenReturn(knownIssue);
        when(knownIssueChecker.getKnownIssue(unmatchedFailure)).thenReturn(null);
        String key = "VVD-2";
        when(knownIssue.getIdentifier()).thenReturn(key);

        testValidator("--file", failuresList.toString());

        assertThat(stdOut.capturedLines(), arrayContaining(
                "Known Issue | Assertion Error",
                key + "       | " + matchedFailure,
                "            | " + unmatchedFailure
        ));
    }

    @Test
    @StdIo
    void testHelpOptionIsPresent(StdOut stdOut) throws IOException, ParseException
    {
        try (var vividus = Mockito.mockStatic(Vividus.class))
        {
            KnownIssueValidator.main(new String[] { "--help" });
            assertThat(stdOut.capturedLines(), hasItemInArray("usage: KnownIssueValidator"));
            vividus.verifyNoInteractions();
        }
    }

    @Test
    void testUnknownOptionIsPresent()
    {
        try (var vividus = Mockito.mockStatic(Vividus.class))
        {
            assertThrows(UnrecognizedOptionException.class, () -> KnownIssueValidator.main(new String[] { "--any" }));
            vividus.verifyNoInteractions();
        }
    }

    private void testValidator(String... args) throws IOException, ParseException
    {
        try (var vividus = Mockito.mockStatic(Vividus.class); var beanFactory = Mockito.mockStatic(BeanFactory.class))
        {
            beanFactory.when(() -> BeanFactory.getBean(KnownIssueChecker.class)).thenReturn(knownIssueChecker);
            beanFactory.when(() -> BeanFactory.getBean(IKnownIssueProvider.class)).thenReturn(knownIssueProvider);

            KnownIssueValidator.main(args);

            vividus.verify(Vividus::init);
            verify(knownIssueChecker).setTestInfoProvider(null);
        }
    }
}
