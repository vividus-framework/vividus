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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.SystemOutTests;
import org.vividus.bdd.issue.BddKnownIssueIdentifier;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.softassert.issue.IKnownIssueProvider;
import org.vividus.softassert.issue.KnownIssueChecker;
import org.vividus.softassert.model.KnownIssue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Vividus.class, BeanFactory.class})
@PowerMockIgnore({"javax.*", "com.sun.*", "jdk.*", "org.xml.*", "org.w3c.*"})
public class KnownIssueValidatorTests extends SystemOutTests
{
    private static final String EMPTY = "";
    private static final String KEY = "key";
    private static final String MATCHED_FAILURE = "Elements to click found by locator By.xpath";
    private static final String UNMATCHED_FAILURE = "There is exactly one element with locator By.xpath";
    private static final String SEPARATOR = "| ";

    @Rule
    private final TemporaryFolder tempDir = new TemporaryFolder();

    @BeforeClass
    public static void mockVividus()
    {
        mockStatic(Vividus.class);
    }

    @Test
    public void testNoOptionsAndNoKnownIssues() throws Exception
    {
        KnownIssueChecker knownIssueChecker = mock(KnownIssueChecker.class);
        IKnownIssueProvider knownIssueProvider = mock(IKnownIssueProvider.class);
        mockBeanFactory(knownIssueChecker, knownIssueProvider);

        when(knownIssueProvider.getKnownIssueIdentifiers()).thenReturn(new HashMap<>());

        KnownIssueValidator.main(new String[0]);
        verify(knownIssueChecker).setTestInfoProvider(null);
        assertThat(getOutput(), is(equalTo(EMPTY)));
    }

    @Test
    public void testNoInputFileAndNonEmptyKnownIssues() throws Exception
    {
        Map<String, BddKnownIssueIdentifier> knownIssues = new HashMap<>();
        knownIssues.put(KEY, null);

        KnownIssueChecker knownIssueChecker = mock(KnownIssueChecker.class);
        IKnownIssueProvider knownIssueProvider = mock(IKnownIssueProvider.class);
        mockBeanFactory(knownIssueChecker, knownIssueProvider);

        doReturn(knownIssues).when(knownIssueProvider).getKnownIssueIdentifiers();

        KnownIssueValidator.main(new String[0]);
        assertThat(getOutput(), containsString(KEY));
    }

    @Test
    public void testFilePassedAsOption() throws Exception
    {
        File failuresList = tempDir.newFile("testFile.txt");
        FileUtils.writeLines(failuresList, List.of(MATCHED_FAILURE, UNMATCHED_FAILURE));

        KnownIssue knownIssue = mock(KnownIssue.class);
        KnownIssueChecker knownIssueChecker = mock(KnownIssueChecker.class);
        IKnownIssueProvider knownIssueProvider = mock(IKnownIssueProvider.class);
        mockBeanFactory(knownIssueChecker, knownIssueProvider);

        when(knownIssueChecker.getKnownIssue(MATCHED_FAILURE)).thenReturn(knownIssue);
        when(knownIssueChecker.getKnownIssue(UNMATCHED_FAILURE)).thenReturn(null);
        when(knownIssue.getIdentifier()).thenReturn(KEY);

        KnownIssueValidator.main(new String[]{"--file", failuresList.toString()});
        assertThat(getOutput(), stringContainsInOrder(KEY, SEPARATOR, MATCHED_FAILURE, SEPARATOR, UNMATCHED_FAILURE));
    }

    @Test
    public void testHelpOptionIsPresent() throws Exception
    {
        KnownIssueValidator.main(new String[]{"--help"});
        assertThat(getOutput(), containsString("usage: KnownIssueValidator"));
    }

    @Test
    public void testUnknownOptionIsPresent()
    {
        assertThrows(UnrecognizedOptionException.class, () ->
                KnownIssueValidator.main(new String[]{"--any"}));
    }

    private void mockBeanFactory(KnownIssueChecker checker, IKnownIssueProvider provider)
    {
        mockStatic(BeanFactory.class);
        when(BeanFactory.getBean(KnownIssueChecker.class)).thenReturn(checker);
        when(BeanFactory.getBean(IKnownIssueProvider.class)).thenReturn(provider);
    }
}
