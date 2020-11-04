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

package org.vividus.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.SystemStreamTests;
import org.vividus.bdd.issue.BddKnownIssueIdentifier;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.softassert.issue.IKnownIssueProvider;
import org.vividus.softassert.issue.KnownIssueChecker;
import org.vividus.softassert.model.KnownIssue;

@ExtendWith(MockitoExtension.class)
class KnownIssueValidatorTests extends SystemStreamTests
{
    private static final String EMPTY = "";
    private static final String KEY = "key";
    private static final String MATCHED_FAILURE = "Elements to click found by locator By.xpath";
    private static final String UNMATCHED_FAILURE = "There is exactly one element with locator By.xpath";
    private static final String SEPARATOR = "| ";

    @Mock private IKnownIssueProvider knownIssueProvider;
    @Mock private KnownIssueChecker knownIssueChecker;

    @Test
    void testNoOptionsAndNoKnownIssues() throws IOException, ParseException
    {
        when(knownIssueProvider.getKnownIssueIdentifiers()).thenReturn(new HashMap<>());
        testValidator();
        assertThat(getOutStreamContent(), is(equalTo(EMPTY)));
    }

    @Test
    void testNoInputFileAndNonEmptyKnownIssues() throws IOException, ParseException
    {
        Map<String, BddKnownIssueIdentifier> knownIssues = new HashMap<>();
        knownIssues.put(KEY, null);
        doReturn(knownIssues).when(knownIssueProvider).getKnownIssueIdentifiers();
        testValidator();
        assertThat(getOutStreamContent(), containsString(KEY));
    }

    @Test
    void testFilePassedAsOption(@TempDir Path tempDir) throws IOException, ParseException
    {
        File failuresList = tempDir.resolve("testFile.txt").toFile();
        FileUtils.writeLines(failuresList, List.of(MATCHED_FAILURE, UNMATCHED_FAILURE));

        KnownIssue knownIssue = mock(KnownIssue.class);
        when(knownIssueChecker.getKnownIssue(MATCHED_FAILURE)).thenReturn(knownIssue);
        when(knownIssueChecker.getKnownIssue(UNMATCHED_FAILURE)).thenReturn(null);
        when(knownIssue.getIdentifier()).thenReturn(KEY);

        testValidator("--file", failuresList.toString());

        assertThat(getOutStreamContent(),
                stringContainsInOrder(KEY, SEPARATOR, MATCHED_FAILURE, SEPARATOR, UNMATCHED_FAILURE));
    }

    @Test
    void testHelpOptionIsPresent() throws IOException, ParseException
    {
        try (MockedStatic<Vividus> vividus = Mockito.mockStatic(Vividus.class))
        {
            KnownIssueValidator.main(new String[] { "--help" });
            assertThat(getOutStreamContent(), containsString("usage: KnownIssueValidator"));
            vividus.verifyNoInteractions();
        }
    }

    @Test
    void testUnknownOptionIsPresent()
    {
        try (MockedStatic<Vividus> vividus = Mockito.mockStatic(Vividus.class))
        {
            assertThrows(UnrecognizedOptionException.class, () -> KnownIssueValidator.main(new String[] { "--any" }));
            vividus.verifyNoInteractions();
        }
    }

    private void testValidator(String... args) throws IOException, ParseException
    {
        try (MockedStatic<Vividus> vividus = Mockito.mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = Mockito.mockStatic(BeanFactory.class))
        {
            beanFactory.when(() -> BeanFactory.getBean(KnownIssueChecker.class)).thenReturn(knownIssueChecker);
            beanFactory.when(() -> BeanFactory.getBean(IKnownIssueProvider.class)).thenReturn(knownIssueProvider);

            KnownIssueValidator.main(args);

            vividus.verify(Vividus::init);
            verify(knownIssueChecker).setTestInfoProvider(null);
        }
    }
}
